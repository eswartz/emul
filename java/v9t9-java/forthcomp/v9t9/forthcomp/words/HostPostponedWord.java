/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ISemantics;
import v9t9.forthcomp.ITargetWord;
import v9t9.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class HostPostponedWord extends BaseWord {

	private final ITargetWord targetWord;
	private final IWord hostWord;

	/**
	 * @param i
	 */
	public HostPostponedWord(IWord hostWord_, ITargetWord targetWord_) {
		this.hostWord = hostWord_;
		this.targetWord = targetWord_;
		setName(targetWord.getEntry().getName());
		
		setExecutionSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				//hostContext.compileWord(targetContext, hostWord, targetWord);
				if (!targetWord.getEntry().isTargetOnly())
					targetWord.getExecutionSemantics().execute(hostContext, targetContext);
				else if (hostWord != null)
					if (hostContext.isCompiling())
						hostWord.getCompilationSemantics().execute(hostContext, targetContext);
					else
						hostWord.getExecutionSemantics().execute(hostContext, targetContext);
				else
					throw hostContext.abort("cannot invoke POSTPONE'd word -- no host-only definition: " + targetWord.getName());
			}
		});
	}
	

}
