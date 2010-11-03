/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import java.util.Map;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ITargetWord;
import org.ejs.v9t9.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class CompileComma extends BaseWord {

	@Override
	public String toString() {
		return "compile,";
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		int val = hostContext.popData();
		for (Map.Entry<String, IWord> entry : targetContext.getDictionary().entrySet()) {
			if (entry.getValue() instanceof ITargetWord
					&& ((ITargetWord) entry.getValue()).getEntry().getContentAddr() == val) {
				targetContext.compile((ITargetWord) entry.getValue());
				return;
			}
		}
		throw hostContext.abort("cannot compile, a host word at " + val);
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
