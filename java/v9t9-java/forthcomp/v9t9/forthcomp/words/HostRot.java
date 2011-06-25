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
public class HostRot extends BaseStdWord {

	/**
	 * 
	 */
	public HostRot() {
	}
	
	@Override
	public String toString() {
		return "ROT";
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.words.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		// a b c
		int c = hostContext.popData();
		int b = hostContext.popData();
		int a = hostContext.popData();
		hostContext.pushData(b);
		hostContext.pushData(c);
		hostContext.pushData(a);	 // b c  a
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
