/*********************************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River)	- [345384]Provide property pages for remote file system nodes
 *********************************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.properties;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.te.tcf.filesystem.internal.handlers.StateManager;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The general information page of a file's properties dialog.
 */
public class GeneralInformationPage extends PropertyPage {
	// The formatter for the modified time and the accessed time.
	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
	// The formatter for the size of a file.
	private static final DecimalFormat SIZE_FORMAT = new DecimalFormat();
	// The original node.
	FSTreeNode node;
	// Cloned node for modification.
	FSTreeNode clone;
	// The button of "Read-Only"
	Button btnReadOnly;
	// The button of "Hidden"
	Button btnHidden;
	// The button of "Permissions"
	Button[] btnPermissions;
	/**
	 * Get the type of an FSTreeNode.
	 *
	 * @return "folder" if it is a directory, "file" if is a file, or else
	 *         defaults to node.type.
	 */
	protected String getNodeTypeLabel() {
		if (clone.isDirectory())
			return Messages.InformationPage_Folder;
		else if (clone.isFile())
			return Messages.InformationPage_File;
		else
			return clone.type;
	}

	/**
	 * Create a horizontal separator between field sections.
	 *
	 * @param parent
	 *            The parent composite of the separator.
	 */
	protected void createSeparator(Composite parent) {
		Label label = new Label(parent, SWT.SEPARATOR | SWT.SHADOW_ETCHED_IN | SWT.HORIZONTAL);
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
	}

	/**
	 * Create a field displaying the a specific value with a specific label.
	 *
	 * @param text
	 *            The label text for the field.
	 * @param value
	 *            The value to be displayed.
	 * @param parent
	 *            The parent composite of the field.
	 */
	protected void createField(String text, String value, Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		label.setLayoutData(data);
		label = new Label(parent, SWT.NONE);
		label.setText(value);
	}

	/**
	 * Get the string of the file size using using the formatter, SIZE_FORMAT.
	 *
	 * @param size
	 *            The size of the file to be formatted.
	 * @return The string in the format of SIZE_FORMAT.
	 */
	protected String getSizeText(long size) {
		return SIZE_FORMAT.format(size / 1024)
				+ " KB (" + SIZE_FORMAT.format(size) + " bytes)"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Get the string of the specific time using the formatter, DATE_FORMAT.
	 *
	 * @param time
	 *            The time to be formatted.
	 * @return The string in the format of DATE_FORMAT.
	 */
	protected String getDateText(long time) {
		return DATE_FORMAT.format(new Date(time));
	}

	/**
	 * Create the attributes section for a Windows file/folder.
	 *
	 * @param parent
	 *            The parent composite on which it is created.
	 */
	protected void createAttributesSection(Composite parent) {
		// Attributes
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.InformationPage_Attributes);
		GridData data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		label.setLayoutData(data);

		Composite attr = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, true);
		layout.marginHeight = 0;
		attr.setLayout(layout);
		// Read-only
		btnReadOnly = new Button(attr, SWT.CHECK);
		btnReadOnly.setText(Messages.InformationPage_ReadOnly);
		btnReadOnly.addSelectionListener(new SelectionAdapter(){
			@Override
            public void widgetSelected(SelectionEvent e) {
				if(btnReadOnly.getSelection()!=clone.isReadOnly()){
					clone.setReadOnly(btnReadOnly.getSelection());
				}
            }
		});
		// Hidden
		btnHidden = new Button(attr, SWT.CHECK);
		btnHidden.setText(Messages.InformationPage_Hidden);
		btnHidden.addSelectionListener(new SelectionAdapter(){
			@Override
            public void widgetSelected(SelectionEvent e) {
				Button btnHidden = (Button) e.getSource();
				if(btnHidden.getSelection()!=clone.isHidden()){
					clone.setHidden(btnHidden.getSelection());
				}
            }
		});
		// Advanced Attributes
		Button btnAdvanced = new Button(attr, SWT.PUSH);
		btnAdvanced.setText(Messages.InformationPage_Advanced);
		btnAdvanced.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showAdvancedAttributes();
			}
		});
		// Update the attribute values.
		updateAttributes();
	}

	/**
	 * Update the value of attributes section.
	 */
	private void updateAttributes() {
		btnReadOnly.setSelection(clone.isReadOnly());
		btnHidden.setSelection(clone.isHidden());
	}

	/**
	 * Show the advanced attributes dialog for the specified file/folder.
	 */
	void showAdvancedAttributes() {
		AdvancedAttributesDialog dialog = new AdvancedAttributesDialog(this.getShell(), (FSTreeNode)(clone.clone()));
		if (dialog.open() == Window.OK) {
			FSTreeNode result = dialog.getResult();
			clone.attr = result.attr;
		}
	}

	/**
	 * Create the permissions section for a Unix/Linux file/folder.
	 *
	 * @param parent
	 *            The parent composite on which it is created.
	 */
	protected void createPermissionsSection(Composite parent) {
		GridLayout gridLayout;
		Label label = new Label(parent, SWT.NONE);
		label.setText("Permissions:"); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		data.verticalAlignment = SWT.TOP;
		label.setLayoutData(data);
		Composite perms = new Composite(parent, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		perms.setLayout(gridLayout);
		btnPermissions = new Button[9];
		createPermissionGroup(perms, 0,
				Messages.PermissionsGroup_UserPermissions);
		createPermissionGroup(perms, 3,
				Messages.PermissionsGroup_GroupPermissions);
		createPermissionGroup(perms, 6,
				Messages.PermissionsGroup_OtherPermissions);
		// Update the permission values.
		updatePermissions();
	}

	/**
	 * Create a permission group for a role, such as a user, a group or others.
	 *
	 * @param parent
	 *            The parent composite.
	 * @param bit
	 *            The permission bit index.
	 * @param header
	 *            The group's header label.
	 */
	protected void createPermissionGroup(Composite parent, int bit, String header) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(header);
		GridData data = new GridData();
		data.horizontalAlignment = SWT.LEFT;
		label.setLayoutData(data);
		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, true);
		layout.marginHeight = 0;
		group.setLayout(layout);
		createPermissionButton(Messages.PermissionsGroup_Readable, bit, group);
		createPermissionButton(Messages.PermissionsGroup_Writable, bit + 1, group);
		createPermissionButton(Messages.PermissionsGroup_Executable, bit + 2, group);
	}

	/**
	 * Create a check-box field for a single permission item.
	 *
	 * @param label
	 *            The label of the permission.
	 * @param index
	 *            The index of current permission bit mask index.
	 * @param parent
	 *            The parent to hold the check-box field.
	 */
	private void createPermissionButton(String label, final int index, Composite parent) {
		btnPermissions[index] = new Button(parent, SWT.CHECK);
		btnPermissions[index].setText(label);
		btnPermissions[index].addSelectionListener(new SelectionAdapter(){
			@Override
            public void widgetSelected(SelectionEvent e) {
				int bit = 1 << (8 - index);
				boolean on = (clone.attr.permissions & bit) != 0;
				boolean newOn = btnPermissions[index].getSelection();
				if (newOn != on) {
					int permissions = clone.attr.permissions;
					permissions = newOn ? (permissions | bit) : (permissions & ~bit);
					clone.setPermissions(permissions);
				}
            }
		});
	}

	/**
	 * Update the value of permissions section.
	 */
	private void updatePermissions(){
		for (int i = 0; i < 9; i++) {
			final int bit = 1 << (8 - i);
			final boolean on = (clone.attr.permissions & bit) != 0;
			btnPermissions[i].setSelection(on);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
    protected void performDefaults() {
		clone = (FSTreeNode) node.clone();
		if (node.isWindowsNode()) {
			updateAttributes();
		}
		else {
			updatePermissions();
		}
	    super.performDefaults();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
    public boolean performOk() {
		if (hasAttrsChanged()) {
			final boolean[] success = new boolean[1];
			success[0] = true;
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void handleException(Throwable exception) {
					String errorMessage = NLS
					                .bind(Messages.GeneralInformationPage_PropertiesChangeFailure, new Object[] { node.name, exception
					                                .getLocalizedMessage() });
					MessageDialog.openError(getShell(), Messages.GeneralInformationPage_PropertiesChangeTitle, errorMessage);
					success[0] = false;
				}

				@Override
				public void run() throws Exception {
					StateManager.getInstance().setFileAttrs(node, clone.attr);
				}
			});
			return success[0];
		}
		return true;
    }

	/**
	 * If the attributes has been changed.
	 * @return If the attributes has been changed.
	 */
	private boolean hasAttrsChanged(){
		if(node.isWindowsNode()){
			// If it is a Windows file, only check its attributes.
			return node.getWin32Attrs() != clone.getWin32Attrs();
		}
		// If it is not a Windows file, only check its permissions.
		return node.attr.permissions != clone.attr.permissions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		IAdaptable element = getElement();
		Assert.isTrue(element instanceof FSTreeNode);

		node = (FSTreeNode) element;
		clone = (FSTreeNode) node.clone();
		Composite page = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		page.setLayout(gridLayout);
		// Field "Name"
		createField(Messages.InformationPage_Name, clone.name, page);
		// Field "Type"
		createField(Messages.InformationPage_Type, getNodeTypeLabel(), page);
		// Field "Location"
		String location = clone.type.endsWith("FSRootNode") //$NON-NLS-1$
				|| clone.type.endsWith("FSRootDirNode") ? Messages.InformationPage_Computer //$NON-NLS-1$
				: clone.getLocation();
		createField(Messages.InformationPage_Location, location, page);
		// Field "Size"
		if (clone.isFile()) {
			createField(Messages.InformationPage_Size, getSizeText(clone.attr.size), page);
		}
		// Field "Modified"
		createField(Messages.InformationPage_Modified, getDateText(clone.attr.mtime), page);
		// Field "Accessed"
		if (clone.isFile()) {
			createField(Messages.InformationPage_Accessed, getDateText(clone.attr.atime), page);
		}
		createSeparator(page);
		if (clone.isWindowsNode()) {
			createAttributesSection(page);
		} else {
			createPermissionsSection(page);
		}
		return page;
	}
}
