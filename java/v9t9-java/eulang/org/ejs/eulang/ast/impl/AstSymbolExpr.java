/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.Collections;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDataType;
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
		return typedString(symbol.getUniqueName());
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
			// The symbol's expr should have a type. 
			changed = inferTypesFromChildren(new ITyped[] { (ITyped) symbol.getDefinition() });
		}
		
		return changed;
    }
    
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	boolean inferTypeFromChildren__(TypeEngine typeEngine)
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
			
			if (!isUnique || (newType != null &&  newType.isGeneric())) {
				ISymbol instanceSymbol = symbol.getScope().addTemporary(symbol.getName());
				instanceSymbol.setType(newType);
				IAstTypedExpr copy = (IAstTypedExpr) selectedBody.copy(null);
				copy.setType(newType);
				// replace self-refs to symbol
				ISymbol theSymbol = symbol;
				if (copy instanceof IAstDataType) {
					theSymbol = ((IAstDataType) ((IAstDataType) selectedBody).getScope().getOwner()).getTypeName();
					((IAstDataType) copy).setTypeName(instanceSymbol);
				}
				AstNode.replaceSymbols(typeEngine, copy, theSymbol.getScope(), Collections.singletonMap(theSymbol.getNumber(), instanceSymbol));
				AstNode.replaceTypesInTree(typeEngine, copy, Collections.singletonMap(selectedBody.getType(), newType));
				//copy.uniquifyIds();
				instanceSymbol.setDefinition(copy);
				
				//
				
				if (copy instanceof IAstDataType) {
					((IAstDataType) copy).setTypeName(instanceSymbol);
				}
				origSymbol = symbol;
				symbol = instanceSymbol;
				
				//stmt.registerInstance(selectedBody, copy);
				
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
