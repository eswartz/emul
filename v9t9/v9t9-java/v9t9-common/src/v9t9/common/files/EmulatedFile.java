/*
  EmulatedFile.java

  (c) 2012 Edward Swartz

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