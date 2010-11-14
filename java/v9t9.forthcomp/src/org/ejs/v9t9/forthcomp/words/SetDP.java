package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;

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