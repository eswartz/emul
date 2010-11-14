/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ITargetWord;
import org.ejs.v9t9.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class Else extends BaseStdWord {
	public Else() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		hostContext.assertPairs(2);
		
		ITargetWord word = (ITargetWord) targetContext.require("branch");
		word.getCompilationSemantics().execute(hostContext, targetContext);
		
		hostContext.compile(hostContext.require("branch"));
		
		targetContext.pushFixup(hostContext);
		targetContext.swapFixup(hostContext);
		
		Then then = (Then) hostContext.require("then");
		
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
