/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.launcher;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtension;
import org.eclipse.tm.te.ui.terminals.interfaces.ILauncherDelegate;

/**
 * Abstract launcher delegate implementation.
 */
public abstract class AbstractLauncherDelegate extends ExecutableExtension implements ILauncherDelegate {
	// The converted expression
	private Expression expression;

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.ExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	    super.setInitializationData(config, propertyName, data);

		// Read the sub elements of the extension
		IConfigurationElement[] children = config != null ? config.getChildren() : null;
		// The "enablement" element is the only expected one
		if (children != null && children.length > 0) {
			expression = ExpressionConverter.getDefault().perform(children[0]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.terminals.interfaces.ILauncherDelegate#getEnablement()
	 */
	@Override
    public Expression getEnablement() {
		return expression;
	}
}
