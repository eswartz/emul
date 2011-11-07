/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.workingsets;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tm.te.runtime.interfaces.workingsets.IWorkingSetElement;

/**
 * The adapter factory to adapt a working set element holder to a working set element.
 */
@SuppressWarnings("rawtypes")
public class WorkingSetElementHolderAdapterFactory implements IAdapterFactory {
	// The adapters.
	private Class[] adapters = { IWorkingSetElement.class };

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof WorkingSetElementHolder) {
			return ((WorkingSetElementHolder) adaptableObject).getAdapter(adapterType);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return adapters;
	}
}
