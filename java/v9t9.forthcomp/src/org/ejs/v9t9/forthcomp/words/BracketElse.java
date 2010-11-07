/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;

/**
 * If and family.
 * 
 * When a word is defined, push 1 and continue parsing.  The else/then/... words will need to skip
 * (by seeing a 1 on the stack).
 * 
 * Else, consume words until else/then/etc is seen and push 0.  Unparse the trigger word.
 * @author ejs
 *
 */
public class BracketElse extends PreProcWord {

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		falseBranch(hostContext, targetContext);
	}
}
