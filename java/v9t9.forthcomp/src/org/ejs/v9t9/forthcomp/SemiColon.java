/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public class SemiColon implements IWord {

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		
		hostContext.assertCSP();
		
		hostContext.stopCompiling();
		
		
		ITargetWord semiS = (ITargetWord) targetContext.find(";S");
		if (semiS == null)
			throw hostContext.abort("no ;S");
		
		targetContext.compile(semiS);
		
		((ITargetWord) targetContext.getLatest()).getEntry().setHidden(false);
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
