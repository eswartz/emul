/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tests.interfaces;

/**
 * Interface to be implemented for conditional test wait interrupts.
 */
public interface IInterruptCondition {

	/**
	 * Test if the interrupt condition is <code>true</code>.
	 *
	 * @return <code>true</code> if the condition is fulfilled, <code>false</code> otherwise.
	 */
	public boolean isTrue();

	/**
	 * Dispose the interrupt condition.
	 */
	public void dispose();
}
