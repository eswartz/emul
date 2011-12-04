/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;

/**
 * @author ejs
 *
 */
public class BracketChar extends BaseWord {
	/**
	 * 
	 */
	public BracketChar() {
		setCompilationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				String name = hostContext.readToken();
				
				targetContext.compileLiteral(name.charAt(0), false, true);
			}
		});
		setExecutionSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
				String name = hostContext.readToken();
				
				hostContext.pushData(name.charAt(0));
			}
		});
	}

	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
