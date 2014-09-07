/*
  HostDoubleLiteral.java

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
public class HostDoubleLiteral extends BaseWord {

	private final int valLo;
	private final int valHi;
	private boolean isUnsigned;
	/**
	 * @param l 
	 * @param isUnsigned_ 
	 * 
	 */
	public HostDoubleLiteral(int valLo_, int valHi_, boolean isUnsigned_) {
		this.valLo = valLo_;
		this.valHi = valHi_;
		this.isUnsigned = isUnsigned_;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				hostContext.build(HostDoubleLiteral.this);
				targetContext.buildDoubleLiteral(valLo, valHi, isUnsigned, true);
			}
		});
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
				hostContext.pushData(valLo);
				hostContext.pushData(valHi);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.BaseWord#toString()
	 */
	@Override
	public String toString() {
		return "HostDoubleLiteral: " + Integer.toHexString(valHi)+":" + Integer.toHexString(valLo);
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getValue()
	 */
	public int getValueLo() {
		return valLo;
	}
	/**
	 * @return the valHi
	 */
	public int getValueHi() {
		return valHi;
	}
	/**
	 * @param forField the forField to set
	 */
	public void setUnsigned(boolean forField) {
		this.isUnsigned = forField;
	}
	/**
	 * @return the forField
	 */
	public boolean isUnsigned() {
		return isUnsigned;
	}
	@Override
	public boolean isCompilerWord() {
		return true;
	}
}
