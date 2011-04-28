/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.core.interfaces.listeners.IChannelStateChangeListener;
import org.eclipse.tm.te.tcf.locator.Scanner;
import org.eclipse.tm.te.tcf.locator.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.locator.interfaces.IModelListener;
import org.eclipse.tm.te.tcf.locator.interfaces.IScanner;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.preferences.IPreferenceKeys;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelService;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
import org.eclipse.tm.te.tcf.locator.listener.ChannelStateChangeListener;
import org.eclipse.tm.te.tcf.locator.listener.LocatorListener;
import org.eclipse.tm.te.tcf.locator.services.LocatorModelLookupService;
import org.eclipse.tm.te.tcf.locator.services.LocatorModelRefreshService;
import org.eclipse.tm.te.tcf.locator.services.LocatorModelUpdateService;
import org.eclipse.tm.te.tcf.locator.utils.IPAddressUtil;


/**
 * Default locator model implementation.
 */
public class LocatorModel extends PlatformObject implements ILocatorModel {
	// Flag to mark the model disposed
	private boolean fDisposed;

	// The list of known peers
	private final Map<String, IPeerModel> fPeers = new HashMap<String, IPeerModel>();

	// Reference to the scanner
	private IScanner fScanner = null;

	// Reference to the model locator listener
	private ILocator.LocatorListener fLocatorListener = null;
	// Reference to the model channel state change listener
	private IChannelStateChangeListener fChannelStateChangeListener = null;

	// The list of registered model listeners
	private final List<IModelListener> fModelListener = new ArrayList<IModelListener>();

	// Reference to the refresh service
	private final ILocatorModelRefreshService fRefreshService = new LocatorModelRefreshService(this);
	// Reference to the lookup service
	private final ILocatorModelLookupService fLookupService = new LocatorModelLookupService(this);
	// Reference to the update service
	private final ILocatorModelUpdateService fUpdateService = new LocatorModelUpdateService(this);

	/**
	 * Constructor.
	 */
	public LocatorModel() {
		super();
		fDisposed = false;

		fChannelStateChangeListener = new ChannelStateChangeListener(this);
		Tcf.addChannelStateChangeListener(fChannelStateChangeListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#addListener(org.eclipse.tm.te.tcf.locator.core.interfaces.IModelListener)
	 */
	public void addListener(IModelListener listener) {
		assert Protocol.isDispatchThread() && listener != null;
		if (!fModelListener.contains(listener)) fModelListener.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#removeListener(org.eclipse.tm.te.tcf.locator.core.interfaces.IModelListener)
	 */
	public void removeListener(IModelListener listener) {
		assert Protocol.isDispatchThread() && listener != null;
		fModelListener.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel#getListener()
	 */
	public IModelListener[] getListener() {
		return fModelListener.toArray(new IModelListener[fModelListener.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#dispose()
	 */
	public void dispose() {
		assert Protocol.isDispatchThread();

		// If already disposed, we are done immediately
		if (fDisposed) return;

		fDisposed = true;

		final IModelListener[] listeners = getListener();
		if (listeners.length > 0) {
			Protocol.invokeLater(new Runnable() {
				public void run() {
					for (IModelListener listener : listeners) {
						listener.locatorModelDisposed(LocatorModel.this);
					}
				}
			});
		}
		fModelListener.clear();

		if (fLocatorListener != null) {
			Protocol.getLocator().removeListener(fLocatorListener);
			fLocatorListener = null;
		}

		if (fChannelStateChangeListener != null) {
			Tcf.removeChannelStateChangeListener(fChannelStateChangeListener);
			fChannelStateChangeListener = null;
		}

		if (fScanner != null) {
			stopScanner();
			fScanner = null;
		}

		fPeers.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#isDisposed()
	 */
	public boolean isDisposed() {
		return fDisposed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#getPeers()
	 */
	public IPeerModel[] getPeers() {
		return fPeers.values().toArray(new IPeerModel[fPeers.values().size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ILocator.LocatorListener.class)) {
			return fLocatorListener;
		}
		if (adapter.isAssignableFrom(IScanner.class)) {
			return fScanner;
		}
		if (adapter.isAssignableFrom(ILocatorModelRefreshService.class)) {
			return fRefreshService;
		}
		if (adapter.isAssignableFrom(ILocatorModelLookupService.class)) {
			return fLookupService;
		}
		if (adapter.isAssignableFrom(ILocatorModelUpdateService.class)) {
			return fUpdateService;
		}
		if (adapter.isAssignableFrom(Map.class)) {
			return fPeers;
		}

		return super.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#getService(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <V extends ILocatorModelService> V getService(Class<V> serviceInterface) {
		assert serviceInterface != null;
		return (V)getAdapter(serviceInterface);
	}

	/**
	 * Check if the locator listener has been created and registered
	 * to the global locator service.
	 * <p>
	 * <b>Note:</b> This method is not intended to be call from clients.
	 */
	public void checkLocatorListener() {
		assert Protocol.isDispatchThread();
		assert Protocol.getLocator() != null;

		if (fLocatorListener == null) {
			fLocatorListener = doCreateLocatorListener(this);
			Protocol.getLocator().addListener(fLocatorListener);
		}
	}

	/**
	 * Creates the locator listener instance.
	 *
	 * @param model The parent model. Must not be <code>null</code>.
	 * @return The locator listener instance.
	 */
	protected ILocator.LocatorListener doCreateLocatorListener(ILocatorModel model) {
		assert model != null;
		return new LocatorListener(model);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#getScanner()
	 */
	public IScanner getScanner() {
		if (fScanner == null) fScanner = new Scanner(this);
		return fScanner;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#startScanner(long, long)
	 */
	public void startScanner(long delay, long schedule) {
		IScanner scanner = getScanner();

		if (scanner != null) {
			// Pass on the schedule parameter
			Map<String, Object> config = new HashMap<String, Object>(scanner.getConfiguration());
			config.put(IScanner.PROP_SCHEDULE, Long.valueOf(schedule));
			scanner.setConfiguration(config);
		}

		// The default scanner implementation is a job.
		// -> schedule here if it is a job
		if (scanner instanceof Job) {
			Job job = (Job)scanner;
			job.setSystem(true);
			job.setPriority(Job.DECORATE);
			job.schedule(delay);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#stopScanner()
	 */
	public void stopScanner() {
		if (fScanner != null) {
			// Terminate the scanner
			fScanner.terminate();
			// Reset the scanner reference
			fScanner = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#validatePeerNodeForAdd(org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel)
	 */
	public IPeerModel validatePeerNodeForAdd(IPeerModel node) {
		assert Protocol.isDispatchThread() && node != null;

		// Get the peer from the peer node
		IPeer peer = node.getPeer();

		IPeerModel result = node;

		// Check on the filtered by preference settings what to do
		boolean isFilterByAgentId = Platform.getPreferencesService().getBoolean(CoreBundleActivator.getUniqueIdentifier(),
		                                                                        IPreferenceKeys.PREF_FILTER_BY_AGENT_ID,
		                                                                        false, null);
		if (isFilterByAgentId) {
			// Peers are filtered by agent id. Don't add the peer node
			// if we have another peer node already having the same agent id
			String agentId = peer.getAgentID();
			IPeerModel previousNode = agentId != null ? getService(ILocatorModelLookupService.class).lkupPeerModelByAgentId(agentId) : null;
			if (previousNode != null) {
				// Get the peer for the previous node
				IPeer previousPeer = previousNode.getPeer();
				if (previousPeer != null) {
					// We prefer to use the peer node for the canonical IP address before
					// the loop back address before any other address.
					String loopback = IPAddressUtil.getInstance().getIPv4LoopbackAddress();
					String canonical = IPAddressUtil.getInstance().getCanonicalAddress();

					boolean fireListener = false;

					String peerIP = peer.getAttributes().get(IPeer.ATTR_IP_HOST);
					String previousPeerIP = previousPeer.getAttributes().get(IPeer.ATTR_IP_HOST);
					if (canonical != null && canonical.equals(peerIP) && !canonical.equals(previousPeerIP)) {
						// Remove the old node and replace it with the new new
						fPeers.remove(previousNode.getPeer().getID());
						fireListener = true;
					} else if (loopback != null && loopback.equals(peerIP) && !loopback.equals(previousPeerIP)
							&& (canonical == null || canonical != null && !canonical.equals(previousPeerIP))) {
						// Remove the old node and replace it with the new new
						fPeers.remove(previousNode.getPeer().getID());
						fireListener = true;
					} else {
						// Drop the current node
						result = null;
					}

					if (fireListener) {
						final IModelListener[] listeners = getListener();
						if (listeners.length > 0) {
							Protocol.invokeLater(new Runnable() {
								public void run() {
									for (IModelListener listener : listeners) {
										listener.locatorModelChanged(LocatorModel.this);
									}
								}
							});
						}
					}
				}
			}
		}

		return result;
	}
}
