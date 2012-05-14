/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author ejs
 *
 */
public class CountingInputStream extends FilterInputStream {

	private long pos;
	private long markedPos;
	
	public CountingInputStream(InputStream in) {
		super(in);
	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#mark(int)
	 */
	@Override
	public synchronized void mark(int readlimit) {
		super.mark(readlimit);
		this.markedPos = pos;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#reset()
	 */
	@Override
	public synchronized void reset() throws IOException {
		super.reset();
		this.pos = markedPos;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read()
	 */
	@Override
	public int read() throws IOException {
		int r = super.read();
		if (r >= 0)
			pos++;
		return r;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[])
	 */
	@Override
	public int read(byte[] b) throws IOException {
		int r = super.read(b);
		if (r > 0)
			pos += r;
		return r;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int r = super.read(b, off, len);
		if (r > 0)
			pos += r;
		return r;
	}
	
	public long getPosition() {
		return pos;
	}
}
