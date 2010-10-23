/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public class Literal implements IWord {

	private final int val;
	private boolean isUnsigned; 
	/**
	 * @param isUnsigned 
	 * 
	 */
	public Literal(int val, boolean isUnsigned) {
		this.val = val;
		this.isUnsigned = isUnsigned;
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#getValue()
	 */
	public int getValue() {
		return val;
	}

	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) {
		hostContext.pushData(val);
	}
	/**
	 * @param forField the forField to set
	 */
	public void setUnsigned(boolean isUnsigned) {
		this.isUnsigned = isUnsigned;
	}
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
