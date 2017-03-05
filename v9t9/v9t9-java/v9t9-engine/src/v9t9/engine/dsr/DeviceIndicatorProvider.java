/*
  DeviceIndicatorProvider.java

  (c) 2011-2015 Edward Swartz

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
	private String title;
	private String[] groups;

	public DeviceIndicatorProvider(IProperty activeProperty, String tooltip, 
			int baseIconIndex, int activeIconIndex,
			String title, String... groups) {
		this.activeProperty = activeProperty;
		this.tooltip = tooltip;
		this.baseIconIndex = baseIconIndex;
		this.activeIconIndex = activeIconIndex;
		this.title = title;
		this.groups = groups;
		
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

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IDeviceIndicatorProvider#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IDeviceIndicatorProvider#getGroups()
	 */
	@Override
	public String[] getGroups() {
		return groups;
	}
}
