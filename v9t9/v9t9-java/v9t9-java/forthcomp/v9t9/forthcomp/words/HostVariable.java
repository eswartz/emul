/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class HostVariable extends BaseStdWord {

	private int val;
	/**
	 * 
	 */
	public HostVariable(int val) {
		this.val = val;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getValue()
	 */
	public int getValue() {
		return val;
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) {
		hostContext.pushData(val);
	}
	/**
	 * @param i
	 */
	public void setValue(int i) {
		this.val = i;
		
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
