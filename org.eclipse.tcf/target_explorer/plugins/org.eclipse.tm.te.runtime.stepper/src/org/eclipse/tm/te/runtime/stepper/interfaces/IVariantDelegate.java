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

import org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * The variant delegate is providing variants to the main modes. Variants can influence the executed
 * steps or step groups.
 */
public interface IVariantDelegate extends IExecutableExtension {

	/**
	 * Returns the valid mode variants for the current context The returned list of variants is
	 * probed in the returned order.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 *
	 * @return The valid variants or an empty array.
	 */
	public String[] getValidVariants(IContext context, IPropertiesContainer data);
}
