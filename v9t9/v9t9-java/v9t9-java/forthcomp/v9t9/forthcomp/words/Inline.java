/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.DictEntry;
import v9t9.forthcomp.HostContext;

/**
 * @author ejs
 *
 */
public class Inline extends BaseStdWord {
	public Inline() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		DictEntry entry = targetContext.getLastEntry();
		if (entry == null)
			hostContext.abort("no target definition found");
		
		entry.setInline(true);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
