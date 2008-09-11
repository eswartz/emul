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

import v9t9.utils.Check;

public class NativeFDRFile implements NativeFile {

    private File file;
    private FDR fdr;
    private String filename;
    
    public NativeFDRFile(File file, FDR fdr) {
        Check.checkArg(file);
        Check.checkArg(fdr);
        this.file = file;
        this.fdr = fdr;
        this.filename = fdr.getFileName();
        if (this.filename == null) {
            this.filename = file.getName();
        }
    }

    public String getFileName() {
        return filename;
    }

    public void readContents(byte[] contents, int offset, int length) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        fis.skip(fdr.getFDRSize() + offset);
        int size = Math.min(fdr.getFileSize(), length);
        fis.read(contents, 0, size);
        fis.close();
    }

    public int getFileSize() {
        return fdr.getFileSize();
    }
    
    public File getFile() {
        return file;
    }

}
