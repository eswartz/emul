/**
 * 
 */
package org.ejs.eulang.symbols;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ast.IAstName;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.ast.impl.AstName;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class BaseSymbol implements ISymbol {

	private IAstName name;
	private IAstNode def;
	private LLType type;

	
	public BaseSymbol(String name, IScope scope, IAstNode def) {
		this.name = new AstName(name, scope);
		Check.checkArg(this.name);
		setDefinition(def);
	}
	public BaseSymbol(IAstName name, IAstNode def) {
		this.name = name;
		setDefinition(def);
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseSymbol other = (BaseSymbol) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\"" + name.getName() + "\"" + ":" +(type != null ? type.toString() : "<unknown>");
	}
	
	@Override
	public IAstName getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.ISymbol#getDefinition()
	 */
	@Override
	public IAstNode getDefinition() {
		return def;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.ISymbol#setDefinition(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public void setDefinition(IAstNode def) {
		this.def = def;
		this.type = def instanceof IAstTypedNode ? ((IAstTypedNode) def).getType() : null;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.ISymbol#getType()
	 */
	@Override
	public LLType getType() {
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.ISymbol#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		this.type = type;
	}
}
