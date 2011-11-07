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
import java.util.EventObject;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.tm.te.runtime.events.EventManager;
import org.eclipse.tm.te.ui.events.AbstractEventListener;
import org.eclipse.tm.te.ui.views.ViewsUtil;
import org.eclipse.tm.te.ui.views.events.ViewerContentChangeEvent;
import org.eclipse.tm.te.ui.views.interfaces.IUIConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetUpdater;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Working set element updater implementation.
 */
public class WorkingSetElementUpdater extends AbstractEventListener implements IWorkingSetUpdater, IExecutableExtension {
	// List of working sets managed by this updater
	private final List<IWorkingSet> workingSets = new ArrayList<IWorkingSet>();
	// The common viewer
	private CommonViewer viewer = null;

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		// Get the target explorer view instance
		IWorkbenchPart part = ViewsUtil.getPart(IUIConstants.ID_EXPLORER);
		if (part instanceof CommonNavigator) viewer = ((CommonNavigator)part).getCommonViewer();

		// Register ourself as ViewContentChangeEvent listener
		EventManager.getInstance().addEventListener(this, ViewerContentChangeEvent.class, viewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkingSetUpdater#add(org.eclipse.ui.IWorkingSet)
	 */
	@Override
	public void add(IWorkingSet workingSet) {
		synchronized (workingSets) {
			workingSets.add(workingSet);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkingSetUpdater#remove(org.eclipse.ui.IWorkingSet)
	 */
	@Override
	public boolean remove(IWorkingSet workingSet) {
		synchronized (workingSets) {
			return workingSets.remove(workingSet);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkingSetUpdater#contains(org.eclipse.ui.IWorkingSet)
	 */
	@Override
	public boolean contains(IWorkingSet workingSet) {
		synchronized (workingSets) {
			return workingSets.contains(workingSet);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkingSetUpdater#dispose()
	 */
	@Override
	public void dispose() {
		synchronized (workingSets) {
			workingSets.clear();
        }

		// Remove ourself as event listener
		EventManager.getInstance().removeEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.events.IEventListener#eventFired(java.util.EventObject)
	 */
	@Override
	public void eventFired(EventObject event) {
		if (!(event instanceof ViewerContentChangeEvent) && viewer.equals(((ViewerContentChangeEvent)event).getSource())) {
			return;
		}
	}
}
