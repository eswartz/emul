/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author eswartz
 *
 */
public class AstSymbolExpr extends AstTypedExpr implements IAstSymbolExpr {
	private ISymbol symbol;
	private ISymbol origSymbol;
	private boolean owns;
	
    public AstSymbolExpr(boolean owns, ISymbol symbol) {
        super();
        this.owns = owns;
        setSymbol(symbol);
    }
    public AstSymbolExpr(AstSymbolExpr other) {
    	// avoid side effects
    	this.origSymbol = other.origSymbol;
    	this.symbol = other.symbol;
    	this.type = other.type;
    	this.owns = other.owns;
    }

    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#copy()
     */
    @Override
    public IAstSymbolExpr copy() {
    	//return fixup(this, new AstSymbolExpr(this));
    	AstSymbolExpr copy = new AstSymbolExpr(this);
    	copy.setSourceRef(getSourceRef());
    	return copy;
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 22;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		//result = prime * result + (owns ? 13942: 0);
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
		AstSymbolExpr other = (AstSymbolExpr) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		//if (owns != other.owns)
		//	return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString(symbol.getUniqueName()) + (owns ? " [owned]" : "");
	}
    	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstSymbolExpr#isOwned()
	 */
	@Override
	public boolean isOwned() {
		return owns;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstSymbolExpr#setOwned(boolean)
	 */
	@Override
	public void setOwned(boolean owned) {
		this.owns = owned;
	}
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getChildren()
     */
    public IAstNode[] getChildren() {
        return NO_CHILDREN;
    }
    /* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		throw new IllegalArgumentException();
	}
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstSymbolExpr#getSymbol()
     */
    @Override
    public ISymbol getSymbol() {
    	return symbol;
    }
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstSymbolExpr#setSymbol(org.ejs.eulang.symbols.ISymbol)
     */
    @Override
    public void setSymbol(ISymbol symbol) {
    	Check.checkArg(symbol);
    	
    	//if (origSymbol == null)
    	//	origSymbol = this.symbol;
    	
    	this.symbol = symbol;
    	setType(symbol.getType());
    }

    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstSymbolExpr#getOriginalSymbol()
     */
    @Override
    public ISymbol getOriginalSymbol() {
    	return origSymbol != null ? origSymbol : symbol;
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.impl.AstTypedNode#getType()
     */
    @Override
    public LLType getType() {
    	//if (type == null)
    	//	return symbol.getType();
    	//else
    	//	return type;
    	if (super.getType() != null)
    		return super.getType();
    	return symbol.getType();
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.impl.AstTypedNode#setType(org.ejs.eulang.types.LLType)
     */
    @Override
    public void setType(LLType type) {
    	super.setType(type);
    	if (type != null && !(symbol.getDefinition() instanceof IAstDefineStmt)) {
    		if (symbol.getType() == null || owns)
    			symbol.setType(type);
    	}
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#simplify()
     */
    public IAstTypedExpr simplify() {
        return this;
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#equalValue(v9t9.tools.decomp.expr.IAstExpression)
     */
    public boolean equalValue(IAstTypedExpr expr) {
        return expr instanceof IAstSymbolExpr
        && ((IAstSymbolExpr) expr).getSymbol().equals(getSymbol());
    }
    

    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
     */
    @Override
    public boolean inferTypeFromChildren(TypeEngine typeEngine)
    		throws TypeException {

		boolean changed = false;
		
		// When a symbol expression is first created, it may point to a
		// define statement, which in turn may have several possible options
		// with several different types, depending on context.
		//
		// The type of this symbol becomes the type of the most appropriate
		// definition, which may be generic.  It is up to an expansion phase 
		// to instantiate any generics.
		
		IAstDefineStmt stmt = getDefinition();
		if (stmt != null && origSymbol == null) {
			IAstTypedExpr selectedBody = null;
			selectedBody = getInstance();
			if (selectedBody == null) {
				if (getType() == null) {
					selectedBody = getBody();
				}
			}
			if (selectedBody == null)
				return false;
			
			changed |= updateType(this, selectedBody.getType());
		} else if (symbol.getDefinition() instanceof ITyped) {
			if (!owns) {
				// honor the real type if it differs, but help if we have better info
				if (getType() != null && symbol.getType() != null && !getType().equals(symbol.getType())) {
					if (getType().isMoreComplete(symbol.getType()))
						symbol.setType(getType());
					else
						setType(symbol.getType());
					changed = true;
				}
			}
			else {
				// The symbol's expr should have a type. 
				changed = inferTypesFromChildren(new ITyped[] { (ITyped) symbol.getDefinition() });
			}
		}
		
		return changed;
    }
    
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateType()
	 */
	@Override
	public void validateType(TypeEngine typeEngine) throws TypeException {
		if (!(getParent() instanceof IAstDefineStmt)) {
			super.validateType(typeEngine);
		}
		if (getDefinition() != null && getInstance() == null)
			throw new TypeException(this, "could not find an instance for symbol " + getSymbol());
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstSymbolExpr#getDefinition()
	 */
	@Override
	public IAstDefineStmt getDefinition() {
		ISymbol theSymbol = symbol;
		if (origSymbol != null) {
			theSymbol = origSymbol;
		}
		if (theSymbol.getDefinition() == null)
			return null;
		if (theSymbol.getDefinition() instanceof IAstDefineStmt)
			return ((IAstDefineStmt) theSymbol.getDefinition());
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstSymbolExpr#getBody()
	 */
	@Override
	public IAstTypedExpr getBody() {
		IAstDefineStmt def = getDefinition();
		if (def == null) 
			return null;
		return def.getMatchingBodyExpr(getType());
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstSymbolExpr#getInstance()
	 */
	@Override
	public IAstTypedExpr getInstance() {
		if (origSymbol != null)
			return (IAstTypedExpr) symbol.getDefinition();
		IAstDefineStmt def = getDefinition();
		if (def == null) { 
			/*if (symbol.getDefinition() instanceof IAstTypedExpr)
				return (IAstTypedExpr)symbol.getDefinition();
			else*/
				return null;
		}
		IAstTypedExpr body = def.getMatchingBodyExpr(getType());
		if (body == null) 
			return null;
		if (body.getType() != null && body.getType().isGeneric()) {
			ISymbol instanceSym = def.getMatchingInstance(body.getType(), getType());
			if (instanceSym == null)
				return null;
			return (IAstTypedExpr) instanceSym.getDefinition();
		}
		return body;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstSymbolExpr#setOriginalSymbol(org.ejs.eulang.symbols.ISymbol)
	 */
	@Override
	public void setOriginalSymbol(ISymbol symbol) {
		this.origSymbol = symbol;
	}
}
