/*
  EmuDiskSetting.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.dsr.emudisk;

import java.net.URL;

import ejs.base.properties.IProperty;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.IconSettingProperty;
import v9t9.common.settings.SettingSchema;
import v9t9.engine.files.directory.EmuDiskSettings;
import v9t9.engine.files.image.RealDiskDsrSettings;

class EmuDiskSetting extends IconSettingProperty {
	private IProperty emuDiskDsrEnabled;
	private IProperty diskImageDsrEnabled;

	public EmuDiskSetting(ISettingsHandler settings, String name, Object storage, URL iconPath) {
		super(new SettingSchema(ISettingsHandler.TRANSIENT,
				name, "DSK" + name.charAt(name.length() - 1) + " Directory",
				"Specify the full path of the directory representing this disk.",
				storage), iconPath);
		
		emuDiskDsrEnabled = settings.get(EmuDiskSettings.emuDiskDsrEnabled);
		addEnablementDependency(emuDiskDsrEnabled);
		diskImageDsrEnabled = settings.get(RealDiskDsrSettings.diskImageDsrEnabled);
		addEnablementDependency(diskImageDsrEnabled);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.base.core.utils.SettingProperty#isAvailable()
	 */
	@Override
	public boolean isEnabled() {
		if (!emuDiskDsrEnabled.getBoolean())
			return false;
		if (!diskImageDsrEnabled.getBoolean())
			return true;
		
		// only DSK3 + are real disks if emu disk also enabled
		return getName().compareTo(EmuDiskSettings.getEmuDiskSetting(3)) >= 0;
	}
}