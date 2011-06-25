/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class QuestionDo extends BaseStdWord {
	public QuestionDo() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
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
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
