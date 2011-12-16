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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tm.te.runtime.interfaces.workingsets.IWorkingSetElement;
import org.eclipse.tm.te.ui.trees.TreeArrayContentProvider;
import org.eclipse.tm.te.ui.views.ViewsUtil;
import org.eclipse.tm.te.ui.views.activator.UIPlugin;
import org.eclipse.tm.te.ui.views.interfaces.IUIConstants;
import org.eclipse.tm.te.ui.views.interfaces.ImageConsts;
import org.eclipse.tm.te.ui.views.internal.ViewRoot;
import org.eclipse.tm.te.ui.views.nls.Messages;
import org.eclipse.tm.te.ui.views.workingsets.WorkingSetElementHolder;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * A target working set page is a wizard page used to configure a custom defined
 * working set. This wizard is used in the configure working set action to edit
 * the working sets used in the working set viewer.
 */
public class TargetWorkingSetPage extends AbstractWorkingSetWizardPage {
	// The target explorer view content service (Never dispose it in here!)
	private INavigatorContentService contentService;
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
	 * @see org.eclipse.tm.te.tcf.ui.internal.workingsets.AbstractWorkingSetWizardPage#configureTree(org.eclipse.jface.viewers.TreeViewer)
	 */
    @Override
	protected void configureTree(TreeViewer tree) {
    	// Get the content service from the Target Explorer view.
    	IWorkbenchPart part = ViewsUtil.getPart(IUIConstants.ID_EXPLORER);
    	if (part instanceof CommonNavigator) {
    		contentService = ((CommonNavigator)part).getNavigatorContentService();

    		tree.setContentProvider(TreeArrayContentProvider.getInstance());
    		tree.setLabelProvider(contentService.createCommonLabelProvider());

    		// Filter out everything not implementing IWorkingSetElement
    		tree.addFilter(new ViewerFilter() {
    			@Override
    			public boolean select(Viewer viewer, Object parentElement, Object element) {
    				return element instanceof IWorkingSetElement;
    			}
    		});

    		// Initialize the tree input
    		tree.setInput(contentService.createCommonContentProvider().getElements(ViewRoot.getInstance()));
    	}
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
			elements = initialSelection.toArray();
		} else {
			List<IWorkingSetElement> result = new ArrayList<IWorkingSetElement>();
			for (IAdaptable adaptable : workingSet.getElements()) {
				if (!(adaptable instanceof WorkingSetElementHolder)) continue;
				WorkingSetElementHolder holder = (WorkingSetElementHolder) adaptable;
				Assert.isNotNull(holder);
				IWorkingSetElement element = holder.getElement();
				// If the element is null, try to look up the element through the content provider
				if (element == null) {
					ITreeContentProvider contentProvider = (ITreeContentProvider)tree.getContentProvider();
					for (Object candidate : contentProvider.getElements(tree.getInput())) {
						if (candidate instanceof IWorkingSetElement && ((IWorkingSetElement)candidate).getElementId().equals(holder.getElementId())) {
							holder.setElement((IWorkingSetElement)candidate);
							element = holder.getElement();
							break;
						}
					}
				}

				if (element != null) result.add(element);
			}
			elements = result.toArray();
		}
		return elements;
	}
}
