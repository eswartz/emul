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

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.te.runtime.interfaces.workingsets.IWorkingSetElement;
import org.eclipse.tm.te.ui.views.interfaces.workingsets.IWorkingSetNameIDs;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * A WorkingSetElementHolder is a place holder for an {@link IWorkingSetElement}. It contains the
 * working set element's id and its working set name that it belongs to. A WorkingSetElementHolder
 * can only belong to one working set, while multiple WorkingSetElementHolder which have a same
 * working set element's id. That means a working set element can be added to multiple working sets
 * at the same time.
 * <p>
 * A WorkingSetElementHolder instance can be persisted and restored by an element factory.
 */
public final class WorkingSetElementHolder extends PlatformObject implements IPersistableElement {
	// The working set element's id.
	private String elementId;
	// The working set's name that it belongs to.
	private String wsName;
	// The working set element
	private IWorkingSetElement element;

	/**
	 * Create an empty holder.
	 */
	public WorkingSetElementHolder() {
	}

	/**
	 * Create an holder with a specified working set element's id and and a working set name.
	 *
	 * @param wsName The working set name.
	 * @param elementId The working set element's id.
	 */
	public WorkingSetElementHolder(String wsName, String elementId) {
		this.elementId = elementId;
		this.wsName = wsName;
	}

	/**
	 * Set the working set element's id.
	 *
	 * @param id The working set element's id.
	 */
	public void setElementId(String id) {
		this.elementId = id;
	}

	/**
	 * Get the working set element's id.
	 *
	 * @return The working set element's id.
	 */
	public String getElementId() {
		return elementId;
	}

	/**
	 * Set the working set's name.
	 *
	 * @param name The working set name.
	 */
	public void setWorkingSetName(String name) {
		this.wsName = name;
	}

	/**
	 * Get the working set's name.
	 *
	 * @return The working set's name.
	 */
	public String getWorkingSetName() {
		return wsName;
	}

	/**
	 * Set the working set element.
	 *
	 * @param element The working set element.
	 */
	public void setElement(IWorkingSetElement element) {
		this.element = element;
	}

	/**
	 * Get the working set element.
	 *
	 * @return The working set element.
	 */
	public IWorkingSetElement getElement() {
		return element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	@Override
	public String getFactoryId() {
		return "org.eclipse.tm.te.ui.views.workingsets.WorkingSetElementHolderFactory"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
    public void saveState(IMemento memento) {
		memento.putString(IWorkingSetNameIDs.FACTORY_ID, getFactoryId());
		memento.putString(IWorkingSetNameIDs.ATTR_ELEMENTID, elementId);
		memento.putString(IWorkingSetNameIDs.ATTR_WORKINGSET_NAME, wsName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
    public Object getAdapter(Class adapter) {
		if (IPersistableElement.class.equals(adapter)) {
			return this;
		}
		if (IWorkingSetElement.class.equals(adapter)) {
			return element;
		}
		return super.getAdapter(adapter);
	}
}
