/**
 * 
 */
package v9t9.engine.dsr.realdisk;

import java.io.File;
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
public class RealDiskDsrSettings {


	public static final SettingDefinition diskImageDebug = new SettingDefinition(
			ISettingsHandler.WORKSPACE,
			"DiskImageDebug",
			"Debug Disk Image Support",
			"When set, log disk operation information to the console.",
			Boolean.FALSE
			);
	public static final SettingDefinition diskImageRealTime = new SettingDefinition(
			ISettingsHandler.WORKSPACE,
			"DiskImageRealTime",
			"Real-Time Disk Images",
			"When set, disk operations on disk images will try to run at a similar speed to the original FDC1771.",
			Boolean.TRUE
			);
	
	static final URL diskImageIconPath = EmulatorData.getDataURL("icons/disk_image.png");
	public static final SettingDefinition diskImageDsrEnabled = new IconSettingDefinition(
			ISettingsHandler.WORKSPACE,
			"DiskImageDSREnabled",
			"Disk Image Support",
			"This implements a drive (like DSK1) in a disk image on your host.\n\n"+
			"Either sector image or track image disks are supported.\n\n"+
			"A track image can support copy-protected disks, while a sector image cannot.",
			Boolean.FALSE, diskImageIconPath
			);
	
	public static File defaultDiskRootDir;

	public static String getDiskImageSetting(int num) {
		return "DSKImage" + num;
	}

	public static File getDefaultDiskImage(String name) {
		return new File(defaultDiskRootDir, name + ".dsk");
	}


}
