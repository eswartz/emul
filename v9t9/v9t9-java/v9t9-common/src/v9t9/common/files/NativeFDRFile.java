/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.common.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import ejs.base.utils.Check;
import ejs.base.utils.CompatUtils;


public class NativeFDRFile implements NativeFile {

    private File file;
    private FDR fdr;
    
    public NativeFDRFile(File file, FDR fdr) {
        Check.checkArg(file);
        Check.checkArg(fdr);
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
    	file.setWritable(true);
    	RandomAccessFile raf = new RandomAccessFile(file, "rw");
        
    	raf.seek(offset + fdr.getFDRSize());
        raf.write(contents, contentOffset, length);
        raf.close();
        file.setWritable(!fdr.isReadOnly());
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
    	file.setWritable(true);
    	RandomAccessFile raf = new RandomAccessFile(file, "rw");
    	fdr.setFileSize(size);
        raf.setLength(fdr.getFDRSize() + fdr.getFileSize());
        raf.close();
        file.setWritable(!fdr.isReadOnly());
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
	 * @see v9t9.engine.files.NativeFile#getFDRFlags()
	 */
	public int getFlags() {
		int flags = fdr.getFlags();
		if (!file.canWrite())
			flags |= FDR.ff_protected;
		return flags;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.files.NativeFile#getSectorsUsed()
	 */
	public int getSectorsUsed() {
		return fdr.secsused;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.files.IFDRInfo#getByteOffset()
	 */
	public int getByteOffset() {
		return fdr.getByteOffset();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.files.IFDRInfo#getNumberRecords()
	 */
	public int getNumberRecords() {
		return fdr.getNumberRecords();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.files.IFDRInfo#getRecordLength()
	 */
	public int getRecordLength() {
		return fdr.getRecordLength();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.files.IFDRInfo#getRecordsPerSector()
	 */
	public int getRecordsPerSector() {
		return fdr.getRecordsPerSector();
	}
}
