/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IOperation;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public abstract class AstTypedNode extends AstNode implements IAstTypedNode {

	protected LLType type;

	/**
	 * 
	 */
	public AstTypedNode() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 111;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		AstTypedNode other = (AstTypedNode) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return type != null ? type.toString() : "<unknown>";
	}
	
	
	@Override
	public LLType getType() {
		return type;
	}

	@Override
	public void setType(LLType type) {
		this.type = type;
	}

	public String typedString(String input) {
		return input + " [" + (getType() != null ? getType().toString() : "<unknown>") + "]";
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateType(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateType(TypeEngine typeEngine) throws TypeException {
		LLType thisType = getType();
		if (thisType == null || !thisType.isComplete() || thisType.isGeneric())
			throw new TypeException(this, "type inference cannot resolve type");
	}
	

    protected static boolean canInferTypeFrom(ITyped child) {
    	return child != null && child.getType() != null; // && child.getType().isComplete(); 
    }
    protected static boolean canReplaceType(ITyped child) {
    	return child != null && (child.getType() == null || !child.getType().isComplete()); 
    }

    public static boolean updateType(ITyped child, LLType newType) {
    	if (child == null || newType == null || child.getType() == newType)
			return false;
		
		if (//(child.getType() == null || !child.getType().isComplete()) &&
				//(child.getType() == null || newType.isCompatibleWith(child.getType())) &&
				newType.isMoreComplete(child.getType())) {
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
    
    protected boolean inferTypesFromChildren(ITyped[] typedChildren) {
    	LLType newType = null;
    	for (ITyped kid : typedChildren) {
    		if (canInferTypeFrom(kid)) {
    			newType = kid.getType();
    			break;
    		}
    	}
    	if (newType == null)
    		return false;
    	
    	boolean changed = false;
    	for (ITyped kid : typedChildren) {
    		changed |= updateType(kid, newType);
    	}
    	changed |= updateType(this, newType);
    	
    	return changed;
    	
    }
    
    /** Get the common type of all the children with types */
    protected boolean inferTypesFromChildList(TypeEngine typeEngine, IAstTypedExpr[] typedChildren) {
    	LLType newType = null;
    	for (IAstTypedExpr kid : typedChildren) {
    		if (canInferTypeFrom(kid)) {
    			LLType oldType = newType;
    			newType = kid.getType();
    			if (oldType != null)
    				newType = typeEngine.getPromotionType(newType, oldType);
    		}
    	}
    	if (newType == null)
    		return false;
    	
    	boolean changed = false;
    	for (IAstTypedExpr kid : typedChildren) {
    		if (kid != null) {
	    		boolean kidChanged = updateType(kid, newType);
	    		if (kidChanged) {
	    			changed = true;
	    		}
	    		kid.getParent().replaceChild(kid, createCastOn(typeEngine, kid, newType));
    		}
    	}
    	changed |= updateType(this, newType);
    	
    	return changed;
    	
    }
	protected IAstTypedExpr createCastOn(TypeEngine typeEngine,
			IAstTypedExpr child, LLType newType) {
		if (child == null)
			return null;
		
		// don't cast if we can fill a slot 
		if (child.getType() == null 
				|| child.getType().equals(newType)) {
			child.setType(newType);
			return child;
		}
		
		// ignore when we're already a cast
		if (this instanceof IAstUnaryExpr && ((IAstUnaryExpr) this).getOp() == IOperation.CAST) {
			if (canReplaceType(this))
				setType(newType);
			return child;
		}
		
		// modify cast when child is a cast and this is legal
		if (child instanceof IAstUnaryExpr && ((IAstUnaryExpr) child).getOp() == IOperation.CAST) {
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
		castExpr.setSourceRef(child.getSourceRef());
		castExpr.setType(newType);
		return castExpr;
	}
	

}