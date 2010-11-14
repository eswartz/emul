/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.DictEntry;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ISemantics;
import org.ejs.v9t9.forthcomp.IWord;

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
}
