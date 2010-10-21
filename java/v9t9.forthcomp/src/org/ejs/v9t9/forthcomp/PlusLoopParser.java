/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public class PlusLoopParser implements IWord {
	public PlusLoopParser() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		hostContext.assertPairs(3);
		
		ITargetWord word = (ITargetWord) targetContext.find("(+loop)");
		if (word == null)
			throw hostContext.abort("no (+loop) word");
		
		targetContext.loopCompile(hostContext, word);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
