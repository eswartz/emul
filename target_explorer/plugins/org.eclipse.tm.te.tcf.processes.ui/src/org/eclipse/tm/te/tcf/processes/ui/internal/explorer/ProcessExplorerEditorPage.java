/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.ui.internal.explorer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.te.tcf.processes.ui.controls.ProcessesTreeControl;
import org.eclipse.tm.te.tcf.processes.ui.internal.help.IContextHelpIds;
import org.eclipse.tm.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tm.te.ui.forms.CustomFormToolkit;
import org.eclipse.tm.te.ui.views.editor.AbstractEditorPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * Processes editor page implementation.
 */
public class ProcessExplorerEditorPage extends AbstractEditorPage {
	// The references to the pages subcontrol's (needed for disposal)
	private ProcessesTreeControl treeControl;

	// Reference to the form toolkit instance
	private CustomFormToolkit toolkit = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#dispose()
	 */
	@Override
	public void dispose() {
		if (treeControl != null) { treeControl.dispose(); treeControl = null; }
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		// Configure the managed form
		configureManagedForm(managedForm);

		// Get the form body
		Composite body = managedForm.getForm().getBody();

		// Create the toolkit instance
		toolkit = new CustomFormToolkit(managedForm.getToolkit());

		// Do create the content of the form now
		doCreateFormContent(body, toolkit);

		// Re-arrange the controls
		managedForm.reflow(true);
	}

	/**
	 * Configure the managed form to be ready for usage.
	 *
	 * @param managedForm The managed form. Must be not <code>null</code>.
	 */
	protected void configureManagedForm(IManagedForm managedForm) {
		Assert.isNotNull(managedForm);

		// Configure main layout
		Composite body = managedForm.getForm().getBody();
		GridLayout layout = new GridLayout();
		layout.marginHeight = 2; layout.marginWidth = 0;
		body.setLayout(layout);
		body.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		// Set context help id
		PlatformUI.getWorkbench().getHelpSystem().setHelp(managedForm.getForm(), IContextHelpIds.PROCESS_EXPLORER_EDITOR_PAGE);
	}

	/**
	 * Do create the managed form content.
	 *
	 * @param parent The parent composite. Must be not <code>null</code>
	 * @param toolkit The {@link CustomFormToolkit} instance. Must be not <code>null</code>.
	 */
	protected void doCreateFormContent(Composite parent, CustomFormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(toolkit);

		Section section = toolkit.getFormToolkit().createSection(parent, ExpandableComposite.TITLE_BAR);
		String title = Messages.ProcessExplorerTreeControl_section_title;
		// Stretch to a length of 40 characters to make sure the title can be changed
		// to hold and show text up to this length
		while (title.length() < 40) {
			title += " "; //$NON-NLS-1$
		}
		// Set the title to the section
		section.setText(title);
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create the client area
		Composite client = toolkit.getFormToolkit().createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0; layout.marginHeight = 0;
		client.setLayout(layout);
		section.setClient(client);

		// Setup the tree control
		treeControl = doCreateTreeControl();
		Assert.isNotNull(treeControl);
		treeControl.setupFormPanel((Composite)section.getClient(), toolkit);

		// Set the initial input
		treeControl.getViewer().setInput(getEditorInputNode());
	}

	/**
	 * Creates and returns a tree control.
	 *
	 * @return The new tree control.
	 */
	protected ProcessesTreeControl doCreateTreeControl() {
		return new ProcessesTreeControl(this);
	}

	/**
	 * Returns the associated tree control.
	 *
	 * @return The associated tree control or <code>null</code>.
	 */
	protected final ProcessesTreeControl getTreeControl() {
		return treeControl;
	}
}
