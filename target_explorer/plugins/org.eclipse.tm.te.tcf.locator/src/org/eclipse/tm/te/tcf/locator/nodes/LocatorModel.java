/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.Assert;
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
import org.eclipse.tm.te.tcf.locator.interfaces.ITracing;
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
	// The unique model id
	private final UUID uniqueId = UUID.randomUUID();
	// Flag to mark the model disposed
	private boolean disposed;

	// The list of known peers
	private final Map<String, IPeerModel> peers = new HashMap<String, IPeerModel>();

	// Reference to the scanner
	private IScanner scanner = null;

	// Reference to the model locator listener
	private ILocator.LocatorListener locatorListener = null;
	// Reference to the model channel state change listener
	private IChannelStateChangeListener channelStateChangeListener = null;

	// The list of registered model listeners
	private final List<IModelListener> modelListener = new ArrayList<IModelListener>();

	// Reference to the refresh service
	private final ILocatorModelRefreshService refreshService = new LocatorModelRefreshService(this);
	// Reference to the lookup service
	private final ILocatorModelLookupService lookupService = new LocatorModelLookupService(this);
	// Reference to the update service
	private final ILocatorModelUpdateService updateService = new LocatorModelUpdateService(this);

	/**
	 * Constructor.
	 */
	public LocatorModel() {
		super();
		disposed = false;

		channelStateChangeListener = new ChannelStateChangeListener(this);
		Tcf.addChannelStateChangeListener(channelStateChangeListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#addListener(org.eclipse.tm.te.tcf.locator.core.interfaces.IModelListener)
	 */
	public void addListener(IModelListener listener) {
		Assert.isNotNull(listener);
		Assert.isTrue(Protocol.isDispatchThread());

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
			CoreBundleActivator.getTraceHandler().trace("LocatorModel.addListener( " + listener + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (!modelListener.contains(listener)) modelListener.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#removeListener(org.eclipse.tm.te.tcf.locator.core.interfaces.IModelListener)
	 */
	public void removeListener(IModelListener listener) {
		Assert.isNotNull(listener);
		Assert.isTrue(Protocol.isDispatchThread());

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
			CoreBundleActivator.getTraceHandler().trace("LocatorModel.removeListener( " + listener + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$
		}

		modelListener.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel#getListener()
	 */
	public IModelListener[] getListener() {
		return modelListener.toArray(new IModelListener[modelListener.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#dispose()
	 */
	public void dispose() {
		Assert.isTrue(Protocol.isDispatchThread());

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
			CoreBundleActivator.getTraceHandler().trace("LocatorModel.dispose()", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$
		}

		// If already disposed, we are done immediately
		if (disposed) return;

		disposed = true;

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
		modelListener.clear();

		if (locatorListener != null) {
			Protocol.getLocator().removeListener(locatorListener);
			locatorListener = null;
		}

		if (channelStateChangeListener != null) {
			Tcf.removeChannelStateChangeListener(channelStateChangeListener);
			channelStateChangeListener = null;
		}

		if (scanner != null) {
			stopScanner();
			scanner = null;
		}

		peers.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#isDisposed()
	 */
	public boolean isDisposed() {
		return disposed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#getPeers()
	 */
	public IPeerModel[] getPeers() {
		return peers.values().toArray(new IPeerModel[peers.values().size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ILocator.LocatorListener.class)) {
			return locatorListener;
		}
		if (adapter.isAssignableFrom(IScanner.class)) {
			return scanner;
		}
		if (adapter.isAssignableFrom(ILocatorModelRefreshService.class)) {
			return refreshService;
		}
		if (adapter.isAssignableFrom(ILocatorModelLookupService.class)) {
			return lookupService;
		}
		if (adapter.isAssignableFrom(ILocatorModelUpdateService.class)) {
			return updateService;
		}
		if (adapter.isAssignableFrom(Map.class)) {
			return peers;
		}

		return super.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof LocatorModel) {
			return uniqueId.equals(((LocatorModel)obj).uniqueId);
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#getService(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <V extends ILocatorModelService> V getService(Class<V> serviceInterface) {
		Assert.isNotNull(serviceInterface);
		return (V)getAdapter(serviceInterface);
	}

	/**
	 * Check if the locator listener has been created and registered
	 * to the global locator service.
	 * <p>
	 * <b>Note:</b> This method is not intended to be call from clients.
	 */
	public void checkLocatorListener() {
		Assert.isTrue(Protocol.isDispatchThread());
		Assert.isNotNull(Protocol.getLocator());

		if (locatorListener == null) {
			locatorListener = doCreateLocatorListener(this);
			Protocol.getLocator().addListener(locatorListener);
		}
	}

	/**
	 * Creates the locator listener instance.
	 *
	 * @param model The parent model. Must not be <code>null</code>.
	 * @return The locator listener instance.
	 */
	protected ILocator.LocatorListener doCreateLocatorListener(ILocatorModel model) {
		Assert.isNotNull(model);
		return new LocatorListener(model);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#getScanner()
	 */
	public IScanner getScanner() {
		if (scanner == null) scanner = new Scanner(this);
		return scanner;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#startScanner(long, long)
	 */
	public void startScanner(long delay, long schedule) {
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
			CoreBundleActivator.getTraceHandler().trace("LocatorModel.startScanner( " + delay + ", " + schedule + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

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
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
			CoreBundleActivator.getTraceHandler().trace("LocatorModel.stopScanner()", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$
		}

		if (scanner != null) {
			// Terminate the scanner
			scanner.terminate();
			// Reset the scanner reference
			scanner = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.ILocatorModel#validatePeerNodeForAdd(org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel)
	 */
	public IPeerModel validatePeerNodeForAdd(IPeerModel node) {
		Assert.isNotNull(node);
		Assert.isTrue(Protocol.isDispatchThread());

		// Get the peer from the peer node
		IPeer peer = node.getPeer();

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
			CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd( " + (peer != null ? peer.getID() : null) + " )", ITracing.ID_TRACE_LOCATOR_MODEL, this); //$NON-NLS-1$ //$NON-NLS-2$
		}

		IPeerModel result = node;

		// Check on the filtered by preference settings what to do
		boolean isFilterByAgentId = Platform.getPreferencesService().getBoolean(CoreBundleActivator.getUniqueIdentifier(),
		                                                                        IPreferenceKeys.PREF_FILTER_BY_AGENT_ID,
		                                                                        false, null);
		if (isFilterByAgentId) {
			// Peers are filtered by agent id. Don't add the peer node
			// if we have another peer node already having the same agent id
			String agentId = peer.getAgentID();
			IPeerModel[] previousNodes = agentId != null ? getService(ILocatorModelLookupService.class).lkupPeerModelByAgentId(agentId) : new IPeerModel[0];

			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
				CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd: agentId=" + agentId + ", Matching peer nodes " //$NON-NLS-1$ //$NON-NLS-2$
															+ (previousNodes.length > 0 ? "found (" + previousNodes.length +")" : "not found --> DONE") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
															, ITracing.ID_TRACE_LOCATOR_MODEL, this);
			}

			boolean fireListener = false;

			for (IPeerModel previousNode : previousNodes) {
				// Get the peer for the previous node
				IPeer previousPeer = previousNode.getPeer();
				if (previousPeer != null) {
					// We prefer to use the peer node for the canonical IP address before
					// the loop back address before any other address.
					String loopback = IPAddressUtil.getInstance().getIPv4LoopbackAddress();
					String canonical = IPAddressUtil.getInstance().getCanonicalAddress();

					String peerIP = peer.getAttributes().get(IPeer.ATTR_IP_HOST);
					String previousPeerIP = previousPeer.getAttributes().get(IPeer.ATTR_IP_HOST);

					String peerPort = peer.getAttributes().get(IPeer.ATTR_IP_PORT);
					if (peerPort == null || "".equals(peerPort)) peerPort = "1534"; //$NON-NLS-1$ //$NON-NLS-2$
					String previousPeerPort = previousPeer.getAttributes().get(IPeer.ATTR_IP_PORT);
					if (previousPeerPort == null || "".equals(previousPeerPort)) previousPeerPort = "1534"; //$NON-NLS-1$ //$NON-NLS-2$

					if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
						CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd: loopback=" + loopback + ", canonical=" + canonical //$NON-NLS-1$ //$NON-NLS-2$
																	+ ", peerIP=" + peerIP + ", previousPeerIP=" + previousPeerIP //$NON-NLS-1$ //$NON-NLS-2$
																	+ ", peerPort=" + peerPort + ", previousPeerPort=" + previousPeerPort //$NON-NLS-1$ //$NON-NLS-2$
																	, ITracing.ID_TRACE_LOCATOR_MODEL, this);
					}

					// If the ports of the agent instances are identical,
					// than try to find the best representation of the agent instance
					if (peerPort.equals(previousPeerPort))  {
						if (canonical != null && canonical.equals(peerIP) && !canonical.equals(previousPeerIP)) {
							// Remove the old node and replace it with the new new
							peers.remove(previousNode.getPeer().getID());
							fireListener = true;

							if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
								CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd: Previous peer node replaced (canonical overwrite)" //$NON-NLS-1$
																			, ITracing.ID_TRACE_LOCATOR_MODEL, this);
							}
						} else if (loopback != null && loopback.equals(peerIP) && !loopback.equals(previousPeerIP)
								&& (canonical == null || !canonical.equals(previousPeerIP))) {
							// Remove the old node and replace it with the new new
							peers.remove(previousNode.getPeer().getID());
							fireListener = true;

							if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
								CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd: Previous peer node replaced (loopback overwrite)" //$NON-NLS-1$
																			, ITracing.ID_TRACE_LOCATOR_MODEL, this);
							}
						} else {
							// Drop the current node
							result = null;

							if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
								CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd: Previous peer node kept, new peer node dropped" //$NON-NLS-1$
																			, ITracing.ID_TRACE_LOCATOR_MODEL, this);
							}

						}

						// Break the loop if the ports matched
						break;
					}

					if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITracing.ID_TRACE_LOCATOR_MODEL)) {
						CoreBundleActivator.getTraceHandler().trace("LocatorModel.validatePeerNodeForAdd: Previous peer node kept, new peer node added (Port mismatch)" //$NON-NLS-1$
																	, ITracing.ID_TRACE_LOCATOR_MODEL, this);
					}
				}
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

		return result;
	}
}
