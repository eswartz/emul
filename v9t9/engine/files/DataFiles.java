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


/**
 * @author ejs
 */
public class DataFiles {

    /** Get the size of an image file on disk
     * @param filepath
     * @return
     */
    public static int getImageSize(String filepath) {
        // TODO: real handling of complex filetypes
        File file = new File(filepath);
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
     * @throws FileNotFoundException
     */
    public static void readMemoryImage(String filepath, int offset, int size, byte[] result) 
    	throws FileNotFoundException, IOException 
	{
        // TODO: real work
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
        FileInputStream stream = new FileInputStream(file);
        stream.skip(offset);
        stream.read(result, 0, Math.min(size, result.length));
        stream.close();
    }

    /**
     * @param filepath
     * @param realsize
     * @param memory
     */
    public static void writeMemoryImage(String filepath, int size, byte[] memory)
    throws FileNotFoundException, IOException 
	{
        // TODO: real work
        File file = new File(filepath);
        
        /* write the chunk */
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(memory, 0, size);
        stream.close();
    }
    
}
