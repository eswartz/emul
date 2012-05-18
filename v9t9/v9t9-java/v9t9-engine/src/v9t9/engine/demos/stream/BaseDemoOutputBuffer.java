/**
 * 
 */
package v9t9.engine.demos.stream;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demos.IDemoOutputBuffer;


/**
 * @author ejs
 * 
 */
public abstract class BaseDemoOutputBuffer implements IDemoOutputBuffer {

	protected final OutputStream stream;

	public abstract boolean isAvailable(int i);

	/**
	 * @param stream
	 * 
	 */
	public BaseDemoOutputBuffer(OutputStream stream) {
		this.stream = stream;
	}

	public void pushData(byte[] data) throws IOException {
		pushData(data, 0, data.length);
	}

}