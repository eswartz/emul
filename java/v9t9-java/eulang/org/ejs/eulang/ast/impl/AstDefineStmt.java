/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.HashMap;
import java.util.Map;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.symbols.ISymbol;


/**
 * @author ejs
 *
 */
public class AstDefineStmt extends AstStatement implements IAstDefineStmt {

	private IAstSymbolExpr id;
	private IAstTypedExpr expr;
	private Map<ISymbol, IAstTypedExpr> expansions = new HashMap<ISymbol, IAstTypedExpr>();
	
	public AstDefineStmt(IAstSymbolExpr name, IAstTypedExpr expr) {
		this.id = name;
		id.setParent(this);
		setSymbolExpr(name);
		setExpr(expr);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstDefineStmt copy(IAstNode copyParent) {
		return fixup(this, new AstDefineStmt(doCopy(id, copyParent), doCopy(expr, copyParent)));
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 0; //super.hashCode();
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		//if (!super.equals(obj))
		//	return false;
		if (getClass() != obj.getClass())
			return false;
		AstDefineStmt other = (AstDefineStmt) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "DEFINE";
	}
	

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (expr != null)
			return new IAstNode[] { id, expr };
		else
			return new IAstNode[] { id };
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#getDumpChildren()
	 */
	@Override
	public IAstNode[] getDumpChildren() {
		IAstNode[] kids = getChildren();
		if (expansions.size() > 0) {
			IAstNode[] allKids = new IAstNode[kids.length + expansions.size()];
			System.arraycopy(kids, 0, allKids, 0, kids.length);
			int idx = kids.length;
			for (IAstTypedExpr expansion : expansions.values())
				allKids[idx++] = expansion;
			return allKids;
		}
		return kids;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getSymbolExpr() == existing) {
			setSymbolExpr((IAstSymbolExpr) another);
		} else if (getExpr() == existing) {
			setExpr((IAstSymbolExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getExpr()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getId()
	 */
	@Override
	public IAstSymbolExpr getSymbolExpr() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefineStmt#getSymbol()
	 */
	@Override
	public ISymbol getSymbol() {
		return id.getSymbol();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setExpr(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		Check.checkArg(expr);
		this.expr = reparent(this.expr, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setId(v9t9.tools.ast.expr.IAstIdExpression)
	 */
	@Override
	public void setSymbolExpr(IAstSymbolExpr id) {
		Check.checkArg(id);
		this.id = reparent(this.id, id);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren()
	 */
	/*
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine) throws TypeException {
		return inferTypesFromChildren(new ITyped[] { expr, id });
	}
	*/

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefineStmt#expansions()
	 */
	@Override
	public Map<ISymbol, IAstTypedExpr> expansions() {
		return expansions;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes()
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws ASTException {
		// don't worry about symbol type
	}
}
