/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.F99TargetContext;
import v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class Repeat extends BaseStdWord {
	public Repeat() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		
		int a = hostContext.popData();
		int b = hostContext.popData();
		
		int c = 0;
		// HACK
		if (targetContext instanceof F99TargetContext)
			c = hostContext.popData();
		
		new Again().execute(hostContext, targetContext);
		
		if (targetContext instanceof F99TargetContext)
			hostContext.pushData(c);
		hostContext.pushData(b);
		hostContext.pushData(a - 2);
		
		new Then().execute(hostContext, targetContext);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
