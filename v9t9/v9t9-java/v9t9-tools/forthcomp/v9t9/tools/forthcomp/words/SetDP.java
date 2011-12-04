package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class SetDP extends BaseStdWord {
	public void execute(HostContext hostContext,
			TargetContext targetContext) throws AbortException {
		targetContext.setDP(hostContext.popData());
	}

	public boolean isImmediate() {
		return false;
	}
}