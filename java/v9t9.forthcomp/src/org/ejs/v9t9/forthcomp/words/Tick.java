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
public class Tick extends BaseStdWord {
	public Tick() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		String name = hostContext.readToken();

		IWord word = targetContext.find(name);
		if (word == null) {
			word = targetContext.defineForward(name, hostContext.getStream().getLocation());
		}
		
		if (!(word instanceof ITargetWord))
			throw hostContext.abort("cannot take address of host word " + name);
		
		hostContext.pushData(((ITargetWord)word).getEntry().getContentAddr());
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
