/*
  DiskEnableEntry.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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