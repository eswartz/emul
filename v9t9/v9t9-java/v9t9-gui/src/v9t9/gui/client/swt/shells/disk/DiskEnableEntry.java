/*
  DiskEnableEntry.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.disk;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ejs.base.properties.IProperty;

class DiskEnableEntry extends DiskSettingEntry {
	public DiskEnableEntry(IDeviceSelectorDialog dialog, final Composite parent, IProperty setting_) {
		super(dialog, parent, setting_, SWT.NONE);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.DiskSelector.SettingEntry#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createControls(Composite parent) {
		final Button checkbox = new Button(this, SWT.CHECK);
		checkbox.setText(setting.getLabel());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(checkbox);
		
		checkbox.setToolTipText(setting.getDescription());
		
		checkbox.setSelection(setting.getBoolean());
		
		checkbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setting.setBoolean(checkbox.getSelection());
				
			};
		});
	}
	
	
}