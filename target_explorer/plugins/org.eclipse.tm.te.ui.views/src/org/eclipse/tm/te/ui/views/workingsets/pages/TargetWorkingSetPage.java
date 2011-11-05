/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		William Chen (Wind River)	[354578] Add support for working sets
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.workingsets.pages;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tm.te.runtime.interfaces.workingsets.IWorkingSetElement;
import org.eclipse.tm.te.ui.views.activator.UIPlugin;
import org.eclipse.tm.te.ui.views.interfaces.IRoot;
import org.eclipse.tm.te.ui.views.interfaces.IUIConstants;
import org.eclipse.tm.te.ui.views.interfaces.ImageConsts;
import org.eclipse.tm.te.ui.views.internal.View;
import org.eclipse.tm.te.ui.views.nls.Messages;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.NavigatorContentServiceContentProvider;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * A target working set page is a wizard page used to configure a custom defined
 * working set. This wizard is used in the configure working set action to edit
 * the working sets used in the working set viewer.
 *
 */
@SuppressWarnings("restriction")
public class TargetWorkingSetPage extends AbstractWorkingSetWizardPage {
	// The common navigator content service
	private INavigatorContentService contentService;
	// The root node
	private IRoot root;
	// The initial selection
	private IStructuredSelection initialSelection;

	/**
	 * Default constructor.
	 */
	public TargetWorkingSetPage() {
		super("targetWorkingSetPage", Messages.TargetWorkingSetPage_title, UIPlugin.getImageDescriptor(ImageConsts.WORKING_SET)); //$NON-NLS-1$
		setDescription(Messages.TargetWorkingSetPage_workingSet_description);
	}

	/**
	 * Set the initial selection.
	 * @param selection The initial selection
	 */
	public void setInitialSelection(IStructuredSelection selection) {
		initialSelection = selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ui.internal.workingsets.AbstractWorkingSetWizardPage#getPageId()
	 */
	@Override
	protected String getPageId() {
		return "org.eclipse.tm.te.tcf.ui.TargetWorkingSetPage"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		if (contentService != null) { contentService.dispose(); contentService = null; }
		root = null;
	    super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ui.internal.workingsets.AbstractWorkingSetWizardPage#configureTree(org.eclipse.jface.viewers.TreeViewer)
	 */
    @Override
	protected void configureTree(TreeViewer tree) {
		// Construct and associate the navigator content service.
		// We have to simulate the common viewer here to get the content right.
		contentService = new NavigatorContentService(IUIConstants.ID_EXPLORER, tree);

		tree.setContentProvider(new NavigatorContentServiceContentProvider((NavigatorContentService)contentService) {
			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.navigator.NavigatorContentServiceContentProvider#hasChildren(java.lang.Object)
			 */
			@Override
			public boolean hasChildren(Object anElementOrPath) {
			    return false;
			}
		});
		tree.setLabelProvider(contentService.createCommonLabelProvider());

		// Filter out everything not implementing IWorkingSetElement
		tree.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return element instanceof IWorkingSetElement;
			}
		});

		// Create the root node
		root = new View.Root();
		tree.setInput(root);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ui.internal.workingsets.AbstractWorkingSetWizardPage#configureTable(org.eclipse.jface.viewers.TableViewer)
	 */
	@Override
	protected void configureTable(TableViewer table) {
		table.setLabelProvider(contentService.createCommonLabelProvider());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ui.internal.workingsets.AbstractWorkingSetWizardPage#getInitialWorkingSetElements(org.eclipse.ui.IWorkingSet)
	 */
	@Override
	protected Object[] getInitialWorkingSetElements(IWorkingSet workingSet) {
		Object[] elements;
		if (workingSet == null) {
			if (initialSelection == null)
				return new IAdaptable[0];

			elements= initialSelection.toArray();
		} else {
			elements= workingSet.getElements();
		}
		return elements;
	}
}
