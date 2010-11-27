/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class Host2Dup extends BaseStdWord {

	/**
	 * 
	 */
	public Host2Dup() {
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.words.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		int v1 = hostContext.popData();
		int v2 = hostContext.popData();
		hostContext.pushData(v2);
		hostContext.pushData(v1);
		hostContext.pushData(v2);
		hostContext.pushData(v1);
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
