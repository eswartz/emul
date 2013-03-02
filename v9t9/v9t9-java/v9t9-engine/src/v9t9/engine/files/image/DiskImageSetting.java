/*
  DiskImageSetting.java

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

import java.net.URL;

import ejs.base.properties.IProperty;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.IconSettingProperty;
import v9t9.common.settings.SettingSchema;
import v9t9.engine.files.directory.EmuDiskSettings;

public class DiskImageSetting extends IconSettingProperty {
	private IProperty realDsrEnabled;
	private IProperty emuDsrEnabled;

	public DiskImageSetting(ISettingsHandler settings, String name, Object storage, URL iconPath) {
		super(new SettingSchema(ISettingsHandler.TRANSIENT,
				name, "DSK" + name.charAt(name.length() - 1) + " Image",
				"Specify the full path of the image for this disk.\n\n"+
				"The extension selects the image type when creating a new image.\n\nUse *.dsk for sector-image disks and *.trk for track image disks.",
				storage), iconPath);
		
		realDsrEnabled = settings.get(RealDiskDsrSettings.diskImageDsrEnabled);
		emuDsrEnabled = settings.get(EmuDiskSettings.emuDiskDsrEnabled);
		addEnablementDependency(emuDsrEnabled);
		addEnablementDependency(realDsrEnabled);
		addEnablementDependency(settings.get(RealDiskDsrSettings.diskImageRealTime));
		addEnablementDependency(settings.get(RealDiskDsrSettings.diskImageDebug));
	}

	@Override
	public boolean isEnabled() {
		if (!realDsrEnabled.getBoolean())
			return false;
		if (!emuDsrEnabled.getBoolean())
			return true;
		
		// only DSK1 and DSK2 are real disks if emu disk also enabled
		return getName().compareTo(RealDiskDsrSettings.getDiskImageSetting(3)) < 0;
	}
}