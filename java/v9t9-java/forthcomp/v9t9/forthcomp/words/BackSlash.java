/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ISemantics;

/**
 * @author ejs
 *
 */
public class BackSlash extends BaseWord {
	public BackSlash() {
		setInterpretationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				hostContext.getStream().readToEOL();
			}
		});
		setCompilationSemantics(getInterpretationSemantics());
	}

	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
