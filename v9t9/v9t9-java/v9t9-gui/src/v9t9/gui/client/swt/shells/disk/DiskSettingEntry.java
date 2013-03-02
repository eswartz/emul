/*
  DiskSettingEntry.java

  (c) 2012-2013 Edward Swartz

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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import v9t9.common.machine.IMachine;
import v9t9.common.settings.ISettingDecorator;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingProperty;

abstract class DiskSettingEntry extends Composite {
	protected final IProperty setting;
	private IPropertyListener enableListener;
	protected IMachine machine;
	
	public DiskSettingEntry(final Composite parent, IMachine machine, IProperty setting_, int style) {
		super(parent, style);
		this.machine = machine;
		
		this.setting = setting_;
		
		enableListener = new IPropertyListener() {
			
			public void propertyChanged(IProperty setting) {
				updateSetting();
			}
		};
		setting.addListener(enableListener);
		
		addDisposeListener(new DisposeListener() {
			
			public void widgetDisposed(DisposeEvent e) {
				setting.removeListener(enableListener);
			}
		});
		
		if (setting instanceof ISettingDecorator) {
			ImageDescriptor descriptor = ((ISettingDecorator) setting).getIcon();
			Label icon = new Label(this, SWT.NONE);
			final Image iconImage = descriptor.createImage();
			icon.setImage(iconImage);
			parent.addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent e) {
					iconImage.dispose();
				}
			});
			
		} else {
			new Label(this, SWT.NONE);
		}
		
		createControls(this);
	}
	
	protected void updateSetting() {
		boolean enabled = !(setting instanceof ISettingProperty) || 
			((ISettingProperty) setting).isEnabled();
		setVisible(enabled);
		GridData data = (GridData) getLayoutData();
		data.exclude = !enabled;
		
		getShell().layout(true, true);
		Point cursz = getShell().getSize();
		Point sz = getShell().computeSize(SWT.DEFAULT, 500, true);
		getShell().setSize(Math.max(cursz.x, sz.x),
				Math.max(cursz.y, sz.y));
				
	}

	protected String[] getHistory(String name) {
		return machine.getSettings().getUserSettings().
			getHistorySettings().getArray("DiskSelector." + name);
	}
	protected void setHistory(String name, String[] history) {
		machine.getSettings().getUserSettings().
			getHistorySettings().put("DiskSelector." + name, history);
		//EmulatorSettings.INSTANCE.save();
	}

	abstract protected void createControls(Composite parent);
	
}