/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public class DoParser implements IWord {
	public DoParser() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		
		ITargetWord word = (ITargetWord) targetContext.find("(do)");
		if (word == null)
			throw hostContext.abort("no (do)");
		
		targetContext.compile(word);
		
		targetContext.pushHere(hostContext);
		hostContext.pushPairs(3);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
