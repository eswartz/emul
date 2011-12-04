/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class DotQuote extends BaseWord {

	/**
	 * 
	 */
	public DotQuote() {
		setInterpretationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				new SQuote().getInterpretationSemantics().execute(hostContext, targetContext);
				new HostType().execute(hostContext, targetContext);
			}
		});
		
		setCompilationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				new SQuote().getCompilationSemantics().execute(hostContext, targetContext);
				IWord hostType = hostContext.require("type");
				ITargetWord type = (ITargetWord) targetContext.require("type");
				hostContext.compileWord(targetContext, hostType, type);
			}
		});
	}

	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
