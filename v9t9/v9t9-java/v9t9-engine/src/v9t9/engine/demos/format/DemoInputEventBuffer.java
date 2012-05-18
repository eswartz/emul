/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.InputStream;

import v9t9.common.demos.IDemoInputEventBuffer;

/**
 * @author ejs
 *
 */
public abstract class DemoInputEventBuffer extends DemoInputBuffer implements
		IDemoInputEventBuffer {

	/**
	 * @param is
	 * @param code
	 * @param identifier
	 */
	public DemoInputEventBuffer(InputStream is, int code, String identifier) {
		super(is, code, identifier);
	}
	

}
