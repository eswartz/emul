/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.engine.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.ejs.coffee.core.utils.CompatUtils;

import v9t9.emulator.hardware.dsrs.DsrException;
import v9t9.emulator.hardware.dsrs.PabConstants;

public class NativeTextFile implements NativeFile {

    private File file;

    public NativeTextFile(File file) {
        org.ejs.coffee.core.utils.Check.checkArg(file);
        this.file = file;
    }

    @Override
    public String toString() {
    	return file.getAbsolutePath() + " (text)";
    }
    public String getFileName() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }

    public int getFileSize() {
        return (int) Math.min(file.length(), Integer.MAX_VALUE);
    }
    
    public int readContents(byte[] contents, int contentOffset, int offset, int length) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        int size = (int) file.length() - offset;
        size = Math.min(size, length);
        
        org.ejs.coffee.core.utils.Check.checkArg((size < (long)Integer.MAX_VALUE));
        CompatUtils.skipFully(fis, offset);
        int ret = fis.read(contents, contentOffset, size);
        fis.close();
        return ret;
    }

    public int writeContents(byte[] contents, int contentOffset, int offset, int length) throws IOException {
    	RandomAccessFile raf = new RandomAccessFile(file, "rw");
        
    	raf.seek(offset);
        raf.write(contents, contentOffset, length);
        raf.close();
        return length;
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.NativeFile#setLength(int)
     */
    public void setFileSize(int size) throws IOException {
    	RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.setLength(size);
        raf.close();
    }

    /* (non-Javadoc)
     * @see v9t9.engine.files.NativeFile#flush()
     */
    public void flush() throws IOException {
    	
    }

    /* (non-Javadoc)
     * @see v9t9.engine.files.NativeFile#validate()
     */
    public void validate() throws InvalidFDRException {
    	
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.NativeFile#isProtected()
     */
    public boolean isProtected() {
    	return !file.canWrite();
    }
}
