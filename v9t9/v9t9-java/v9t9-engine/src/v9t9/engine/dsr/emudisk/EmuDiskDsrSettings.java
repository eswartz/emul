/**
 * 
 */
package v9t9.engine.dsr.emudisk;

import java.net.URL;


import v9t9.base.settings.SettingProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.IconSettingDefinition;
import v9t9.common.settings.IconSettingProperty;
import v9t9.common.settings.SettingDefinition;
import v9t9.engine.EmulatorData;

/**
 * @author ejs
 *
 */
public class EmuDiskDsrSettings {

	public static URL diskDirectoryIconPath = EmulatorData.getDataURL("icons/disk_directory.png");
	public static final SettingDefinition emuDiskDsrEnabled = new IconSettingDefinition(
			ISettingsHandler.WORKSPACE,
			"EmuDiskDSREnabled", 
	"Disk Directory Support",
	"This implements a drive (like DSK1) in a single directory level on your host.",
	Boolean.FALSE,
	diskDirectoryIconPath);

	public static String getEmuDiskSetting(int i) {
		return "DSK" + i;
	}

}
