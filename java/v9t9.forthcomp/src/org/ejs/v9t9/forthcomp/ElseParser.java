/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public class ElseParser implements IWord {
	public ElseParser() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		hostContext.assertPairs(2);
		
		ITargetWord word = (ITargetWord) targetContext.require("branch");
		
		targetContext.compile(word);
		
		targetContext.pushFixup(hostContext);
		targetContext.swapFixup(hostContext);
		
		IWord then = hostContext.require("then");
		
		hostContext.pushPairs(2);
		
		then.execute(hostContext, targetContext);
		
		hostContext.pushPairs(2);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
