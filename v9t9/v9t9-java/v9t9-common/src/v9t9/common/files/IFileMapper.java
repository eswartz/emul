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
	
	void registerDiskSetting(String devname, IProperty diskSetting);
	void unregisterDiskSetting(String devname);
	void setDiskPath(String device, File dir);	
}