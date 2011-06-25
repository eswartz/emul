package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class HostExitWord extends BaseStdWord {
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EXIT";
	}

	public void execute(HostContext hostContext,
			TargetContext targetContext) throws AbortException {
		hostContext.popCall();
	}

	public boolean isImmediate() {
		return false;
	}
}