/**
 * 
 */
package v9t9.engine.demos.stream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import ejs.base.utils.CountingInputStream;

/**
 * @author ejs
 *
 */
public class BaseReader {

	protected CountingInputStream is;

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

	public IOException newFormatException(String string) {
		return newFormatException(string, getPosition());
	}

	public IOException newFormatException(String string, long effectivePos) {
		return new IOException("Demo corrupted at 0x" + 
				Long.toHexString(effectivePos) + ": " + string);

	}

	/**
	 * @return the is
	 */
	public CountingInputStream getInputStream() {
		return is;
	}


}