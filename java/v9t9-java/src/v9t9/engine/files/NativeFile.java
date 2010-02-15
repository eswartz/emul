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
    /** Get the host file */
    public File getFile();

    /** Read contents, excluding headers  */
    public int readContents(byte[] contents, int contentOffset, int offset, int length) throws IOException;

    /** Get the represented file content size */
    public int getFileSize();

    /** Write file contents, excluding headers */
 	public int writeContents(byte[] contents, int contentOffset, int offset, int length) throws IOException;

	/**
	 * Set absolute file length
	 * @param size
	 * @throws IOException 
	 */
	public void setFileSize(int size) throws IOException;
	
	/**
	 * Validate state.
	 * @throws InvalidFDRException 
	 */
	void validate() throws InvalidFDRException;
	
	/**
	 * Flush to disk.
	 */
	void flush() throws IOException;

	/**
	 * Get the FDR flags
	 * @return
	 */
	public int getFDRFlags();
	
	
}
