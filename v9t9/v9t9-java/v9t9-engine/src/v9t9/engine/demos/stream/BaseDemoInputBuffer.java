/**
 * 
 */
package v9t9.engine.demos.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author ejs
 *
 */
public abstract class BaseDemoInputBuffer implements IDemoInputBuffer {

	protected final InputStream is;
	protected final String label;
	protected long startPos;

	/**
	 * 
	 */
	public BaseDemoInputBuffer(InputStream is, String label) {
		this.is = is;
		this.label = label;
	}


	protected IOException newFormatException(String string) {
		return newFormatException(string, startPos);
	}

	protected IOException newFormatException(String string, long effectivePos) {
		return new IOException("Demo corrupted at 0x" + 
				Long.toHexString(effectivePos) + ": " + string);

	}
	
	public IOException newBufferException(String string) {
		return newFormatException(string, getEffectivePos());
	}


}