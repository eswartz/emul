/**
 * 
 */
package v9t9.common.files;

import java.io.IOException;

/**
 * @author ejs
 *
 */
public interface EmulatedFile extends IFDRInfo {

	/**
	 * Read contents, excluding headers
	 * @param contents destination array
	 * @param contentOffset offset into contents to start
	 * @param offset offset into current file to start
	 * @param length bytes to read
	 * @return number of bytes read (may be less than length)
	 * @throws IOException
	 */

	int readContents(byte[] contents, int contentOffset, int offset, int length)
			throws IOException;

	/** Get the represented file content size */
	int getFileSize();

	/** Write file contents, excluding headers */
	int writeContents(byte[] contents, int contentOffset, int offset, int length)
			throws IOException;

	/**
	 * Set absolute file length
	 * @param size
	 * @throws IOException 
	 */
	void setFileSize(int size) throws IOException;

	/**
	 * Validate state.
	 * @throws InvalidFDRException 
	 */
	void validate() throws InvalidFDRException;

	/**
	 * Flush to disk.
	 */
	void flush() throws IOException;

	String toString();

}