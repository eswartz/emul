/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.te.runtime.events.EventManager;
import org.eclipse.tm.te.ui.views.events.ViewerContentChangeEvent;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Target Explorer common viewer implementation.
 */
public class ViewViewer extends CommonViewer {

	/**
	 * Constructor.
	 *
	 * @param viewerId
	 *            An id tied to the extensions that is used to focus specific
	 *            content to a particular instance of the Common Navigator
	 * @param parent
	 *            A Composite parent to contain the actual SWT widget
	 * @param style
	 *            A style mask that will be used to create the TreeViewer
	 *            Composite.
	 */
	public ViewViewer(String viewerId, Composite parent, int style) {
		super(viewerId, parent, style);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonViewer#add(java.lang.Object, java.lang.Object[])
	 */
	@Override
	public void add(Object parentElement, Object[] childElements) {
	    super.add(parentElement, childElements);

	    ViewerContentChangeEvent event = new ViewerContentChangeEvent(this, ViewerContentChangeEvent.ADD);
	    EventManager.getInstance().fireEvent(event);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonViewer#remove(java.lang.Object[])
	 */
	@Override
	public void remove(Object[] elements) {
	    super.remove(elements);

	    ViewerContentChangeEvent event = new ViewerContentChangeEvent(this, ViewerContentChangeEvent.REMOVE);
	    EventManager.getInstance().fireEvent(event);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#remove(java.lang.Object, java.lang.Object[])
	 */
	@Override
	public void remove(Object parent, Object[] elements) {
	    super.remove(parent, elements);

	    ViewerContentChangeEvent event = new ViewerContentChangeEvent(this, ViewerContentChangeEvent.REMOVE);
	    EventManager.getInstance().fireEvent(event);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonViewer#refresh(java.lang.Object)
	 */
	@Override
	public void refresh(Object element) {
	    super.refresh(element);

	    ViewerContentChangeEvent event = new ViewerContentChangeEvent(this, ViewerContentChangeEvent.REFRESH);
	    EventManager.getInstance().fireEvent(event);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonViewer#refresh(java.lang.Object, boolean)
	 */
	@Override
	public void refresh(Object element, boolean updateLabels) {
	    super.refresh(element, updateLabels);

	    ViewerContentChangeEvent event = new ViewerContentChangeEvent(this, ViewerContentChangeEvent.REFRESH);
	    EventManager.getInstance().fireEvent(event);
	}
}
