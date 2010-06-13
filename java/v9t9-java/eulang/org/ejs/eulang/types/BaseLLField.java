/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.IAstAttributes;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.impl.AstNode;

/**
 * A field in a data declaration.  This instance may be held in only one place
 * for a given instantiation of the data that originally defined it.
 * @author ejs
 *
 */
public class BaseLLField implements ITyped, IAstAttributes {

	protected LLType type;
	protected final String name;
	protected final IAstNode def;
	protected final IAstTypedExpr defaul;
	protected Set<String> attrs;

	/**
	 * Create a field, passing the 'def' IAstNode for reference/errors only
	 * @param name
	 * @param type 
	 * @param def definition of field
	 * @param defaul the default value
	 */
	public BaseLLField(String name, LLType type, IAstNode def, IAstTypedExpr defaul, Set<String> attrs) {
		this.name = name;
		this.type = type;
		this.def = def;
		this.defaul = defaul;
		this.attrs = attrs;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "field : " + type + (defaul != null ? " = " + DumpAST.dumpString(defaul) : "") + ' ' + AstNode.toString(attrs);
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
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAttributes#getAttrs()
	 */
	@Override
	public Set<String> getAttrs() {
		return Collections.unmodifiableSet(attrs);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAttributes#attrs()
	 */
	@Override
	public Set<String> attrs() {
		if (attrs == Collections.<String>emptySet())
			attrs = new HashSet<String>();
		return attrs;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAttributes#hasAttr(java.lang.String)
	 */
	@Override
	public boolean hasAttr(String attr) {
		return attrs.contains(attr);
	}
}
