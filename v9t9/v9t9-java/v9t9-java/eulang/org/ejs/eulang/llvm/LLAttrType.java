/**
 * 
 */
package org.ejs.eulang.llvm;

import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLAttrType {
	private LLType type;
	private LLAttrs attrs;
	private ISymbol typeSymbol;
	/**
	 * @param object
	 * @param iAstArgDef
	 */
	public LLAttrType(LLAttrs attrs, LLType type) {
		this.attrs = attrs;
		this.type = type;
	}
	public LLAttrType(LLAttrs attrs, LLType type, ISymbol typeSymbol) {
		this.attrs = attrs;
		this.type = type;
		this.typeSymbol = typeSymbol;
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
		return (typeSymbol != null ? typeSymbol.getLLVMName() : type.getLLVMName()) + (attrs != null ? attrs + " " : "");
	}
	
}
