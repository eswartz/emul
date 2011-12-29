/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.common;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author ejs
 */
public class FileUtils  {
	/**
	 * Overcome egregious buggy surprising behavior in {@link InputStream#skip(long)}
	 * @param in
	 * @param nBytes
	 * @throws IOException
	 */
	public static void skipFully(InputStream in, long nBytes) throws IOException {
    	long remaining = nBytes;
    	while (remaining > 0) {
    		long skipped = in.skip(remaining);
    		if (skipped == 0)
    			throw new EOFException();
    		remaining -= skipped;
    	}
    }
	
}
