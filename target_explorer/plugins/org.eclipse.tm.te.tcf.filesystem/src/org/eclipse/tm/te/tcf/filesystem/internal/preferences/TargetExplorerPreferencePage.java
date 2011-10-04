/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tm.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The preference page for configuring the preference options for Target
 * Explorer File System Explorer.
 *
 */
public class TargetExplorerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	// The preference key to access the option of auto saving
	public static final String PREF_AUTOSAVING = "PrefAutoSaving"; //$NON-NLS-1$
	// The default value of the option of auto saving.
	public static final boolean DEFAULT_AUTOSAVING = true;

	// The editor to edit the value of auto saving.
	protected BooleanFieldEditor fAutoSaving;

	/***
	 * Create a preference page for Target Explorer File System Explorer.
	 */
	public TargetExplorerPreferencePage() {
		super(GRID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		UIPlugin plugin = UIPlugin.getDefault();
		IPreferenceStore preferenceStore = plugin.getPreferenceStore();
		setPreferenceStore(preferenceStore);
		fAutoSaving = new BooleanFieldEditor(PREF_AUTOSAVING, "Automatically upload files to targets upon saving.", //$NON-NLS-1$
				getFieldEditorParent());
		addField(fAutoSaving);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		// do nothing
	}
}
