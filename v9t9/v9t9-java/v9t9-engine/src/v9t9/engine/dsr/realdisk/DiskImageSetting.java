/**
 * 
 */
package v9t9.engine.dsr.realdisk;

import java.net.URL;

import v9t9.common.settings.IconSettingProperty;
import v9t9.engine.dsr.emudisk.EmuDiskDsrSettings;

public class DiskImageSetting extends IconSettingProperty {
	public DiskImageSetting(String name, Object storage, URL iconPath) {
		super(name, 
				"DSK" + name.charAt(name.length() - 1) + " Image",
				"Specify the full path of the image for this disk.\n\n"+
				"The extension selects the image type when creating a new image.\n\nUse *.dsk for sector-image disks and *.trk for track image disks.",
				storage, iconPath);
		
		addEnablementDependency(EmuDiskDsrSettings.emuDiskDsrEnabled);
		addEnablementDependency(RealDiskDsrSettings.diskImageDsrEnabled);
		addEnablementDependency(RealDiskDsrSettings.diskImageRealTime);
		addEnablementDependency(RealDiskDsrSettings.diskImageDebug);
	}

	/* (non-Javadoc)
	 * @see v9t9.base.core.utils.SettingProperty#isAvailable()
	 */
	@Override
	public boolean isEnabled() {
		if (!RealDiskDsrSettings.diskImageDsrEnabled.getBoolean())
			return false;
		if (!EmuDiskDsrSettings.emuDiskDsrEnabled.getBoolean())
			return true;
		
		// only DSK1 and DSK2 are real disks if emu disk also enabled
		return getName().compareTo(RealDiskDsrSettings.getDiskImageSetting(3)) < 0;
	}
}