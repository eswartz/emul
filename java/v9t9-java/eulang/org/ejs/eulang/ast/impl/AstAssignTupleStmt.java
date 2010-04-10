/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstAssignTupleStmt;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTupleNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.InferenceGraph;
import org.ejs.eulang.types.LLTupleType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TupleRelation;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstAssignTupleStmt extends AstTypedExpr implements IAstAssignTupleStmt {

	private IAstTupleNode tuple;
	private IAstTypedExpr expr;

	/**
	 * @param expr2 
	 * @param left
	 * @param right
	 */
	public AstAssignTupleStmt(IAstTupleNode id, IAstTypedExpr expr) {
		setExpr(expr);
		setSymbols(id);
	}

	public IAstAssignTupleStmt copy(IAstNode copyParent) {
		return fixup(this, new AstAssignTupleStmt(doCopy(tuple, copyParent), doCopy(expr, copyParent)));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + ((tuple == null) ? 0 : tuple.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AstAssignTupleStmt other = (AstAssignTupleStmt) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (tuple == null) {
			if (other.tuple != null)
				return false;
		} else if (!tuple.equals(other.tuple))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("()=");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { tuple, expr };
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getExpr() == existing) {
			setExpr((IAstTypedExpr) another);
		} else if (getSymbols() == existing) {
			setSymbols((IAstTupleNode) another);
		} else {
			throw new IllegalArgumentException();
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#equalValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
		return false;
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
	public IAstTupleNode getSymbols() {
		return tuple;
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
	public void setSymbols(IAstTupleNode id) {
		Check.checkArg(id);
		this.tuple = reparent(this.tuple, id);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		// TODO Auto-generated method stub
		super.setType(type);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren()
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine) throws TypeException {
		if (!inferTypesFromChildren(new ITyped[] { expr }))
			return false;
		
		LLType right = expr.getType();
		if (right != null) {
			if (!(right instanceof LLTupleType)) {
				throw new TypeException("unpacking from non-tuple value");
			}
			
			if (((LLTupleType) right).getElementTypes().length != tuple.elements().nodeCount())
				//throw new TypeException("mismatch in tuple sizes: "
				//		+ ((LLTupleType) right).getElementTypes().length + " !=  " + tuple.elements().nodeCount());
				return false;
			
			for (int idx = 0; idx < tuple.elements().nodeCount(); idx++) {
				IAstSymbolExpr sym = tuple.elements().list().get(idx);
				updateType(sym, ((LLTupleType) right).getElementTypes()[idx]);
			}
			updateType(this, right);
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#getTypeRelations(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.InferenceGraph)
	 */
	@Override
	public void getTypeRelations(TypeEngine typeEngine, InferenceGraph graph) {
		graph.addEquivalence(this, expr);
		
		ITyped[] tails = new ITyped[tuple.elements().nodeCount()];
		for (int idx = 0; idx < tails.length; idx++) {
			tails[idx] = tuple.elements().list().get(idx);
		}
		
		graph.add(new TupleRelation(this, tails));
	}
	
}
