/*
  DiskImageFactory.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
