/**
 * 
 */
package org.ejs.eulang.llvm;

import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLArgAttrType extends LLAttrType {

	private final String name;

	/**
	 * @param attrs
	 * @param type
	 */
	public LLArgAttrType(String name, LLAttrs attrs, LLType type) {
		super(attrs, type);
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLAttrType#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " " + "%" + name;
	}
	

}
