/**
 * 
 */
package v9t9.engine.dsr.realdisk;

import java.net.URL;

import v9t9.base.properties.IProperty;
import v9t9.base.settings.SettingProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.IconSettingProperty;
import v9t9.engine.dsr.emudisk.EmuDiskDsrSettings;

public class DiskImageSetting extends IconSettingProperty {
	private IProperty realDsrEnabled;
	private IProperty emuDsrEnabled;

	public DiskImageSetting(ISettingsHandler settings, String name, Object storage, URL iconPath) {
		super(name, 
				"DSK" + name.charAt(name.length() - 1) + " Image",
				"Specify the full path of the image for this disk.\n\n"+
				"The extension selects the image type when creating a new image.\n\nUse *.dsk for sector-image disks and *.trk for track image disks.",
				storage, iconPath);
		
		realDsrEnabled = settings.get(RealDiskDsrSettings.diskImageDsrEnabled);
		emuDsrEnabled = settings.get(EmuDiskDsrSettings.emuDiskDsrEnabled);
		addEnablementDependency(emuDsrEnabled);
		addEnablementDependency(realDsrEnabled);
		addEnablementDependency(settings.get(RealDiskDsrSettings.diskImageRealTime));
		addEnablementDependency(settings.get(RealDiskDsrSettings.diskImageDebug));
	}

	@Override
	public boolean isEnabled() {
		if (!realDsrEnabled.getBoolean())
			return false;
		if (!emuDsrEnabled.getBoolean())
			return true;
		
		// only DSK1 and DSK2 are real disks if emu disk also enabled
		return getName().compareTo(RealDiskDsrSettings.getDiskImageSetting(3)) < 0;
	}
}