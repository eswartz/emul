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
public class Do extends BaseStdWord {
	public Do() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		
		targetContext.markHostExecutionUnsupported();
		
		ITargetWord word = (ITargetWord) targetContext.require("(do)");
		word.getCompilationSemantics().execute(hostContext, targetContext);
		
		hostContext.compile(hostContext.find("(do)"));
		
		targetContext.pushHere(hostContext);
		hostContext.pushData(0);		// not ?do
		hostContext.pushPairs(3);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
