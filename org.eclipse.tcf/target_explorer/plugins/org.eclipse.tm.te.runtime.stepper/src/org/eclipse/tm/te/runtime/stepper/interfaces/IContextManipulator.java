/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.stepper.interfaces;

/**
 * A step or step group capable of modifying the context the steps or step
 * groups are operating on.
 */
public interface IContextManipulator {

	/**
	 * The suffix to append to the full qualified step id to
	 * get the delayed status object.
	 */
	public final static String CONTEXT_ID = "contextId"; //$NON-NLS-1$

	/**
	 * The suffix to append to the full qualified step id to
	 * get the delayed status object.
	 */
	public final static String CONTEXT = "context"; //$NON-NLS-1$
}
