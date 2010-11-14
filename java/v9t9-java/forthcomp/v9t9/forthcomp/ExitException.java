/**
 * 
 */
package v9t9.forthcomp;

import v9t9.forthcomp.AbortException;

/**
 * @author ejs
 *
 */
public class ExitException extends AbortException {

	/**
	 * @param string
	 */
	public ExitException() {
		super("Exit");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6849411457765574521L;

}
