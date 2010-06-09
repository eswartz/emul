/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import org.ejs.eulang.IRegClass;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class RegisterLocal extends BaseLocal {

	private final IRegClass regClass;
	private int vr;
	private boolean regPair;

	/**
	 * @param name
	 * @param type
	 * @param size in bits
	 * @param vr 
	 */
	public RegisterLocal(IRegClass regClass, ISymbol name, LLType type, int vr) {
		super(name, type);
		this.regClass = regClass;
		this.vr = vr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.BaseLocal#toString()
	 */
	@Override
	public String toString() {
		return "register local vr" + vr + "("+ getName().getName() +") [" + getType() +"]"+  (regPair ? " (pair)" : "");
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((regClass == null) ? 0 : regClass.hashCode());
		result = prime * result + vr;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegisterLocal other = (RegisterLocal) obj;
		if (regClass == null) {
			if (other.regClass != null)
				return false;
		} else if (!regClass.equals(other.regClass))
			return false;
		if (vr != other.vr)
			return false;
		return true;
	}

	/**
	 * @return the regClass
	 */
	public IRegClass getRegClass() {
		return regClass;
	}
	
	/**
	 * @return the vr
	 */
	public int getVr() {
		return vr;
	}
	/**
	 * @param vr the vr to set
	 */
	public void setVr(int vr) {
		this.vr = vr;
	}
	
	public boolean isPhysReg() {
		return vr < regClass.getRegisterCount();
	}
	
	public boolean isRegPair() {
		return regPair;
	}
	public void setRegPair(boolean regPair) {
		this.regPair = regPair;
	}

}
