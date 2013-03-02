/*
  BaseStdWord.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
