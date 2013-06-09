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
import org.eclipse.swt.widgets.Label;

import v9t9.common.machine.IMachine;
import ejs.base.properties.IProperty;

class DiskComboEntry extends DiskSettingEntry {
	public DiskComboEntry(final Composite parent, IMachine machine, IProperty setting_) {
		super(parent, machine, setting_, SWT.NONE);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.DiskSelector.SettingEntry#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createControls(Composite parent) {
		Composite all = new Composite(parent, SWT.NONE);
		
		Object[] enms = setting.getType().getEnumConstants();
		
		GridLayoutFactory.fillDefaults().numColumns(enms.length + 1).applyTo(all);
		
		Label label = new Label(all, SWT.NONE);
		label.setText(setting.getLabel() +": ");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(label);
		
		for (final Object enm : enms) {
			final Button radio = new Button(all, SWT.RADIO);
			radio.setText(enm.toString());
			GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(radio);
			
			radio.setToolTipText(setting.getDescription());
			
			radio.setSelection(setting.getValue() == enm);
			
			radio.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setting.setValue(enm);
				};
			});
		}
	}
	
	
}