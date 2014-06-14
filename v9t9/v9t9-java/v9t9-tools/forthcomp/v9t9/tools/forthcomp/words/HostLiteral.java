/*
  HostLiteral.java

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
import v9t9.tools.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class HostLiteral extends BaseWord {

	private final int val;
	private boolean isUnsigned; 
	/**
	 * @param isUnsigned 
	 * 
	 */
	public HostLiteral(int val_, boolean isUnsigned_) {
		this.val = val_;
		this.isUnsigned = isUnsigned_;
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				hostContext.build(HostLiteral.this);
				targetContext.buildLiteral(val, isUnsigned, true);
			}
		});
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
				hostContext.pushData(val);
			}
		});
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LITERAL "  + val;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getValue()
	 */
	public int getValue() {
		return val;
	}
	/**
	 * @param forField the forField to set
	 */
	public void setUnsigned(boolean isUnsigned) {
		this.isUnsigned = isUnsigned;
	}
	public boolean isUnsigned() {
		return isUnsigned;
	}

	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
