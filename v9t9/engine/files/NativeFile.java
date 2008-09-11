/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.engine.files;

import java.io.File;
import java.io.IOException;

/**
 * This is the interface to a native (TI-99) file
 * @author ejs
 */
public interface NativeFile {
    /** Get the file name, as seen on the TI (base) */
    public String getFileName();

    /** Get the host file */
    public File getFile();

    /** Read contents, excluding headers */
    public void readContents(byte[] contents, int offset, int length) throws IOException;

    /** Get the file size */
    public int getFileSize();
}
