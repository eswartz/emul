/**
 * 
 */
package v9t9.tools.ast.expr.impl;

import v9t9.tools.ast.expr.AstVisitor;
import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.ISourceRef;

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
    	if (existing != null)
			existing.setParent(null);
		if (newkid != null)
			newkid.setParent(this);
		return newkid;
    }
}
