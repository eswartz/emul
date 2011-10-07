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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
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

	/**
	 * Get the type of an FSTreeNode.
	 *
	 * @param node
	 *            The FSTreeNode instance
	 * @return "folder" if it is a directory, "file" if is a file, or else
	 *         defaults to node.type.
	 */
	protected String getNodeTypeLabel(FSTreeNode node) {
		if (node.isDirectory())
			return Messages.InformationPage_Folder;
		else if (node.isFile())
			return Messages.InformationPage_File;
		else
			return node.type;
	}

	/**
	 * Create a horizontal separator between field sections.
	 *
	 * @param parent
	 *            The parent composite of the separator.
	 * @param style
	 *            The style of the separator.
	 */
	protected void createSeparator(Composite parent, int style) {
		Label label = new Label(parent, SWT.SHADOW_NONE | SWT.HORIZONTAL);
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
	 * @param node
	 *            The file/folder node.
	 */
	protected void createAttributesSection(Composite parent,
			final FSTreeNode node) {
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
		Button button = new Button(attr, SWT.CHECK);
		button.setText(Messages.InformationPage_ReadOnly);
		button.setEnabled(false);
		button.setSelection(node.isReadOnly());
		// Hidden
		button = new Button(attr, SWT.CHECK);
		button.setText(Messages.InformationPage_Hidden);
		button.setEnabled(false);
		button.setSelection(node.isHidden());
		// Advanced Attributes
		button = new Button(attr, SWT.PUSH);
		button.setText(Messages.InformationPage_Advanced);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				showAdvancedAttributes(node);
			}
		});
	}

	/**
	 * Show the advanced attributes dialog for the specified file/folder.
	 *
	 * @param node
	 *            The file/folder node.
	 */
	void showAdvancedAttributes(FSTreeNode node) {
		AdvancedAttributesDialog dialog = new AdvancedAttributesDialog(this.getShell(), node);
		dialog.open();
	}

	/**
	 * Create the permissions section for a Unix/Linux file/folder.
	 *
	 * @param parent
	 *            The parent composite on which it is created.
	 * @param node
	 *            The file/folder node.
	 */
	protected void createPermissionsSection(Composite parent, FSTreeNode node) {
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
		createPermissionGroup(perms, 0,
				Messages.PermissionsGroup_UserPermissions, node.attr.permissions);
		createPermissionGroup(perms, 3,
				Messages.PermissionsGroup_GroupPermissions,
				node.attr.permissions);
		createPermissionGroup(perms, 6,
				Messages.PermissionsGroup_OtherPermissions,
				node.attr.permissions);
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
	 * @param permissions
	 *            The permissions bit mask.
	 */
	protected void createPermissionGroup(Composite parent, int bit,
			String header, int permissions) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(header);
		GridData data = new GridData();
		data.horizontalAlignment = SWT.LEFT;
		label.setLayoutData(data);
		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, true);
		layout.marginHeight = 0;
		group.setLayout(layout);
		createPermissionButton(Messages.PermissionsGroup_Readable, bit,
				permissions, group);
		createPermissionButton(Messages.PermissionsGroup_Writable, bit + 1,
				permissions, group);
		createPermissionButton(Messages.PermissionsGroup_Executable, bit + 2,
				permissions, group);
	}

	/**
	 * Create a check-box field for a single permission item.
	 *
	 * @param label
	 *            The label of the permission.
	 * @param index
	 *            The index of current permission bit mask index.
	 * @param permissions
	 *            The permissions bit mask.
	 * @param parent
	 *            The parent to hold the check-box field.
	 */
	private void createPermissionButton(String label, int index,
			int permissions, Composite parent) {
		Button button = new Button(parent, SWT.CHECK);
		button.setText(label);
		int bit = 1 << (8 - index);
		button.setSelection((permissions & bit) != 0);
		// Not editable yet.
		button.setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		IAdaptable element = getElement();
		Assert.isTrue(element instanceof FSTreeNode);

		FSTreeNode node = (FSTreeNode) element;
		Composite page = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		page.setLayout(gridLayout);
		// Field "Name"
		createField(Messages.InformationPage_Name, node.name, page);
		createSeparator(page, SWT.SHADOW_NONE);
		// Field "Type"
		createField(Messages.InformationPage_Type, getNodeTypeLabel(node), page);
		// Field "Size"
		if (node.isFile()) {
			createField(Messages.InformationPage_Size, getSizeText(node.attr.size), page);
		}
		// Field "Location"
		String location = node.type.endsWith("FSRootNode") //$NON-NLS-1$
				|| node.type.endsWith("FSRootDirNode") ? Messages.InformationPage_Computer //$NON-NLS-1$
				: node.getLocation();
		createField(Messages.InformationPage_Location, location, page);
		createSeparator(page, SWT.SHADOW_NONE);
		// Field "Modified"
		createField(Messages.InformationPage_Modified, getDateText(node.attr.mtime), page);
		// Field "Accessed"
		if (node.isFile()) {
			createField(Messages.InformationPage_Accessed, getDateText(node.attr.atime), page);
		}
		createSeparator(page, SWT.SHADOW_NONE);
		if (node.isWindowsNode()) {
			createAttributesSection(page, node);
		} else {
			createPermissionsSection(page, node);
		}
		return page;
	}
}
