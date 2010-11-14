/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class QuestionDo extends BaseStdWord {
	public QuestionDo() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		
		targetContext.markHostExecutionUnsupported();
		
		ITargetWord qdoWord = (ITargetWord) targetContext.require("(?do)");

		qdoWord.getCompilationSemantics().execute(hostContext, targetContext);// ends in 0branch
		
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
