/*
 * (c) Ed Swartz, 2010
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.common.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import ejs.base.utils.CompatUtils;
import ejs.base.utils.HexUtils;

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
    
	public static String readInputStreamTextAndClose(InputStream is) throws IOException {
		return new String(readInputStreamContentsAndClose(is));
	}
	
	
	public static byte[] readInputStreamContentsAndClose(InputStream is) throws IOException {
		return readInputStreamContentsAndClose(is, Integer.MAX_VALUE);
	}
	
	
	public static byte[] readInputStreamContentsAndClose(InputStream is, int maxsize) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] result = new byte[1024];
		try {
			int len;
			int left = maxsize;
			while (left > 0  && (len = is.read(result)) >= 0) {
				bos.write(result, 0, Math.min(len, left));
			}
		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException e) { }
			}
		}
		return bos.toByteArray();
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


    /**

     */
    public static void writeOutputStreamContentsAndClose(OutputStream os, byte[] memory, int size) throws IOException 
	{
    	try {
    		os.write(memory, 0, size);
    	} finally {
    		os.close();
    	}
    }

    /**
     * Get the MD5 hash of the given content as a hex-encoded string.
     * @return String
     * @throws NoSuchAlgorithmException 
     */
    public static String getMD5Hash(byte[] content) throws IOException {
    	MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
    	byte[] md5 = digest.digest(content);
    	StringBuilder sb = new StringBuilder();
    	for (byte b : md5) {
    		sb.append(HexUtils.toHex2(b));
    	}
    	return sb.toString();
    }
    
}
