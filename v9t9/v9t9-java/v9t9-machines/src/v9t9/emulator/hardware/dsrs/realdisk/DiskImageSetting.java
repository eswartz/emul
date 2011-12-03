/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.net.URL;

import v9t9.emulator.clients.builtin.IconSetting;
import v9t9.emulator.hardware.dsrs.emudisk.EmuDiskDsr;

public class DiskImageSetting extends IconSetting {
	public DiskImageSetting(String name, Object storage, URL iconPath) {
		super(name, 
				"DSK" + name.charAt(name.length() - 1) + " Image",
				"Specify the full path of the image for this disk.\n\n"+
				"The extension selects the image type when creating a new image.\n\nUse *.dsk for sector-image disks and *.trk for track image disks.",
				storage, iconPath);
		
		addEnablementDependency(EmuDiskDsr.emuDiskDsrEnabled);
		addEnablementDependency(RealDiskUtils.diskImageDsrEnabled);
		addEnablementDependency(RealDiskUtils.diskImageRealTime);
		addEnablementDependency(RealDiskUtils.diskImageDebug);
	}

	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.utils.SettingProperty#isAvailable()
	 */
	@Override
	public boolean isEnabled() {
		if (!RealDiskUtils.diskImageDsrEnabled.getBoolean())
			return false;
		if (!EmuDiskDsr.emuDiskDsrEnabled.getBoolean())
			return true;
		
		// only DSK1 and DSK2 are real disks if emu disk also enabled
		return getName().compareTo(RealDiskUtils.getDiskImageSetting(3)) < 0;
	}
}