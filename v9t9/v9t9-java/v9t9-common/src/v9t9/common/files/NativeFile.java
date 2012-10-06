/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.common.files;

import java.io.File;

/**
 * This is the interface to a native (TI-99) file
 * @author ejs
 */
public interface NativeFile extends EmulatedFile {
    /** Get the host file */
    public File getFile();
}
