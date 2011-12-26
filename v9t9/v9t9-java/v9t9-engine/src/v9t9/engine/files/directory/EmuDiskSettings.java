/**
 * 
 */
package v9t9.engine.files.directory;

import java.net.URL;


import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.IconSettingSchema;
import v9t9.common.settings.SettingSchema;
import v9t9.engine.EmulatorEngineData;

/**
 * @author ejs
 *
 */
public class EmuDiskSettings {

	public static URL diskDirectoryIconPath = EmulatorEngineData.getDataURL("icons/disk_directory.png");
	public static final SettingSchema emuDiskDsrEnabled = new IconSettingSchema(
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
