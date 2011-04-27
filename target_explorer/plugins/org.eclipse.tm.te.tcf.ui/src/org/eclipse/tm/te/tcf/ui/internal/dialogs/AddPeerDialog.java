/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.te.tcf.ui.internal.help.IContextHelpIds;
import org.eclipse.tm.te.tcf.ui.internal.nls.Messages;
import org.eclipse.tm.te.ui.dialogs.CustomTrayDialog;


/**
 * Target Explorer: Add peer dialog implementation.
 */
public class AddPeerDialog extends CustomTrayDialog {
	private Combo fTransportTypeControl;
	private Text fAddressControl;
	private Text fPortControl;
	private Text fPeerIdControl;
	private Text fPeerNameControl;

	private Map<String, String> fPeerAttributes = null;

	/**
	 * Constructor.
	 *
	 * @param parent The parent shell used to view the dialog.
	 */
	public AddPeerDialog(Shell parent) {
		this(parent, IContextHelpIds.ADD_PEER_DIALOG);
	}

	/**
	 * Constructor.
	 *
	 * @param parent The parent shell used to view the dialog.
	 * @param contextHelpId The dialog context help id or <code>null</code>.
	 */
	public AddPeerDialog(Shell parent, String contextHelpId) {
		super(parent, contextHelpId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ide.target.ui.dialogs.WRUnifiedTitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);

		configureTitles();

		Composite panel = new Composite(composite, SWT.NONE);
		panel.setLayout(new GridLayout(2, false));
		panel.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Add the controls
		Label label = new Label(panel, SWT.NONE);
		label.setText(Messages.TransportTypeControl_label);

		fTransportTypeControl = new Combo(panel, SWT.READ_ONLY);
		fTransportTypeControl.setItems(new String[] { "TCP" }); //$NON-NLS-1$
		fTransportTypeControl.select(0);
		fTransportTypeControl.setEnabled(false);
		fTransportTypeControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.AgentHostControl_label);

		fAddressControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		fAddressControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fAddressControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updatePeerId();
			}
		});

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.AgentPortControl_label);

		fPortControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		fPortControl.setText("1534"); //$NON-NLS-1$
		fPortControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fPortControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updatePeerId();
			}
		});

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.PeerIdControl_label);

		fPeerIdControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		fPeerIdControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.PeerNameControl_label);

		fPeerNameControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		fPeerNameControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Setup the control content
		setupContent();

		// Adjust the font
		applyDialogFont(composite);

		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		validateDialog();
		return control;
	}

	/**
	 * Configure the dialog title and the title area content. The method
	 * is called from {@link #createDialogArea(Composite)}.
	 */
	protected void configureTitles() {
		setDialogTitle(Messages.AddPeerDialog_title);
	}

	/**
	 * Setup the control content.
	 */
	protected void setupContent() {
		restoreWidgetValues();
		updatePeerId();
	}

	/**
	 * Update peer id control.
	 */
	protected void updatePeerId() {
		String address = fAddressControl.getText();
		String port = fPortControl.getText();
		String type = fTransportTypeControl.getText();

		if (!"".equals(address) && !"".equals(port)) { //$NON-NLS-1$ //$NON-NLS-2$
			fPeerIdControl.setText(type + ":" //$NON-NLS-1$
			                        	+ address + ":" //$NON-NLS-1$
			                        	+ port);
		} else {
			fPeerIdControl.setText(""); //$NON-NLS-1$
		}

		validateDialog();
	}

	/**
	 * Validates the dialog.
	 */
	protected void validateDialog() {
		boolean valid = true;

		if ("".equals(fAddressControl.getText()) || "".equals(fPortControl)) { //$NON-NLS-1$ //$NON-NLS-2$
			valid = false;
		}

		if (getButton(IDialogConstants.OK_ID) != null)
			getButton(IDialogConstants.OK_ID).setEnabled(valid);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		// Dispose the peer attributes
		fPeerAttributes = null;

		super.cancelPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ide.target.ui.dialogs.WRUnifiedTitleAreaDialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		saveWidgetValues();

		// Create a new peer attributes map
		fPeerAttributes = new HashMap<String, String>();
		// Update with the current control content
		updatePeerAttributes(fPeerAttributes);

		super.okPressed();
	}

	/**
	 * Updates the given attributes map with the current control content.
	 */
	protected void updatePeerAttributes(Map<String, String> peerAttributes) {
		assert peerAttributes != null;

		peerAttributes.put(IPeer.ATTR_IP_HOST, fAddressControl.getText());
		peerAttributes.put(IPeer.ATTR_IP_PORT, fPortControl.getText());
		peerAttributes.put(IPeer.ATTR_ID, fPeerIdControl.getText());
		peerAttributes.put(IPeer.ATTR_NAME, fPeerNameControl.getText());
		peerAttributes.put(IPeer.ATTR_TRANSPORT_NAME, fTransportTypeControl.getText());
	}

	/**
	 * Returns the peer attributes.
	 *
	 * @return The peer attributes or <code>null</code> if canceled.
	 */
	public final Map<String, String> getPeerAttributes() {
		return fPeerAttributes;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ide.target.ui.dialogs.WRUnifiedTitleAreaDialog#saveWidgetValues()
	 */
	protected void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			// The transport type control is not saved
			settings.put(IPeer.ATTR_IP_HOST, fAddressControl.getText());
			settings.put(IPeer.ATTR_IP_PORT, fPortControl.getText());
			settings.put(IPeer.ATTR_ID, fPeerIdControl.getText());
			settings.put(IPeer.ATTR_NAME, fPeerNameControl.getText());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.ide.target.ui.dialogs.WRUnifiedTitleAreaDialog#restoreWidgetValues()
	 */
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			// The transport type control is not restored
			if (settings.get(IPeer.ATTR_IP_HOST) != null) fAddressControl.setText(settings.get(IPeer.ATTR_IP_HOST));
			if (settings.get(IPeer.ATTR_IP_PORT) != null) fPortControl.setText(settings.get(IPeer.ATTR_IP_PORT));
			if (settings.get(IPeer.ATTR_ID) != null) fPeerIdControl.setText(settings.get(IPeer.ATTR_ID));
			if (settings.get(IPeer.ATTR_NAME) != null) fPeerNameControl.setText(settings.get(IPeer.ATTR_NAME));
		}
	}
}
