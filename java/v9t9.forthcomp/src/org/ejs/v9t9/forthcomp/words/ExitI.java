/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.F99TargetContext;
import org.ejs.v9t9.forthcomp.F99bTargetContext;
import org.ejs.v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class ExitI extends BaseWord {

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		hostContext.assertCompiling();
		if (targetContext instanceof F99TargetContext)
			((F99TargetContext) targetContext).compileExitI();
		else
			((F99bTargetContext) targetContext).compileExitI();
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		
		return true;
	}

}
