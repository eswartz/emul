/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.core.launcher;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IProcesses.ProcessContext;
import org.eclipse.tm.tcf.services.IStreams;
import org.eclipse.tm.te.core.async.AsyncCallbackCollector;
import org.eclipse.tm.te.runtime.callback.Callback;
import org.eclipse.tm.te.runtime.events.DisposedEvent;
import org.eclipse.tm.te.runtime.events.EventManager;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.runtime.interfaces.events.IEventListener;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.properties.PropertiesContainer;
import org.eclipse.tm.te.runtime.services.ServiceManager;
import org.eclipse.tm.te.runtime.services.interfaces.ITerminalService;
import org.eclipse.tm.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tm.te.tcf.core.streams.StreamsDataProvider;
import org.eclipse.tm.te.tcf.core.streams.StreamsDataReceiver;
import org.eclipse.tm.te.tcf.processes.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.processes.core.interfaces.launcher.IProcessContextAwareListener;
import org.eclipse.tm.te.tcf.processes.core.interfaces.launcher.IProcessLauncher;
import org.eclipse.tm.te.tcf.processes.core.interfaces.launcher.IProcessStreamsProxy;
import org.eclipse.tm.te.tcf.processes.core.internal.tracing.ITraceIds;
import org.eclipse.tm.te.tcf.processes.core.nls.Messages;

/**
 * Remote process launcher.
 * <p>
 * The process launcher is implemented fully asynchronous.
 */
public class ProcessLauncher extends PlatformObject implements IProcessLauncher {
	// The backend channel instance
	/* default */ IChannel channel;
	// The process properties instance
	private IPropertiesContainer properties;

	// The processes service instance
	/* default */ IProcesses svcProcesses;
	// The streams service instance
	/* default */ IStreams svcStreams;
	// The remote process context
	/* default */ IProcesses.ProcessContext processContext;

	// The callback instance
	private ICallback callback;

	// The streams listener instance
	private IStreams.StreamsListener streamsListener = null;
	// The process listener instance
	private IProcesses.ProcessesListener processesListener = null;
	// The event listener instance
	private IEventListener eventListener = null;

	// The streams proxy instance
	private IProcessStreamsProxy streamsProxy = null;

	/**
	 * Constructor.
	 */
	public ProcessLauncher() {
		this(null);
	}

	/**
	 * Constructor.
	 */
	public ProcessLauncher(IProcessStreamsProxy streamsProxy) {
		super();
		this.streamsProxy = streamsProxy;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.processes.core.interfaces.launcher.IProcessLauncher#dispose()
	 */
	@Override
	public void dispose() {
		// Unlink the process context
		processContext = null;

		// Store a final reference to the channel instance
		final IChannel finChannel = channel;

		// Remove the notification listener
		if (eventListener != null) {
			EventManager.getInstance().removeEventListener(eventListener);
			eventListener = null;
		}

		// Create the callback collector
		final AsyncCallbackCollector collector = new AsyncCallbackCollector(new Callback() {
			@Override
			protected void internalDone(Object caller, IStatus status) {
				// Close the channel as all disposal is done
				if (finChannel != null) {
					if (Protocol.isDispatchThread()) finChannel.close();
					else Protocol.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							finChannel.close();
						}
					});
				}
			}
		});

		if (streamsListener != null) {
			// Dispose the streams listener
			if (streamsListener instanceof ProcessStreamsListener) {
				((ProcessStreamsListener)streamsListener).dispose(new AsyncCallbackCollector.SimpleCollectorCallback(collector));
			}
			streamsListener = null;
		}

		// Dispose the processes listener if created
		if (processesListener != null) {
			// Dispose the processes listener
			if (processesListener instanceof ProcessProcessesListener) {
				((ProcessProcessesListener)processesListener).dispose(new AsyncCallbackCollector.SimpleCollectorCallback(collector));
			}
			processesListener = null;
			// Remove the processes listener from the processes service
			getSvcProcesses().removeListener(processesListener);
		}

		// Dispose the streams proxy if created
		if (streamsProxy != null) {
			streamsProxy.dispose(new AsyncCallbackCollector.SimpleCollectorCallback(collector));
			streamsProxy = null;
		}
		// Mark the collector initialization as done
		collector.initDone();

		// Dissociate the channel
		channel = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.processes.core.interfaces.launcher.IProcessLauncher#terminate()
	 */
	@Override
	public void terminate() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (processContext != null && processContext.canTerminate()) {
					// Try to terminate the process the usual way first (sending SIGTERM)
					processContext.terminate(new IProcesses.DoneCommand() {
						@Override
						public void doneCommand(IToken token, Exception error) {
							onTerminateDone(processContext, error);
						}
					});
				}

			}
		};

		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);
	}

	/**
	 * Check if the process context really died after sending SIGTERM.
	 * <p>
	 * Called from {@link #terminate()}.
	 *
	 * @param context The process context. Must not be <code>null</code>.
	 * @param error The exception in case {@link #terminate()} returned with an error or <code>null</code>.
	 */
	protected void onTerminateDone(IProcesses.ProcessContext context, Exception error) {
		Assert.isNotNull(context);

		// If the terminate of the remote process context failed, give a warning to the user
		if (error != null) {
			String message = NLS.bind(Messages.ProcessLauncher_error_processTerminateFailed, context.getName());
			message += NLS.bind(Messages.ProcessLauncher_error_possibleCause, error.getLocalizedMessage());

			IStatus status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(), message, error);
			Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);

			// Dispose the launcher directly
			dispose();
		}
		// No error from terminate, this does not mean that the process went down
		// really -> SIGTERM might have been ignored from the process!
		else {
			final IProcesses.ProcessContext finContext = context;
			// Let's see if we can still get information about the context
			getSvcProcesses().getContext(context.getID(), new IProcesses.DoneGetContext() {
				@Override
				public void doneGetContext(IToken token, Exception error, ProcessContext context) {
					// In case there is no error and we do get back an process context,
					// the process must be still running, having ignored the SIGTERM.
					if (error == null && context != null && context.getID().equals(finContext.getID())) {
						// Let's send a SIGHUP next.
						getSvcProcesses().signal(context.getID(), 15, new IProcesses.DoneCommand() {
							@Override
							public void doneCommand(IToken token, Exception error) {
								onSignalSIGHUPDone(finContext, error);
							}
						});
					}
				}
			});
		}
	}

	/**
	 * Check if the process context died after sending SIGHUP.
	 * <p>
	 * Called from {@link #onTerminateDone(IProcesses.ProcessContext, Exception)}.
	 *
	 * @param context The process context. Must not be <code>null</code>.
	 * @param error The exception in case sending the signal returned with an error or <code>null</code>.
	 */
	protected void onSignalSIGHUPDone(IProcesses.ProcessContext context, Exception error) {
		Assert.isNotNull(context);

		// If the terminate of the remote process context failed, give a warning to the user
		if (error != null) {
			String message = NLS.bind(Messages.ProcessLauncher_error_processSendSignalFailed, "SIGHUP(15)", context.getName()); //$NON-NLS-1$
			message += NLS.bind(Messages.ProcessLauncher_error_possibleCause, error.getLocalizedMessage());

			IStatus status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(), message, error);
			Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);

			// Dispose the launcher directly
			dispose();
		}
		// No error from terminate, this does not mean that the process went down
		// really -> SIGTERM might have been ignored from the process!
		else {
			final IProcesses.ProcessContext finContext = context;
			// Let's see if we can still get information about the context
			getSvcProcesses().getContext(context.getID(), new IProcesses.DoneGetContext() {
				@Override
				public void doneGetContext(IToken token, Exception error, ProcessContext context) {
					// In case there is no error and we do get back an process context,
					// the process must be still running, having ignored the SIGHUP.
					if (error == null && context != null && context.getID().equals(finContext.getID())) {
						// Finally send a SIGKILL.
						getSvcProcesses().signal(context.getID(), 9, new IProcesses.DoneCommand() {
							@Override
							public void doneCommand(IToken token, Exception error) {
								if (error != null) {
									String message = NLS.bind(Messages.ProcessLauncher_error_processSendSignalFailed, "SIGKILL(15)", finContext.getName()); //$NON-NLS-1$
									message += NLS.bind(Messages.ProcessLauncher_error_possibleCause, error.getLocalizedMessage());

									IStatus status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(), message, error);
									Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);

									// Dispose the launcher directly
									dispose();
								}
							}
						});
					}
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.processes.core.interfaces.launcher.IProcessLauncher#launch(org.eclipse.tm.tcf.protocol.IPeer, org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tm.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void launch(final IPeer peer, final IPropertiesContainer properties, final ICallback callback) {
		Assert.isNotNull(peer);
		Assert.isNotNull(properties);

		// Normalize the callback
		if (callback == null) {
			this.callback = new Callback() {
				/* (non-Javadoc)
				 * @see org.eclipse.tm.te.runtime.callback.Callback#internalDone(java.lang.Object, org.eclipse.core.runtime.IStatus)
				 */
				@Override
				public void internalDone(Object caller, IStatus status) {
				}
			};
		}
		else {
			this.callback = callback;
		}

		// Remember the process properties
		this.properties = properties;

		// Open a channel to the given peer
		Tcf.getChannelManager().openChannel(peer, new IChannelManager.DoneOpenChannel() {
			/* (non-Javadoc)
			 * @see org.eclipse.tm.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel#doneOpenChannel(java.lang.Throwable, org.eclipse.tm.tcf.protocol.IChannel)
			 */
			@Override
			public void doneOpenChannel(Throwable error, IChannel channel) {
				if (error == null) {
					ProcessLauncher.this.channel = channel;

					// Attach a channel listener so we can dispose ourself if the channel
					// is closed from the remote side.
					channel.addChannelListener(new IChannelListener() {
						/* (non-Javadoc)
						 * @see org.eclipse.tm.tcf.protocol.IChannel.IChannelListener#onChannelOpened()
						 */
						@Override
						public void onChannelOpened() {
						}
						/* (non-Javadoc)
						 * @see org.eclipse.tm.tcf.protocol.IChannel.IChannelListener#onChannelClosed(java.lang.Throwable)
						 */
						@Override
						public void onChannelClosed(Throwable error) {
							if (error != null) {
								IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
												NLS.bind(Messages.ProcessLauncher_error_channelConnectFailed, peer.getID(), error.getLocalizedMessage()),
												error);
								invokeCallback(status, null);
							}
						}
						/* (non-Javadoc)
						 * @see org.eclipse.tm.tcf.protocol.IChannel.IChannelListener#congestionLevel(int)
						 */
						@Override
						public void congestionLevel(int level) {
						}
					});


					// Check if the channel is in connected state
					if (channel.getState() != IChannel.STATE_OPEN) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										Messages.ProcessLauncher_error_channelNotConnected,
										new IllegalStateException());
						invokeCallback(status, null);
						return;
					}

					// Do some very basic sanity checking on the process properties
					if (properties.getStringProperty(PROP_PROCESS_PATH) == null) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										Messages.ProcessLauncher_error_missingProcessPath,
										new IllegalArgumentException(PROP_PROCESS_PATH));
						invokeCallback(status, null);
						return;
					}

					// Get the process and streams services
					svcProcesses = channel.getRemoteService(IProcesses.class);
					if (svcProcesses == null) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										NLS.bind(Messages.ProcessLauncher_error_missingRequiredService, IProcesses.class.getName()),
										null);

						invokeCallback(status, null);
						return;
					}

					svcStreams = channel.getRemoteService(IStreams.class);
					if (svcStreams == null) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										NLS.bind(Messages.ProcessLauncher_error_missingRequiredService, IStreams.class.getName()),
										null);
						invokeCallback(status, null);
						return;
					}

					// Execute the launch now
					executeLaunch();
				} else {
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
									NLS.bind(Messages.ProcessLauncher_error_channelConnectFailed, peer.getID(), error.getLocalizedMessage()),
									error);
					invokeCallback(status, null);
				}
			}
		});
	}

	/**
	 * Executes the launch of the remote process.
	 */
	protected void executeLaunch() {
		// Get the process properties container
		final IPropertiesContainer properties = getProperties();
		if (properties == null) {
			// This is an illegal argument. Properties must be set
			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
							NLS.bind(Messages.ProcessLauncher_error_illegalNullArgument, "properties"), //$NON-NLS-1$
							new IllegalArgumentException());
			invokeCallback(status, null);
			return;
		}

		// If a console should be associated, a streams listener needs to be created
		if (properties.getBooleanProperty(IProcessLauncher.PROP_PROCESS_ASSOCIATE_CONSOLE)
						|| properties.getStringProperty(IProcessLauncher.PROP_PROCESS_OUTPUT_REDIRECT_TO_FILE) != null) {
			// Create the streams listener
			streamsListener = createStreamsListener();
			// If available, we need to subscribe to the streams.
			if (streamsListener != null) {
				getSvcStreams().subscribe(IProcesses.NAME, streamsListener, new IStreams.DoneSubscribe() {
					@Override
					public void doneSubscribe(IToken token, Exception error) {
						// In case the subscribe to the stream fails, we pass on
						// the error to the user and stop the launch
						if (error != null) {
							// Construct the error message to show to the user
							String message = NLS.bind(Messages.ProcessLauncher_error_processLaunchFailed,
											properties.getStringProperty(IProcessLauncher.PROP_PROCESS_PATH),
											makeString((String[])properties.getProperty(IProcessLauncher.PROP_PROCESS_ARGS)));
							message += NLS.bind(Messages.ProcessLauncher_error_possibleCause, Messages.ProcessLauncher_cause_subscribeFailed);

							// Construct the status object
							IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), message, error);
							invokeCallback(status, null);
						} else {
							// Initialize the console or output file
							onSubscribeStreamsDone();
						}
					}
				});
			} else {
				// No streams to attach to -> go directly to the process launch
				onAttachStreamsDone();
			}
		} else {
			// No streams to attach to -> go directly to the process launch
			onAttachStreamsDone();
		}
	}

	/**
	 * Initialize and attach the output console and/or the output file.
	 * <p>
	 * Called from {@link IStreams#subscribe(String, org.eclipse.tm.tcf.services.IStreams.StreamsListener, org.eclipse.tm.tcf.services.IStreams.DoneSubscribe)}.
	 */
	protected void onSubscribeStreamsDone() {
		// Get the process properties container
		IPropertiesContainer properties = getProperties();
		if (properties == null) {
			// This is an illegal argument. Properties must be set
			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
							NLS.bind(Messages.ProcessLauncher_error_illegalNullArgument, "properties"), //$NON-NLS-1$
							new IllegalArgumentException());
			invokeCallback(status, null);
			return;
		}

		// The streams got subscribed, check if we shall attach the console
		if (properties.getBooleanProperty(IProcessLauncher.PROP_PROCESS_ASSOCIATE_CONSOLE)) {
			// If no specific streams proxy is set, the output redirection will default
			// to the standard terminals console view
			if (streamsProxy == null) {
				// Register the notification listener to listen to the console disposal
				eventListener = new ProcessLauncherEventListener(this);
				EventManager.getInstance().addEventListener(eventListener, DisposedEvent.class);

				// Get the terminal service
				ITerminalService terminal = ServiceManager.getInstance().getService(ITerminalService.class);
				// If not available, we cannot fulfill this request
				if (terminal != null) {
					// Create the terminal streams settings
					PropertiesContainer props = new PropertiesContainer();
					props.setProperty(ITerminalsConnectorConstants.PROP_CONNECTOR_TYPE_ID, "org.eclipse.tm.te.ui.terminals.type.streams"); //$NON-NLS-1$
					props.setProperty(ITerminalsConnectorConstants.PROP_ID, "org.eclipse.tm.te.ui.terminals.TerminalsView"); //$NON-NLS-1$
					// Set the terminal tab title
					String terminalTitle = getTerminalTitle();
					if (terminalTitle != null) {
						props.setProperty(ITerminalsConnectorConstants.PROP_TITLE, terminalTitle);
					}

					// Create and store the streams which will be connected to the terminals stdin
					props.setProperty(ITerminalsConnectorConstants.PROP_STREAMS_STDIN, connectRemoteOutputStream(getStreamsListener(), new String[] { IProcesses.PROP_STDIN_ID }));
					// Create and store the streams the terminal will see as stdout
					props.setProperty(ITerminalsConnectorConstants.PROP_STREAMS_STDOUT, connectRemoteInputStream(getStreamsListener(), new String[] { IProcesses.PROP_STDOUT_ID }));
					// Create and store the streams the terminal will see as stderr
					props.setProperty(ITerminalsConnectorConstants.PROP_STREAMS_STDERR, connectRemoteInputStream(getStreamsListener(), new String[] { IProcesses.PROP_STDERR_ID }));

					// Copy the terminal properties
					props.setProperty(ITerminalsConnectorConstants.PROP_LOCAL_ECHO, properties.getBooleanProperty(ITerminalsConnectorConstants.PROP_LOCAL_ECHO));
					props.setProperty(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR, properties.getStringProperty(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR));

					// The custom data object is the process launcher itself
					props.setProperty(ITerminalsConnectorConstants.PROP_DATA, this);

					// Open the console
					terminal.openConsole(props, null);
				}
			} else {
				// Create and connect the streams which will be connected to the terminals stdin
				streamsProxy.connectInputStreamMonitor(connectRemoteOutputStream(getStreamsListener(), new String[] { IProcesses.PROP_STDIN_ID }));
				// Create and store the streams the terminal will see as stdout
				streamsProxy.connectOutputStreamMonitor(connectRemoteInputStream(getStreamsListener(), new String[] { IProcesses.PROP_STDOUT_ID }));
				// Create and store the streams the terminal will see as stderr
				streamsProxy.connectErrorStreamMonitor(connectRemoteInputStream(getStreamsListener(), new String[] { IProcesses.PROP_STDERR_ID }));
			}
		}

		// The streams got subscribed, check if we shall configure the output redirection to a file
		if (properties.getStringProperty(IProcessLauncher.PROP_PROCESS_OUTPUT_REDIRECT_TO_FILE) != null) {
			// Get the file name where to redirect the process output to
			String filename = properties.getStringProperty(IProcessLauncher.PROP_PROCESS_OUTPUT_REDIRECT_TO_FILE);
			try {
				// Create the receiver instance. If the file already exist, we
				// overwrite the file content.
				StreamsDataReceiver receiver = new StreamsDataReceiver(new BufferedWriter(new FileWriter(filename)),
																					 new String[] { IProcesses.PROP_STDOUT_ID, IProcesses.PROP_STDERR_ID });
				// Register the receiver to the streams listener
				if (getStreamsListener() instanceof ProcessStreamsListener) {
					((ProcessStreamsListener)getStreamsListener()).registerDataReceiver(receiver);
				}
			} catch (IOException e) {
				// Construct the error message to show to the user
				String message = NLS.bind(Messages.ProcessLauncher_error_processLaunchFailed,
								properties.getStringProperty(IProcessLauncher.PROP_PROCESS_PATH),
								makeString((String[])properties.getProperty(IProcessLauncher.PROP_PROCESS_ARGS)));
				message += NLS.bind(Messages.ProcessLauncher_error_possibleCause,
								e.getLocalizedMessage() != null ? e.getLocalizedMessage() : Messages.ProcessLauncher_cause_ioexception);

				// Construct the status object
				IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), message, e);
				invokeCallback(status, null);
			}
		}

		// Launch the process
		onAttachStreamsDone();
	}

	/**
	 * Returns the terminal title string.
	 * <p>
	 * The default implementation constructs a title like &quot;<process> (Start time) [channel name]&quot;.
	 *
	 * @return The terminal title string or <code>null</code>.
	 */
	protected String getTerminalTitle() {
		if (properties == null) {
			return null;
		}

		StringBuilder title = new StringBuilder();

		IPath processPath = new Path(properties.getStringProperty(IProcessLauncher.PROP_PROCESS_PATH));
		IPath monitoredProcessPath = null;
		if (properties.getStringProperty(IProcessLauncher.PROP_PROCESS_MONITORED_PATH) != null) {
			monitoredProcessPath = new Path(properties.getStringProperty(IProcessLauncher.PROP_PROCESS_MONITORED_PATH));
		}

		// In case, we do have a monitored process path here, we construct the title
		// as <monitor_app_basename>: <monitored_app>"
		if (monitoredProcessPath != null) {
			title.append(processPath.lastSegment());
			title.append(": "); //$NON-NLS-1$
			processPath = monitoredProcessPath;
		}

		// Avoid very long terminal title's by shortening the path if it has more than 3 segments
		if (processPath.segmentCount() > 3) {
			title.append(".../"); //$NON-NLS-1$
			title.append(processPath.lastSegment());
		} else {
			title.append(processPath.toString());
		}

		// Get the peer name
		final AtomicReference<String> peerName = new AtomicReference<String>(getProperties().getStringProperty(IProcessLauncher.PROP_CONNECTION_NAME));
		if (peerName.get() == null) {
			// Query the peer from the open channel
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					if (channel != null) {
						peerName.set(channel.getRemotePeer().getName());
					}
				}
			};

			if (Protocol.isDispatchThread()) runnable.run();
			else Protocol.invokeAndWait(runnable);
		}

		if (peerName.get() != null) {
			title.append(" [").append(peerName.get()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
		String date = format.format(new Date(System.currentTimeMillis()));
		title.append(" (").append(date).append(")"); //$NON-NLS-1$ //$NON-NLS-2$

		return title.toString();
	}

	/**
	 * Connects the given stream id's to a local {@link InputStream} instance.
	 *
	 * @param streamsListener The streams listener. Must not be <code>null</code>.
	 * @param streamIds The stream id's. Must not be <code>null</code>.
	 *
	 * @return The local input stream instance or <code>null</code>.
	 */
	protected InputStream connectRemoteInputStream(IStreams.StreamsListener streamsListener, String[] streamIds) {
		Assert.isNotNull(streamsListener);
		Assert.isNotNull(streamIds);

		InputStream stream = null;

		// Create the output stream receiving the data from remote
		PipedOutputStream remoteStreamDataReceiverStream = new PipedOutputStream();
		// Create the piped input stream instance
		try { stream = new PipedInputStream(remoteStreamDataReceiverStream); } catch (IOException e) { /* ignored on purpose */ }

		// If the input stream creation succeeded, connect the data receiver
		if (stream != null) {
			StreamsDataReceiver receiver = new StreamsDataReceiver(new OutputStreamWriter(remoteStreamDataReceiverStream), streamIds);
			// Register the data receiver to the streams listener
			if (getStreamsListener() instanceof ProcessStreamsListener) {
				((ProcessStreamsListener)getStreamsListener()).registerDataReceiver(receiver);
			}
		}

		return stream;
	}

	/**
	 * Connects the given stream id's to a local {@link OutputStream} instance.
	 *
	 * @param streamsListener The streams listener. Must not be <code>null</code>.
	 * @param streamIds The stream id's. Must not be <code>null</code>.
	 *
	 * @return The local output stream instance or <code>null</code>.
	 */
	protected OutputStream connectRemoteOutputStream(IStreams.StreamsListener streamsListener, String[] streamIds) {
		Assert.isNotNull(streamsListener);
		Assert.isNotNull(streamIds);

		PipedInputStream inStream = null;

		// Create the output stream receiving the data from local
		PipedOutputStream stream = new PipedOutputStream();
		// Create the piped input stream instance
		try { inStream = new PipedInputStream(stream); } catch (IOException e) { stream = null; }

		// If the stream creation succeeded, connect the data provider
		if (stream != null && inStream != null) {
			StreamsDataProvider provider = new StreamsDataProvider(new InputStreamReader(inStream), streamIds);
			// Register the data provider to the streams listener
			if (getStreamsListener() instanceof ProcessStreamsListener) {
				((ProcessStreamsListener)getStreamsListener()).setDataProvider(provider);
			}
		}

		return stream;
	}

	/**
	 * Queries the initial new process environment from remote.
	 */
	protected void onAttachStreamsDone() {
		// Query the default environment for a new process
		getSvcProcesses().getEnvironment(new IProcesses.DoneGetEnvironment() {
			@Override
			public void doneGetEnvironment(IToken token, Exception error, Map<String, String> environment) {
				if (error != null) {
					// Construct the error message to show to the user
					String message = Messages.ProcessLauncher_error_getEnvironmentFailed;
					message += NLS.bind(Messages.ProcessLauncher_error_possibleCause,
									error.getLocalizedMessage() != null ? error.getLocalizedMessage() : Messages.ProcessLauncher_cause_startFailed);

					// Construct the status object
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), message, error);
					invokeCallback(status, null);
				} else {
					// Initiate the process launch
					onGetEnvironmentDone(environment);
				}
			}
		});
	}

	/**
	 * Initiate the process launch.
	 * <p>
	 * Called from {@link #executeLaunch()} or {@link #onAttachStreamsDone()}.
	 */
	protected void onGetEnvironmentDone(final Map<String, String> environment) {
		// Get the process properties container
		final IPropertiesContainer properties = getProperties();
		if (properties == null) {
			// This is an illegal argument. Properties must be set
			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
							NLS.bind(Messages.ProcessLauncher_error_illegalNullArgument, "properties"), //$NON-NLS-1$
							new IllegalArgumentException());
			invokeCallback(status, null);
			return;
		}

		// Create the process listener
		processesListener = createProcessesListener();
		if (processesListener != null) {
			getSvcProcesses().addListener(processesListener);
		}

		// Get the process attributes
		String processPath = properties.getStringProperty(IProcessLauncher.PROP_PROCESS_PATH);

		String[] processArgs = (String[])properties.getProperty(IProcessLauncher.PROP_PROCESS_ARGS);
		// Assure that the first argument is the process path itself
		if (!(processArgs != null && processArgs.length > 0 && processPath.equals(processArgs[0]))) {
			// Prepend the process path to the list of arguments
			List<String> args = processArgs != null ? new ArrayList<String>(Arrays.asList(processArgs)) : new ArrayList<String>();
			args.add(0, processPath);
			processArgs = args.toArray(new String[args.size()]);
		}

		String processCWD = properties.getStringProperty(IProcessLauncher.PROP_PROCESS_CWD);
		// If the process working directory is not explicitly set, default to the process path directory
		if (processCWD == null || "".equals(processCWD.trim())) { //$NON-NLS-1$
			processCWD = new Path(processPath).removeLastSegments(1).toString();
		}

		// Merge the initial process environment and the desired process environment
		Map<String, String> processEnv = new HashMap<String, String>(environment);
		Map<String, String> processEnvDiff = (Map<String, String>)properties.getProperty(IProcessLauncher.PROP_PROCESS_ENV);
		if (processEnvDiff != null && !processEnvDiff.isEmpty()) {
			processEnv.putAll(processEnvDiff);
		}
		// Assure that the TERM variable is set to "ansi"
		processEnv.put("TERM", "ansi"); //$NON-NLS-1$ //$NON-NLS-2$

		boolean attach = properties.getBooleanProperty(IProcessLauncher.PROP_PROCESS_ATTACH);

		// Launch the remote process
		getSvcProcesses().start(processCWD, processPath, processArgs, processEnv, attach, new IProcesses.DoneStart() {
			@Override
			public void doneStart(IToken token, Exception error, ProcessContext process) {
				if (error != null) {
					// Construct the error message to show to the user
					String message = NLS.bind(Messages.ProcessLauncher_error_processLaunchFailed,
									properties.getStringProperty(IProcessLauncher.PROP_PROCESS_PATH),
									makeString((String[])properties.getProperty(IProcessLauncher.PROP_PROCESS_ARGS)));
					message += NLS.bind(Messages.ProcessLauncher_error_possibleCause,
									error.getLocalizedMessage() != null ? error.getLocalizedMessage() : Messages.ProcessLauncher_cause_startFailed);

					// Construct the status object
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), message, error);
					invokeCallback(status, null);
				} else {
					// Register the process context to the listener
					onProcessLaunchDone(process);
				}
			}
		});
	}

	/**
	 * Register the process context to the listeners.
	 *
	 * @param process The process context or <code>null</code>.
	 */
	protected void onProcessLaunchDone(ProcessContext process) {
		// Register the process context with the listeners
		if (process != null) {
			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_PROCESS_LAUNCHER)) {
				CoreBundleActivator.getTraceHandler().trace("Process context created: id='" + process.getID() + "', name='" + process.getName() + "'", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								0, ITraceIds.TRACE_PROCESS_LAUNCHER, IStatus.INFO, getClass());
			}

			// Remember the process context
			processContext = process;

			// Push the process context to the listeners
			if (getStreamsListener() instanceof IProcessContextAwareListener) {
				((IProcessContextAwareListener)getStreamsListener()).setProcessContext(process);
			}
			if (getProcessesListener() instanceof IProcessContextAwareListener) {
				((IProcessContextAwareListener)getProcessesListener()).setProcessContext(process);
			}

			// Send a notification
			ProcessStateChangeEvent event = createRemoteProcessStateChangeEvent(process);
			if (event != null) EventManager.getInstance().fireEvent(event);
		}

		// Invoke the callback to signal that we are done
		invokeCallback(Status.OK_STATUS, process);
	}

	/**
	 * Creates a new remote process state change event instance.
	 *
	 * @param context The process context. Must not be <code>null</code>.
	 * @return The event instance or <code>null</code>.
	 */
	protected ProcessStateChangeEvent createRemoteProcessStateChangeEvent(IProcesses.ProcessContext context) {
		Assert.isNotNull(context);
		return new ProcessStateChangeEvent(context, ProcessStateChangeEvent.EVENT_PROCESS_CREATED, Boolean.FALSE, Boolean.TRUE, -1);
	}

	/**
	 * Invoke the callback with the given parameters. If the given status severity
	 * is {@link IStatus#ERROR}, the process launcher object is disposed automatically.
	 *
	 * @param status The status. Must not be <code>null</code>.
	 * @param result The result object or <code>null</code>.
	 */
	protected void invokeCallback(IStatus status, Object result) {
		// Dispose the process launcher if we report an error
		if (status.getSeverity() == IStatus.ERROR) {
			dispose();
		}

		// Invoke the callback
		ICallback callback = getCallback();
		if (callback != null) {
			callback.setResult(result);
			callback.done(this, status);
		}
	}

	/**
	 * Returns the channel instance.
	 *
	 * @return The channel instance or <code>null</code> if none.
	 */
	public final IChannel getChannel() {
		return channel;
	}

	/**
	 * Returns the process properties container.
	 *
	 * @return The process properties container or <code>null</code> if none.
	 */
	public final IPropertiesContainer getProperties() {
		return properties;
	}

	/**
	 * Returns the processes service instance.
	 *
	 * @return The processes service instance or <code>null</code> if none.
	 */
	public final IProcesses getSvcProcesses() {
		return svcProcesses;
	}

	/**
	 * Returns the streams service instance.
	 *
	 * @return The streams service instance or <code>null</code> if none.
	 */
	public final IStreams getSvcStreams() {
		return svcStreams;
	}

	/**
	 * Returns the callback instance.
	 *
	 * @return The callback instance or <code>null</code> if none.
	 */
	protected final ICallback getCallback() {
		return callback;
	}

	/**
	 * Create the streams listener instance.
	 *
	 * @return The streams listener instance or <code>null</code> if none.
	 */
	protected IStreams.StreamsListener createStreamsListener() {
		return new ProcessStreamsListener(this);
	}

	/**
	 * Returns the streams listener instance.
	 *
	 * @return The streams listener instance or <code>null</code>.
	 */
	protected final IStreams.StreamsListener getStreamsListener() {
		return streamsListener;
	}

	/**
	 * Create the processes listener instance.
	 *
	 * @return The processes listener instance or <code>null</code> if none.
	 */
	protected IProcesses.ProcessesListener createProcessesListener() {
		return new ProcessProcessesListener(this);
	}

	/**
	 * Returns the processes listener instance.
	 *
	 * @return The processes listener instance or <code>null</code> if none.
	 */
	protected final IProcesses.ProcessesListener getProcessesListener() {
		return processesListener;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(IProcesses.ProcessesListener.class)) {
			return processesListener;
		}
		else if (adapter.isAssignableFrom(IStreams.StreamsListener.class)) {
			return streamsListener;
		}
		else if (adapter.isAssignableFrom(IStreams.class)) {
			return svcStreams;
		}
		else if (adapter.isAssignableFrom(IProcesses.class)) {
			return svcProcesses;
		}
		else if (adapter.isAssignableFrom(IChannel.class)) {
			return channel;
		}
		else if (adapter.isAssignableFrom(IPropertiesContainer.class)) {
			return properties;
		}
		else if (adapter.isAssignableFrom(IProcesses.ProcessContext.class)) {
			return processContext;
		}
		else if (adapter.isAssignableFrom(this.getClass())) {
			return this;
		}


		return super.getAdapter(adapter);
	}

	/**
	 * Makes a space separated string from the given array.
	 *
	 * @param array The string array or <code>null</code>.
	 * @return The string.
	 */
	String makeString(String[] array) {
		if (array == null) {
			return ""; //$NON-NLS-1$
		}
		StringBuilder result = new StringBuilder();
		for (String element : array) {
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(element);
		}
		return result.toString();
	}
}
