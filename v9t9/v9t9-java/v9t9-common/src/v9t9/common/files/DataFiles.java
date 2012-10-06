/*
 * (c) Ed Swartz, 2010
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.common.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ejs.base.utils.CompatUtils;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.settings.SettingSchema;


/**
 * Utilities for finding needed files around the filesystem based on lists of paths.
 * @author ejs
 */
public class DataFiles {
	static public final SettingSchema settingBootRomsPath = 
		new SettingSchema(
				ISettingsHandler.INSTANCE,
				"BootRomsPath", String.class, new ArrayList<String>());
	static public final SettingSchema settingUserRomsPath = 
		new SettingSchema(
				ISettingsHandler.INSTANCE,
				"UserRomsPath", String.class, new ArrayList<String>());
	static public final SettingSchema settingStoredRamPath = 
		new SettingSchema(
				ISettingsHandler.INSTANCE,
				"StoredRamPath", ".");
	
	public static void addSearchPath(ISettingsHandler settings, String filepath) {
		List<String> list = settings.get(settingBootRomsPath).getList();
		if (!list.contains(filepath)) {
			list.add(filepath);
			settings.get(settingBootRomsPath).firePropertyChange();
		}
	}

	
    /**
     * @param filepath
     * @param offset offset in bytes into file
     * @param size maximum size to read
     * @param result 
     * @return File located
     * @throws FileNotFoundException
     */
    public static byte[] readMemoryImage(String filepath, int offset, int size) 
    	throws FileNotFoundException, IOException 
	{
        File file = new File(filepath);
        
        NativeFile nativeFile = NativeFileFactory.INSTANCE.INSTANCE.createNativeFile(file);
        
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
        byte[] result = new byte[sz];
        FileInputStream stream = new FileInputStream(file);
        CompatUtils.skipFully(stream, offset);
        stream.read(result, 0, size);
        stream.close();
        
        return result;
    }

    public static File writeMemoryImage(String filepath, int size, byte[] memory)
    throws FileNotFoundException, IOException 
	{
        File file = new File(filepath);
        
        /* write the chunk */
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(memory, 0, size);
        stream.close();
        
        return file;
    }

    
    public static File writeMemoryImage(String filepath, int addr, int size, IMemoryDomain memory)
    throws FileNotFoundException, IOException 
	{
        File file = new File(filepath);
        
        /* write the chunk */
        FileOutputStream stream = new FileOutputStream(file);
        for (int i = 0; i < size; i++) {
        	stream.write(memory.readByte(addr + i));
        }
        stream.close();
        
        return file;
    }
    
}
