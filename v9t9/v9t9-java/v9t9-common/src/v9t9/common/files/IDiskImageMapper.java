/*
  IFileMapper.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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
	Catalog createCatalog(String name, File image) throws IOException;

	/**
	 * @param name
	 * @param defaultDiskImage
	 */
	IProperty registerDiskImagePath(String device, File image);

	/**
	 * @return
	 */
	Map<String, IDiskImage> getDiskImageMap();

	/**
	 * @param name
	 * @return
	 */
	IDiskImage getDiskImage(String name);

	/**
	 * @return
	 */
	Map<String, IProperty> getDiskSettingsMap();

	
}
