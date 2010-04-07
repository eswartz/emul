/**
 * 
 */
package org.ejs.eulang.llvm;

import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLAttrType {
	private LLType type;
	private LLAttrs attrs;
	/**
	 * @param object
	 * @param iAstArgDef
	 */
	public LLAttrType(LLAttrs attrs, LLType type) {
		this.attrs = attrs;
		this.type = type;
	}
	public LLType getType() {
		return type;
	}
	public LLAttrs getAttrs() {
		return attrs;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return type + (attrs != null ? attrs + " " : "");
	}
	
}
