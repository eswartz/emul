/*
  NativeTextFile.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import ejs.base.utils.Check;
import ejs.base.utils.CompatUtils;


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
    @Override
    public String getFileName() {
        return file.getName().toUpperCase();
    }
    @Override
    public File getFile() {
        return file;
    }
    @Override
    public int getFileSize() {
        return (int) Math.min(file.length(), Integer.MAX_VALUE);
    }
    @Override
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
    @Override
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
    @Override
    public void setFileSize(int size) throws IOException {
    	RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.setLength(size);
        raf.close();
    }

    /* (non-Javadoc)
     * @see v9t9.engine.files.NativeFile#flush()
     */
    @Override
    public void flush() throws IOException {
    	
    }

    /* (non-Javadoc)
     * @see v9t9.engine.files.NativeFile#validate()
     */
    @Override
    public void validate() throws InvalidFDRException {
    	
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.NativeFile#getFDRFlags()
     */
    @Override
    public int getFlags() {
    	int flags = FDR.ff_variable;
    	if (!file.canWrite())
    		flags |= FDR.ff_protected;
    	return flags;
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.NativeFile#getSectorsUsed()
     */
    @Override
    public int getSectorsUsed() {
    	return (getFileSize() + 255) / 256;
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.IFDRInfo#getByteOffset()
     */
    @Override
    public int getByteOffset() {
    	return getFileSize() % 256;
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.IFDRInfo#getNumberRecords()
     */
    @Override
    public int getNumberRecords() {
    	return getSectorsUsed();
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.IFDRInfo#getRecordLength()
     */
    @Override
    public int getRecordLength() {
    	return 80;
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.files.IFDRInfo#getRecordsPerSector()
     */
    @Override
    public int getRecordsPerSector() {
    	return 3;
    }
    
    /* (non-Javadoc)
     * @see v9t9.common.files.IFDRInfo#getContentSectors()
     */
    @Override
    public int[] getContentSectors() {
    	int[] secs = new int[getSectorsUsed()];
    	for (int i = 0; i < secs.length; i++) {
    		secs[i] = i * 256;
    	}
    	return secs;
    }
}
