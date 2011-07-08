/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.interfaces.nodes;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.te.tcf.locator.interfaces.IModelListener;
import org.eclipse.tm.te.tcf.locator.interfaces.IScanner;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelService;


/**
 * The locator model is an extension to the TCF locator service. The
 * model allows to store additional properties for each peer, keep
 * track of peers from different origins.
 * <p>
 * <b>Note:</b> Updates to the locator model, and the locator model
 * children needs to be performed in the TCF dispatch thread. The
 * locator model and all child model nodes do assert this core
 * assumption. To maintain consistency, and to avoid any performance
 * overhead for thread synchronization, the model read access must
 * happen in the TCF dispatch thread as well.
 *
 * @see ILocator
 */
public interface ILocatorModel extends IAdaptable {

	/**
	 * Adds the specified listener to the list of model listener.
	 * If the same listener has been added before, the listener will
	 * not be added again.
	 *
	 * @param listener The listener. Must not be <code>null</code>.
	 */
	public void addListener(IModelListener listener);

	/**
	 * Removes the specified listener from the list of model listener.
	 *
	 * @param listener The listener. Must not be <code>null</code>.
	 */
	public void removeListener(IModelListener listener);

	/**
	 * Returns the list of registered model listeners.
	 *
	 * @return The list of registered model listeners or an empty list.
	 */
	public IModelListener[] getListener();

	/**
	 * Dispose the locator model instance.
	 */
	public void dispose();

	/**
	 * Returns if or if not the locator model instance is disposed.
	 *
	 * @return <code>True</code> if the locator model instance is disposed, <code>false/code> otherwise.
	 */
	public boolean isDisposed();

	/**
	 * Returns the list of known peers.
	 *
	 * @return The list of known peers or an empty list.
	 */
	public IPeerModel[] getPeers();

	/**
	 * Returns the scanner instance being associated with the
	 * locator model.
	 *
	 * @return The scanner instance.
	 */
	public IScanner getScanner();

	/**
	 * Starts the scanner.
	 *
	 * @param delay The delay in millisecond before the scanning starts.
	 * @param schedule The time in millisecond between the scanner runs.
	 */
	public void startScanner(long delay, long schedule);

	/**
	 * Stops the scanner.
	 */
	public void stopScanner();

	/**
	 * Returns the locator model service, implementing at least the specified
	 * service interface.
	 *
	 * @param serviceInterface The service interface class. Must not be <code>null</code>.
	 * @return The service instance implementing the specified service interface, or <code>null</code>.
	 */
	public <V extends ILocatorModelService> V getService(Class<V> serviceInterface);

	/**
	 * Validate the given peer model if or if not it can be added
	 * to the locator model as new peer node.
	 *
	 * @param node The peer model. Must not be <code>null</code>.
	 * @return The peer node if it allowed add it to the model, or <code>null</code> if not.
	 */
	public IPeerModel validatePeerNodeForAdd(IPeerModel node);

}
