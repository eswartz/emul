/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class DLiteral extends BaseWord {

	private final boolean optimize;

	/**
	 * 
	 */
	public DLiteral(boolean optimize) {
		this.optimize = optimize;
		
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DLITERAL";
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		int valH = hostContext.popData();
		int valL = hostContext.popData();
		targetContext.compileDoubleLiteral(valL, valH, false, optimize);
		
		hostContext.compile(new HostDoubleLiteral(valL, valH, false));
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
