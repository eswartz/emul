/*
  EmuDiskSettings.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.directory;

import java.net.URL;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.IconSettingSchema;
import v9t9.common.settings.SettingSchema;
import v9t9.engine.EmulatorEngineData;
import ejs.base.properties.IProperty;

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

	/**
	 * @param machine
	 * @param i
	 * @return
	 */
	public static IProperty getEmuDiskSetting(IMachine machine, int i) {
		String diskName = getEmuDiskSetting(i);
		IStoredSettings storage = machine.getSettings().findSettingStorage(diskName);
		if (storage == null)
			return null;
		IProperty dskProp = storage.find(diskName);
		return dskProp;
	}
	
}
