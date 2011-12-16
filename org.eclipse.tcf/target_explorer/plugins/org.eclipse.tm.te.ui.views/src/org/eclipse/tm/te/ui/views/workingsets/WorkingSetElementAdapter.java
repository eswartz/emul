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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tm.te.runtime.interfaces.workingsets.IWorkingSetElement;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetElementAdapter;

/**
 * Working set element adapter implementation.
 */
public class WorkingSetElementAdapter implements IWorkingSetElementAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkingSetElementAdapter#adaptElements(org.eclipse.ui.IWorkingSet, org.eclipse.core.runtime.IAdaptable[])
	 */
	@Override
	public IAdaptable[] adaptElements(IWorkingSet ws, IAdaptable[] elements) {
		List<IAdaptable> adapted = new ArrayList<IAdaptable>();

		// All elements of a target explorer working set needs to
		// be of type WorkingSetElementHolder
		for (IAdaptable adaptable : elements) {
			if (adaptable instanceof WorkingSetElementHolder) {
				adapted.add(adaptable);
			} else {
				IWorkingSetElement element = null;
				if (adaptable instanceof IWorkingSetElement) {
					element = (IWorkingSetElement) adaptable;
				} else {
					element = (IWorkingSetElement) adaptable.getAdapter(IWorkingSetElement.class);
				}
				// Create the WorkingSetElementHolder for the element
				if (element != null) {
					WorkingSetElementHolder holder = new WorkingSetElementHolder(ws.getName(), element.getElementId());
					holder.setElement(element);
					adapted.add(holder);
				}
			}
		}

		return adapted.toArray(new IAdaptable[adapted.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkingSetElementAdapter#dispose()
	 */
	@Override
	public void dispose() {
	}

}
