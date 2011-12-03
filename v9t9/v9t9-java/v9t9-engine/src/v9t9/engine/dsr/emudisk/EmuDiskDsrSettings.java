/**
 * 
 */
package v9t9.engine.dsr.emudisk;

import java.net.URL;


import v9t9.base.properties.SettingProperty;
import v9t9.engine.EmulatorData;
import v9t9.engine.settings.IconSettingProperty;

/**
 * @author ejs
 *
 */
public class EmuDiskDsrSettings {

	public static URL diskDirectoryIconPath = EmulatorData.getDataURL("icons/disk_directory.png");
	public static final SettingProperty emuDiskDsrEnabled = new IconSettingProperty("EmuDiskDSREnabled", 
	"Disk Directory Support",
	"This implements a drive (like DSK1) in a single directory level on your host.",
	Boolean.FALSE,
	diskDirectoryIconPath);

	public static String getEmuDiskSetting(int i) {
		return "DSK" + i;
	}

}
