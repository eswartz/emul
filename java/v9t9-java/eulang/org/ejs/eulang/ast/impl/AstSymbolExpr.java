/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.ITyped;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author eswartz
 *
 */
public class AstSymbolExpr extends AstTypedExpr implements IAstSymbolExpr {
	private ISymbol symbol;

    public AstSymbolExpr(ISymbol symbol) {
        super();
        setSymbol(symbol);
    }

    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#copy()
     */
    @Override
    public IAstSymbolExpr copy(IAstNode copyParent) {
    	return fixup(this, new AstSymbolExpr(symbol));
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
		return symbol.toString();
	}
    	
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getChildren()
     */
    public IAstNode[] getChildren() {
        return NO_CHILDREN;
    }
    @Override
	public void replaceChildren(IAstNode[] children) {
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
		return inferTypesFromChildren(new ITyped[] { symbol });

	}
}
