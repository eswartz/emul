/*
  EmuDiskSettings.java

  (c) 2011-2013 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
			ISettingsHandler.MACHINE,
			"EmuDiskDSREnabled", 
			"Disk Directory Support",
			"This implements a drive (like DSK1) in a single directory level on your host.",
			Boolean.TRUE,
			diskDirectoryIconPath);

	public static String getEmuDiskSetting(int i) {
		return "EmuDisk" + i;
	}

	public static String getEmuDiskSetting(String devname) {
		char last = devname.charAt(devname.length() - 1);
		return "EmuDisk" + last;
	}

	/**
	 * @param dev
	 * @return
	 */
	public static String getDeviceName(int dev) {
		return "DSK" + dev;
	}
	
}
