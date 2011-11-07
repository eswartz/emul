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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tm.te.ui.views.interfaces.workingsets.IWorkingSetNameIDs;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * A WorkingSetElementHolderFactory is an element factory used to create a working set element
 * holder from a memento element.
 */
public class WorkingSetElementHolderFactory implements IElementFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
	 */
	@Override
    public IAdaptable createElement(IMemento memento) {
		WorkingSetElementHolder holder = new WorkingSetElementHolder();
		String elementId = memento.getString(IWorkingSetNameIDs.ATTR_ELEMENTID);
		holder.setElementId(elementId);
		String workingSetName = memento.getString(IWorkingSetNameIDs.ATTR_WORKINGSET_NAME);
		holder.setWorkingSetName(workingSetName);
		return holder;
	}

}
