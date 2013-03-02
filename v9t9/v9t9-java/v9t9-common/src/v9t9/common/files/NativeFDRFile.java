/*
  NativeFDRFile.java

  (c) 2008-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.common.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import ejs.base.utils.Check;
import ejs.base.utils.CompatUtils;


public class NativeFDRFile extends EmulatedBaseFDRFile implements NativeFile, IFDROwner {

    private File file;
    public NativeFDRFile(File file, FDR fdr) {
    	super(fdr);
        Check.checkArg(file);
        this.file = file;
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
}
