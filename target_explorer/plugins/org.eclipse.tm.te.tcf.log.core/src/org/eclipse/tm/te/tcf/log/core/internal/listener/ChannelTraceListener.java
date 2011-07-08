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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.tcf.core.AbstractChannel.TraceListener;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.te.tcf.log.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.log.core.interfaces.IPreferenceKeys;
import org.eclipse.tm.te.tcf.log.core.interfaces.ITracing;
import org.eclipse.tm.te.tcf.log.core.internal.LogManager;
import org.eclipse.tm.te.tcf.log.core.internal.nls.Messages;

/**
 * Target Explorer: TCF logging channel trace listener implementation.
 */
public class ChannelTraceListener implements TraceListener {
	/**
	 * Time format representing time with milliseconds.
	 */
	public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS"); //$NON-NLS-1$

	/**
	 * Time format representing date and time with milliseconds.
	 */
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //$NON-NLS-1$

	// Reference to the channel
	private final IChannel channel;

	/**
	 * Constructor.
	 *
	 * @param channel The channel. Must be not <code>null</code>.
	 */
	public ChannelTraceListener(IChannel channel) {
		Assert.isNotNull(channel);
		this.channel = channel;
	}

	/**
	 * Return the associated channel.
	 *
	 * @return The channel instance.
	 */
	protected final IChannel getChannel() {
		return channel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.core.AbstractChannel.TraceListener#onChannelClosed(java.lang.Throwable)
	 */
	public void onChannelClosed(Throwable error) {
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_CHANNEL_TRACE_LISTENER)) {
			CoreBundleActivator.getTraceHandler().trace("TraceListener.onChannelClosed ( " + error + " )", //$NON-NLS-1$ //$NON-NLS-2$
														ITracing.ID_TRACE_CHANNEL_TRACE_LISTENER, this);
		}

		// Get the current time stamp
		String date = DATE_FORMAT.format(new Date(System.currentTimeMillis()));

		String message = NLS.bind(Messages.ChannelTraceListener_channelClosed_message,
								  new Object[] {
										date,
										Integer.toHexString(channel.hashCode()),
										error
								  });

		LogManager.getInstance().closeWriter(channel, message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.core.AbstractChannel.TraceListener#onMessageReceived(char, java.lang.String, java.lang.String, java.lang.String, byte[])
	 */
	public void onMessageReceived(char type, String token, String service, String name, byte[] data) {
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_CHANNEL_TRACE_LISTENER)) {
			CoreBundleActivator.getTraceHandler().trace("TraceListener.onMessageReceived ( " + type //$NON-NLS-1$
														+ ", " + token + ", " + service + ", " + name + ", ... )", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
														ITracing.ID_TRACE_CHANNEL_TRACE_LISTENER, this);
		}

		doLogMessage(type, token, service, name, data);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.core.AbstractChannel.TraceListener#onMessageSent(char, java.lang.String, java.lang.String, java.lang.String, byte[])
	 */
	public void onMessageSent(final char type, String token, String service, String name, byte[] data) {
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_CHANNEL_TRACE_LISTENER)) {
			CoreBundleActivator.getTraceHandler().trace("TraceListener.onMessageSent ( " + type //$NON-NLS-1$
														+ ", " + token + ", " + service + ", " + name + ", ... )", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
														ITracing.ID_TRACE_CHANNEL_TRACE_LISTENER, this);
		}

		doLogMessage(type, token, service, name, data);
	}

	/**
	 * Helper method to output the message to the logger.
	 */
	private void doLogMessage(final char type, String token, String service, String name, byte[] data) {
		// Filter out the heart beat messages if not overwritten by the preferences
		boolean showHeartbeats = Platform.getPreferencesService().getBoolean(CoreBundleActivator.getUniqueIdentifier(),
																			 IPreferenceKeys.PREF_SHOW_HEARTBEATS, false, null);
		if (!showHeartbeats && name != null && name.toLowerCase().contains("heartbeat")) { //$NON-NLS-1$
			return;
		}

		// Format the message
		final String message = formatMessage(type, token, service, name, data);
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
	 * Format the trace message.
	 */
	protected String formatMessage(char type, String token, String service, String name, byte[] data) {
		// Get the current time stamp
		String time = TIME_FORMAT.format(new Date(System.currentTimeMillis()));

		// Decode the arguments again for tracing purpose
		Object[] args = null;
		if (data != null) try { args = JSON.parseSequence(data); } catch (IOException e) { /* ignored on purpose */ }

		// Construct the full message
		String message = NLS.bind(Messages.ChannelTraceListener_message,
		                          new Object[] { time,
												 Character.valueOf(type).toString(),
												 token != null ? token : "-", //$NON-NLS-1$
												 service != null ? service : "", //$NON-NLS-1$
												 name != null ? "#" + name : "", //$NON-NLS-1$ //$NON-NLS-2$
												 (data != null ? Arrays.toString(args) : "") }); //$NON-NLS-1$

		return message;
	}

}
