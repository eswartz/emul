/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class HostDoubleLiteral implements IWord {

	private final long val;
	private boolean isUnsigned;
	/**
	 * @param isUnsigned 
	 * 
	 */
	public HostDoubleLiteral(long val, boolean isUnsigned) {
		this.val = val;
		this.isUnsigned = isUnsigned;
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#getValue()
	 */
	public int getValue() {
		return (int) val;
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) {
		hostContext.pushData((int) (val & 0xffff));
		hostContext.pushData((int) (val >> 16));
	}
	/**
	 * @param forField the forField to set
	 */
	public void setUnsigned(boolean forField) {
		this.isUnsigned = forField;
	}
	/**
	 * @return the forField
	 */
	public boolean isUnsigned() {
		return isUnsigned;
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
