/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public abstract class HostBinOp implements IWord {

	public HostBinOp() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) {
		int l = hostContext.popData();
		int r = hostContext.popData();
		hostContext.pushData(getResult(l, r));
	}
	
	abstract public int getResult(int l, int r);
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
