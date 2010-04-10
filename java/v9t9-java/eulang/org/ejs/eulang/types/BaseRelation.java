/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Arrays;

import org.ejs.eulang.IOperation;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.impl.AstUnaryExpr;

/**
 * @author ejs
 *
 */
public abstract class BaseRelation implements IRelation {

	protected final ITyped head;
	protected final ITyped[] tails;
	protected boolean isComplete;

	public BaseRelation(ITyped head, ITyped tails) {
		this(head, new ITyped[] { tails });
	}
	public BaseRelation(ITyped head, ITyped[] tails) {
		this.head = head;
		this.tails = tails;
		
		updateComplete();
		
		
	}
	protected void updateComplete() {
		if (isCompleteType(head.getType())) {
			isComplete = true;
			for (ITyped tail : tails) {
				if (tail == null || !isCompleteType(tail.getType())) {
					isComplete = false;
					break;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return isComplete;
	}

	protected boolean isCompleteType(LLType type) {
		return type != null && type.isComplete();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(').append(head).append(") <- ");
		boolean first = true;
		for (ITyped tail : tails) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append('(').append(tail).append(')');
		}
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((head == null) ? 0 : head.hashCode());
		result = prime * result + Arrays.hashCode(tails);
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
		BaseRelation other = (BaseRelation) obj;
		if (head == null) {
			if (other.head != null)
				return false;
		} else if (head != other.head)
			return false;
		if (!Arrays.equals(tails, other.tails))
			return false;
		return true;
	}
	@Override
	public ITyped getHead() {
		return head;
	}

	@Override
	public ITyped[] getTails() {
		return tails;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#finalize(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void finalize(TypeEngine typeEngine) throws TypeException {
	}

    protected boolean canInferTypeFrom(ITyped child) {
    	return child != null && child.getType() != null; // && child.getType().isComplete(); 
    }
    protected boolean canReplaceType(ITyped child, LLType candidate) {
    	return child != null && candidate != null
    	&& (child.getType() == null || candidate.isMoreComplete(child.getType()));
    }

    protected boolean updateType(ITyped child, LLType newType) {
    	if (child == null || newType == null)
			return false;
		
		if ((child.getType() == null || !child.getType().isComplete())
				&& newType.isMoreComplete(child.getType())) {
			child.setType(newType);
			return true;
		}
		
		// ignore wildcard casts
		if (child.getType() != null && child.getType().getBasicType() == newType.getBasicType()) {
			if (newType.getBits() != 0 && child.getType().getBits() == 0) {
				child.setType(newType);
				return true;
			}
		}
		
		return false;
    }
 
    protected IAstTypedExpr createCastOn(TypeEngine typeEngine,
			IAstTypedExpr child, LLType newType) {
		if (child == null)
			return null;
		
		// don't cast if we can fill a slot 
		if (child.getType() == null || child.getType().equals(newType)) {
			child.setType(newType);
			return child;
		}
		
		// ignore wildcard casts
		if (child.getType() != null && child.getType().getBasicType() == newType.getBasicType() &&
				newType.getBits() == 0) {
			return child;
		}
		
		child.setParent(null);
		IAstUnaryExpr castExpr = new AstUnaryExpr(IOperation.CAST, child);
		castExpr.setType(newType);
		return castExpr;
	}


}