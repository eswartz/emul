/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.connection.interfaces;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tm.te.core.model.IConnectable;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension;

/**
 * A connect strategy.
 * <p>
 * The connect strategy describes the step to perform to connect or disconnect
 * to or from a given context.
 */
public interface IConnectStrategy extends IExecutableExtension {

	/**
	 * Check if the given connectable is valid for this connect strategy.
	 *
	 * @param connectable The connectable context. Must not be <code>null</code>.
	 * @return An error message if the connectable context is invalid, </code>null</code> if it is valid.
	 */
	public String validate(IConnectable connectable);

	/**
	 * Check if prerequisites for connecting are valid.
	 * <p>
	 * Don't run any time-consuming checks.
	 *
	 * @param connectable The connectable context. Must not be <code>null</code>.
	 * @return <code>True</code> if the connectable can be connected, <code>false</code> otherwise.
	 */
	public boolean canConnect(IConnectable connectable);

	/**
	 * Check if prerequisites for disconnecting are valid.
	 * <p>
	 * Don't run any time-consuming checks.
	 *
	 * @param connectable The connectable context. Must not be <code>null</code>.
	 * @return <code>True</code> if the connectable can be disconnected, <code>false</code> otherwise.
	 */
	public boolean canDisconnect(IConnectable connectable);

	/**
	 * Connect the context.
	 * <p>
	 * The connect will be performed by the eclipse job model when no external progress monitor is given.
	 * If the connect sequence finished or failed or got canceled, the given callback is called if not <code>null</code>.
	 *
	 * @param connectable The connectable context. Must not be <code>null</code>.
	 * @param progress An possible external progress monitor or <code>null</code>.
	 * @param ticksToUse The ticks to use.
	 * @param cb The callback to call when completed.
	 * @param autoAttach <code>false</code> to avoid auto attaching.
	 */
	public void connect(IConnectable connectable, IProgressMonitor progress, int ticksToUse, ICallback cb, boolean autoAttach);

	/**
	 * Disconnect the context.
	 * <p>
	 * The connect will be performed by the eclipse job model when no external progress monitor is given.
	 * If the connect sequence finished or failed , the given callback is called if not <code>null</code>.
	 *
	 * @param connectable The connectable context. Must not be <code>null</code>.
	 * @param progress An possible external progress monitor or <code>null</code>.
	 * @param cb The callback to call when completed.
	 * @param useJob <code>true</code> if the disconnect should run in a job.
	 * @param quitting <code>true</code> if disconnect is called during workbench shutdown.
	 */
	public void disconnect(IConnectable connectable, IProgressMonitor progress, int ticksToUse, ICallback cb, boolean quitting);

	/**
	 * Returns if or if not this connect strategy describes a connection type
	 * of the given family.
	 *
	 * @param connectable The connectable context or <code>null</code>.
	 * @param typeFamily The connection type family id.
	 *
	 * @return <code>True</code> if this connect strategy describes a connection type of the given family.
	 */
	public boolean isConnectionTypeFamily(IConnectable connectable, long typeFamily);
}
