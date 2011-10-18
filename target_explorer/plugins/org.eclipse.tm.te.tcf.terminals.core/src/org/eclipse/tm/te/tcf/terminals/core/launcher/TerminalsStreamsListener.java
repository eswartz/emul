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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IStreams;
import org.eclipse.tm.tcf.services.ITerminals;
import org.eclipse.tm.tcf.services.ITerminals.TerminalContext;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.tm.te.core.async.AsyncCallbackCollector;
import org.eclipse.tm.te.runtime.callback.Callback;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.tcf.core.streams.StreamsDataProvider;
import org.eclipse.tm.te.tcf.core.streams.StreamsDataReceiver;
import org.eclipse.tm.te.tcf.core.utils.ExceptionUtils;
import org.eclipse.tm.te.tcf.terminals.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsContextAwareListener;
import org.eclipse.tm.te.tcf.terminals.core.internal.tracing.ITraceIds;
import org.eclipse.tm.te.tcf.terminals.core.nls.Messages;

/**
 * Remote terminal streams listener implementation.
 */
public class TerminalsStreamsListener implements IStreams.StreamsListener, ITerminalsContextAwareListener {
	// The parent terminals launcher instance
	private final TerminalsLauncher parent;
	// The remote terminal context
	private ITerminals.TerminalContext context;
	// The list of registered stream data receivers
	private final List<StreamsDataReceiver> dataReceiver = new ArrayList<StreamsDataReceiver>();
	// The stream data provider
	private StreamsDataProvider dataProvider;
	// The list of delayed stream created events
	private final List<StreamCreatedEvent> delayedCreatedEvents = new ArrayList<StreamCreatedEvent>();
	// The list of created runnable's
	private final List<Runnable> runnables = new ArrayList<Runnable>();

	/**
	 * Immutable stream created event.
	 */
	private final static class StreamCreatedEvent {
		/**
		 * The stream type.
		 */
		public final String streamType;
		/**
		 * The stream id.
		 */
		public final String streamId;
		/**
		 * The context id.
		 */
		public final String contextId;

		// As the class is immutable, we do not need to build the toString
		// value again and again. Build it once in the constructor and reuse it later.
		private final String toString;

		/**
		 * Constructor.
		 *
		 * @param streamType The stream type.
		 * @param streamId The stream id.
		 * @param contextId The context id.
		 */
		public StreamCreatedEvent(String streamType, String streamId, String contextId) {
			this.streamType = streamType;
			this.streamId = streamId;
			this.contextId = contextId;

			toString = toString();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return obj instanceof StreamCreatedEvent
					&& toString().equals(((StreamCreatedEvent)obj).toString());
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			if (toString != null) return toString;

			StringBuilder builder = new StringBuilder(getClass().getSimpleName());
			builder.append(": streamType = "); //$NON-NLS-1$
			builder.append(streamType);
			builder.append("; streamId = "); //$NON-NLS-1$
			builder.append(streamId);
			builder.append("; contextId = "); //$NON-NLS-1$
			builder.append(contextId);

			return builder.toString();
		}
	}

	/**
	 * Remote stream reader runnable implementation.
	 * <p>
	 * The runnable will be executed within a thread and is responsible to read the
	 * incoming data from the associated stream and forward them to the registered receivers.
	 */
	protected class StreamReaderRunnable implements Runnable {
		// The associated stream id
		private final String streamId;
		// The associated stream type id
		private final String streamTypeId;
		// The list of receivers applicable for the associated stream type id
		private final List<StreamsDataReceiver> receivers = new ArrayList<StreamsDataReceiver>();
		// The currently active read task
		private TCFTask<ReadData> activeTask;
		// The callback to invoke if the runnable stopped
		private ICallback callback;

		// Flag to stop the runnable
		private boolean stopped = false;

		/**
		 * Immutable class describing the result returned by {@link StreamReaderRunnable#read(IStreams, String, int)}.
		 */
		protected class ReadData {
			/**
			 * The number of lost bytes in case of a buffer overflow. If <code>-1</code>,
			 * an unknown number of bytes were lost. If non-zero and <code>data.length</code> is
			 * non-zero, the lost bytes are considered located right before the read bytes.
			 */
			public final int lostBytes;
			/**
			 * The read data as byte array.
			 */
			public final byte[] data;
			/**
			 * Flag to signal if the end of the stream has been reached.
			 */
			public final boolean eos;

			/**
			 * Constructor.
			 */
			public ReadData(int lostBytes, byte[] data, boolean eos) {
				this.lostBytes = lostBytes;
				this.data = data;
				this.eos = eos;
			}
		}

		/**
		 * Constructor.
		 *
		 * @param streamId The associated stream id. Must not be <code>null</code>.
		 * @param streamTypeId The associated stream type id. Must not be <code>null</code>.
		 * @param receivers The list of registered data receivers. Must not be <code>null</code>.
		 */
		public StreamReaderRunnable(String streamId, String streamTypeId, StreamsDataReceiver[] receivers) {
			Assert.isNotNull(streamId);
			Assert.isNotNull(streamTypeId);
			Assert.isNotNull(receivers);

			this.streamId = streamId;
			this.streamTypeId = streamTypeId;

			// Loop the list of receivers and filter out the applicable ones
			for (StreamsDataReceiver receiver : receivers) {
				if (receiver.isApplicable(this.streamTypeId))
					this.receivers.add(receiver);
			}
		}

		/**
		 * Returns the associated stream id.
		 *
		 * @return The associated stream id.
		 */
		public final String getStreamId() {
			return streamId;
		}

		/**
		 * Returns if or if not the list of applicable receivers is empty.
		 *
		 * @return <code>True</code> if the list of applicable receivers is empty, <code>false</code> otherwise.
		 */
		public final boolean isEmpty() {
			return receivers.isEmpty();
		}

		/**
		 * Stop the runnable.
		 *
		 * @param callback The callback to invoke if the runnable stopped.
		 */
		public final synchronized void stop(ICallback callback) {
			// If the runnable is stopped already, invoke the callback directly
			if (stopped) {
				if (callback != null) callback.done(this, Status.OK_STATUS);
				return;
			}

			// Store the callback instance
			this.callback = callback;
			// Mark the runnable as stopped
			stopped = true;
		}

		/**
		 * Returns if the runnable should stop.
		 */
		protected final synchronized boolean isStopped() {
			return stopped;
		}

		/**
		 * Sets the currently active reader task.
		 *
		 * @param task The currently active reader task or <code>null</code>.
		 */
		protected final synchronized void setActiveTask(TCFTask<ReadData> task) {
			activeTask = task;
		}

		/**
		 * Returns the currently active reader task.
		 *
		 * @return The currently active reader task or <code>null</code>.
		 */
		protected final TCFTask<ReadData> getActiveTask() {
			return activeTask;
		}

		/**
		 * Returns the callback instance to invoke.
		 *
		 * @return The callback instance or <code>null</code>.
		 */
		protected final ICallback getCallback() {
			return callback;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
        public void run() {
			// Create a snapshot of the receivers
			final StreamsDataReceiver[] receivers = this.receivers.toArray(new StreamsDataReceiver[this.receivers.size()]);
			// Get the service instance from the parent
			final IStreams svcStreams = getParent().getSvcStreams();

			// Run until stopped and the streams service is available
			while (!isStopped() && svcStreams != null) {
				try {
					ReadData streamData = read(svcStreams, streamId, 1024);
					if (streamData != null) {
						// Check if the received data contains some stream data
						if (streamData.data != null) {
							// Notify the data receivers about the new received data
							notifyReceiver(new String(streamData.data), receivers);
						}
						// If the end of the stream have been reached --> break out
						if (streamData.eos) break;
					}
				} catch (Exception e) {
					// An error occurred -> Dump to the error log
					e = ExceptionUtils.checkAndUnwrapException(e);
					// Check if the blocking read task got canceled
					if (!(e instanceof CancellationException)) {
						// Log the error to the user, might be something serious
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
													NLS.bind(Messages.TerminalsStreamReaderRunnable_error_readFailed, streamId, e.getLocalizedMessage()),
													e);
						Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
					}
					// break out of the loop
					break;
				}
			}

			// Disconnect from the stream
			if (svcStreams != null) {
				svcStreams.disconnect(streamId, new IStreams.DoneDisconnect() {
					@Override
                    @SuppressWarnings("synthetic-access")
					public void doneDisconnect(IToken token, Exception error) {
						// Disconnect is done, ignore any error, invoke the callback
						synchronized (this) {
							if (getCallback() != null) getCallback().done(this, Status.OK_STATUS);
						}
						// Mark the runnable definitely stopped
						stopped = true;
					}
				});
			} else {
				// Invoke the callback directly, if any
				synchronized (this) {
					if (callback != null) callback.done(this, Status.OK_STATUS);
				}
				// Mark the runnable definitely stopped
				stopped = true;
			}
		}

		/**
		 * Reads data from the stream and blocks until some data has been received.
		 *
		 * @param service The streams service. Must not be <code>null</code>.
		 * @param streamId The stream id. Must not be <code>null</code>.
		 * @param size The size of the data to read.
		 *
		 * @return The read data.
		 *
		 * @throws Exception In case the read fails.
		 */
		protected final ReadData read(final IStreams service, final String streamId, final int size) throws Exception {
			Assert.isNotNull(service);
			Assert.isNotNull(streamId);
			Assert.isTrue(!Protocol.isDispatchThread());

			// Create the task object
			TCFTask<ReadData> task = new TCFTask<ReadData>(getParent().getChannel()) {
				@Override
                public void run() {
					service.read(streamId, size, new IStreams.DoneRead() {
						/* (non-Javadoc)
						 * @see org.eclipse.tm.tcf.services.IStreams.DoneRead#doneRead(org.eclipse.tm.tcf.protocol.IToken, java.lang.Exception, int, byte[], boolean)
						 */
						@Override
                        public void doneRead(IToken token, Exception error, int lostSize, byte[] data, boolean eos) {
							if (error == null) done(new ReadData(lostSize, data, eos));
							else error(error);
						}
					});
				}
			};

			// Push the task object to the runnable instance
			setActiveTask(task);

			// Block until some data is received
			return task.get();
		}

		/**
		 * Notify the data receiver that some data has been received.
		 *
		 * @param data The data or <code>null</code>.
		 */
		protected final void notifyReceiver(final String data, final StreamsDataReceiver[] receivers) {
			if (data == null) return;
			// Notify the data receiver
			for (StreamsDataReceiver receiver : receivers) {
				try {
					// Get the writer
					Writer writer = receiver.getWriter();
					// Append the data
					writer.append(data);
					// And flush it
					writer.flush();
				} catch (IOException e) {
					if (CoreBundleActivator.getTraceHandler().isSlotEnabled(1, null)) {
						IStatus status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(),
													NLS.bind(Messages.TerminalsStreamReaderRunnable_error_appendFailed, streamId, data),
													e);
						Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
					}
				}
			}
		}
	}

	/**
	 * Remote stream writer runnable implementation.
	 * <p>
	 * The runnable will be executed within a thread and is responsible to read the
	 * incoming data from the registered providers and forward them to the associated stream.
	 */
	protected class StreamWriterRunnable implements Runnable {
		// The associated stream id
		/* default */ final String streamId;
		// The associated stream type id
		@SuppressWarnings("unused")
		private final String streamTypeId;
		// The data provider applicable for the associated stream type id
		private final StreamsDataProvider provider;
		// The currently active write task
		private TCFTask<Object> activeTask;
		// The callback to invoke if the runnable stopped
		private ICallback callback;

		// Flag to stop the runnable
		private boolean stopped = false;

		/**
		 * Constructor.
		 *
		 * @param streamId The associated stream id. Must not be <code>null</code>.
		 * @param streamTypeId The associated stream type id. Must not be <code>null</code>.
		 * @param provider The data provider. Must not be <code>null</code> and must be applicable for the stream type.
		 */
		public StreamWriterRunnable(String streamId, String streamTypeId, StreamsDataProvider provider) {
			Assert.isNotNull(streamId);
			Assert.isNotNull(streamTypeId);
			Assert.isNotNull(provider);
			Assert.isTrue(provider.isApplicable(streamTypeId));

			this.streamId = streamId;
			this.streamTypeId = streamTypeId;
			this.provider = provider;
		}

		/**
		 * Returns the associated stream id.
		 *
		 * @return The associated stream id.
		 */
		public final String getStreamId() {
			return streamId;
		}

		/**
		 * Stop the runnable.
		 *
		 * @param callback The callback to invoke if the runnable stopped.
		 */
		public final synchronized void stop(ICallback callback) {
			// If the runnable is stopped already, invoke the callback directly
			if (stopped) {
				if (callback != null) callback.done(this, Status.OK_STATUS);
				return;
			}

			// Store the callback instance
			this.callback = callback;
			// Mark the runnable as stopped
			stopped = true;
		}

		/**
		 * Returns if the runnable should stop.
		 */
		protected final synchronized boolean isStopped() {
			return stopped;
		}

		/**
		 * Sets the currently active writer task.
		 *
		 * @param task The currently active writer task or <code>null</code>.
		 */
		protected final synchronized void setActiveTask(TCFTask<Object> task) {
			activeTask = task;
		}

		/**
		 * Returns the currently active writer task.
		 *
		 * @return The currently active writer task or <code>null</code>.
		 */
		protected final TCFTask<Object> getActiveTask() {
			return activeTask;
		}

		/**
		 * Returns the callback instance to invoke.
		 *
		 * @return The callback instance or <code>null</code>.
		 */
		protected final ICallback getCallback() {
			return callback;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
        public void run() {
			// If not data provider is set, we are done here immediately
			if (provider == null) {
				// Invoke the callback directly, if any
				synchronized (this) {
					if (callback != null) callback.done(this, Status.OK_STATUS);
				}
				// Mark the runnable definitely stopped
				stopped = true;

				return;
			}

			// Get the service instance from the parent
			final IStreams svcStreams = getParent().getSvcStreams();

			// Create the data buffer instance
			final char[] buffer = new char[1024];

			// Run until stopped and the streams service is available
			while (!isStopped() && svcStreams != null) {
				try {
					// Read available data from the data provider
					int charactersRead = provider.getReader().read(buffer, 0, 1024);
					// Have we reached the end of the stream -> break out
					if (charactersRead == -1) break;
					// If we read some data from the provider, write it to the stream
					if (charactersRead > 0) write(svcStreams, streamId, new String(buffer).getBytes(), charactersRead);
				} catch (Exception e) {
					// An error occurred -> Dump to the error log
					e = ExceptionUtils.checkAndUnwrapException(e);
					// Check if the blocking read task got canceled
					if (!(e instanceof CancellationException)) {
						// Log the error to the user, might be something serious
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
													NLS.bind(Messages.TerminalsStreamWriterRunnable_error_writeFailed, streamId, e.getLocalizedMessage()),
													e);
						Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
					}
					// break out of the loop
					break;
				}
			}

			// Disconnect from the stream
			if (svcStreams != null) {
				// Write EOS first
				svcStreams.eos(streamId, new IStreams.DoneEOS() {
					@Override
					public void doneEOS(IToken token, Exception error) {
						// Disconnect now
						svcStreams.disconnect(streamId, new IStreams.DoneDisconnect() {
							@Override
		                    @SuppressWarnings("synthetic-access")
							public void doneDisconnect(IToken token, Exception error) {
								// Disconnect is done, ignore any error, invoke the callback
								synchronized (this) {
									if (getCallback() != null) getCallback().done(this, Status.OK_STATUS);
								}
								// Mark the runnable definitely stopped
								stopped = true;
							}
						});
					}
				});
			} else {
				// Invoke the callback directly, if any
				synchronized (this) {
					if (callback != null) callback.done(this, Status.OK_STATUS);
				}
				// Mark the runnable definitely stopped
				stopped = true;
			}
		}

		/**
		 * Writes data to the stream.
		 *
		 * @param service The streams service. Must not be <code>null</code>.
		 * @param streamId The stream id. Must not be <code>null</code>.
		 * @param data The data buffer. Must not be <code>null</code>.
		 * @param size The size of the data to write.
		 *
		 * @throws Exception In case the write fails.
		 */
		protected final void write(final IStreams service, final String streamId, final byte[] data, final int size) throws Exception {
			Assert.isNotNull(service);
			Assert.isNotNull(streamId);
			Assert.isTrue(!Protocol.isDispatchThread());

			// Create the task object
			TCFTask<Object> task = new TCFTask<Object>() {
				@Override
                public void run() {
					service.write(streamId, data, 0, size, new IStreams.DoneWrite() {
						/* (non-Javadoc)
						 * @see org.eclipse.tm.tcf.services.IStreams.DoneWrite#doneWrite(org.eclipse.tm.tcf.protocol.IToken, java.lang.Exception)
						 */
						@Override
                        public void doneWrite(IToken token, Exception error) {
							if (error == null) done(null);
							else error(error);
						}
					});
				}
			};
			task.get();

			// Push the task object to the runnable instance
			setActiveTask(task);

			// Execute the write
			task.get();
		}
	}

	/**
	 * Constructor.
	 *
	 * @param parent The parent terminals launcher instance. Must not be <code>null</code>
	 */
	public TerminalsStreamsListener(TerminalsLauncher parent) {
		Assert.isNotNull(parent);
		this.parent = parent;
	}

	/**
	 * Returns the parent terminals launcher instance.
	 *
	 * @return The parent terminals launcher instance.
	 */
	protected final TerminalsLauncher getParent() {
		return parent;
	}

	/**
	 * Dispose the streams listener instance.
	 *
	 * @param callback The callback to invoke if the dispose finished or <code>null</code>.
	 */
	public void dispose(final ICallback callback) {
		// Store a final reference to the streams listener instance
		final IStreams.StreamsListener finStreamsListener = this;

		// Store a final reference to the data receivers list
		final List<StreamsDataReceiver> finDataReceivers;
		synchronized (dataReceiver) {
			finDataReceivers = new ArrayList<StreamsDataReceiver>(dataReceiver);
			dataReceiver.clear();
		}

		// Create a new collector to catch all runnable stop callback's
		AsyncCallbackCollector collector = new AsyncCallbackCollector(new Callback() {
			/* (non-Javadoc)
			 * @see org.eclipse.tm.te.runtime.callback.Callback#internalDone(java.lang.Object, org.eclipse.core.runtime.IStatus)
			 */
			@Override
			protected void internalDone(final Object caller, final IStatus status) {
				// Get the service instance from the parent
				IStreams svcStreams = getParent().getSvcStreams();
				// Unsubscribe the streams listener from the service
				svcStreams.unsubscribe(ITerminals.NAME, finStreamsListener, new IStreams.DoneUnsubscribe() {
					@Override
                    public void doneUnsubscribe(IToken token, Exception error) {
						// Loop all registered listeners and close them
						for (StreamsDataReceiver receiver : finDataReceivers) receiver.dispose();
						// Call the original outer callback
						if (callback != null) callback.done(caller, status);
					}
				});
			}
		});

		// Loop all runnable's and force them to stop
		synchronized (runnables) {
			for (Runnable runnable : runnables) {
				if (runnable instanceof StreamReaderRunnable) {
					((StreamReaderRunnable)runnable).stop(new AsyncCallbackCollector.SimpleCollectorCallback(collector));
				}
			}
			runnables.clear();
		}

		// Mark the collector initialization done
		collector.initDone();
	}

	/**
	 * Adds the given receiver to the stream data receiver list.
	 *
	 * @param receiver The data receiver. Must not be <code>null</code>.
	 */
	public void registerDataReceiver(StreamsDataReceiver receiver) {
		Assert.isNotNull(receiver);
		synchronized (dataReceiver) {
			if (!dataReceiver.contains(receiver)) dataReceiver.add(receiver);
		}
	}

	/**
	 * Removes the given receiver from the stream data receiver list.
	 *
	 * @param receiver The data receiver. Must not be <code>null</code>.
	 */
	public void unregisterDataReceiver(StreamsDataReceiver receiver) {
		Assert.isNotNull(receiver);
		synchronized (dataReceiver) {
			dataReceiver.remove(receiver);
		}
	}

	/**
	 * Sets the stream data provider instance.
	 *
	 * @param provider The stream data provider instance or <code>null</code>.
	 */
	public void setDataProvider(StreamsDataProvider provider) {
		dataProvider = provider;
	}

	/**
	 * Returns the stream data provider instance.
	 *
	 * @return The stream data provider instance or <code>null</code>.
	 */
	public StreamsDataProvider getDataProvider() {
		return dataProvider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsContextAwareListener#setTerminalsContext(org.eclipse.tm.tcf.services.ITerminals.TerminalContext)
	 */
	@Override
	public void setTerminalsContext(TerminalContext context) {
		Assert.isNotNull(context);
		this.context = context;

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_STREAMS_LISTENER)) {
			CoreBundleActivator.getTraceHandler().trace("Terminals context set to: id='" + context.getID() + "', PTY type='" + context.getPtyType() + "'", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			                                            0, ITraceIds.TRACE_STREAMS_LISTENER,
			                                            IStatus.INFO, getClass());
		}

		// Loop all delayed create events and look for the streams for our context
		synchronized (delayedCreatedEvents) {
			Iterator<StreamCreatedEvent> iterator = delayedCreatedEvents.iterator();
			while (iterator.hasNext()) {
				StreamCreatedEvent event = iterator.next();
				if (context.getID().equals(event.contextId) || event.contextId == null) {
					// Re-dispatch the event
					created(event.streamType, event.streamId, event.contextId);
				}
			}
			// Clear all events
			delayedCreatedEvents.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsContextAwareListener#getTerminalsContext()
	 */
	@Override
	public TerminalContext getTerminalsContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.IStreams.StreamsListener#created(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
    public void created(String streamType, String streamId, String contextId) {
		// We ignore any other stream type than ITerminals.NAME
		if (!ITerminals.NAME.equals(streamType)) return;

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_STREAMS_LISTENER)) {
			CoreBundleActivator.getTraceHandler().trace("New remote terminals stream created: streamId='" + streamId + "', contextId='" + contextId + "'", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			                                            0, ITraceIds.TRACE_STREAMS_LISTENER,
			                                            IStatus.INFO, getClass());
		}

		// If a terminals context is set, check if the created event is for the
		// monitored terminals context
		final ITerminals.TerminalContext context = getTerminalsContext();
		// The contextId is null if used with an older TCF agent not sending the third parameter
		if (context != null && (context.getID().equals(contextId) || contextId == null)) {
			// Create a snapshot of the registered data receivers
			StreamsDataReceiver[] receivers;
			synchronized (dataReceiver) {
				receivers = dataReceiver.toArray(new StreamsDataReceiver[dataReceiver.size()]);
			}
			// The created event is for the monitored terminals context
			// --> Create the stream reader thread(s)
			if (streamId != null && streamId.equals(context.getProperties().get(ITerminals.PROP_STDIN_ID))) {
				// Data provider set?
				if (dataProvider != null) {
					// Create the stdin stream writer runnable
					StreamWriterRunnable runnable = new StreamWriterRunnable(streamId, ITerminals.PROP_STDIN_ID, dataProvider);
					// Add to the list of created runnable's
					synchronized (runnables) { runnables.add(runnable); }
					// And create and start the thread
					Thread thread = new Thread(runnable, "Thread-" + ITerminals.PROP_STDIN_ID + "-" + streamId); //$NON-NLS-1$ //$NON-NLS-2$
					thread.start();
				}
			}
			if (streamId != null && streamId.equals(context.getProperties().get(ITerminals.PROP_STDOUT_ID))) {
				// Create the stdout stream reader runnable
				StreamReaderRunnable runnable = new StreamReaderRunnable(streamId, ITerminals.PROP_STDOUT_ID, receivers);
				// If not empty, create the thread
				if (!runnable.isEmpty()) {
					// Add to the list of created runnable's
					synchronized (runnables) { runnables.add(runnable); }
					// And create and start the thread
					Thread thread = new Thread(runnable, "Thread-" + ITerminals.PROP_STDOUT_ID + "-" + streamId); //$NON-NLS-1$ //$NON-NLS-2$
					thread.start();
				}
			}
			if (streamId != null && streamId.equals(context.getProperties().get(ITerminals.PROP_STDERR_ID))) {
				// Create the stdout stream reader runnable
				StreamReaderRunnable runnable = new StreamReaderRunnable(streamId, ITerminals.PROP_STDERR_ID, receivers);
				// If not empty, create the thread
				if (!runnable.isEmpty()) {
					// Add to the list of created runnable's
					synchronized (runnables) { runnables.add(runnable); }
					// And create and start the thread
					Thread thread = new Thread(runnable, "Thread-" + ITerminals.PROP_STDERR_ID + "-" + streamId); //$NON-NLS-1$ //$NON-NLS-2$
					thread.start();
				}
			}
		} else if (context == null) {
			// Context not set yet --> add to the delayed list
			StreamCreatedEvent event = new StreamCreatedEvent(streamType, streamId, contextId);
			synchronized (delayedCreatedEvents) {
				if (!delayedCreatedEvents.contains(event)) delayedCreatedEvents.add(event);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.IStreams.StreamsListener#disposed(java.lang.String, java.lang.String)
	 */
	@Override
    public void disposed(String streamType, String streamId) {
		// We ignore any other stream type than ITerminals.NAME
		if (!ITerminals.NAME.equals(streamType)) return;

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_STREAMS_LISTENER)) {
			CoreBundleActivator.getTraceHandler().trace("Remote terminals stream disposed: streamId='" + streamId + "'", //$NON-NLS-1$ //$NON-NLS-2$
			                                            0, ITraceIds.TRACE_STREAMS_LISTENER,
			                                            IStatus.INFO, getClass());
		}

		// If the delayed created events list is not empty, we have
		// to check if one of the delayed create events got disposed
		synchronized (delayedCreatedEvents) {
			Iterator<StreamCreatedEvent> iterator = delayedCreatedEvents.iterator();
			while (iterator.hasNext()) {
				StreamCreatedEvent event = iterator.next();
				if (event.streamType != null && event.streamType.equals(streamType)
						&& event.streamId != null && event.streamId.equals(streamId)) {
					// Remove the create event from the list
					iterator.remove();
				}
			}
		}

		// Stop the thread(s) if the disposed event is for the active
		// monitored stream id(s).
		synchronized (runnables) {
			Iterator<Runnable> iterator = runnables.iterator();
			while (iterator.hasNext()) {
				Runnable runnable = iterator.next();
				if (runnable instanceof StreamReaderRunnable) {
					StreamReaderRunnable myRunnable = (StreamReaderRunnable)runnable;
					if (myRunnable.getStreamId().equals(streamId)) {
						// This method is called within the TCF event dispatch thread, so
						// we cannot wait for a callback here
						myRunnable.stop(null);
						iterator.remove();
					}
				}
			}
		}
	}
}
