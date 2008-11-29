/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.engine.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    
    public int readContents(byte[] contents, int contentOffset, int offset, int length) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        int size = (int) file.length() - offset;
        size = Math.min(size, length);
        
        Check.checkArg(size < (long)Integer.MAX_VALUE);
        fis.skip(offset);
        int ret = fis.read(contents, contentOffset, size);
        fis.close();
        return ret;
    }

    public int writeContents(byte[] contents, int contentOffset, int offset, int length) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, true);
        
        fos.getChannel().position(offset);
        fos.write(contents, contentOffset, length);
        fos.close();
        return length;
    }

}
