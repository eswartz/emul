/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.core.interfaces.launcher;

import org.eclipse.tm.tcf.services.IProcesses;

/**
 * Remote process context aware listener.
 */
public interface IProcessContextAwareListener {

	/**
	 * Sets the process context.
	 *
	 * @param context The process context. Must not be <code>null</code>.
	 */
	public void setProcessContext(IProcesses.ProcessContext context);

	/**
	 * Returns the process context.
	 *
	 * @return The process context.
	 */
	public IProcesses.ProcessContext getProcessContext();
}
