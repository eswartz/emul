/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.core.interfaces.listeners;

/**
 * Interface for clients to implement that wishes to listen
 * for the TCF protocol framework to come up and shutdown.
 */
public interface IProtocolStateChangeListener {

	/**
	 * Invoked if the TCF framework comes up, <i>state == true</i>, or
	 * if it shuts down, <i>state == false</i>.
	 *
	 * @param state The current TCF framework state.
	 */
	public void stateChanged(boolean state);
}
