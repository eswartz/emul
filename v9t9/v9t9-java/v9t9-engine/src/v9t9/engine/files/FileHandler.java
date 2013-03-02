/*
  FileHandler.java

  (c) 2011-2012 Edward Swartz

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
package v9t9.engine.files;

import java.io.File;
import java.io.IOException;

import ejs.base.properties.IProperty;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.Catalog;
import v9t9.common.files.IFileHandler;
import v9t9.common.files.IFileMapper;
import v9t9.engine.files.directory.FileDirectory;
import v9t9.engine.files.image.BaseDiskImage;
import v9t9.engine.files.image.DiskImageFactory;

/**
 * @author Ed
 *
 */
public class FileHandler implements IFileHandler {

	private final ISettingsHandler settings;
	private IFileMapper fileMapper;

	public FileHandler(ISettingsHandler settings, IFileMapper fileMapper) {
		this.fileMapper = fileMapper;
		this.settings = settings;
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileHandler#createCatalog(v9t9.base.properties.IProperty, boolean)
	 */
	@Override
	public Catalog createCatalog(IProperty diskProperty, boolean isDiskImage)
			throws IOException {
		String name = diskProperty.getName();
		File spec = new File(diskProperty.getString());
		if (isDiskImage) {
			BaseDiskImage image = DiskImageFactory.createDiskImage(
					settings, name, spec);
			image.openDiskImage();
			
			Catalog catalog = image.readCatalog("DSK" + name.charAt(name.length() - 1));
			
			image.closeDiskImage();
			return catalog;
		} else {
			FileDirectory fileDir = new FileDirectory(spec, fileMapper);

			Catalog catalog = fileDir.readCatalog();

			return catalog;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileHandler#getFileMapper()
	 */
	@Override
	public IFileMapper getFileMapper() {
		return fileMapper;
	}
}
