package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class Here extends BaseStdWord {
	@Override
	public String toString() {
		return "HERE";
	}

	public void execute(HostContext hostContext,
			TargetContext targetContext) throws AbortException {
		hostContext.pushData(targetContext.getDP());
	}

	public boolean isImmediate() {
		return false;
	}
}