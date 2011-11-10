/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tm.te.tcf.ui.internal.help.IContextHelpIds;
import org.eclipse.tm.te.tcf.ui.internal.model.Model;
import org.eclipse.tm.te.tcf.ui.internal.navigator.LabelProviderDelegate;
import org.eclipse.tm.te.tcf.ui.nls.Messages;
import org.eclipse.tm.te.ui.jface.dialogs.CustomTitleAreaDialog;
import org.eclipse.tm.te.ui.swt.SWTControlUtil;

/**
 * TCF agent selection dialog implementation.
 */
public class AgentSelectionDialog extends CustomTitleAreaDialog {
	// The list of remote services the agents must provide to be included
	/* default */ final String[] services;

	// The table viewer
	private TableViewer viewer;

	// The selection. Will be filled in if either "OK" or "Cancel" is pressed
	private ISelection selection;

	/**
	 * Constructor.
	 *
	 * @param services The list of (remote) services the agents must provide to be selectable, or <code>null</code>.
	 */
	public AgentSelectionDialog(String[] services) {
		this(null, services);
	}

	/**
	 * Constructor.
	 *
	 * @param parent The parent shell used to view the dialog, or <code>null</code>.
	 * @param services The list of (remote) services the agents must provide to be selectable, or <code>null</code>.
	 */
	public AgentSelectionDialog(Shell parent, String[] services) {
		super(parent, IContextHelpIds.AGENT_SELECTION_DIALOG);

		this.services = services != null && services.length > 0 ? services : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	@Override
	protected boolean isResizable() {
	    return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
	    Composite top = (Composite)super.createDialogArea(parent);

		setDialogTitle(getDialogTitle());
		setTitle(getTitle());
		setDefaultMessage(getDefaultMessage(), IMessageProvider.NONE);

	    // Create the table viewer
	    viewer = new TableViewer(top, SWT.MULTI | SWT.BORDER);

	    // Configure the table
	    Table table = viewer.getTable();

		@SuppressWarnings("unused")
        TableColumn column = new TableColumn(table, SWT.LEFT);

		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(100));
		table.setLayout(tableLayout);

	    GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
	    table.setLayoutData(layoutData);

	    viewer.setContentProvider(new ArrayContentProvider());
	    LabelProviderDelegate labelProvider = new LabelProviderDelegate() {
	    	/* (non-Javadoc)
	    	 * @see org.eclipse.tm.te.tcf.ui.internal.navigator.LabelProviderDelegate#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
	    	 */
	    	@Override
	    	public Image decorateImage(Image image, Object element) {
	    	    return image;
	    	}
	    };
	    viewer.setLabelProvider(new DecoratingLabelProvider(labelProvider, labelProvider));

	    // The content to show is static. Do the filtering manually so that
	    // we can disable the OK Button if the dialog would not show any content.
	    List<IPeerModel> nodes = new ArrayList<IPeerModel>(Arrays.asList(Model.getModel().getPeers()));
	    Iterator<IPeerModel> iterator = nodes.iterator();
	    while (iterator.hasNext()) {
	    	IPeerModel peerModel = iterator.next();
	    	if (isFiltered(peerModel)) iterator.remove();
	    }

	    // Set the remaining nodes as input
	    viewer.setInput(nodes.toArray(new IPeerModel[nodes.size()]));

	    return top;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
	    Control buttonBar = super.createButtonBar(parent);

	    // Adjust the OK button enablement
	    Button okButton = getButton(IDialogConstants.OK_ID);
	    SWTControlUtil.setEnabled(okButton, ((IPeerModel[])viewer.getInput()).length > 0);

	    return buttonBar;
	}
	/**
	 * Returns the dialog title.
	 *
	 * @return The dialog title.
	 */
	protected String getDialogTitle() {
		return Messages.AgentSelectionDialog_dialogTitle;
	}

	/**
	 * Returns the title.
	 *
	 * @return The title.
	 */
	protected String getTitle() {
		return Messages.AgentSelectionDialog_title;
	}

	/**
	 * Returns the default message.
	 *
	 * @return The default message.
	 */
	protected String getDefaultMessage() {
		return Messages.AgentSelectionDialog_message;
	}

	/**
	 * Returns if the given peer model node is filtered or not.
	 *
	 * @param peerModel The peer model node. Must not be <code>null</code>.
	 * @return
	 */
	protected boolean isFiltered(final IPeerModel peerModel) {
		Assert.isNotNull(peerModel);

		final AtomicReference<String> remoteServices = new AtomicReference<String>();
		Protocol.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				remoteServices.set(peerModel.getStringProperty(IPeerModelProperties.PROP_REMOTE_SERVICES));
			}
		});

		boolean matchedService = false;
		if (remoteServices.get() != null) {
			for (String service : services) {
				if (remoteServices.get().contains(service)) {
					matchedService = true;
					break;
				}
			}
		}

		return !matchedService;
	}

	/**
	 * Returns the selection which had been set to the viewer at
	 * the time of closing the dialog with either "OK" or "Cancel".
	 *
	 * @return The selection or <code>null</code>.
	 */
	public ISelection getSelection() {
		return selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTitleAreaDialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		selection = viewer.getSelection();
	    super.okPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		selection = null;
	    super.cancelPressed();
	}
}
