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

public class NativeFDRFile implements NativeFile {

    private File file;
    private FDR fdr;
    
    public NativeFDRFile(File file, FDR fdr) {
        org.ejs.coffee.core.utils.Check.checkArg(file);
        org.ejs.coffee.core.utils.Check.checkArg(fdr);
        this.file = file;
        this.fdr = fdr;
    }
    
    @Override
    public String toString() {
    	return file + " (FDR)";
    }

    public int readContents(byte[] contents, int contentOffset, int offset, int length) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        CompatUtils.skipFully(fis, (fdr.getFDRSize() + offset));
        int size = Math.min(fdr.getFileSize(), length);
        int read = fis.read(contents, contentOffset, size);
        fis.close();
        return read;
    }

    public int writeContents(byte[] contents, int contentOffset, int offset,
			int length) throws IOException {
    	RandomAccessFile raf = new RandomAccessFile(file, "rw");
        
    	raf.seek(offset + fdr.getFDRSize());
        raf.write(contents, contentOffset, length);
        raf.close();
        return length;
	}

    public int getFileSize() {
        return fdr.getFileSize();
    }
    
    public File getFile() {
        return file;
    }

    /* (non-Javadoc)
     * @see v9t9.engine.files.NativeFile#setLength(int)
     */
    public void setFileSize(int size) throws IOException {
    	RandomAccessFile raf = new RandomAccessFile(file, "rw");
    	fdr.setFileSize(size);
        raf.setLength(fdr.getFDRSize() + fdr.getFileSize());
        raf.close();
    }
    
	public FDR getFDR() {
		return fdr;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.files.NativeFile#validate()
	 */
	public void validate() throws InvalidFDRException {
		fdr.validate(file);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.files.NativeFile#flush()
	 */
	public void flush() throws IOException {
		fdr.writeFDR(file);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.files.NativeFile#isProtected()
	 */
	public boolean isProtected() {
		return !file.canWrite() || (fdr.getFlags() & IFDRFlags.ff_protected) != 0; 
		
	}
}
