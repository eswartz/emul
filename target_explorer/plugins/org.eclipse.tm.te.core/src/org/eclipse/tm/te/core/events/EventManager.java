/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.events;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.te.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.core.interfaces.events.IEventFireDelegate;
import org.eclipse.tm.te.core.interfaces.events.IEventListener;
import org.eclipse.tm.te.core.interfaces.tracing.ITraceIds;
import org.osgi.framework.Bundle;


/**
 * The event manager implementation.
 */
public final class EventManager {
	// Flag to remember if the extension point has been processed.
	private boolean extensionPointProcessed;
	// The list of registered listeners.
	private final List<ListenerListEntry> listeners = new ArrayList<ListenerListEntry>();

	/**
	 * Runnable implementation to fire a given event to a given listener.
	 */
	protected static class FireRunnable implements Runnable {
		private final IEventListener listener;
		private final EventObject event;

		/**
		 * Constructor.
		 *
		 * @param listener The event listener. Must not be <code>null</code>.
		 * @param event The event. Must not be <code>null</code>.
		 */
		public FireRunnable(IEventListener listener, EventObject event) {
			Assert.isNotNull(listener);
			Assert.isNotNull(event);
			this.listener = listener;
			this.event = event;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			listener.eventFired(event);
		}
	}

	/**
	 * Listener list entry.
	 * <p>
	 * Each entry contains a reference to the listener and a list of valid source classes.
	 * If an event source can be casted to one of the classes the listener is invoked.
	 */
	private class ListenerListEntry {
		private final IEventListener listener;
		private final Object[] eventSources;
		private final Class<?>[] eventTypes;

		/**
		 * Constructor.
		 *
		 * @param listener The listener.
		 * @param eventType The event type the listener is interested in.
		 * @param eventSource The source type for which events should be fired to the listener.
		 */
		protected ListenerListEntry(IEventListener listener, Class<?> eventType, Object eventSource) {
			this(listener, eventType == null ? null : new Class[] { eventType }, eventSource == null ? null : new Object[] { eventSource });
		}

		/**
		 * Constructor.
		 *
		 * @param listener The listener.
		 * @param eventTypes The event types the listener is interested in.
		 * @param eventSources The source types for which events should be fired to the listener.
		 */
		protected ListenerListEntry(IEventListener listener, Class<?>[] eventTypes, Object[] eventSources) {
			this.listener = listener;
			if (eventTypes == null || eventTypes.length == 0) {
				this.eventTypes = null;
			} else {
				this.eventTypes = eventTypes;
			}
			if (eventSources == null || eventSources.length == 0) {
				this.eventSources = null;
			} else {
				this.eventSources = eventSources;
			}
		}

		/**
		 * Get the listener of this entry.
		 */
		protected EventListener getListener() {
			return listener;
		}

		/**
		 * Check whether the listener wants to be called for changes of the source.
		 * The check is made through <code>instanceof</code>.
		 *
		 * @param source The source of the event.
		 * @return True, if the source can be casted to one of the registered event source types
		 * 		   or no event sources are registered.
		 */
		protected boolean listensTo(EventObject event) {
			boolean types = (eventTypes == null || eventTypes.length == 0);
			boolean sources = (eventSources == null || eventSources.length == 0);

			int t = 0;
			while (!types && eventTypes != null && t < eventTypes.length) {
				types = eventTypes[t].isInstance(event);
				t++;
			}

			int s = 0;
			while (!sources && eventSources != null && s < eventSources.length) {
				Object eventSource = eventSources[s];
				if (eventSource instanceof Class<?>) {
					Class<?> eventSourceClass = (Class<?>)eventSource;
					sources = eventSourceClass.isInstance(event.getSource());
				} else {
					sources = eventSource == event.getSource();
				}
				s++;
			}

			return types && sources;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof ListenerListEntry) {
				ListenerListEntry other = (ListenerListEntry)obj;
				return this.getListener() == other.getListener();
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getClass().getName() + "{" + //$NON-NLS-1$
											"listener=" + listener + //$NON-NLS-1$
											",eventTypes=" + eventTypes + //$NON-NLS-1$
											",eventSources=" + eventSources + //$NON-NLS-1$
										  "}"; //$NON-NLS-1$
		}
	}

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static EventManager instance = new EventManager();
	}

	/**
	 * Private Constructor.
	 */
	EventManager() {
		extensionPointProcessed = false;
	}

	/**
	 * Returns the singleton instance for the event manager.
	 */
	public static EventManager getInstance() {
		return LazyInstance.instance;
	}

	/**
	 * Add a change listener to listen to a single event.
	 *
	 * @param listener The listener to add.
	 * @param eventType The event type this listeners wants to be invoked.
	 */
	public void addEventListener(IEventListener listener, Class<?> eventType) {
		addEventListener(listener, eventType != null ? new Class[] { eventType } : null, null);
	}

	/**
	 * Add a change listener to listen to multiple events.
	 *
	 * @param listener The listener to add.
	 * @param eventTypes The event types this listeners wants to be invoked.
	 */
	public void addEventListener(IEventListener listener, Class<?>[] eventTypes) {
		addEventListener(listener, eventTypes, null);
	}

	/**
	 * Add a change listener to listen to event from the specified event
	 * source. If the listener instance had been registered already, the listener
	 * event sources are updated
	 *
	 * @param listener The listener to add.
	 * @param eventType The event type this listeners wants to be invoked.
	 * @param eventSource The event source type this listeners wants to be invoked.
	 */
	public void addEventListener(IEventListener listener, Class<?> eventType, Object eventSource) {
		addEventListener(listener, eventType != null ? new Class[] { eventType } : null, eventSource != null ? new Object[] { eventSource } : null);
	}

	/**
	 * Add a change listener to listen to events from the specified event
	 * sources. If the listener instance had been registered already, the listener
	 * event sources are updated
	 *
	 * @param listener The listener to add.
	 * @param eventType The event type this listeners wants to be invoked.
	 * @param eventSources The event sources type this listeners wants to be invoked.
	 */
	public void addEventListener(IEventListener listener, Class<?> eventType, Object[] eventSources) {
		addEventListener(listener, eventType != null ? new Class[] { eventType } : null, eventSources);
	}

	/**
	 * Add a change listener to listen to event from the specified event
	 * sources. If the listener instance had been registered already, the listener
	 * event sources are updated
	 *
	 * @param listener The listener to add.
	 * @param eventTypes The event types this listeners wants to be invoked.
	 * @param eventSources The event source types this listeners wants to be invoked.
	 */
	public void addEventListener(IEventListener listener, Class<?>[] eventTypes, Object[] eventSources) {
		ListenerListEntry listEntry = new ListenerListEntry(listener, eventTypes, eventSources);
		// We must assure that the existing list entries can _never_ change!
		synchronized (listeners) {
			if (listeners.contains(listEntry)) {
				listeners.remove(listEntry);
			}
			listeners.add(listEntry);
		}
	}

	/**
	 * Remove a change listener for all event types and sources.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeEventListener(IEventListener listener) {
		ListenerListEntry listEntry = new ListenerListEntry(listener, (Class<?>)null, (Object)null);
		listeners.remove(listEntry);
	}

	/**
	 * Remove all change listeners for all event types and sources.
	 */
	public void clear() {
		listeners.clear();
		extensionPointProcessed = false;
	}

	/**
	 * Notify all registered listeners.
	 *
	 * @param event The event. Must not be <code>null</code>
	 */
	public void fireEvent(final EventObject event) {
		Assert.isNotNull(event);

		synchronized (this) {
			// if the extension point has not been processed till here, now we have to do
			if (!extensionPointProcessed) {
				addExtensionPointNotificationListeners();
				extensionPointProcessed = true;
			}
		}

		// Based on the current listener listener list, compile a list of event
		// listeners to where this event would have been send to in a synchronous invocation scheme.
		List<ListenerListEntry> affected = new ArrayList<ListenerListEntry>();

		// Get the array of registered event listeners.
		ListenerListEntry[] registered = listeners.toArray(new ListenerListEntry[listeners.size()]);

		for (ListenerListEntry listEntry : registered) {
			// ignore listeners not listening to the event type and source
			if (listEntry.listensTo(event)) {
				affected.add(listEntry);
			}
		}

		// If no current listener is affected, return now immediately
		if (affected.size() == 0) {
			return;
		}

		// Loop over the list of affected listeners and fire the event.
		// If the affected listener is a fire delegate -> use it itself to fire the event
		for (ListenerListEntry listEntry : affected) {
			if (!(listEntry.getListener() instanceof IEventListener)) {
				continue;
			}
			// Create the runnable to use for executing the event firing
			Runnable runnable = new FireRunnable((IEventListener)listEntry.getListener(), event);
			// Check on how to fire the runnable
			if (listEntry.getListener() instanceof IEventFireDelegate) {
				// The listener is a fire delegate -> use it itself to fire the runnable
				((IEventFireDelegate)listEntry.getListener()).fire(runnable);
			} else {
				// Listener isn't a fire delegate -> fire the runnable directly
				runnable.run();
			}
		}
	}

	/*
	 * Register change listeners defined by extension.
	 */
	private void addExtensionPointNotificationListeners() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.tm.te.core.eventListeners"); //$NON-NLS-1$
		if (ep != null) {
			IExtension[] extensions = ep.getExtensions();
			if (extensions != null && extensions.length > 0) {
				for (IExtension extension : extensions) {
					IConfigurationElement[] configElements = extension.getConfigurationElements();
					if (configElements != null && configElements.length > 0) {
						for (IConfigurationElement configElement : configElements) {
							String name = configElement.getName();
							if ("eventListener".equals(name)) { //$NON-NLS-1$
								// try to read the "eventType" and "eventSourceType" configuration elements if any.
								List<Class<?>> eventTypes = new ArrayList<Class<?>>();
								List<Class<?>> eventSourceTypes = new ArrayList<Class<?>>();

								IConfigurationElement[] children = configElement.getChildren();
								for (IConfigurationElement child : children) {
									if ("eventType".equals(child.getName())) { //$NON-NLS-1$
										// The event types, we have to instantiate here as we need the class object!
										try {
											// First we try to instantiate the class using our own local class loader.
											// This trick can avoid activating the contributing plugin if we can load
											// the class ourself.
											// First we try to instantiate the class using our own context
											String className = child.getAttribute("class"); //$NON-NLS-1$
											if (className == null || className.trim().length() == 0) {
												continue;
											}

											String bundleId = child.getAttribute("bundleId"); //$NON-NLS-1$

											// If a bundle id got specified, use the specified bundle to load the service class
											Bundle bundle = bundleId != null ? bundle = Platform.getBundle(bundleId) : null;
											// If we don't have a bundle to load from yet, fallback to the declaring bundle
											if (bundle == null) bundle = Platform.getBundle(child.getDeclaringExtension().getNamespaceIdentifier());
											// And finally, use our own bundle to load the class. This fallback is expected
											// to never be used.
											if (bundle == null) bundle = CoreBundleActivator.getContext().getBundle();

											// Try to load the event type class now
											Class<?> eventType = bundle != null ? bundle.loadClass(className) : Class.forName(className);
											if (!eventTypes.contains(eventType)) {
												eventTypes.add(eventType);
											}
										} catch (Exception ex) {
											if (isTracingEnabled())
												CoreBundleActivator.getTraceHandler().trace("Error instantiating event listener event type object instance: " + child.getAttribute("class"), //$NON-NLS-1$ //$NON-NLS-2$
												                                            0, ITraceIds.TRACE_EVENTS, IStatus.ERROR, this);
										}
									}

									if ("eventSourceType".equals(child.getName())) { //$NON-NLS-1$
										// The event source types, we have to instantiate here as we need the class object!
										try {
											// First we try to instantiate the class using our own local class loader.
											// This trick can avoid activating the contributing plugin if we can load
											// the class ourself.
											// First we try to instantiate the class using our own context
											String className = child.getAttribute("class"); //$NON-NLS-1$
											if (className == null || className.trim().length() == 0) {
												continue;
											}

											String bundleId = child.getAttribute("bundleId"); //$NON-NLS-1$

											// If a bundle id got specified, use the specified bundle to load the service class
											Bundle bundle = bundleId != null ? bundle = Platform.getBundle(bundleId) : null;
											// If we don't have a bundle to load from yet, fallback to the declaring bundle
											if (bundle == null) bundle = Platform.getBundle(child.getDeclaringExtension().getNamespaceIdentifier());
											// And finally, use our own bundle to load the class. This fallback is expected
											// to never be used.
											if (bundle == null) bundle = CoreBundleActivator.getContext().getBundle();

											// Try to load the event source type class now
											Class<?> eventSourceType = bundle != null ? bundle.loadClass(className) : Class.forName(className);
											if (!eventSourceTypes.contains(eventSourceType)) {
												eventSourceTypes.add(eventSourceType);
											}
										} catch (Exception ex) {
											if (isTracingEnabled())
												CoreBundleActivator.getTraceHandler().trace("Error instantiating event listener event source type object instance: " + child.getAttribute("class"), //$NON-NLS-1$ //$NON-NLS-2$
												                                            0, ITraceIds.TRACE_EVENTS, IStatus.ERROR, this);
										}
									}
								}

								// For extension point contributed event listeners, we use delegating
								// event listener instances
								IEventListener listener = new EventListenerProxy(configElement);
								addEventListener(listener,
								                 !eventTypes.isEmpty() ? eventTypes.toArray(new Class[eventTypes.size()]) : null,
								                 !eventSourceTypes.isEmpty() ? eventSourceTypes.toArray(new Class[eventSourceTypes.size()]) : null
								                );

								if (isTracingEnabled())
									CoreBundleActivator.getTraceHandler().trace("Add extension point change listener: " + configElement.getAttribute("class"), //$NON-NLS-1$ //$NON-NLS-2$
									                                            0, ITraceIds.TRACE_EVENTS, IStatus.INFO, this);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Internal class used to delay the instantiation and plugin activation of
	 * event listeners which are contributed via extension point till they
	 * are really fired.
	 */
	private class EventListenerProxy implements IEventListener, IEventFireDelegate {
		private final IConfigurationElement configElement;
		private IEventListener delegate;

		/**
		 * Constructor.
		 *
		 * @param configElement The contributing configuration element of the encapsulated event listener.
		 *                      Must not be <code>null</code>.
		 */
		public EventListenerProxy(IConfigurationElement configElement) {
			Assert.isNotNull(configElement);
			this.configElement = configElement;
			delegate = null;
		}

		/**
		 * Returns the event listener delegate and instantiate the delegate
		 * if not yet done.
		 *
		 * @return The event listener delegate or <code>null</code> if the instantiation fails.
		 */
		private IEventListener getDelegate() {
			if (delegate == null) {
				// Check the contributing plug-in state
				boolean forcePluginActivation = Boolean.parseBoolean(configElement.getAttribute("forcePluginActivation")); //$NON-NLS-1$
				if (!forcePluginActivation) {
					Bundle bundle = Platform.getBundle(configElement.getContributor().getName());
					forcePluginActivation = bundle != null ? bundle.getState() == Bundle.ACTIVE : false;
				}
				// Load the event listener implementation class if plugin activations is allowed.
				if (forcePluginActivation) {
					try {
						Object executable = configElement.createExecutableExtension("class"); //$NON-NLS-1$
						if (executable instanceof IEventListener) {
							delegate = (IEventListener)executable;
						}
					} catch (Exception ex) {
						if (isTracingEnabled())
							CoreBundleActivator.getTraceHandler().trace("Error instantiating extension point event listener: " + configElement.getAttribute("class") //$NON-NLS-1$ //$NON-NLS-2$
                                    										+ "(Possible Cause: " + ex.getLocalizedMessage() + ")", //$NON-NLS-1$ //$NON-NLS-2$
                                    										0, ITraceIds.TRACE_EVENTS, IStatus.ERROR, this);
					}
				}
			}

			return delegate;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.core.interfaces.events.IEventListener#eventFired(java.util.EventObject)
		 */
		public void eventFired(EventObject event) {
			Assert.isNotNull(event);
			// Get the delegate (may force instantiation)
			IEventListener delegate = getDelegate();
			// And pass on the event to the delegate if we got a valid delegate
			if (delegate != null) delegate.eventFired(event);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.core.interfaces.events.IEventFireDelegate#fire(java.lang.Runnable)
		 */
		public void fire(Runnable runnable) {
			Assert.isNotNull(runnable);
			// Pass on to the delegate if the delegate itself is an fire delegate,
			if (getDelegate() instanceof IEventFireDelegate) {
				((IEventFireDelegate)getDelegate()).fire(runnable);
			}
			else {
				runnable.run();
			}
		}
	}

	/**
	 * Return <code>true</code> if the tracing mode is enabled for the
	 * event manager and trace messages shall be printed to the debug console.
	 */
	public static boolean isTracingEnabled() {
		return CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_EVENTS);
	}

}
