/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class HostSwap extends BaseStdWord {

	/**
	 * 
	 */
	public HostSwap() {
	}
	
	@Override
	public String toString() {
		return "SWAP";
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.words.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		int x = hostContext.popData();
		int y = hostContext.popData();
		hostContext.pushData(x);
		hostContext.pushData(y);
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
