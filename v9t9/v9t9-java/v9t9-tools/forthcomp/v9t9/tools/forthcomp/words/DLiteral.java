/*
  DLiteral.java

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
import v9t9.tools.forthcomp.TargetContext;

/**
 * @author ejs
 *
 */
public class DLiteral extends BaseWord {

	private final boolean optimize;

	/**
	 * 
	 */
	public DLiteral(boolean optimize_) {
		this.optimize = optimize_;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				int valH = hostContext.popData();
				int valL = hostContext.popData();
				targetContext.compileDoubleLiteral(valL, valH, false, optimize);
				
				hostContext.build(new HostDoubleLiteral(valL, valH, false));
			}
		});
		setInterpretationSemantics(getCompilationSemantics());
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DLITERAL";
	}
	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
