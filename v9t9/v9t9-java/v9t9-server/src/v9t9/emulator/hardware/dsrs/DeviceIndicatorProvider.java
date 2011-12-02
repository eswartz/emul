/**
 * Mar 12, 2011
 */
package v9t9.emulator.hardware.dsrs;

import org.ejs.coffee.core.properties.SettingProperty;

import v9t9.emulator.hardware.IDeviceIndicatorProvider;

/**
 * @author ejs
 *
 */
public class DeviceIndicatorProvider implements IDeviceIndicatorProvider {

	private final int baseIconIndex;
	private final int activeIconIndex;
	private final SettingProperty activeProperty;
	private final String tooltip;

	public DeviceIndicatorProvider(SettingProperty activeProperty, String tooltip, 
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
	public SettingProperty getActiveProperty() {
		return activeProperty;
	}

}
