package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class SetDP implements IWord {
	public void execute(HostContext hostContext,
			TargetContext targetContext) throws AbortException {
		targetContext.setDP(hostContext.popData());
	}

	public boolean isImmediate() {
		return false;
	}
}