/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.AstVisitor;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.IOperation;
import org.ejs.eulang.ast.ISourceRef;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.LLType;

/**
 * @author eswartz
 *
 */
abstract public class AstNode implements IAstNode {

    private IAstNode parent;

    protected boolean dirty;

	private ISourceRef sourceRef;
    
    public AstNode() {
    }

    public abstract boolean equals(Object obj);
    public abstract int hashCode();
    
    @Override
	public String toString() {
        String name = getClass().getName();
        int idx = name.lastIndexOf('.');
        if (idx > 0) {
			name = name.substring(idx+1);
		}
        return "{ " + name + ":" +hashCode() + " }"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
	protected String catenate(IAstNode[] nodes) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (IAstNode k : nodes) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(k);
		}
		return sb.toString();
	}

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#isDirty()
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#isDirtyTree()
     */
    public boolean isDirtyTree() {
        if (dirty) {
			return true;
		}
        IAstNode children[] = getChildren();
        for (IAstNode element : children) {
            if (element.isDirtyTree()) {
				return true;
			}
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#setDirty(boolean)
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        
    }
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getParent()
     */
    public IAstNode getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#setParent(v9t9.tools.decomp.expr.IAstNode)
     */
    public void setParent(IAstNode node) {
        if (node != null && node != parent) {
			org.ejs.coffee.core.utils.Check.checkArg((parent == null));
		}
        parent = node;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#accept(v9t9.tools.decomp.expr.AstVisitor)
     */
    public int accept(AstVisitor visitor) {
        int ret = visitor.visit(this);
        if (ret == AstVisitor.PROCESS_ABORT)
        	return ret;
        if (ret == AstVisitor.PROCESS_CONTINUE) {
        	visitor.visitChildren(this);
	        IAstNode[] children = getChildren();
			for (IAstNode node : children) {
	        	ret = node.accept(visitor);
	        	if (ret == AstVisitor.PROCESS_ABORT)
	        		return ret;
	        }
        }
        visitor.visitEnd(this);
        return ret;
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#acceptReference(v9t9.tools.decomp.expr.AstVisitor)
     */
    public void acceptReference(AstVisitor visitor) {
        visitor.visitReference(this);
    }


    /* (non-Javadoc)
     * @see v9t9.tools.ast.expr.IAstNode#getSourceRef()
     */
    @Override
    public ISourceRef getSourceRef() {
    	return sourceRef;
    }
    /* (non-Javadoc)
     * @see v9t9.tools.ast.expr.IAstNode#setSourceRef(v9t9.tools.ast.expr.ISourceRef)
     */
    @Override
    public void setSourceRef(ISourceRef sourceRef) {
    	this.sourceRef = sourceRef;
    }
    
    protected <T extends IAstNode> T reparent(T existing, T newkid) {
    	if (existing != null && existing.getParent() == this)
			existing.setParent(null);
		if (newkid != null)
			newkid.setParent(this);
		return newkid;
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#getDepth()
     */
    @Override
    public int getDepth() {
    	IAstNode[] kids = getChildren();
    	if (kids.length > 0) {
    		int maxKid = 0;
    		for (IAstNode kid : kids) {
    			maxKid = Math.max(kid.getDepth(), maxKid);
    		}
    		return 1 + maxKid;
    	}
    	return 1;
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
