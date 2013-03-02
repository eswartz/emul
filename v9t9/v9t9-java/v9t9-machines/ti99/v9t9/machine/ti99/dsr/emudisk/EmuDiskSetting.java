/*
  EmuDiskSetting.java

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