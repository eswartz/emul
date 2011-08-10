/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;



/**
 * Locator model property tester.
 */
public class MyPropertyTester extends PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		// The receiver is expected to be a peer model node
		if (receiver instanceof IPeerModel) {
			final Boolean[] result = new Boolean[1];
			if (Protocol.isDispatchThread()) {
				result[0] = Boolean.valueOf(testPeerModel((IPeerModel)receiver, property, args, expectedValue));
			} else {
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						result[0] = Boolean.valueOf(testPeerModel((IPeerModel)receiver, property, args, expectedValue));
					}
				});
			}
			if (result[0] != null) return result[0].booleanValue();
		}
		return false;
	}

	/**
	 * Test the specific peer model node properties.
	 *
	 * @param node The model node. Must not be <code>null</code>.
	 * @param property The property to test.
	 * @param args The property arguments.
	 * @param expectedValue The expected value.
	 *
	 * @return <code>True</code> if the property to test has the expected value, <code>false</code> otherwise.
	 */
	protected boolean testPeerModel(IPeerModel node, String property, Object[] args, Object expectedValue) {
		Assert.isNotNull(node);
		Assert.isTrue(Protocol.isDispatchThread());

		if ("name".equals(property)) { //$NON-NLS-1$
			if (node.getPeer().getName() != null && node.getPeer().getName().equals(expectedValue)) {
				return true;
			}
		}

		if ("hasLocalService".equals(property) || "hasRemoteService".equals(property)) { //$NON-NLS-1$ //$NON-NLS-2$
			String services = null;

			if ("hasLocalService".equals(property)) services = node.getStringProperty(IPeerModelProperties.PROP_LOCAL_SERVICES); //$NON-NLS-1$
			if ("hasRemoteService".equals(property)) services = node.getStringProperty(IPeerModelProperties.PROP_REMOTE_SERVICES); //$NON-NLS-1$

			if (services != null) {
				// Lookup each service individually to avoid "accidental" matching
				for (String service : services.split(",")) { //$NON-NLS-1$
					if (service != null && service.trim().equals(expectedValue)) {
						return true;
					}
				}
			}
		}

		if ("isStaticPeer".equals(property)) { //$NON-NLS-1$
			String path = node.getPeer().getAttributes().get("Path"); //$NON-NLS-1$
			boolean isStaticPeer = path != null && !"".equals(path.trim()); //$NON-NLS-1$
			if (expectedValue instanceof Boolean) return ((Boolean)expectedValue).booleanValue() == isStaticPeer;
		}

		return false;
	}
}
