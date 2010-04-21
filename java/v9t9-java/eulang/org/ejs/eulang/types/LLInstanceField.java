/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;

/**
 * An instance field in a data declaration.  This class instance may be held in only one place
 * for a given instantiation of the data that originally defined it.
 * @author ejs
 *
 */
public class LLInstanceField extends BaseLLField {

	private int offset;

	public LLInstanceField(String name, LLType type, IAstNode def, IAstTypedExpr defaul) {
		super(name, type, def, defaul);
		this.offset = -1;
	}
	
	@Override
	public String toString() {
		return "@" + offset + " " + super.toString();
	}
	
	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @param offset
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
}
