/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.interfaces.callback;

import javax.security.auth.callback.Callback;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tm.te.core.interfaces.nodes.IPropertiesContainer;


/**
 * Generic callback interface for asynchronous calls.
 *
 * @noimplement Not intended to be implemented by clients, use {@link Callback} as base implementation instead.
 * @noextend
 */
public interface ICallback extends IPropertiesContainer {

	/**
	 * Returns if or if not the callbacks <code>done</code> method
	 * was called already.
	 *
	 * @return <code>True</code> if {@link #done(Object, IStatus)} was called already, <code>false</code> otherwise.
	 */
	public boolean isDone();

	/**
	 * Callback method invoked when a request is completed.
	 *
	 * @param caller
	 *            The caller or <code>null</code>.
	 * @param status
	 *            The status. Must not be <code>null</code>.
	 */
	public void done(Object caller, IStatus status);

	/**
	 * Add an additional parent callback to the end of the parent callback list.
	 *
	 * @param callback
	 *            The parent callback. Must not be <code>null</code>.
	 */
	public void addParentCallback(ICallback callback);
}
