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
import org.eclipse.tm.te.ui.views.activator.UIPlugin;
import org.eclipse.tm.te.ui.views.interfaces.ImageConsts;
import org.eclipse.tm.te.ui.views.nls.Messages;
import org.eclipse.ui.IWorkingSet;

/**
 * A target working set page is a wizard page used to configure a custom defined
 * working set. This wizard is used in the configure working set action to edit
 * the working sets used in the working set viewer.
 *
 */
public class TargetWorkingSetPage extends AbstractWorkingSetWizardPage {

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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ui.internal.workingsets.AbstractWorkingSetWizardPage#getPageId()
	 */
	@Override
	protected String getPageId() {
		return "org.eclipse.tm.te.tcf.ui.TargetWorkingSetPage"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ui.internal.workingsets.AbstractWorkingSetWizardPage#configureTree(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected void configureTree(TreeViewer tree) {
//		tree.setContentProvider(new PeerContentProvider());
//		tree.setLabelProvider(new LabelProvider());
//		tree.setInput(Model.getModel());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ui.internal.workingsets.AbstractWorkingSetWizardPage#configureTable(org.eclipse.jface.viewers.TableViewer)
	 */
	@Override
	protected void configureTable(TableViewer table) {
//		table.setLabelProvider(new LabelProvider());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ui.internal.workingsets.AbstractWorkingSetWizardPage#getInitialWorkingSetElements(org.eclipse.ui.IWorkingSet)
	 */
	@Override
	protected Object[] getInitialWorkingSetElements(IWorkingSet workingSet) {
		Object[] elements = new Object[0];
		if (workingSet == null) {
			if (initialSelection == null)
				return new IAdaptable[0];
			elements = initialSelection.toArray();
		} else {
//			List<IPeerModel> result = new ArrayList<IPeerModel>();
//			elements = workingSet.getElements();
//			for (int i = 0; i < elements.length; i++) {
//				PeerHolder holder = (PeerHolder) elements[i];
//				IPeerModel peer = holder.getPeerModel();
//				if (peer != null)
//					result.add(peer);
//			}
//			elements = result.toArray();
		}
		return elements;
	}
}
