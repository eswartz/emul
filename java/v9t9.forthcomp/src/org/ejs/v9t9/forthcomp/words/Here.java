package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class Here extends BaseWord {
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