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
import org.ejs.eulang.types.LLPointerType;
import org.ejs.eulang.types.LLRefType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.LLUpType;
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
		LLType type = getType();
		String typeString = (type == null ? "<unknown>" : (!type.isComplete() ? "<incomplete> : "  : "") + type.getLLVMType());
		return input + " [" + typeString + "]";
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
    	
    	if (child == null || newType == null)
			return false;
		
    	//newType = getConcreteType( child, newType);
    	
    	if (child.getType() == newType)
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
    
    /**
	 * @param typeEngine 
     * @param newType
	 * @return
	 */
	public static LLType getConcreteType(TypeEngine typeEngine, ITyped child, LLType newType) {
		
		// don't put uprefs in normal code
    	if (newType instanceof LLUpType) {
    		/*
    		IAstType actual = ((LLUpType) newType).getRealType();
    		if(actual == null)
    			return newType;
    		IAstNode n = null;
    		if (child != null) {
				n = child instanceof IAstNode ? (IAstNode) child : ((ISymbol) child).getDefinition();
	    		while (n != null) {
	    			if (n == actual)
	    				break;
	    			n = n.getParent();
	    		}
    		}
    		if (n == null)
    			return actual.getType();
    			*/
    		return newType.getSubType();
    	} else if (newType instanceof LLPointerType) {
    		LLType sub = getConcreteType(typeEngine, child, newType.getSubType());
    		if (sub != newType.getSubType()) {
    			return typeEngine.getPointerType(sub);
    		}
    	} else if (newType instanceof LLRefType) {
    		LLType sub = getConcreteType(typeEngine, child, newType.getSubType());
    		if (sub != newType.getSubType()) {
    			return typeEngine.getRefType(sub);
    		}
    	}
		return newType;
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
		if (child instanceof IAstUnaryExpr && ((IAstUnaryExpr) child).getOp() == IOperation.CAST 
				&& ((IAstUnaryExpr)child).getExpr().getType().getBasicType() == newType.getBasicType()) {
			child.setType(newType);
			return child;
		}
		
		// ignore wildcard casts
		if (child.getType() != null && child.getType().getBasicType() == newType.getBasicType() &&
				newType.getBits() == 0) {
			return child;
		}
		
		/*
		// ignore uprefs
		if (child.getType().getSubType() instanceof LLUpType) {
			IAstType realType = ((LLUpType) child.getType().getSubType()).getRealType();
			if (realType != null && realType.getType().equals(newType))
				return child;
		}*/
		
		// don't implicitly cast to pointers of different types
		//if (child.getType() != null && !child.getType().isCompatibleWith(newType)) {
		//	if (child.getType().getBasicType() != BasicType.VOID && newType.getBasicType() != BasicType.VOID)
		//		return child;
		//}
		child.setParent(null);
		IAstUnaryExpr castExpr = new AstUnaryExpr(IOperation.CAST, child);
		castExpr.setSourceRef(child.getSourceRef());
		castExpr.setType(newType);
		return castExpr;
	}


}