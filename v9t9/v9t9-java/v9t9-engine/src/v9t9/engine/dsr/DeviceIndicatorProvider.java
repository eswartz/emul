/*
  DeviceIndicatorProvider.java

  (c) 2011 Edward Swartz

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
package v9t9.engine.dsr;


import ejs.base.properties.IProperty;
import v9t9.common.dsr.IDeviceIndicatorProvider;

/**
 * @author ejs
 *
 */
public class DeviceIndicatorProvider implements IDeviceIndicatorProvider {

	private final int baseIconIndex;
	private final int activeIconIndex;
	private final IProperty activeProperty;
	private final String tooltip;

	public DeviceIndicatorProvider(IProperty activeProperty, String tooltip, 
			int baseIconIndex, int activeIconIndex) {
		this.activeProperty = activeProperty;
		this.tooltip = tooltip;
		this.baseIconIndex = baseIconIndex;
		this.activeIconIndex = activeIconIndex;
		
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.IDeviceIndicatorProvider#getBaseIconIndex()
	 */
	@Override
	public int getBaseIconIndex() {
		return baseIconIndex;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.IDeviceIndicatorProvider#getActiveIconIndex()
	 */
	@Override
	public int getActiveIconIndex() {
		return activeIconIndex;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.IDeviceIndicatorProvider#getToolTip()
	 */
	@Override
	public String getToolTip() {
		return tooltip;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.IDeviceIndicatorProvider#getActiveProperty()
	 */
	@Override
	public IProperty getActiveProperty() {
		return activeProperty;
	}

}
