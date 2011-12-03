/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.io.File;
import java.net.URL;

import org.ejs.coffee.core.properties.SettingProperty;
import v9t9.emulator.EmulatorServer;
import v9t9.emulator.clients.builtin.IconSetting;

/**
 * @author ejs
 *
 */
public class RealDiskDsrSettings {


	public static final SettingProperty diskImageDebug = new SettingProperty("DiskImageDebug",
	"Debug Disk Image Support",
	"When set, log disk operation information to the console.",
	Boolean.FALSE);
	public static final SettingProperty diskImageRealTime = new SettingProperty("DiskImageRealTime",
	"Real-Time Disk Images",
	"When set, disk operations on disk images will try to run at a similar speed to the original FDC1771.",
	Boolean.TRUE);
	
	static final URL diskImageIconPath = EmulatorServer.getDataURL("icons/disk_image.png");
	public static final SettingProperty diskImageDsrEnabled = new IconSetting("DiskImageDSREnabled",
		"Disk Image Support",
		"This implements a drive (like DSK1) in a disk image on your host.\n\n"+
		"Either sector image or track image disks are supported.\n\n"+
		"A track image can support copy-protected disks, while a sector image cannot.",
		Boolean.FALSE, diskImageIconPath);
	
	public static File defaultDiskRootDir;

	public static String getDiskImageSetting(int num) {
		return "DSKImage" + num;
	}

	public static File getDefaultDiskImage(String name) {
		return new File(defaultDiskRootDir, name + ".dsk");
	}


}
