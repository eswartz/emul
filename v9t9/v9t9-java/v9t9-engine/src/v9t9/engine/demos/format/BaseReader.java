/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.BufferedInputStream;
import java.io.InputStream;

import v9t9.common.events.NotifyException;

/**
 * @author ejs
 *
 */
public class BaseReader {

	protected final CountingInputStream is;

	/**
	 * 
	 */
	public BaseReader(InputStream is) {
		BufferedInputStream bis = is instanceof BufferedInputStream ? 
				(BufferedInputStream) is : new BufferedInputStream(is);
		this.is = new CountingInputStream(bis);
	}
	
	public long getPosition() {
		return is.getPosition();
	}

	public NotifyException newFormatException(String string) {
		return newFormatException(string, getPosition());
	}

	public NotifyException newFormatException(String string, long effectivePos) {
		return new NotifyException(null, "Demo corrupted at 0x" + 
				Long.toHexString(effectivePos) + ": " + string);

	}


}