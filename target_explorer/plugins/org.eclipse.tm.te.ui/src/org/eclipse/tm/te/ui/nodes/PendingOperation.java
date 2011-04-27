/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.nodes;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.te.ui.nls.Messages;


/**
 * Target Explorer: Pending operation data node.
 */
public class PendingOperation extends PlatformObject {

	/**
	 * Returns the pending operation node name.
	 *
	 * @return The node name.
	 */
	public final String getName() {
		return Messages.PendingOperation_label;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return getName().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return getName();
	}
}
