/**
 * 
 */
package v9t9.emulator.hardware.dsrs.emudisk;

import java.net.URL;

import org.ejs.coffee.core.properties.SettingProperty;

import v9t9.emulator.EmulatorServer;
import v9t9.emulator.clients.builtin.IconSetting;

/**
 * @author ejs
 *
 */
public class EmuDiskDsrSettings {

	static URL diskDirectoryIconPath = EmulatorServer.getDataURL("icons/disk_directory.png");
	public static final SettingProperty emuDiskDsrEnabled = new IconSetting("EmuDiskDSREnabled", 
	"Disk Directory Support",
	"This implements a drive (like DSK1) in a single directory level on your host.",
	Boolean.FALSE,
	diskDirectoryIconPath);

	public static String getEmuDiskSetting(int i) {
		return "DSK" + i;
	}

}
