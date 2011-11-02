/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal.workingsets;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tm.te.runtime.model.interfaces.IContainerModelNode;
import org.eclipse.tm.te.runtime.model.interfaces.IModelNode;
import org.eclipse.ui.IContainmentAdapter;
import org.eclipse.ui.IWorkingSet;

/**
 * The working set filter filters elements from a view that are neither a parent nor
 * children of a working set element.
 */
public class WorkingSetFilter extends ViewerFilter {
    private IWorkingSet workingSet = null;

    private IAdaptable[] cachedWorkingSet = null;

    /**
     * Returns the active working set the filter is working with.
     *
     * @return the active working set
     */
    public IWorkingSet getWorkingSet() {
        return workingSet;
    }

    /**
     * Sets the active working set.
     *
     * @param workingSet the working set the filter should work with
     */
    public void setWorkingSet(IWorkingSet workingSet) {
        this.workingSet = workingSet;
    }

    /**
     * Determines if an element should be filtered out.
     *
     * @see ViewerFilter#select(Viewer, Object, Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (workingSet == null || (workingSet.isAggregateWorkingSet() && workingSet.isEmpty())) {
            return true;
        }
        if (element != null) {
            return isEnclosed(element);
        }
        return true;
    }

    /**
     * Returns if the given element is enclosed by a working set element.
     * The IContainmentAdapter of each working set element is used for the
     * containment test. If there is no IContainmentAdapter for a working
     * set element, a simple resource based test is used.
     *
     * @param element The element to test for enclosure by a working set element
     * @return true if element is enclosed by a working set element and false otherwise.
     */
    private boolean isEnclosed(Object element) {
        IAdaptable[] workingSetElements = cachedWorkingSet;

        // working set elements won't be cached if select is called
        // directly, outside filter. fixes bug 14500.
        if (workingSetElements == null) {
			workingSetElements = workingSet.getElements();
		}

        for (int i = 0; i < workingSetElements.length; i++) {
            IAdaptable workingSetElement = workingSetElements[i];
            IContainmentAdapter containmentAdapter = (IContainmentAdapter) workingSetElement.getAdapter(IContainmentAdapter.class);

            // if there is no IContainmentAdapter defined for the working
            // set element type fall back to using resource based
            // containment check
            if (containmentAdapter != null) {
                if (containmentAdapter.contains(workingSetElement, element,
                        IContainmentAdapter.CHECK_CONTEXT
                                | IContainmentAdapter.CHECK_IF_CHILD
                                | IContainmentAdapter.CHECK_IF_ANCESTOR
                                | IContainmentAdapter.CHECK_IF_DESCENDANT)) {
					return true;
				}
            } else if (isEnclosedElement(element, workingSetElement)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns if the given element is enclosed by a working set element.
     * A element is enclosed if it is either a parent of a working set
     * element, a child of a working set element or a working set element
     * itself.
     *
     * @param element The element to test for enclosure by a working set element
     * @param workingSetElement The working set element
     *
     * @return true if element is enclosed by a working set element and false otherwise.
     */
    private boolean isEnclosedElement(Object element, IAdaptable workingSetElement) {
        if (workingSetElement.equals(element)) {
			return true;
		}

        if (element instanceof IModelNode) {
        	IContainerModelNode parent = ((IModelNode)element).getParent();
        	while (parent != null && !workingSetElement.equals(parent)) {
        		parent = parent.getParent();
        	}
        	if (parent != null && workingSetElement.equals(parent)) {
        		return true;
        	}
        }

        if (element != null) {
        	IModelNode adapter = (IModelNode)workingSetElement.getAdapter(IModelNode.class);
        	if (adapter != null) {
        		if (element.equals(adapter)) {
        			return true;
        		}
            	IContainerModelNode parent = adapter.getParent();
            	while (parent != null && !element.equals(parent)) {
            		parent = parent.getParent();
            	}
            	if (parent != null && element.equals(parent)) {
            		return true;
            	}
        	}
        }

        return false;
    }

    /**
     * Filters out elements that are neither a parent nor a child of
     * a working set element.
     *
     * @see ViewerFilter#filter(Viewer, Object, Object[])
     */
    @Override
    public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
        Object[] result = null;
        if (workingSet != null) {
			cachedWorkingSet = workingSet.getElements();
		}
        try {
            result = super.filter(viewer, parent, elements);
        } finally {
            cachedWorkingSet = null;
        }
        return result;
    }
}
