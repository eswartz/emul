/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.tables;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Target Explorer: Immutable representation of a table node.
 */
public final class TableNode extends PlatformObject {
	/**
	 * The node name.
	 */
	public final String name;

	/**
	 * The node value.
	 */
	public final String value;

	/**
	 * Constructor.
	 *
	 * @param name The node name. Must not be <code>null</code>.
	 * @param value The node value. Must not be <code>null</code>.
	 */
	public TableNode(String name, String value) {
		Assert.isNotNull(name);
		Assert.isNotNull(value);

		this.name = name;
		this.value = value;
	}
}
