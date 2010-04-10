/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalScope;
import org.ejs.eulang.types.InferenceGraph;
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
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedExpression#setType(org.ejs.eulang.llvm.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		symbol.setType(type);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		//if (getSymbol().getDefinition() instanceof IAstTypedNode)
		//	if ( canInferTypeFrom((IAstTypedNode) getSymbol().getDefinition()))
		//		return updateType(symbol, (((IAstTypedNode)symbol.getDefinition()).getType()));
		return false;

	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#getTypeRelations(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.InferenceGraph)
	 */
	@Override
	public void getTypeRelations(TypeEngine typeEngine, InferenceGraph graph) {
		//if (symbol.getScope() instanceof LocalScope)
		//	graph.addEquivalence(this, symbol);
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
	public void validateType(TypeEngine typeEngine) throws ASTException {
		if (!(getParent() instanceof IAstDefineStmt)) {
			super.validateType(typeEngine);
		}
	}
}
