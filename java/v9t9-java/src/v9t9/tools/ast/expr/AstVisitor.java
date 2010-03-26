/**
 * 
 */
package v9t9.tools.ast.expr;

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

    /** Visit the node and its children */
    public int visit(IAstNode node) {
        
        return PROCESS_CONTINUE;
    }

    public void visitChildren(IAstNode node) {
    	
    }
    /** Done visiting node and children */
    public int visitEnd(IAstNode node) {
    	return PROCESS_CONTINUE;
    	
    }
    /** Visit a referenced node */
    public int visitReference(IAstNode node) {
        return PROCESS_CONTINUE;
    }
    
    public void traverseChildren(IAstNode node) {
        IAstNode[] refs = node.getReferencedNodes();
        IAstNode[] kids = node.getChildren();
        for (IAstNode element : refs) {
            boolean isKid = false;
            for (IAstNode element2 : kids) {
                if (element2 == element) {
                    isKid = true;
                    element.accept(this);
                }
            }
            if (!isKid) {
				element.acceptReference(this);
			}
        }
    }

}
