/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.terminals.core.launcher;

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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IStreams;
import org.eclipse.tm.tcf.services.ITerminals;
import org.eclipse.tm.tcf.services.ITerminals.TerminalContext;
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
import org.eclipse.tm.te.tcf.terminals.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsContextAwareListener;
import org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsLauncher;
import org.eclipse.tm.te.tcf.terminals.core.internal.tracing.ITraceIds;
import org.eclipse.tm.te.tcf.terminals.core.nls.Messages;

/**
 * Remote terminals launcher.
 * <p>
 * The terminals launcher is implemented fully asynchronous.
 */
public class TerminalsLauncher extends PlatformObject implements ITerminalsLauncher {
	// The channel instance
	/* default */ IChannel channel;
	// The terminals properties instance
	private IPropertiesContainer properties;

	// The terminals service instance
	/* default */ ITerminals svcTerminals;
	// The streams service instance
	/* default */ IStreams svcStreams;
	// The remote terminals context
	/* default */ ITerminals.TerminalContext terminalContext;

	// The callback instance
	private ICallback callback;

	// The streams listener instance
	private IStreams.StreamsListener streamsListener = null;
	// The terminals listener instance
	private ITerminals.TerminalsListener terminalsListener = null;
	// The event listener instance
	private IEventListener eventListener = null;

	/**
	 * Constructor.
	 */
	public TerminalsLauncher() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsLauncher#dispose()
	 */
	@Override
	public void dispose() {
		// Unlink the process context
		terminalContext = null;

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
			if (streamsListener instanceof TerminalsStreamsListener) {
				((TerminalsStreamsListener)streamsListener).dispose(new AsyncCallbackCollector.SimpleCollectorCallback(collector));
			}
			streamsListener = null;
		}

		// Dispose the terminals listener if created
		if (terminalsListener != null) {
			// Dispose the terminals listener
			if (terminalsListener instanceof TerminalsListener) {
				((TerminalsListener)terminalsListener).dispose(new AsyncCallbackCollector.SimpleCollectorCallback(collector));
			}
			terminalsListener = null;
			// Remove the terminals listener from the processes service
			getSvcTerminals().removeListener(terminalsListener);
		}

		// Mark the collector initialization as done
		collector.initDone();

		// Dissociate the channel
		channel = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsLauncher#exit()
	 */
	@Override
	public void exit() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (terminalContext != null) {
					// Exit the terminal
					terminalContext.exit(new ITerminals.DoneCommand() {
						@Override
						public void doneCommand(IToken token, Exception error) {
							onExitDone(terminalContext, error);
						}
					});
				}

			}
		};

		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);
	}

	/**
	 * Check if the terminal context really exited.
	 * <p>
	 * Called from {@link #exit()}.
	 *
	 * @param context The terminal context. Must not be <code>null</code>.
	 * @param error The exception in case {@link #exit()} returned with an error or <code>null</code>.
	 */
	protected void onExitDone(ITerminals.TerminalContext context, Exception error) {
		Assert.isNotNull(context);

		// If the exit of the remote terminal context failed, give a warning to the user
		if (error != null) {
			String message = NLS.bind(Messages.TerminalsLauncher_error_terminalExitFailed, context.getProcessID());
			message += NLS.bind(Messages.TerminalsLauncher_error_possibleCause, error.getLocalizedMessage());

			IStatus status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(), message, error);
			Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);

			// Dispose the launcher directly
			dispose();
		}
		// No error from exit -> double-check.
		else {
			final ITerminals.TerminalContext finContext = context;
			// Let's see if we can still get information about the context
			getSvcTerminals().getContext(context.getID(), new ITerminals.DoneGetContext() {
				@Override
				public void doneGetContext(IToken token, Exception error, TerminalContext context) {
					// In case there is no error and we do get back an process context,
					// the process must be still running, having ignored the SIGTERM.
					if (error == null && context != null && context.getID().equals(finContext.getID())) {
						String message = NLS.bind(Messages.TerminalsLauncher_error_terminalExitFailed, context.getProcessID());
						message += Messages.TerminalsLauncher_error_possibleCauseUnknown;

						IStatus status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(), message, error);
						Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);

						// Dispose the launcher directly
						dispose();
					}
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsLauncher#launch(org.eclipse.tm.tcf.protocol.IPeer, org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tm.te.runtime.interfaces.callback.ICallback)
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
					TerminalsLauncher.this.channel = channel;

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
												NLS.bind(Messages.TerminalsLauncher_error_channelConnectFailed, peer.getID(), error.getLocalizedMessage()),
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
										Messages.TerminalsLauncher_error_channelNotConnected,
										new IllegalStateException());
						invokeCallback(status, null);
						return;
					}

					// Get the terminals and streams services
					svcTerminals = channel.getRemoteService(ITerminals.class);
					if (svcTerminals == null) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										NLS.bind(Messages.TerminalsLauncher_error_missingRequiredService, ITerminals.class.getName()),
										null);

						invokeCallback(status, null);
						return;
					}

					svcStreams = channel.getRemoteService(IStreams.class);
					if (svcStreams == null) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										NLS.bind(Messages.TerminalsLauncher_error_missingRequiredService, IStreams.class.getName()),
										null);
						invokeCallback(status, null);
						return;
					}

					// Execute the launch now
					executeLaunch();
				} else {
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
									NLS.bind(Messages.TerminalsLauncher_error_channelConnectFailed, peer.getID(), error.getLocalizedMessage()),
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
							NLS.bind(Messages.TerminalsLauncher_error_illegalNullArgument, "properties"), //$NON-NLS-1$
							new IllegalArgumentException());
			invokeCallback(status, null);
			return;
		}

		// Create the streams listener
		streamsListener = createStreamsListener();
		// If available, we need to subscribe to the streams.
		if (streamsListener != null) {
			getSvcStreams().subscribe(ITerminals.NAME, streamsListener, new IStreams.DoneSubscribe() {
				@Override
				public void doneSubscribe(IToken token, Exception error) {
					// In case the subscribe to the stream fails, we pass on
					// the error to the user and stop the launch
					if (error != null) {
						// Construct the error message to show to the user
						String message = NLS.bind(Messages.TerminalsLauncher_error_terminalLaunchFailed,
												  properties.getStringProperty(ITerminalsLauncher.PROP_CONNECTION_NAME));
						message += NLS.bind(Messages.TerminalsLauncher_error_possibleCause, Messages.TerminalsLauncher_cause_subscribeFailed);

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
			// No streams to attach to -> go directly to the terminals launch
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
							NLS.bind(Messages.TerminalsLauncher_error_illegalNullArgument, "properties"), //$NON-NLS-1$
							new IllegalArgumentException());
			invokeCallback(status, null);
			return;
		}

		// Register the notification listener to listen to the console disposal
		eventListener = new TerminalsLauncherEventListener(this);
		EventManager.getInstance().addEventListener(eventListener, DisposedEvent.class);

		// Get the terminal service
		ITerminalService terminal = (ITerminalService)ServiceManager.getInstance().getService(ITerminalService.class);
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
			props.setProperty(ITerminalsConnectorConstants.PROP_STREAMS_STDIN, connectRemoteOutputStream(getStreamsListener(), new String[] { ITerminals.PROP_STDIN_ID }));
			// Create and store the streams the terminal will see as stdout
			props.setProperty(ITerminalsConnectorConstants.PROP_STREAMS_STDOUT, connectRemoteInputStream(getStreamsListener(), new String[] { ITerminals.PROP_STDOUT_ID }));
			// Create and store the streams the terminal will see as stderr
			props.setProperty(ITerminalsConnectorConstants.PROP_STREAMS_STDERR, connectRemoteInputStream(getStreamsListener(), new String[] { ITerminals.PROP_STDERR_ID }));

			// Copy the terminal properties
			props.setProperty(ITerminalsConnectorConstants.PROP_LOCAL_ECHO, properties.getBooleanProperty(ITerminalsConnectorConstants.PROP_LOCAL_ECHO));
			props.setProperty(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR, properties.getStringProperty(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR));

			// The custom data object is the process launcher itself
			props.setProperty(ITerminalsConnectorConstants.PROP_DATA, this);

			// Open the console
			terminal.openConsole(props, null);
		}

		// The streams got subscribed, check if we shall configure the output redirection to a file
		if (properties.getStringProperty(ITerminalsLauncher.PROP_TERMINAL_OUTPUT_REDIRECT_TO_FILE) != null) {
			// Get the file name where to redirect the terminal output to
			String filename = properties.getStringProperty(ITerminalsLauncher.PROP_TERMINAL_OUTPUT_REDIRECT_TO_FILE);
			try {
				// Create the receiver instance. If the file already exist, we
				// overwrite the file content.
				StreamsDataReceiver receiver = new StreamsDataReceiver(new BufferedWriter(new FileWriter(filename)),
																 	   new String[] { ITerminals.PROP_STDOUT_ID, ITerminals.PROP_STDERR_ID });
				// Register the receiver to the streams listener
				if (getStreamsListener() instanceof TerminalsStreamsListener) {
					((TerminalsStreamsListener)getStreamsListener()).registerDataReceiver(receiver);
				}
			} catch (IOException e) {
				// Construct the error message to show to the user
				String message = NLS.bind(Messages.TerminalsLauncher_error_terminalLaunchFailed,
								  		  properties.getStringProperty(ITerminalsLauncher.PROP_CONNECTION_NAME));
				message += NLS.bind(Messages.TerminalsLauncher_error_possibleCause,
								e.getLocalizedMessage() != null ? e.getLocalizedMessage() : Messages.TerminalsLauncher_cause_ioexception);

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
	 * The default implementation constructs a title like &quot;[peer name] (Start time) &quot;.
	 *
	 * @return The terminal title string or <code>null</code>.
	 */
	protected String getTerminalTitle() {
		if (properties == null) {
			return null;
		}

		StringBuilder title = new StringBuilder();

		// Get the peer name
		final AtomicReference<String> peerName = new AtomicReference<String>(getProperties().getStringProperty(ITerminalsLauncher.PROP_CONNECTION_NAME));
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
			title.append("[").append(peerName.get()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
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
			if (getStreamsListener() instanceof TerminalsStreamsListener) {
				((TerminalsStreamsListener)getStreamsListener()).registerDataReceiver(receiver);
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
			if (getStreamsListener() instanceof TerminalsStreamsListener) {
				((TerminalsStreamsListener)getStreamsListener()).setDataProvider(provider);
			}
		}

		return stream;
	}

	/**
	 * Initiate the process launch.
	 * <p>
	 * Called from {@link #executeLaunch()} or {@link #onAttachStreamsDone()}.
	 */
	protected void onAttachStreamsDone() {
		// Get the process properties container
		final IPropertiesContainer properties = getProperties();
		if (properties == null) {
			// This is an illegal argument. Properties must be set
			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
							NLS.bind(Messages.TerminalsLauncher_error_illegalNullArgument, "properties"), //$NON-NLS-1$
							new IllegalArgumentException());
			invokeCallback(status, null);
			return;
		}

		// Create the process listener
		terminalsListener = createTerminalsListener();
		if (terminalsListener != null) {
			getSvcTerminals().addListener(terminalsListener);
		}

		// Get the terminal attributes
		String type = properties.getStringProperty(ITerminalsLauncher.PROP_TERMINAL_TYPE);
		String encoding = properties.getStringProperty(ITerminalsLauncher.PROP_TERMINAL_ENCODING);
		Map<String, String> env = (Map<String, String>)properties.getProperty(ITerminalsLauncher.PROP_TERMINAL_ENV);

		// Launch the remote process
		getSvcTerminals().launch(type, encoding, makeEnvironmentArray(env), new ITerminals.DoneLaunch() {
			@Override
			public void doneLaunch(IToken token, Exception error, ITerminals.TerminalContext terminal) {
				if (error != null) {
					// Construct the error message to show to the user
					String message = NLS.bind(Messages.TerminalsLauncher_error_terminalLaunchFailed,
							  		  properties.getStringProperty(ITerminalsLauncher.PROP_CONNECTION_NAME));
					message += NLS.bind(Messages.TerminalsLauncher_error_possibleCause,
									error.getLocalizedMessage() != null ? error.getLocalizedMessage() : Messages.TerminalsLauncher_cause_startFailed);

					// Construct the status object
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), message, error);
					invokeCallback(status, null);
				} else {
					// Register the terminal context to the listener
					onTerminalLaunchDone(terminal);
				}
			}
		});
	}

	/**
	 * Register the terminals context to the listeners.
	 *
	 * @param terminal The terminals context or <code>null</code>.
	 */
	protected void onTerminalLaunchDone(ITerminals.TerminalContext terminal) {
		// Register the process context with the listeners
		if (terminal != null) {
			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_TERMINALS_LAUNCHER)) {
				CoreBundleActivator.getTraceHandler().trace("Terminal context created: id='" + terminal.getID() + "', PTY type='" + terminal.getPtyType() + "'", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								0, ITraceIds.TRACE_TERMINALS_LAUNCHER, IStatus.INFO, getClass());
			}

			// Remember the process context
			terminalContext = terminal;

			// Push the terminals context to the listeners
			if (getStreamsListener() instanceof ITerminalsContextAwareListener) {
				((ITerminalsContextAwareListener)getStreamsListener()).setTerminalsContext(terminal);
			}
			if (getProcessesListener() instanceof ITerminalsContextAwareListener) {
				((ITerminalsContextAwareListener)getProcessesListener()).setTerminalsContext(terminal);
			}

			// Send a notification
			TerminalsStateChangeEvent event = createRemoteTerminalsStateChangeEvent(terminal);
			if (event != null) EventManager.getInstance().fireEvent(event);
		}

		// Invoke the callback to signal that we are done
		invokeCallback(Status.OK_STATUS, terminal);
	}

	/**
	 * Creates a new remote terminals state change event instance.
	 *
	 * @param context The terminals context. Must not be <code>null</code>.
	 * @return The event instance or <code>null</code>.
	 */
	protected TerminalsStateChangeEvent createRemoteTerminalsStateChangeEvent(ITerminals.TerminalContext context) {
		Assert.isNotNull(context);
		return new TerminalsStateChangeEvent(context, TerminalsStateChangeEvent.EVENT_PROCESS_CREATED, Boolean.FALSE, Boolean.TRUE, -1);
	}

	/**
	 * Invoke the callback with the given parameters. If the given status severity
	 * is {@link IStatus#ERROR}, the terminals launcher object is disposed automatically.
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
	 * Returns the terminals properties container.
	 *
	 * @return The terminals properties container or <code>null</code> if none.
	 */
	public final IPropertiesContainer getProperties() {
		return properties;
	}

	/**
	 * Returns the terminals service instance.
	 *
	 * @return The terminals service instance or <code>null</code> if none.
	 */
	public final ITerminals getSvcTerminals() {
		return svcTerminals;
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
		return new TerminalsStreamsListener(this);
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
	 * Create the terminals listener instance.
	 *
	 * @return The terminals listener instance or <code>null</code> if none.
	 */
	protected ITerminals.TerminalsListener createTerminalsListener() {
		return new TerminalsListener(this);
	}

	/**
	 * Returns the terminals listener instance.
	 *
	 * @return The terminals listener instance or <code>null</code> if none.
	 */
	protected final ITerminals.TerminalsListener getProcessesListener() {
		return terminalsListener;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ITerminals.TerminalsListener.class)) {
			return terminalsListener;
		}
		else if (adapter.isAssignableFrom(IStreams.StreamsListener.class)) {
			return streamsListener;
		}
		else if (adapter.isAssignableFrom(IStreams.class)) {
			return svcStreams;
		}
		else if (adapter.isAssignableFrom(ITerminals.class)) {
			return svcTerminals;
		}
		else if (adapter.isAssignableFrom(IChannel.class)) {
			return channel;
		}
		else if (adapter.isAssignableFrom(IPropertiesContainer.class)) {
			return properties;
		}
		else if (adapter.isAssignableFrom(ITerminals.TerminalContext.class)) {
			return terminalContext;
		}
		else if (adapter.isAssignableFrom(this.getClass())) {
			return this;
		}


		return super.getAdapter(adapter);
	}

	/**
	 * Makes an environment array out of the given map.
	 *
	 * @param env The environment map or <code>null</code>.
	 * @return The string.
	 */
	private String[] makeEnvironmentArray(Map<String, String> env) {
		if (env == null) return null;

		List<String> envList = new ArrayList<String>();
		for (String key : env.keySet()) {
			String entry = key.trim() + "=" + env.get(key).trim(); //$NON-NLS-1$
			envList.add(entry);
		}

		return envList.toArray(new String[envList.size()]);
	}
}
