/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;

/**
 * A field in a data declaration.  This instance may be held in only one place
 * for a given instantiation of the data that originally defined it.
 * @author ejs
 *
 */
public class BaseLLField implements ITyped {

	protected LLType type;
	protected final String name;
	protected final IAstNode def;
	protected final IAstTypedExpr defaul;

	/**
	 * Create a field, passing the 'def' IAstNode for reference/errors only
	 * @param name
	 * @param type 
	 * @param def definition of field
	 * @param defaul the default value
	 */
	public BaseLLField(String name, LLType type, IAstNode def, IAstTypedExpr defaul) {
		this.name = name;
		this.type = type;
		this.def = def;
		this.defaul = defaul;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "field : " + type + (defaul != null ? " = " + DumpAST.dumpString(defaul) : "");
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the def
	 */
	public IAstNode getDefinition() {
		return def;
	}
	
	public LLType getType() {
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ITyped#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		this.type = type;
		if (def instanceof ITyped)
			((ITyped) def).setType(type);
	}
	/**
	 * @return the defaul
	 */
	public IAstTypedExpr getDefault() {
		return defaul;
	}
	
}
