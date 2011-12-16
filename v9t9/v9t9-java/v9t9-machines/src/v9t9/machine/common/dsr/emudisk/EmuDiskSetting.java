/**
 * 
 */
package v9t9.machine.common.dsr.emudisk;

import java.net.URL;

import ejs.base.properties.IProperty;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.IconSettingProperty;
import v9t9.engine.files.directory.EmuDiskSettings;
import v9t9.engine.files.image.RealDiskDsrSettings;

class EmuDiskSetting extends IconSettingProperty {
	private IProperty emuDiskDsrEnabled;
	private IProperty diskImageDsrEnabled;

	public EmuDiskSetting(ISettingsHandler settings, String name, Object storage, URL iconPath) {
		super(name, 
				"DSK" + name.charAt(name.length() - 1) + " Directory",
				"Specify the full path of the directory representing this disk.",
				storage, iconPath);
		
		emuDiskDsrEnabled = settings.get(EmuDiskSettings.emuDiskDsrEnabled);
		addEnablementDependency(emuDiskDsrEnabled);
		diskImageDsrEnabled = settings.get(RealDiskDsrSettings.diskImageDsrEnabled);
		addEnablementDependency(diskImageDsrEnabled);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.base.core.utils.SettingProperty#isAvailable()
	 */
	@Override
	public boolean isEnabled() {
		if (!emuDiskDsrEnabled.getBoolean())
			return false;
		if (!diskImageDsrEnabled.getBoolean())
			return true;
		
		// only DSK3 + are real disks if emu disk also enabled
		return getName().compareTo(EmuDiskSettings.getEmuDiskSetting(3)) >= 0;
	}
}