/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;

/**
 * Ifdef and family.
 * 
 * When a word is defined, push 1 and continue parsing.  The else/then/... words will need to skip
 * (by seeing a 1 on the stack).
 * 
 * Else, consume words until else/then/etc is seen and push 0.  Unparse the trigger word.
 * @author ejs
 *
 */
public class BracketIfdef extends PreProcWord {

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		String word = hostContext.readToken();
		if (targetContext.find(word) == null) {
			falseBranch(hostContext, targetContext);
		}
	}
}
