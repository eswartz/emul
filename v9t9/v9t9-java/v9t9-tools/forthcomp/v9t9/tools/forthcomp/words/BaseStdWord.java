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
public abstract class BaseStdWord extends BaseWord {

	/**
	 * 
	 */
	public BaseStdWord() {
		if (isImmediate()) {
			setInterpretationSemantics(new ISemantics() {
				
				public void execute(HostContext hostContext, TargetContext targetContext)
						throws AbortException {
					throw hostContext.abort(getName()+ ": cannot invoke in interpret mode");
				}
			});
			setCompilationSemantics(new ISemantics() {
				
				public void execute(HostContext hostContext, TargetContext targetContext)
						throws AbortException {
					BaseStdWord.this.execute(hostContext, targetContext);
				}
			});
		} else {
			setExecutionSemantics(new ISemantics() {
			
				public void execute(HostContext hostContext, TargetContext targetContext)
				throws AbortException {
					BaseStdWord.this.execute(hostContext, targetContext);
				}
			});
			setCompilationSemantics(new ISemantics() {
				
				public void execute(HostContext hostContext, TargetContext targetContext)
						throws AbortException {
					hostContext.compile(BaseStdWord.this);
				}
			});
		}
	}
	abstract public boolean isImmediate();
	abstract public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException;
	

	@Override
	public boolean isCompilerWord() {
		return isImmediate();
	}
}
