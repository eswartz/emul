/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.model;

import org.eclipse.tm.te.runtime.concurrent.util.ExecutorsUtil;
import org.eclipse.tm.te.runtime.model.ModelNode;

/**
 * Model node implementation assuring thread safety by enforcing model operations to happen in the
 * executor thread.
 */
public class ThreadSafeModelNode extends ModelNode {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.nodes.PropertiesContainer#checkThreadAccess()
	 */
	@Override
	protected boolean checkThreadAccess() {
		return ExecutorsUtil.isExecutorThread();
	}
}
