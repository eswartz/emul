/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.editor.pages;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.te.tcf.ui.internal.help.IContextHelpIds;
import org.eclipse.tm.te.tcf.ui.tables.NodePropertiesContentProvider;
import org.eclipse.tm.te.tcf.ui.tables.NodePropertiesLabelProvider;
import org.eclipse.tm.te.tcf.ui.tables.NodePropertiesViewerComparator;
import org.eclipse.tm.te.ui.forms.CustomFormToolkit;
import org.eclipse.tm.te.ui.nls.Messages;
import org.eclipse.tm.te.ui.tables.properties.NodePropertiesTableControl;
import org.eclipse.tm.te.ui.views.editor.AbstractCustomFormToolkitEditorPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;


/**
 * Target Explorer: TCF node properties details editor page implementation.
 */
public class NodePropertiesEditorPage extends AbstractCustomFormToolkitEditorPage {
	// The references to the pages subcontrol's (needed for disposal)
	private NodePropertiesTableControl nodePropertiesTableControl;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#dispose()
	 */
	@Override
	public void dispose() {
		if (nodePropertiesTableControl != null) { nodePropertiesTableControl.dispose(); nodePropertiesTableControl = null; }
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		// Configure the managed form
		configureManagedForm(managedForm);

		// Do create the content of the form now
		doCreateFormContent(managedForm.getForm().getBody(), getFormToolkit());

		// Re-arrange the controls
		managedForm.reflow(true);
	}

	/**
	 * Configure the managed form to be ready for usage.
	 *
	 * @param managedForm The managed form. Must not be <code>null</code>.
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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(managedForm.getForm(), IContextHelpIds.NODE_PROPERTIES_EDITOR_PAGE);
	}

	/**
	 * Do create the managed form content.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>
	 * @param toolkit The {@link CustomFormToolkit} instance. Must not be <code>null</code>.
	 */
	protected void doCreateFormContent(Composite parent, CustomFormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(toolkit);

		Section section = toolkit.getFormToolkit().createSection(parent, ExpandableComposite.TITLE_BAR);
		String title = NLS.bind(Messages.NodePropertiesTableControl_section_title, Messages.NodePropertiesTableControl_section_title_noSelection);
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

		// Setup the node properties table  control
		nodePropertiesTableControl = doCreateNodePropertiesTableControl();
		Assert.isNotNull(nodePropertiesTableControl);
		nodePropertiesTableControl.setupFormPanel((Composite)section.getClient(), toolkit);

		// Set the initial input
		nodePropertiesTableControl.getViewer().setInput(getEditorInputNode());
	}

	/**
	 * Creates and returns a new node properties table control.
	 *
	 * @return The new node properties table control.
	 */
	protected NodePropertiesTableControl doCreateNodePropertiesTableControl() {
		return new NodePropertiesTableControl(this) {
			/* (non-Javadoc)
			 * @see org.eclipse.tm.te.tcf.vtl.ui.datasource.controls.tables.NodePropertiesTableControl#doCreateTableViewerContentProvider(org.eclipse.jface.viewers.TableViewer)
			 */
			@Override
			protected IStructuredContentProvider doCreateTableViewerContentProvider(TableViewer viewer) {
				return new NodePropertiesContentProvider(true);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.tm.te.tcf.vtl.ui.datasource.controls.tables.properties.NodePropertiesTableControl#doCreateTableViewerLabelProvider(org.eclipse.jface.viewers.TableViewer)
			 */
			@Override
			protected ITableLabelProvider doCreateTableViewerLabelProvider(TableViewer viewer) {
				return new NodePropertiesLabelProvider(viewer);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.tm.te.tcf.vtl.ui.datasource.controls.tables.NodePropertiesTableControl#doCreateTableViewerComparator(org.eclipse.jface.viewers.TableViewer)
			 */
			@Override
			protected ViewerComparator doCreateTableViewerComparator(TableViewer viewer) {
				return new NodePropertiesViewerComparator(viewer, (ITableLabelProvider)viewer.getLabelProvider());
			}
		};
	}

	/**
	 * Returns the associated node properties table control.
	 *
	 * @return The associated node properties table control or <code>null</code>.
	 */
	protected final NodePropertiesTableControl getNodePropertiesTableControl() {
		return nodePropertiesTableControl;
	}
}
