/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ITargetWord;
import org.ejs.v9t9.forthcomp.IWord;
import org.ejs.v9t9.forthcomp.LocalVariableTriple;

/**
 * Placeholder for local used in a colon-colon def
 * @author ejs
 *
 */
public class To implements IWord {

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		String name = hostContext.readToken();
		
		if (hostContext.isCompiling()) { 
			LocalVariableTriple triple = ((ITargetWord) targetContext.getLatest()).getEntry().findLocal(name);
			if (triple != null) {
				targetContext.compileToLocal(triple.var.getIndex());
				return;
			}
		}
		
		IWord word = targetContext.find(name);
		if (word == null)
			throw hostContext.abort(name + "?");
		
		if (!(word instanceof TargetValue))
			throw hostContext.abort("cannot handle " + name);
		
		if (hostContext.isCompiling()) {
			targetContext.compileToValue((TargetValue) word);
		}
		else {
			((TargetValue) word).setValue(targetContext, hostContext.popData());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
