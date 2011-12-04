/*
 * (c) Ed Swartz, 2010
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.engine.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import v9t9.base.properties.SettingProperty;
import v9t9.base.settings.ISettingSection;
import v9t9.base.utils.CompatUtils;
import v9t9.common.memory.IMemoryDomain;


/**
 * Utilities for finding needed files around the filesystem based on lists of paths.
 * @author ejs
 */
public class DataFiles {
	static public final SettingProperty settingBootRomsPath = new SettingProperty("BootRomsPath", String.class, new ArrayList<String>());
	static public final SettingProperty settingUserRomsPath = new SettingProperty("UserRomsPath", String.class, new ArrayList<String>());
	static public final SettingProperty settingStoredRamPath = new SettingProperty("StoredRamPath", ".");
	
	public static void addSearchPath(String filepath) {
		List<String> list = settingBootRomsPath.getList();
		if (!list.contains(filepath)) {
			list.add(filepath);
			settingBootRomsPath.firePropertyChange();
		}
	}

	/**
	 * Look for a file along the search paths with the given name, using case-insensitive match
	 * @param filepath file name or relative path
	 * @return File where it exists or a default location
	 */
	public static File resolveFile(String filepath) {
		File file = new File(filepath);
		if (file.isAbsolute())
			return file;
		for (SettingProperty setting : new SettingProperty[] { settingBootRomsPath, settingUserRomsPath }) {
			for (Object pathObj : setting.getList()) {
				file = resolveFileAtPath(pathObj.toString(), filepath);
				if (file != null)
					return file;
			}
		}
		return new File(filepath);
	}

	/**
	 * Try to resolve a path against a given base path, using case-insensitive match
	 * @param base
	 * @param path
	 * @return existing file or <code>null</code>
	 */
	public static File resolveFileAtPath(String base, String path) {
		File file;
		file = new File(base, path);
		if (file.exists())
			return file;
		file = new File(base, path.toLowerCase());
		if (file.exists())
			return file;
		return null;
	}
	
    /** Get the size of an image file on disk
     * @param filepath
     * @return
     * @throws IOException
     */
    public static int getImageSize(String filepath) throws IOException {
        File file = resolveFile(filepath);
        if (!file.exists())
        	throw new FileNotFoundException(filepath);
        long sz = file.length();
        if ((int)sz != sz) {
			return 0;
		}
        return (int)sz;
    }

    /**
     * @param filepath
     * @param offset offset in bytes into file
     * @param size maximum size to read
     * @param result 
     * @return File located
     * @throws FileNotFoundException
     */
    public static File readMemoryImage(String filepath, int offset, int size, byte[] result) 
    	throws FileNotFoundException, IOException 
	{
        File file = resolveFile(filepath);
        if (file == null)
        	throw new FileNotFoundException(filepath);
        
        NativeFile nativeFile = NativeFileFactory.createNativeFile(file);
        
        /* adjust sizes */
        int sz = nativeFile.getFileSize();
        
        if (sz < offset + size) {
            // TODO: exception?
            size = sz - offset;	    
            if (size < 0) {
				throw new AssertionError();
			}
        }
     
        /* read the chunk */
        FileInputStream stream = new FileInputStream(file);
        CompatUtils.skipFully(stream, offset);
        stream.read(result, 0, Math.min(size, result.length));
        stream.close();
        
        return file;
    }

    /**
     * @param filepath
     * @param realsize
     * @param memory
     * @return File written
     */
    public static File writeMemoryImage(String filepath, int size, byte[] memory)
    throws FileNotFoundException, IOException 
	{
        File file = new File(filepath);
        if (!file.isAbsolute()) {
        	File dir = new File(settingStoredRamPath.getString());
        	dir.mkdirs();
        	file = new File(dir, filepath);
        }
        
        /* write the chunk */
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(memory, 0, size);
        stream.close();
        
        return file;
    }

    /**
     * @param filepath
     * @param realsize
     * @param memory
     * @return File written
     */
    public static File writeMemoryImage(String filepath, int addr, int size, IMemoryDomain memory)
    throws FileNotFoundException, IOException 
	{
        File file = new File(filepath);
        if (!file.isAbsolute()) {
        	File dir = new File(settingStoredRamPath.getString());
        	dir.mkdirs();
        	file = new File(dir, filepath);
        }
        
        /* write the chunk */
        FileOutputStream stream = new FileOutputStream(file);
        for (int i = 0; i < size; i++) {
        	stream.write(memory.readByte(addr + i));
        }
        stream.close();
        
        return file;
    }
	/**
	 * @param section
	 */
	public static void loadState(ISettingSection section) {
		settingUserRomsPath.loadState(section);
	}

	/**
	 * @param section
	 */
	public static void saveState(ISettingSection section) {
		settingUserRomsPath.saveState(section);
		
	}

	public static String readInputStreamText(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] result = new byte[1024];
        try {
        	int len;
        	while ((len = is.read(result)) >= 0) {
        		bos.write(result, 0, len);
        	}
        } catch (IOException e) {
        	if (is != null)
        		is.close();
        	throw e;
        }
		return new String(bos.toByteArray());
	}
	
	public static String readFileText(File file) throws IOException {
		FileInputStream stream = null;
		byte[] result;
		try {
			long size = file.length();
			stream = new FileInputStream(file);
			result = new byte[(int) size];
			stream.read(result);
		} catch (IOException e) {
			if (stream != null)
				stream.close();
			throw e;
		}
		return new String(result);
	}



}
