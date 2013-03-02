/*
  DeviceIndicatorProvider.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
