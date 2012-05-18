/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoEventFormatter;

/**
 * @author ejs
 *
 */
public final class DemoFormatterOutputEventBuffer extends
		DemoOutputEventBuffer {
	private final IDemoEventFormatter formatter;

	/**
	 * @param stream
	 * @param code
	 * @param id
	 */
	public DemoFormatterOutputEventBuffer(OutputStream stream, int code,
			String id, IDemoEventFormatter formatter) {
		super(stream, id, code);
		this.formatter = formatter;
	}

	@Override
	public void encodeEvent(IDemoEvent event) throws IOException {
		formatter.writeEvent(this, event);
	}
}