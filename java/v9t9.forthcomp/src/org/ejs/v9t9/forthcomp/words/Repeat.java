/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.IWord;
import org.ejs.v9t9.forthcomp.TargetContext;

/**
 * @author ejs
 *
 */
public class Repeat implements IWord {
	public Repeat() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		
		int a = hostContext.popData();
		int b = hostContext.popData();
		int c = hostContext.popData();
		
		new Again().execute(hostContext, targetContext);
		
		hostContext.pushData(c);
		hostContext.pushData(b);
		hostContext.pushData(a - 2);
		
		new Then().execute(hostContext, targetContext);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
