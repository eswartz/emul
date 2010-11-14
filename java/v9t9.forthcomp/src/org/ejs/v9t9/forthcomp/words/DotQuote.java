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
public class DotQuote extends BaseStdWord {

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.words.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		new SQuote().execute(hostContext, targetContext);
		if (hostContext.isCompiling()) {
			IWord hostType = hostContext.require("type");
			ITargetWord type = (ITargetWord) targetContext.require("type");
			hostContext.compileWord(targetContext, hostType, type);
		} else {
			new HostType().execute(hostContext, targetContext);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
