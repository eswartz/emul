/**
 * 
 */
package v9t9.engine.demos.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.demos.IDemoOutputBuffer;
import v9t9.common.demos.IDemoOutputStream;
import ejs.base.utils.CountingOutputStream;

/**
 * @author ejs
 *
 */
public abstract class BaseDemoOutputStream implements IDemoOutputStream {

	protected final CountingOutputStream os;
	
	protected abstract void writeTimerTick() throws IOException;

	protected List<IDemoOutputBuffer> buffers = new ArrayList<IDemoOutputBuffer>(4);

	/**
	 * @param os 
	 * 
	 */
	public BaseDemoOutputStream(OutputStream os_) {
		this.os = os_ instanceof CountingOutputStream ? (CountingOutputStream) os_ : new CountingOutputStream(os_);
		
	}

	public synchronized void close() throws IOException {
		flushAll();

		preClose();
		
		if (os != null) {
			os.close();
		}
		
		buffers.clear();
	}

	/**
	 * Called immediately before closing.
	 * @throws IOException
	 */
	protected void preClose() throws IOException {
		
	}


	protected boolean anythingToFlush() {
		boolean any = false;
		for (IDemoOutputBuffer buffer : buffers) {
			any |= !buffer.isEmpty();
		}
		return any;
	}
	
	protected void flushAll() throws IOException {
		for (IDemoOutputBuffer buffer : buffers) {
			buffer.flush();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoOutputStream#getPosition()
	 */
	@Override
	public long getPosition() {
		return os.getPosition();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoOutputStream#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() {
		return os;
	}
}