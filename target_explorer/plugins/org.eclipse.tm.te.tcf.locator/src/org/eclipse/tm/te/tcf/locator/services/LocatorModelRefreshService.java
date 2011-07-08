/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.services;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.tcf.core.TransientPeer;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.locator.ScannerRunnable;
import org.eclipse.tm.te.tcf.locator.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.preferences.IPreferenceKeys;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
import org.eclipse.tm.te.tcf.locator.nodes.LocatorModel;
import org.eclipse.tm.te.tcf.locator.nodes.PeerModel;


/**
 * Default locator model refresh service implementation.
 */
public class LocatorModelRefreshService extends AbstractLocatorModelService implements ILocatorModelRefreshService {

	/**
	 * Constructor.
	 *
	 * @param parentModel The parent locator model instance. Must not be <code>null</code>.
	 */
	public LocatorModelRefreshService(ILocatorModel parentModel) {
		super(parentModel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.services.ILocatorModelRefreshService#refresh()
	 */
	public void refresh() {
		Assert.isTrue(Protocol.isDispatchThread());

		// Get the parent locator model
		ILocatorModel model = getLocatorModel();

		// If the parent model is already disposed, the service will drop out immediately
		if (model.isDisposed()) return;

		// If the TCF framework isn't initialized yet, the service will drop out immediately
		if (!Tcf.isRunning()) return;

		// Get the list of old children (update node instances where possible)
		final List<IPeerModel> oldChildren = new ArrayList<IPeerModel>(Arrays.asList(model.getPeers()));

		// Get the locator service
		ILocator locatorService = Protocol.getLocator();
		if (locatorService != null) {
			// Check for the locator listener to be created and registered
			if (model instanceof LocatorModel) ((LocatorModel)model).checkLocatorListener();
			// Get the map of peers known to the locator service.
			Map<String, IPeer> peers = locatorService.getPeers();
			// Process the peers
			processPeers(peers, oldChildren, model);
		}

		// Refresh the static peer definitions
		refreshStaticPeers(oldChildren, model);

		// If there are remaining old children, remove them from the model (non-recursive)
		for (IPeerModel oldChild : oldChildren) model.getService(ILocatorModelUpdateService.class).remove(oldChild);
	}

	/**
	 * Process the given map of peers and update the given locator model.
	 *
	 * @param peers The map of peers to process. Must not be <code>null</code>.
	 * @param oldChildren The list of old children. Must not be <code>null</code>.
	 * @param model The locator model. Must not be <code>null</code>.
	 */
	protected void processPeers(Map<String, IPeer> peers, List<IPeerModel> oldChildren, ILocatorModel model) {
		Assert.isNotNull(peers);
		Assert.isNotNull(oldChildren);
		Assert.isNotNull(model);

		for (String peerId : peers.keySet()) {
			// Get the peer instance for the current peer id
			IPeer peer = peers.get(peerId);
			// Try to find an existing peer node first
			IPeerModel peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(peerId);
			// And create a new one if we cannot find it
			if (peerNode == null) peerNode = new PeerModel(model, peer);
			else oldChildren.remove(peerNode);
			// Validate the peer node before adding
			if (peerNode != null) peerNode = model.validatePeerNodeForAdd(peerNode);
			if (peerNode != null) {
				// Add the peer node to model
				model.getService(ILocatorModelUpdateService.class).add(peerNode);
				// And schedule for immediate status update
				Runnable runnable = new ScannerRunnable(model.getScanner(), peerNode);
				Protocol.invokeLater(runnable);
			}
		}
	}

	/**
	 * Refresh the static peer definitions.
	 *
	 * @param oldChildren The list of old children. Must not be <code>null</code>.
	 * @param model The locator model. Must not be <code>null</code>.
	 */
	protected void refreshStaticPeers(List<IPeerModel> oldChildren, ILocatorModel model) {
		Assert.isNotNull(oldChildren);
		Assert.isNotNull(model);

		// Get the root locations to lookup the static peer definitions
		File[] roots = getStaticPeerLookupDirectories();
		if (roots.length > 0) {
			// The map of peers created from the static definitions
			Map<String, IPeer> peers = new HashMap<String, IPeer>();
			// Process the root locations
			for (File root : roots) {
				// List all "*.ini" files within the root location
				File[] candidates = root.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						IPath path = new Path(pathname.getAbsolutePath());
						return path.getFileExtension() != null && path.getFileExtension().toLowerCase().equals("ini"); //$NON-NLS-1$
					}
				});
				// If there are ini files to read, process them
				if (candidates != null && candidates.length > 0) {

					for (File candidate : candidates) {
						try {
							Properties properties = new Properties();
							properties.load(new FileInputStream(candidate));

							// Validate the name attribute. If not set, set
							// it to the file name without the .ini extension.
							String name = properties.getProperty(IPeer.ATTR_NAME);
							if (name == null || (name != null && "".equals(name.trim()))) { //$NON-NLS-1$
								name = new Path(candidate.getAbsolutePath()).removeFileExtension().lastSegment();
								properties.setProperty(IPeer.ATTR_NAME, name);
							}

							// Validate the id attribute. If not set, generate one.
							String id = properties.getProperty(IPeer.ATTR_ID);
							if (id == null || (id != null && "".equals(id.trim())) || (id != null && "USR:".equals(id.trim()))) { //$NON-NLS-1$ //$NON-NLS-2$
								String transport = properties.getProperty(IPeer.ATTR_TRANSPORT_NAME);
								String host = properties.getProperty(IPeer.ATTR_IP_HOST);
								String port = properties.getProperty(IPeer.ATTR_IP_PORT);

								if (transport != null && host != null && !(id != null && "USR:".equals(id.trim()))) { //$NON-NLS-1$
									id = transport.trim() + ":" + host.trim(); //$NON-NLS-1$
									id += port != null ? ":" + port.trim() : ":1534"; //$NON-NLS-1$ //$NON-NLS-2$
								} else {
									id = "USR:" + System.currentTimeMillis(); //$NON-NLS-1$
									// If the key is not unique, we have to wait a little bit an try again
									while (peers.containsKey(id)) {
										try { Thread.sleep(20); } catch (InterruptedException e) { /* ignored on purpose */ }
										id = "USR:" + System.currentTimeMillis(); //$NON-NLS-1$
									}
								}
								properties.put(IPeer.ATTR_ID, id);
							}

							// Copy all string attributes
							Map<String, String> attrs = new HashMap<String, String>();
							for (Object key : properties.keySet()) {
								if (key instanceof String && properties.get(key) instanceof String) {
									attrs.put((String)key, (String)properties.get(key));
								}
							}

							// Construct the peer from the attributes
							IPeer peer = new TransientPeer(attrs);
							// Add the constructed peer to the peers map
							peers.put(peer.getID(), peer);
						} catch (IOException e) {
							/* ignored on purpose */
						}
					}
				}
			}
			// Process the read peers
			if (!peers.isEmpty()) processPeers(peers, oldChildren, model);
		}
	}

	/**
	 * Returns the list of root locations to lookup for static peers definitions.
	 *
	 * @return The list of root locations or an empty list.
	 */
	protected File[] getStaticPeerLookupDirectories() {
		// The list defining the root locations
		List<File> rootLocations = new ArrayList<File>();

		// Check on the peers root locations preference setting
		String roots = Platform.getPreferencesService().getString(CoreBundleActivator.getUniqueIdentifier(),
																 IPreferenceKeys.PREF_STATIC_PEERS_ROOT_LOCATIONS,
																 null, null);
		// If set, split it in its single components
		if (roots != null) {
			String[] candidates = roots.split(File.pathSeparator);
			// Check on each candidate to denote an existing directory
			for (String candidate : candidates) {
				File file = new File(candidate);
				if (file.canRead() && file.isDirectory() && !rootLocations.contains(file)) {
					rootLocations.add(file);
				}
			}
		} else {
			// Try the bundles state location first (not available if launched with -data @none).
			try {
				File file = CoreBundleActivator.getDefault().getStateLocation().append(".peers").toFile(); //$NON-NLS-1$
				if (file.canRead() && file.isDirectory() && !rootLocations.contains(file)) {
					rootLocations.add(file);
				}
			} catch (IllegalStateException e) {
				/* ignored on purpose */
			}

			// The users local peers lookup directory is $HOME/.tcf/.peers.
			File file = new Path(System.getProperty("user.home")).append(".tcf/.peers").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
			if (file.canRead() && file.isDirectory() && !rootLocations.contains(file)) {
				rootLocations.add(file);
			}
		}

		return rootLocations.toArray(new File[rootLocations.size()]);
	}
}
