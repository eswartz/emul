/**
 * 
 */
package v9t9.engine.files.image;

import java.io.File;
import java.net.URL;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.IconSettingSchema;
import v9t9.common.settings.SettingSchema;
import v9t9.engine.EmulatorEngineData;

/**
 * @author ejs
 *
 */
public class RealDiskDsrSettings {


	public static final SettingSchema diskImageDebug = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"DiskImageDebug",
			"Debug Disk Image Support",
			"When set, log disk operation information to the console.",
			Boolean.FALSE
			);
	public static final SettingSchema diskImageRealTime = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"DiskImageRealTime",
			"Real-Time Disk Images",
			"When set, disk operations on disk images will try to run at a similar speed to the original FDC1771.",
			Boolean.TRUE
			);
	
	public static final URL diskImageIconPath = EmulatorEngineData.getDataURL("icons/disk_image.png");
	public static final SettingSchema diskImageDsrEnabled = new IconSettingSchema(
			ISettingsHandler.WORKSPACE,
			"DiskImageDSREnabled",
			"Disk Image Support",
			"This implements a drive (like DSK1) in a disk image on your host.\n\n"+
			"Either sector image or track image disks are supported.\n\n"+
			"A track image can support copy-protected disks, while a sector image cannot.",
			Boolean.TRUE, diskImageIconPath
			);
	
	public static File defaultDiskRootDir;

	public static String getDiskImageSetting(int num) {
		return "DSKImage" + num;
	}

	public static File getDefaultDiskImage(String name) {
		return new File(defaultDiskRootDir, name + ".dsk");
	}


}
