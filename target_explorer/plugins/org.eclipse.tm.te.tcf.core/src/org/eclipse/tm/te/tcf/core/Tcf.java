/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.protocol.Protocol.ChannelOpenListener;
import org.eclipse.tm.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tm.te.tcf.core.interfaces.listeners.IChannelStateChangeListener;
import org.eclipse.tm.te.tcf.core.interfaces.listeners.IProtocolStateChangeListener;
import org.eclipse.tm.te.tcf.core.internal.ChannelManager;
import org.eclipse.tm.te.tcf.core.internal.Startup;
import org.eclipse.tm.te.tcf.core.internal.interfaces.IChannelOpenListener;
import org.eclipse.tm.te.tcf.core.internal.listener.InternalChannelOpenListener;


/**
 * The main entry point to access the TCF framework extensions.
 */
public final class Tcf {
	/* default */ IChannelManager fChannelManager;

	/* default */ ChannelOpenListener fChannelOpenListener;

	/* default */ final List<IProtocolStateChangeListener> fProtocolStateChangeListeners = new ArrayList<IProtocolStateChangeListener>();
	/* default */ final List<IChannelStateChangeListener> fChannelStateChangeListeners = new ArrayList<IChannelStateChangeListener>();


	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static Tcf fInstance = new Tcf();
	}

	/**
	 * Constructor.
	 */
	/* default */ Tcf() {
		super();
	}

	/**
	 * Returns the singleton instance.
	 */
	/* default */ static Tcf getInstance() {
		return LazyInstance.fInstance;
	}

	/**
	 * Executes the given runnable within the TCF protocol dispatch thread.
	 * <p>
	 * <b>Note:</b> Code which is executed in the TCF protocol dispatch thread
	 * cannot use any blocking API!
	 *
	 * @param runnable The runnable. Must be not <code>null</code>.
	 */
	private static final void runSafe(Runnable runnable) {
		assert runnable != null;

		if (Protocol.isDispatchThread()) {
			runnable.run();
		} else {
			Protocol.invokeAndWait(runnable);
		}
	}

	/**
	 * Adds a listener that will be notified once the TCF framework state changes.
	 *
	 * @param listener The listener. Must be not <code>null</code>.
	 */
	public static final void addProtocolStateChangeListener(IProtocolStateChangeListener listener) {
		assert Protocol.isDispatchThread() && listener != null;

		Tcf tcf = getInstance();
		assert tcf != null;

		if (!tcf.fProtocolStateChangeListeners.contains(listener)) {
			tcf.fProtocolStateChangeListeners.add(listener);
		}
	}

	/**
	 * Removes the specified protocol state change listener.
	 *
	 * @param listener The listener. Must be not <code>null</code>.
	 */
	public static final void removeProtocolStateChangeListener(IProtocolStateChangeListener listener) {
		assert Protocol.isDispatchThread() && listener != null;

		Tcf tcf = getInstance();
		assert tcf != null;

		tcf.fProtocolStateChangeListeners.remove(listener);
	}

	/**
	 * Adds a listener that will be notified once the TCF framework state changes.
	 *
	 * @param listener The listener. Must be not <code>null</code>.
	 */
	public static final void addChannelStateChangeListener(IChannelStateChangeListener listener) {
		assert Protocol.isDispatchThread() && listener != null;

		Tcf tcf = getInstance();
		assert tcf != null;

		if (!tcf.fChannelStateChangeListeners.contains(listener)) {
			tcf.fChannelStateChangeListeners.add(listener);
		}
	}

	/**
	 * Removes the specified protocol state change listener.
	 *
	 * @param listener The listener. Must be not <code>null</code>.
	 */
	public static final void removeChannelStateChangeListener(IChannelStateChangeListener listener) {
		assert Protocol.isDispatchThread() && listener != null;

		Tcf tcf = getInstance();
		assert tcf != null;

		tcf.fChannelStateChangeListeners.remove(listener);
	}

	/**
	 * Fires the channel state change listeners.
	 *
	 * @param channel The channel which changed state. Must be not <code>null</code>.
	 * @param state The new state.
	 */
	public static final void fireChannelStateChangeListeners(final IChannel channel, final int state) {
		assert channel != null;

		Tcf tcf = getInstance();
		assert tcf != null;

		final IChannelStateChangeListener[] listeners = tcf.fChannelStateChangeListeners.toArray(new IChannelStateChangeListener[tcf.fChannelStateChangeListeners.size()]);
		if (listeners.length > 0) {
			Protocol.invokeLater(new Runnable() {
				public void run() {
					for (IChannelStateChangeListener listener : listeners) {
						listener.stateChanged(channel, state);
					}
				}
			});
		}
	}

	/**
	 * Returns if or if not the TCF framework is up and running.
	 *
	 * @return <code>True</code> if the framework is up and running, <code>false</code> otherwise.
	 */
	public static final boolean isRunning() {
		return Startup.isStarted();
	}

	/**
	 * Startup TCF related services and listeners once the core
	 * TCF framework starts up.
	 * <p>
	 * <b>Note:</b> The method is expected to be called within the TCF protocol dispatch thread.
	 *
	 * @see Startup#setStarted(boolean)
	 */
	public static void start() {
		assert Protocol.isDispatchThread();

		Tcf tcf = getInstance();
		assert tcf != null;

		// Create and register the global channel open listener
		if (tcf.fChannelOpenListener == null) {
			tcf.fChannelOpenListener = new InternalChannelOpenListener();
			Protocol.addChannelOpenListener(tcf.fChannelOpenListener);
		}

		// Signal to interested listeners that we've started up
		final IProtocolStateChangeListener[] listeners = tcf.fProtocolStateChangeListeners.toArray(new IProtocolStateChangeListener[tcf.fProtocolStateChangeListeners.size()]);
		if (listeners.length > 0) {
			Protocol.invokeLater(new Runnable() {
				public void run() {
					for (IProtocolStateChangeListener listener : listeners) {
						listener.stateChanged(true);
					}
				}
			});
		}
	}

	/**
	 * Shutdown TCF related services and listeners once the core
	 * TCF framework shuts down.
	 * <p>
	 * <b>Note:</b> The method is expected to be called within the TCF protocol dispatch thread.
	 *
	 * @see Startup#setStarted(boolean)
	 */
	public static void stop() {
		assert Protocol.isDispatchThread();

		Tcf tcf = getInstance();
		assert tcf != null;

		// Unregister the channel open listener of created
		if (tcf.fChannelOpenListener != null) {
			Protocol.removeChannelOpenListener(tcf.fChannelOpenListener);
			tcf.fChannelOpenListener = null;
		}

		// Signal to interested listeners that we've just went down
		final IProtocolStateChangeListener[] listeners = tcf.fProtocolStateChangeListeners.toArray(new IProtocolStateChangeListener[tcf.fProtocolStateChangeListeners.size()]);
		if (listeners.length > 0) {
			Protocol.invokeLater(new Runnable() {
				public void run() {
					for (IProtocolStateChangeListener listener : listeners) {
						listener.stateChanged(false);
					}
				}
			});
		}
	}

	/**
	 * Returns the channel manager instance.
	 * <p>
	 * <b>Note:</b> The method will create the channel manager instance on
	 * first invocation.
	 *
	 * @return The channel manager instance.
	 */
	public static IChannelManager getChannelManager() {
		final Tcf tcf = getInstance();
		assert tcf != null;

		runSafe(new Runnable() {
			public void run() {
				assert Protocol.isDispatchThread();

				if (tcf.fChannelManager == null) {
					// We have to create the channel manager
					tcf.fChannelManager = new ChannelManager();
				}
			}
		});

		return tcf.fChannelManager;
	}

	/**
	 * Returns an object which is an instance of the given class associated with the given object.
	 * Returns <code>null</code> if no such object can be found.
	 *
	 * @param adapter The type of adapter to look up
	 * @return An object castable to the given adapter type, or <code>null</code>
	 *         if the given adaptable object does not have an available adapter of the given type
	 *
	 * @see IAdapterManager#getAdapter(Object, Class)
	 */
	public static Object getAdapter(Class<?> adapter) {
		assert adapter != null;

		Tcf tcf = getInstance();
		assert tcf != null;

		if (IChannelManager.class.equals(adapter)) {
			return tcf.fChannelManager;
		}
		if (IChannelOpenListener.class.equals(adapter)) {
			return tcf.fChannelOpenListener;
		}

		return Platform.getAdapterManager().getAdapter(tcf, adapter);
	}
}
