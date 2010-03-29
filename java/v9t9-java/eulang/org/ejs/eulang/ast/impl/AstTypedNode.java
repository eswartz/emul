/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.IOperation;
import org.ejs.eulang.ast.ITyped;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.LLType;

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
		AstTypedExpr other = (AstTypedExpr) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public LLType getType() {
		return type;
	}

	@Override
	public void setType(LLType type) {
		this.type = type;
	}

	public String getTypeString() {
		return getType() != null ? getType().toString() : "<unknown>";
	}
	

    protected boolean canInferTypeFrom(ITyped child) {
    	return child != null && child.getType() != null; // && child.getType().isComplete(); 
    }
    protected boolean canReplaceType(ITyped child) {
    	return child != null && (child.getType() == null || !child.getType().isComplete()); 
    }

    protected boolean updateType(ITyped child, LLType newType) {
    	if (child == null || newType == null)
			return false;
		
		if ((child.getType() == null || !child.getType().isComplete())
				&& newType.isComplete()) {
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