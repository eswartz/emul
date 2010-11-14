/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ISemantics;

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
				hostContext.compile(HostDoubleLiteral.this);
				targetContext.compileDoubleLiteral(valLo, valHi, isUnsigned, true);
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
	 * @see org.ejs.v9t9.forthcomp.IWord#getValue()
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
}
