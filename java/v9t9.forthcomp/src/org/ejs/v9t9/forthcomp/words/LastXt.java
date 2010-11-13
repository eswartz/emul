package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class LastXt extends BaseWord {
	@Override
	public String toString() {
		return "LASTXT";
	}

	public void execute(HostContext hostContext,
			TargetContext targetContext) throws AbortException {
		hostContext.pushData(((ITargetWord) targetContext.getLatest()).getEntry().getContentAddr());
	}

	public boolean isImmediate() {
		return false;
	}
}