/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * @author ejs
 *
 */
public class BaseReader {

	protected final InputStream is;
	protected int isPos;

	/**
	 * 
	 */
	public BaseReader(InputStream is) {
		this.is = is instanceof BufferedInputStream ? is : new BufferedInputStream(is);

	}

}