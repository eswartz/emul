/*
  RealDiskDsrSettings.java

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
			ISettingsHandler.MACHINE,
			"DiskImageDebug",
			"Debug Disk Image Support",
			"When set, log disk operation information to the console.",
			Boolean.FALSE
			);
	public static final SettingSchema diskImageRealTime = new SettingSchema(
			ISettingsHandler.MACHINE,
			"DiskImageRealTime",
			"Real-Time Disk Images",
			"When set, disk operations on disk images will try to run at a similar speed to the original FDC1771.",
			Boolean.TRUE
			);
	
	public static final URL diskImageIconPath = EmulatorEngineData.getDataURL("icons/disk_image.png");
	public static final SettingSchema diskImageDsrEnabled = new IconSettingSchema(
			ISettingsHandler.MACHINE,
			"DiskImageDSREnabled",
			"Disk Image Support",
			"This implements a drive (like DSK1) in a disk image on your host.\n\n"+
			"Either sector image or track image disks are supported.\n\n"+
			"A track image can support copy-protected disks, while a sector image cannot.",
			Boolean.TRUE, diskImageIconPath
			);
	
	public static File defaultDiskRootDir;

	public static String getDiskImageSetting(int num) {
		return "DiskImage" + num;
	}

	public static File getDefaultDiskImage(String name) {
		return new File(defaultDiskRootDir, name + ".dsk");
	}


}
