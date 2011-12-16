/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * Property tester implementation for objects of type {@link IPropertiesContainer}.
 */
public class PropertiesContainerPropertyTester extends PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		// The receiver is expected to be a properties container
		if (receiver instanceof IPropertiesContainer) {

			if ("isProperty".equals(property)) { // //$NON-NLS-1$
				// Test for an individual property within the property container
				return testIsProperty((IPropertiesContainer)receiver, args, expectedValue);
			}
		}
		return false;
	}

	/**
	 * Test the specific model node properties.
	 *
	 * @param node The properties container. Must not be <code>null</code>.
	 * @param args The property arguments.
	 * @param expectedValue The expected value.
	 *
	 * @return <code>True</code> if the property to test has the expected value, <code>false</code> otherwise.
	 */
	protected boolean testIsProperty(IPropertiesContainer node, Object[] args, Object expectedValue) {
		Assert.isNotNull(node);

		if (args != null && args.length > 0 && args[0] instanceof String) {
			return node.isProperty((String)args[0], expectedValue);
		}

		return false;
	}

}
