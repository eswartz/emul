/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.log.core.internal.listener;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.tcf.core.AbstractChannel;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.log.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.log.core.interfaces.IPreferenceKeys;
import org.eclipse.tm.te.tcf.log.core.interfaces.ITracing;
import org.eclipse.tm.te.tcf.log.core.internal.LogManager;
import org.eclipse.tm.te.tcf.log.core.internal.nls.Messages;

/**
 * Target Explorer: TCF logging channel trace listener manager implementation.
 */
public class ChannelTraceListenerManager {
	// The map of trace listeners per channel
	private final Map<IChannel, AbstractChannel.TraceListener> listeners = new HashMap<IChannel, AbstractChannel.TraceListener>();

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstanceHolder {
		public static ChannelTraceListenerManager instance = new ChannelTraceListenerManager();
	}

	/**
	 * Returns the singleton instance for the manager.
	 */
	public static ChannelTraceListenerManager getInstance() {
		return LazyInstanceHolder.instance;
	}

	/**
	 * Constructor.
	 */
	/* default */ ChannelTraceListenerManager() {
	}

	/**
	 * New channel opened. Attach a channel trace listener.
	 *
	 * @param channel The channel. Must not be <code>null</code>.
	 */
	public void onChannelOpened(final IChannel channel) {
		Assert.isNotNull(channel);
		Assert.isTrue(Protocol.isDispatchThread());

		// The trace listener interface does not have a onChannelOpenend method, but
		// for consistency, log the channel opening similar to the others.
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_CHANNEL_TRACE_LISTENER)) {
			CoreBundleActivator.getTraceHandler().trace("TraceListener.onChannelOpened ( " + channel + " )", //$NON-NLS-1$ //$NON-NLS-2$
														ITracing.ID_TRACE_CHANNEL_TRACE_LISTENER, this);
		}

		// The trace listeners can be accessed only via AbstractChannel
		if (!(channel instanceof AbstractChannel)) return;

		// Get the preference key if or if not logging is enabled
		boolean loggingEnabled = Platform.getPreferencesService().getBoolean(CoreBundleActivator.getUniqueIdentifier(),
																			 IPreferenceKeys.PREF_LOGGING_ENABLED, false, null);
		// If false, we are done here and wont create any console or trace listener.
		if (!loggingEnabled) return;

		// As the channel has just opened, there should be no trace listener, but better be safe and check
		AbstractChannel.TraceListener traceListener = listeners.remove(channel);
		if (traceListener != null) ((AbstractChannel)channel).removeTraceListener(traceListener);
		// Create a new trace listener instance
		traceListener = new ChannelTraceListener(channel);
		// Attach trace listener to the channel
		((AbstractChannel)channel).addTraceListener(traceListener);
		// Remember the associated trace listener
		listeners.put(channel, traceListener);

		// Log the channel opening
		String date = ChannelTraceListener.DATE_FORMAT.format(new Date(System.currentTimeMillis()));

		String message = NLS.bind(Messages.ChannelTraceListener_channelOpened_message,
								  new Object[] {
										date,
										Integer.toHexString(channel.hashCode())
								  });

		// Get the file writer
		FileWriter writer = LogManager.getInstance().getWriter(channel);
		if (writer != null) {
			try {
				writer.write(message);
				writer.write("\n"); //$NON-NLS-1$
				writer.flush();
			} catch (IOException e) {
				/* ignored on purpose */
			}
		}
	}

	/**
	 * Channel closed. Detach the channel trace listener if any.
	 *
	 * @param channel The channel. Must not be <code>null</code>.
	 */
	public void onChannelClosed(final IChannel channel) {
		Assert.isNotNull(channel);
		Assert.isTrue(Protocol.isDispatchThread());

		// The trace listeners can be accessed only via AbstractChannel
		if (!(channel instanceof AbstractChannel)) return;

		// Remove the trace listener if any
		final AbstractChannel.TraceListener traceListener = listeners.remove(channel);
		if (traceListener != null) {
			Protocol.invokeLater(new Runnable() {
				@Override
				public void run() {
					((AbstractChannel)channel).removeTraceListener(traceListener);
				}
			});
		}
	}
}
