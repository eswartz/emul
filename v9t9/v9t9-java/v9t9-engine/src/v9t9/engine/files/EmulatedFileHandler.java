/*
  FileHandler.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files;

import java.io.File;
import java.io.IOException;

import ejs.base.properties.IProperty;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.Catalog;
import v9t9.common.files.IDiskImageMapper;
import v9t9.common.files.IDiskDriveSetting;
import v9t9.common.files.IEmulatedFileHandler;
import v9t9.common.files.IFileExecutionHandler;
import v9t9.common.files.IFilesInDirectoryMapper;
import v9t9.engine.files.directory.FileDirectory;
import v9t9.engine.files.image.BaseDiskImage;
import v9t9.engine.files.image.DiskImageFactory;

/**
 * @author Ed
 *
 */
public class EmulatedFileHandler implements IEmulatedFileHandler {

	private final ISettingsHandler settings;
	private IFilesInDirectoryMapper fileMapper;
	private IDiskImageMapper imageMapper;
	private IFileExecutionHandler execHandler;

	public EmulatedFileHandler(ISettingsHandler settings, IFilesInDirectoryMapper fileMapper,
			IDiskImageMapper imageMapper, IFileExecutionHandler execHandler) {
		this.fileMapper = fileMapper;
		this.settings = settings;
		this.imageMapper = imageMapper;
		this.execHandler = execHandler;
		
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
			
			int drive = 1;
			if (diskProperty instanceof IDiskDriveSetting)
				drive = ((IDiskDriveSetting) diskProperty).getDrive();
			
			Catalog catalog = image.readCatalog("DSK" + drive);
			
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
	public IFilesInDirectoryMapper getFilesInDirectoryMapper() {
		return fileMapper;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IEmulatedFileHandler#getDiskImageMapper()
	 */
	@Override
	public IDiskImageMapper getDiskImageMapper() {
		return imageMapper;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IEmulatedFileHandler#getFileExecutionHandler()
	 */
	@Override
	public IFileExecutionHandler getFileExecutionHandler() {
		return execHandler;
	}
	
}
