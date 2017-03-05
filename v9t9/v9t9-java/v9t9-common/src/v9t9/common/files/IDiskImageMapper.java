/*
  IFileMapper.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import v9t9.common.settings.SettingSchema;


import ejs.base.properties.IPersistable;
import ejs.base.properties.IProperty;


/**
 * This maps DSR devices back and forth to filesystem ones
 * @author ejs
 *
 */
public interface IDiskImageMapper extends IPersistable {
	interface IDiskImageListener {
		void diskChanged(String device, IDiskImage oldImage, IDiskImage newImage);
	}
	
	void addListener(IDiskImageListener listener);
	void removeListener(IDiskImageListener listener);
	
	/**
	 * Get a catalog for the given disk
	 * @param name 
	 * @param image the image
	 * @return
	 * @throws IOException
	 */
	//Catalog createCatalog(String name, File image) throws IOException;

	/**
	 * Register a new disk image property
	 * @param enabledProperty 
	 * @param name
	 * @param defaultPath
	 * @return new property
	 */
	IProperty registerDiskImageSetting(SettingSchema enabledProperty, String device, String initialPath);

	/**
	 * Get the current disk property to disk image map
	 * @return
	 */
	Map<String, IDiskImage> getDiskImageMap();

	/**
	 * Get the current disk image at the given property name
	 * @param name
	 * @return
	 */
	IDiskImage getDiskImage(String name);

	/**
	 * Get the map of disk property name to disk image property
	 * @return
	 */
	Map<String, IProperty> getDiskSettingsMap();
	
	/**
	 * Try to create a disk image from the given file
	 * @param file
	 * @return image
	 * @throws IOException if not a recognized disk image
	 */
	IDiskImage createDiskImage(File file) throws IOException;
	
	/**
	 * Get the property that controls whether disk image support is enabled
	 * @return
	 */
	IProperty getImageSupportProperty();
	
}
