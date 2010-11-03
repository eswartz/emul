/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.HostContext;

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
	 * @param isUnsigned 
	 * 
	 */
	public HostDoubleLiteral(int valLo, int valHi, boolean isUnsigned) {
		this.valLo = valLo;
		this.valHi = valHi;
		this.isUnsigned = isUnsigned;
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
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) {
		hostContext.pushData(valLo);
		hostContext.pushData(valHi);
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
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
