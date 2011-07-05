/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.statushandler.interfaces;

import org.eclipse.core.expressions.Expression;

/**
 * Target Explorer: Status handler API declaration
 */
public interface IStatusHandler {

	/**
	 * Returns the enablement expression which is associated with this status handler.
	 *
	 * @return The enablement expression or <code>null</code>.
	 */
	public Expression getEnablement();
}
