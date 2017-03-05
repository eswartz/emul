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
import java.util.Map;

import ejs.base.properties.IPersistable;
import ejs.base.properties.IProperty;


/**
 * This maps DSR device+filenames back and forth to disk ones
 * @author ejs
 *
 */
public interface IFilesInDirectoryMapper extends IPersistable {
	/**
	 * Get all the registered settings (String)
	 */
	IProperty[] getSettings();
	
	/**
	 * Get the candidate file for the given device.filename
	 * @param deviceFilename name like DSK1.FOO
	 * @return File (directory or file possibly not existing)
	 */
	File getLocalDottedFile(String deviceFilename);
	
	/**
	 * Get the candidate file for the given filename
	 * @param directory
	 * @param filename name, or null
	 * @return File (directory or file possibly not existing)
	 */
	File getLocalFile(File dir, String filename);
	
	/**
	 * Get the candidate file for the given filename
	 * @param device
	 * @param filename name, or null
	 * @return File (directory or file possibly not existing)
	 */
	File getLocalFile(String device, String filename);
	
	/**
	 * Get the local filename for the given DSR filename.
	 * @param fileName the DSR filename (without device)
	 * @return the local filename 
	 */
	String getLocalFileName(String fileName);
	
	/**
	 * Get the root device file (e.g. for a file or filepath) 
	 */
	File getLocalRoot(File file);
	
	/**
	 * Get the root device with this DSR name (e.g. FOO in DSK.FOO) 
	 */
	String getDeviceNamed(String name);
	
	/**
	 * Get the DSR filename for the given filename
	 * @param filename the file segment (or dotted path)
	 * @return DSR-formatted filename
	 */
	String getDsrFileName(String filename);
	
	/**
	 * Get the device matching the given directory (exactly;
	 * use {@link #getLocalRoot(File)} if needed)
	 * @return device name or <code>null</code>
	 */
	String getDsrDeviceName(File dir);
	
	/**
	 * Map the given device name (e.g. "DSK1") to a property (which need
	 * not be named the same)
	 */
	void registerDiskSetting(String devname, IProperty diskSetting);
	/**
	 * Unnap the given device name from its setting
	 */
	void unregisterDiskSetting(String devname);
	/**
	 * Set the directory for the given device to the given directory
	 * @param device
	 * @param dir
	 */
	void setDiskPath(String device, File dir);	
	
	/**
	 * Get a catalog for the given disk
	 * @param dir the directory
	 * @return
	 * @throws IOException
	 */
//	Catalog createCatalog(File dir) throws IOException;

	/**
	 * @return
	 */
	IProperty getDirectorySupportProperty();

	/**
	 * @return
	 */
	Map<String, IProperty> getDiskSettingMap();

	/**
	 * @param dir
	 * @return
	 */
	IDiskDirectory createDiskDirectory(File dir);
	
}
