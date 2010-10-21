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

		ITargetWord unloop = (ITargetWord) targetContext.find("unloop");
		if (unloop == null)
			throw hostContext.abort("no unloop");
		
		ITargetWord branch = (ITargetWord) targetContext.find("branch");
		if (branch == null)
			throw hostContext.abort("no branch");
		
		targetContext.compile(unloop);
		targetContext.compile(branch);
		
		hostContext.leaves().add(targetContext.pushFixup(hostContext));
		hostContext.popData();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
