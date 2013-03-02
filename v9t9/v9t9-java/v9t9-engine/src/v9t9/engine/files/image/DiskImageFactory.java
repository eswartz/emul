/*
  DiskImageFactory.java

  (c) 2010-2011 Edward Swartz

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

import v9t9.common.client.ISettingsHandler;


/**
 * @author ejs
 *
 */
public class DiskImageFactory {

	/**
	 * @param name
	 * @param file
	 * @return
	 */
	public static BaseDiskImage createDiskImage(ISettingsHandler settings, String name, File file) {
		if (file.exists()) {
			if (V9t9TrackDiskImage.isTrackImage(file))
				return new V9t9TrackDiskImage(settings, name, file);
			
			if (RawTrackDiskImage.isTrackImage(file))
				return new RawTrackDiskImage(settings, name, file);
		}
		if (file.getName().toLowerCase().endsWith(".trk"))
			return new V9t9TrackDiskImage(settings, name, file);
		else
			return new SectorDiskImage(settings, name, file);
	}

}
