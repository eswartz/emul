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
public class UPlusLoop extends BaseStdWord {
	public UPlusLoop() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		hostContext.assertPairs(3);
		
		targetContext.markHostExecutionUnsupported();
		
		ITargetWord word = (ITargetWord) targetContext.require("(u+loop)");
		targetContext.loopCompile(hostContext, word);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
