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

import org.ejs.coffee.core.utils.Check;
import org.ejs.coffee.core.utils.CompatUtils;

public class NativeTextFile implements NativeFile {

    private File file;

    public NativeTextFile(File file) {
        Check.checkArg(file);
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
        
        Check.checkArg((size < (long)Integer.MAX_VALUE));
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
     * @see v9t9.engine.files.NativeFile#getFDRFlags()
     */
    public int getFlags() {
    	int flags = IFDRFlags.ff_variable;
    	if (!file.canWrite())
    		flags |= IFDRFlags.ff_protected;
    	return flags;
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.NativeFile#getSectorsUsed()
     */
    public int getSectorsUsed() {
    	return (getFileSize() + 255) / 256;
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.IFDRInfo#getByteOffset()
     */
    public int getByteOffset() {
    	return getFileSize() % 256;
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.IFDRInfo#getNumberRecords()
     */
    public int getNumberRecords() {
    	return getSectorsUsed();
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.IFDRInfo#getRecordLength()
     */
    public int getRecordLength() {
    	return 80;
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.IFDRInfo#getRecordsPerSector()
     */
    public int getRecordsPerSector() {
    	return 3;
    }
}
