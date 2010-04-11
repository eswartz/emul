/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
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
	private boolean isAddress;

    public AstSymbolExpr(ISymbol symbol) {
        super();
        setSymbol(symbol);
        setAddress(false);
    }
    public AstSymbolExpr(ISymbol symbol, boolean isAddress) {
    	super();
    	setSymbol(symbol);
    	setAddress(isAddress);
    }

    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#copy()
     */
    @Override
    public IAstSymbolExpr copy(IAstNode copyParent) {
    	return fixup(this, new AstSymbolExpr(symbol, isAddress));
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
		return (isAddress ? "&" : "") + symbol.toString();
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
    	//setType(symbol.getType());
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
	 * @see org.ejs.eulang.ast.IAstTypedExpression#getType()
	 */
	@Override
	public LLType getType() {
		return symbol.getType();
		//return super.getType();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedExpression#setType(org.ejs.eulang.llvm.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		//super.setType(type);
		symbol.setType(type);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		
		/*if ( symbol.getDefinition() instanceof IAstDefineStmt) {
			IAstDefineStmt define = (IAstDefineStmt) symbol.getDefinition();
			if (canInferTypeFrom(define.getExpr())) {
				return updateType(this, define.getExpr().getType());
			}
		}*/
		
		//if (getSymbol().getDefinition() instanceof IAstTypedNode)
		//	if ( canInferTypeFrom((IAstTypedNode) getSymbol().getDefinition()))
		//		return updateType(symbol, (((IAstTypedNode)symbol.getDefinition()).getType()));
		return false;

	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstSymbolExpr#isAddress()
	 */
	@Override
	public boolean isAddress() {
		return isAddress;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstSymbolExpr#setAddress(boolean)
	 */
	@Override
	public void setAddress(boolean isAddress) {
		this.isAddress = isAddress;
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
		if (symbol.getDefinition() == null)
			return null;
		if (symbol.getDefinition() instanceof IAstDefineStmt)
			return ((IAstDefineStmt) symbol.getDefinition());
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
		IAstDefineStmt def = getDefinition();
		if (def == null) 
			return null;
		IAstTypedExpr body = def.getMatchingBodyExpr(getType());
		if (body == null) 
			return null;
		IAstTypedExpr instance = def.getMatchingInstance(body.getType(), getType());
		if (instance == null)
			return body;
		return instance;
	}
	
}
