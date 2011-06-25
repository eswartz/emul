/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * Visitor class for nodes in the DOM.
 * 
 * @author eswartz
 *
 */
public class AstVisitor {
    /**
     * return continue to continue visiting, abort to stop, skip to not descend
     *         into this node.
     */
    public final static int PROCESS_SKIP = 1;

    public final static int PROCESS_ABORT = 2;

    public final static int PROCESS_CONTINUE = 3;

    public boolean visitDumpChildren = false;
    
    /** Visit the node and its children 
     * @param node */
    public int visit(IAstNode node) {
        
        return PROCESS_CONTINUE;
    }

    /**
	 * @param node  
	 */
    public void visitChildren(IAstNode node) {
    	
    }
    /** Done visiting node and children 
     * @param node */
    public int visitEnd(IAstNode node) {
    	return PROCESS_CONTINUE;
    	
    }
    
    public int traverseChildren(IAstNode node) {
        IAstNode[] kids = visitDumpChildren ? node.getDumpChildren() : node.getChildren();
        for (IAstNode element : kids) {
            int ret = element.accept(this);
            if (ret == PROCESS_ABORT)
            	return ret;
        }
        return PROCESS_CONTINUE;
    }

}
