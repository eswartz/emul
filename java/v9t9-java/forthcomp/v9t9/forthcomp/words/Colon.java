/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class Colon extends BaseStdWord {

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		String name = hostContext.readToken();
		
		hostContext.setCompiling(true);
		
		hostContext.setCSP();
		
		targetContext.defineColonWord(name);
		
		ITargetWord targetWord = (ITargetWord) targetContext.getLatest();
		
		targetWord.getEntry().setHidden(true);
		targetWord.setHostDp(hostContext.getLocalDP());
		
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
