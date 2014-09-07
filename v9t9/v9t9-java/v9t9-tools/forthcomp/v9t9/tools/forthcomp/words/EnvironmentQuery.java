/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.TargetContext;

/**
 * @author ejs
 *
 */
public class EnvironmentQuery extends BaseStdWord {

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.BaseStdWord#isImmediate()
	 */
	@Override
	public boolean isImmediate() {
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.BaseStdWord#execute(v9t9.tools.forthcomp.HostContext, v9t9.tools.forthcomp.TargetContext)
	 */
	@Override
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		@SuppressWarnings("unused")
		String str = popString(hostContext, targetContext);
		hostContext.pushData(0);
	}
	
}
