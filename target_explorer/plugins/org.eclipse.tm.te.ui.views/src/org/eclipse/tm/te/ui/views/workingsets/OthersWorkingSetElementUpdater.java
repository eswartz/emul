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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tm.te.runtime.interfaces.workingsets.IWorkingSetElement;
import org.eclipse.tm.te.ui.views.internal.ViewRoot;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * "Others" working set element updater.
 */
public class OthersWorkingSetElementUpdater extends WorkingSetElementUpdater {
	// The reference to the "Others" working set
	private IWorkingSet othersWorkingSet;

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.views.workingsets.WorkingSetElementUpdater#add(org.eclipse.ui.IWorkingSet)
	 */
	@Override
	public void add(IWorkingSet workingSet) {
	    Assert.isTrue(othersWorkingSet == null);
	    othersWorkingSet = workingSet;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.views.workingsets.WorkingSetElementUpdater#remove(org.eclipse.ui.IWorkingSet)
	 */
	@Override
	public boolean remove(IWorkingSet workingSet) {
		Assert.isTrue(othersWorkingSet == workingSet);
		othersWorkingSet = null;
	    return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.views.workingsets.WorkingSetElementUpdater#contains(org.eclipse.ui.IWorkingSet)
	 */
	@Override
	public boolean contains(IWorkingSet workingSet) {
	    return othersWorkingSet == workingSet;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.views.workingsets.WorkingSetElementUpdater#onUpdateWorkingSets(org.eclipse.ui.navigator.CommonViewer, org.eclipse.ui.IWorkingSet[])
	 */
	@Override
	protected void onUpdateWorkingSets(CommonViewer viewer, IWorkingSet[] workingsets) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(workingsets);

		// The list of elements not be contained by any other working set
		List<WorkingSetElementHolder> otherElements = new ArrayList<WorkingSetElementHolder>();

		// Get all (root) elements from the common viewer
		Object[] elements = viewer.getNavigatorContentService().createCommonContentProvider().getElements(ViewRoot.getInstance());

		// Get all working sets
		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet[] allWorkingSets = manager.getAllWorkingSets();

		// Loop the elements and check if they are contained in a working set
		for (Object element : elements) {
			if (!(element instanceof IWorkingSetElement)) continue;

			boolean isContained = isContained((IWorkingSetElement)element, allWorkingSets);
			if (!isContained) {
				WorkingSetElementHolder holder = new WorkingSetElementHolder(othersWorkingSet.getName(), ((IWorkingSetElement)element).getElementId());
				holder.setElement((IWorkingSetElement)element);
				otherElements.add(holder);
			}
		}

		othersWorkingSet.setElements(otherElements.toArray(new IAdaptable[otherElements.size()]));
	}

	/**
	 * Walks over the given working set list and checks if the element is contained in
	 * one of them.
	 *
	 * @param element The element. Must not be <code>null</code>.
	 * @param allWorkingSets The list of working sets. Must not be <code>null</code>.
	 *
	 * @return <code>True</code> if the element is contained in at least one of the given working sets, <code>false</code> otherwise.
	 */
	protected boolean isContained(IWorkingSetElement element, IWorkingSet[] allWorkingSets) {
		Assert.isNotNull(element);
		Assert.isNotNull(allWorkingSets);

		boolean contained = false;

		for (IWorkingSet workingSet : allWorkingSets) {
			IAdaptable[] wsElements = workingSet.getElements();
			for (IAdaptable wsElement : wsElements) {
				if (!(wsElement instanceof WorkingSetElementHolder)) continue;

				IWorkingSetElement candidate = ((WorkingSetElementHolder)wsElement).getElement();
				String candidateId = ((WorkingSetElementHolder)wsElement).getElementId();

				if (element.equals(candidate)) {
					contained = true;
					break;
				} else if (candidate == null && element.getElementId().equals(candidateId)) {
					contained = true;
					((WorkingSetElementHolder)wsElement).setElement(element);
					break;
				}
			}
			if (contained) break;
		}

		return contained;
	}
}
