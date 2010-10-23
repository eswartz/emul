/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public class LeaveParser implements IWord {
	public LeaveParser() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();

		//ITargetWord unloop = (ITargetWord) targetContext.require("unloop");
		
		ITargetWord branch = (ITargetWord) targetContext.require("branch");
		
		//targetContext.compile(unloop);
		targetContext.compile(branch);
		
		targetContext.pushLeave(hostContext);
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
