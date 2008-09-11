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

public class NativeTextFile implements NativeFile {

    private File file;

    public NativeTextFile(File file) {
        Check.checkArg(file);
        this.file = file;
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
    
    public void readContents(byte[] contents, int offset, int length) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        int size = (int) file.length() - offset;
        size = Math.min(size, length);
        
        Check.checkArg(size < (long)Integer.MAX_VALUE);
        fis.skip(offset);
        fis.read(contents, 0, size);
        fis.close();
    }

}
