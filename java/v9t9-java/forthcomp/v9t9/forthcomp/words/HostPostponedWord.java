/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ITargetWord;
import v9t9.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class HostPostponedWord extends BaseStdWord {

	private final ITargetWord targetWord;
	private final IWord hostWord;

	/**
	 * @param i
	 */
	public HostPostponedWord(IWord hostWord, ITargetWord targetWord) {
		this.hostWord = hostWord;
		this.targetWord = targetWord;
		
		setName(targetWord.getEntry().getName());
	}
	

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.HostContext, v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		hostContext.compileWord(targetContext, hostWord, targetWord);
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
