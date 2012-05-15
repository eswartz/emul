/**
 * 
 */
package ejs.base.utils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author ejs
 *
 */
public class CountingOutputStream extends FilterOutputStream {

	private long pos;
	
	public CountingOutputStream(OutputStream out) {
		super(out);
	}

	/* (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		super.write(b);
		pos++;
	}
	
	public long getPosition() {
		return pos;
	}
	
}
