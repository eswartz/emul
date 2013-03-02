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
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import v9t9.common.machine.IMachine;
import ejs.base.properties.IProperty;

class DiskEnableEntry extends DiskSettingEntry {
	public DiskEnableEntry(final Composite parent, IMachine machine, IProperty setting_) {
		super(parent, machine, setting_, SWT.NONE);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.DiskSelector.SettingEntry#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createControls(Composite parent) {
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);


		final Button checkbox = new Button(parent, SWT.CHECK);
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