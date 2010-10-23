/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public class QuestionDoParser implements IWord {
	public QuestionDoParser() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		
		ITargetWord qdoWord = (ITargetWord) targetContext.require("(?do)");

		targetContext.compile(qdoWord);	// ends in 0branch
		targetContext.pushFixup(hostContext);
		
		targetContext.pushHere(hostContext);
		hostContext.pushData(-1);		// ?do
		hostContext.pushPairs(3);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
