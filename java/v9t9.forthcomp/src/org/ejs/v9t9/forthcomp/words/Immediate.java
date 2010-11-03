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
public class Immediate extends BaseWord {
	public Immediate() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		((ITargetWord) targetContext.getLatest()).getEntry().setImmediate(targetContext, true);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
