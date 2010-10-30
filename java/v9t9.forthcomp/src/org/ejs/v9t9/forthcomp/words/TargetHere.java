package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.DictEntry;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.TargetContext;
import org.ejs.v9t9.forthcomp.TargetWord;

/**
 * @author ejs
 *
 */
public class TargetHere extends TargetWord {
	/**
	 * @param entry
	 */
	public TargetHere(DictEntry entry) {
		super(entry);
	}

	public void execute(HostContext hostContext,
			TargetContext targetContext) throws AbortException {
		if (hostContext.isCompiling())
			targetContext.compile(this);
		else
			hostContext.pushData(targetContext.getDP());
	}

	public boolean isImmediate() {
		return false;
	}
}