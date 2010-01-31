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
    private String filename;
    
    public NativeFDRFile(File file, FDR fdr) {
        org.ejs.coffee.core.utils.Check.checkArg(file);
        org.ejs.coffee.core.utils.Check.checkArg(fdr);
        this.file = file;
        this.fdr = fdr;
        this.filename = fdr.getFileName();
        if (this.filename == null) {
            this.filename = file.getName();
        }
    }
    
    @Override
    public String toString() {
    	return file + " (FDR)";
    }

    public String getFileName() {
        return filename;
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

	public FDR getFDR() {
		return fdr;
	}

}
