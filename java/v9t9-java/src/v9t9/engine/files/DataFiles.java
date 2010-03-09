/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.engine.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.ejs.coffee.core.utils.CompatUtils;
import org.ejs.coffee.core.utils.Setting;


/**
 * @author ejs
 */
public class DataFiles {
	static public final Setting settingBootRomsPath = new Setting("BootRomPath", new ArrayList<String>());
	static public final Setting settingUserRomsPath = new Setting("UserRomPath", new ArrayList<String>());
	static public final Setting settingStoredRamPath = new Setting("StoredRamPath", ".");
	
	public static void addSearchPath(String filepath) {
		List<String> list = settingBootRomsPath.getList();
		if (!list.contains(filepath)) {
			list.add(filepath);
			settingBootRomsPath.notifyListeners(list);
		}
	}

	/**
	 * Look for a file along the search paths with the given name
	 * @param filepath file name or relative path
	 * @return File where it exists or a default location
	 */
	public static File resolveFile(String filepath) {
		File file = new File(filepath);
		if (file.isAbsolute())
			return file;
		for (Setting setting : new Setting[] { settingBootRomsPath, settingUserRomsPath }) {
			for (String path : setting.getList()) {
				file = new File(path, filepath);
				if (file.exists())
					return file;
				file = new File(path, filepath.toLowerCase());
				if (file.exists())
					return file;
			}
		}
		return new File(filepath);
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
	 * @param settings
	 */
	public static void loadState(IDialogSettings settings) {
		settingUserRomsPath.loadState(settings);
	}

	/**
	 * @param settings
	 */
	public static void saveState(IDialogSettings settings) {
		settingUserRomsPath.saveState(settings);
		
	}
    
}
