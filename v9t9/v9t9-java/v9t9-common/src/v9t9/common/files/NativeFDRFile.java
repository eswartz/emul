/*
  NativeFDRFile.java

  (c) 2008-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import ejs.base.utils.Check;


public class NativeFDRFile extends EmulatedBaseFDRFile implements NativeFile, IFDROwner {

    private File file;
    
    public NativeFDRFile(IEmulatedDisk disk, File file, FDR fdr) {
    	super(disk, fdr);
        Check.checkArg(file);
        this.file = file;
    }
    
    public NativeFDRFile(File file, FDR fdr) {
    	this(null, file, fdr);
    }
    
    @Override
    public String toString() {
    	return file + " (FDR)";
    }
    
    public int readContents(byte[] contents, int contentOffset, int offset, int length) throws IOException {
//        FileInputStream fis = new FileInputStream(file);
//        CompatUtils.skipFully(fis, (fdr.getFDRSize() + offset));
//        int size = Math.min(fdr.getFileSize(), length);
//        int read = fis.read(contents, contentOffset, size);
//        fis.close();
//        return read;
    	
    	RandomAccessFile raf = new RandomAccessFile(file, "r");
        
    	raf.seek(offset + fdr.getFDRSize());
        int len = raf.read(contents, contentOffset, length);
        raf.close();
        return len;
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

    public File getFile() {
        return file;
    }

    /* (non-Javadoc)
     * @see v9t9.engine.files.NativeFile#setLength(int)
     */
    public void setFileSize(int size) throws IOException {
    	super.setFileSize(size);
    	
    	file.setWritable(true);
    	RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.setLength(fdr.getFDRSize() + fdr.getFileSize());
        raf.close();
        file.setWritable(!fdr.isReadOnly());
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
	 * @see v9t9.common.files.IFDRInfo#getComment()
	 */
	@Override
	public String getComment() {
		return fdr.getComment();
	}
}
