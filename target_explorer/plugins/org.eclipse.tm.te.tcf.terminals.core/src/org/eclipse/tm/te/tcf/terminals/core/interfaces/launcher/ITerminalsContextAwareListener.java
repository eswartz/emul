/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher;

import org.eclipse.tm.tcf.services.ITerminals;

/**
 * Remote terminal context aware listener.
 */
public interface ITerminalsContextAwareListener {

	/**
	 * Sets the terminals context.
	 *
	 * @param context The terminals context. Must not be <code>null</code>.
	 */
	public void setTerminalsContext(ITerminals.TerminalContext context);

	/**
	 * Returns the terminals context.
	 *
	 * @return The terminals context.
	 */
	public ITerminals.TerminalContext getTerminalsContext();
}
