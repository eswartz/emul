/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstTupleExpr;
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
public class AstTupleExpr extends AstTypedExpr implements IAstTupleExpr {

	private IAstNodeList<IAstTypedExpr> elements;
	/**
	 * 
	 */
	public AstTupleExpr(IAstNodeList<IAstTypedExpr> elements) {
		this.elements = reparent(this.elements, elements);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("TUPLE");
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstTupleExpr copy(IAstNode copyParent) {
		return new AstTupleExpr(doCopy(elements, copyParent));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 11; //super.hashCode();
		result = prime * result
				+ ((elements == null) ? 0 : elements.hashCode());
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
		AstTupleExpr other = (AstTupleExpr) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTupleNode#elements()
	 */
	@Override
	public IAstNodeList<IAstTypedExpr> elements() {
		return elements;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { elements };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == elements())
			this.elements = (IAstNodeList<IAstTypedExpr>) reparent(this.elements, another);
		else
			throw new IllegalArgumentException();
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedExpr#equalValue(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
		return false;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		LLTupleType newType = null;
		
		if (getType() != null && getType().isComplete() && getType() instanceof LLTupleType) {
			newType = (LLTupleType) getType();
		} else {
			LLType[] tupleTypes = new LLType[elements.nodeCount()];
			for (int idx = 0; idx < elements.nodeCount(); idx++)
				tupleTypes[idx] = elements.list().get(idx).getType();
			newType = new LLTupleType(typeEngine, tupleTypes);
		}
		boolean changed = false;
		
		if (elements.nodeCount() != newType.getElementTypes().length)
			throw new TypeException("mismatched sizes in tuples: " + 
					elements.nodeCount() + " != " + newType.getElementTypes().length);
			
		if (adaptToType(newType))
			changed = true;
		changed |= updateType(this, newType);
		
		return changed;
	}
	
	protected boolean adaptToType(LLTupleType tupleType) {
		boolean changed = false;
		for (int idx = 0; idx < elements.nodeCount(); idx++)
			changed |= updateType(elements.list().get(idx), tupleType.getElementTypes()[idx]);
		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#getTypeRelations(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.InferenceGraph)
	 */
	@Override
	public void getTypeRelations(TypeEngine typeEngine, InferenceGraph graph) {
		ITyped[] tails = new ITyped[elements.nodeCount()];
		for (int idx = 0; idx < tails.length; idx++) {
			tails[idx] = elements.list().get(idx);
		}
		
		graph.add(new TupleRelation(this, tails));
	}

}
