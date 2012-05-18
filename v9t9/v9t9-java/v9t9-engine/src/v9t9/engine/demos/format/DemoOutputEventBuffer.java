/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.OutputStream;

import v9t9.common.demos.IDemoOutputEventBuffer;

/**
 * @author ejs
 *
 */
public abstract class DemoOutputEventBuffer extends DemoOutputBuffer implements
		IDemoOutputEventBuffer {

	/**
	 * @param stream
	 * @param id
	 * @param code
	 */
	public DemoOutputEventBuffer(OutputStream stream, String id, int code) {
		super(stream, id, code);
	}

}
