/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstCodeExpr;
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
	
    public AstSymbolExpr(ISymbol symbol) {
        super();
        setSymbol(symbol);
    }
    public AstSymbolExpr(ISymbol symbol, boolean isAddress) {
    	super();
    	setSymbol(symbol);
    }
    public AstSymbolExpr(AstSymbolExpr other) {
    	// avoid side effects
    	this.origSymbol = other.origSymbol;
    	this.symbol = other.symbol;
    	this.type = other.type;
    }

    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#copy()
     */
    @Override
    public IAstSymbolExpr copy(IAstNode copyParent) {
    	return fixup(this, new AstSymbolExpr(this));
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 22;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstSymbolExpr other = (AstSymbolExpr) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString(symbol.getName());
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
    	this.symbol = symbol;
    	setType(symbol.getType());
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
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		
		boolean changed = false;
		
		// When a symbol expression is first created, it may point to a
		// define statement, which in turn may have several possible options
		// with several different types, depending on context.
		//
		// It is up to the owner of this symbol expression to decide when
		// enough information has been deduced, at which time it will
		// set this node's type.  Once set, then here, we will create a new symbol
		// pointing to the specific definition that matches. 
		
		IAstDefineStmt stmt = getDefinition();
		if (origSymbol == null && stmt != null) {
			LLType newType = getType();
			
			IAstTypedExpr selectedBody = null;
			boolean isUnique = false;
			if (stmt.bodyList().size() == 1) {
				// no question
				//changed = inferTypesFromChildren(new ITyped[] { symbol, stmt.bodyList().get(0) });
				selectedBody = stmt.bodyList().get(0);
				isUnique = true;
			} else {
				// Multiple choices.  This expr will take the type from a parent node with more context. 
				if (getType() == null)
					return false;
				
				selectedBody = stmt.getMatchingBodyExpr(getType());
				
				if (selectedBody == null)
					return false;
			}
			
			// ignore macros here
			if (selectedBody instanceof IAstCodeExpr && ((IAstCodeExpr) selectedBody).isMacro())
				return false;
			
			if (selectedBody.getType() != null && selectedBody.getType().isMoreComplete(newType))
				newType = selectedBody.getType();
			
			if (true || !isUnique) {
				ISymbol instanceSymbol = symbol.getScope().addTemporary(symbol.getName(),
						false);
				instanceSymbol.setType(newType);
				IAstTypedExpr copy = (IAstTypedExpr) selectedBody.copy(null);
				copy.setType(newType);
				//copy.uniquifyIds();
				instanceSymbol.setDefinition(copy);
				
				origSymbol = symbol;
				symbol = instanceSymbol;
				setType(newType);
			} else {
				super.setType(newType);
				selectedBody.setType(newType);
			}
			
			//);
			
		} else if (symbol.getDefinition() instanceof ITyped) {
			// The symbol's expr should have a type. 
			changed = inferTypesFromChildren(new ITyped[] { (ITyped) symbol.getDefinition() });
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
		IAstTypedExpr instance = def.getMatchingInstance(body.getType(), getType());
		if (instance == null)
			return body;
		return instance;
	}
	
}
