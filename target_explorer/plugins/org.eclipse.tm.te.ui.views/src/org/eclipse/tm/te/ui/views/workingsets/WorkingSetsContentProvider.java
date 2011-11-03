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

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm.te.ui.views.interfaces.IUIConstants;
import org.eclipse.ui.IAggregateWorkingSet;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;

/**
 * Provides children and parents for IWorkingSets.
 * <p>
 * Copied and adapted from <code>org.eclipse.ui.internal.navigator.workingsets.WorkingSetContentProvider</code>.
 */
@SuppressWarnings("restriction")
public class WorkingSetsContentProvider implements ICommonContentProvider {

	/**
	 * The extension id for the WorkingSet extension.
	 */
	public static final String EXTENSION_ID = "org.eclipse.tm.te.ui.views.navigator.workingSets"; //$NON-NLS-1$

	/**
	 * A key used by the Extension State Model to keep track of whether top level Working Sets or
	 * Projects should be shown in the viewer.
	 */
	public static final String SHOW_TOP_LEVEL_WORKING_SETS = EXTENSION_ID + ".showTopLevelWorkingSets"; //$NON-NLS-1$

	private static final Object[] NO_CHILDREN = new Object[0];

	private WorkingSetHelper helper;
	/* default */ IAggregateWorkingSet workingSetRoot;
	private IExtensionStateModel extensionStateModel;
	private CommonNavigator targetExplorer;
	private CommonViewer viewer;

	private IPropertyChangeListener rootModeListener = new IPropertyChangeListener() {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (SHOW_TOP_LEVEL_WORKING_SETS.equals(event.getProperty())) {
				updateRootMode();
			}
		}

	};

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */
	@Override
	public void init(ICommonContentExtensionSite config) {
		NavigatorContentService cs = (NavigatorContentService) config.getService();
		viewer = (CommonViewer) cs.getViewer();
		targetExplorer = viewer.getCommonNavigator();

		extensionStateModel = config.getExtensionStateModel();
		extensionStateModel.addPropertyChangeListener(rootModeListener);
		updateRootMode();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void restoreState(IMemento aMemento) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento aMemento) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IWorkingSet) {
			IWorkingSet workingSet = (IWorkingSet) parentElement;
			if (workingSet.isAggregateWorkingSet() && targetExplorer != null) {
				switch (targetExplorer.getRootMode()) {
				case IUIConstants.MODE_WORKING_SETS:
					return ((IAggregateWorkingSet) workingSet).getComponents();
				case IUIConstants.MODE_NORMAL:
					return getWorkingSetElements(workingSet);
				}
			}
			return getWorkingSetElements(workingSet);
		}
		return NO_CHILDREN;
	}

	/* default */IAdaptable[] getWorkingSetElements(IWorkingSet workingSet) {
		return workingSet.getElements();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (helper != null) return helper.getParent(element);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		helper = null;
		extensionStateModel.removePropertyChangeListener(rootModeListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof IWorkingSet) {
			IWorkingSet rootSet = (IWorkingSet) newInput;
			helper = new WorkingSetHelper(rootSet);
		}
	}

	private void updateRootMode() {
		if (targetExplorer == null) {
			return;
		}
		if (extensionStateModel.getBooleanProperty(SHOW_TOP_LEVEL_WORKING_SETS)) {
			targetExplorer.setRootMode(IUIConstants.MODE_WORKING_SETS);
		}
		else {
			targetExplorer.setRootMode(IUIConstants.MODE_NORMAL);
		}
	}

	protected class WorkingSetHelper {

		private final IWorkingSet workingSet;
		private final Map<Object, Object> parents = new WeakHashMap<Object, Object>();

		/**
		 * Create a Helper class for the given working set
		 *
		 * @param set The set to use to build the item to parent map.
		 */
		public WorkingSetHelper(IWorkingSet set) {
			workingSet = set;

			if (workingSet.isAggregateWorkingSet()) {
				IAggregateWorkingSet aggregateSet = (IAggregateWorkingSet) workingSet;
				if (workingSetRoot == null) workingSetRoot = aggregateSet;

				IWorkingSet[] components = aggregateSet.getComponents();

				for (int componentIndex = 0; componentIndex < components.length; componentIndex++) {
					IAdaptable[] elements = getWorkingSetElements(components[componentIndex]);
					for (int elementsIndex = 0; elementsIndex < elements.length; elementsIndex++) {
						parents.put(elements[elementsIndex], components[componentIndex]);
					}
					parents.put(components[componentIndex], aggregateSet);

				}
			}
			else {
				IAdaptable[] elements = getWorkingSetElements(workingSet);
				for (int elementsIndex = 0; elementsIndex < elements.length; elementsIndex++) {
					parents.put(elements[elementsIndex], workingSet);
				}
			}
		}

		/**
		 *
		 * @param element An element from the viewer
		 * @return The parent associated with the element, if any.
		 */
		public Object getParent(Object element) {
			if (element instanceof IWorkingSet && element != workingSetRoot) return workingSetRoot;
			return parents.get(element);
		}
	}

}
