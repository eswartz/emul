/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class HostDoDoes extends BaseWord {

	private final int redirectDp;
	private final int targetDP;

	/**
	 * @param redirectDp
	 * @param targetDP 
	 * 
	 */
	public HostDoDoes(int redirectDp, int targetDP) {
		this.redirectDp = redirectDp;
		this.targetDP = targetDP;
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.words.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		hostContext.popCall();
		hostContext.compile(new HostBranch(redirectDp));
		
		ITargetWord lastWord = (ITargetWord) targetContext.getLatest();
		targetContext.getDictionary().put(lastWord.getName(), new TargetDoesWord(lastWord, redirectDp));
		
		targetContext.compileDoes(hostContext, lastWord.getEntry(), targetDP);
		//hostContext.setHostPc(redirectDp);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
	
}
