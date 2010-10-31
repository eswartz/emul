/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.DictEntry;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class TargetColonWord extends TargetWord implements ITargetWord {

	/**
	 * @param entry
	 */
	public TargetColonWord(DictEntry entry) {
		super(entry);
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		throw hostContext.abort("cannot execute compiled word: " + entry.getName());
	}
}
