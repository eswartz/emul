/*
  IFileMapper.java

  (c) 2010-2012 Edward Swartz

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
package v9t9.common.files;

import java.io.File;

import ejs.base.properties.IPersistable;
import ejs.base.properties.IProperty;


/**
 * This maps DSR device+filenames back and forth to disk ones
 * @author ejs
 *
 */
public interface IFileMapper extends IPersistable {
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
}