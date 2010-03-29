/**
 * 
 */
package org.ejs.eulang.ast;



/** 
 * Base interface for all nodes 
 * 
 * @author eswartz
 *
 */
public interface IAstNode {
    public static final IAstNode[] NO_CHILDREN = new IAstNode[] { };
    
    /** Get the unique id of the node */
    public int getId();
    
    /** Fully copy the node 
     * @param copyParent TODO*/
    IAstNode copy(IAstNode copyParent);
    
    /** Tell whether the node is dirty (changed with respect to original source)
     * @see #isDirtyTree()
     * @see #hasDirtySource()
     */
    public boolean isDirty();

    /** Tell whether the node or children are dirty
     * @see #hasDirtySource()
     */
    public boolean isDirtyTree();

    /** Set the node's dirty flag 
     */
    public void setDirty(boolean dirty);

    /** Get parent of this node */
    public IAstNode getParent();

    /** Set parent of this node 
     * <p>
     * A node is parented whenever it is owned by the parent.
     * Usually a setter that passes an IAstNode to another
     * will also assign the parent.  
     */
    public void setParent(IAstNode node);

    /**Get the owned children of this node.  Each child's
     * #getParent() returns this.
     * @return array of children (never null) 
     */
    public IAstNode[] getChildren();
    /**Get the owned children of this node to show in a dump.
     * @return array of children (never null) 
     */
    public IAstNode[] getDumpChildren();
    
    /** Get references to other nodes from this node.
     * This set includes nodes which are not owned (i.e.
     * shared types and names).
     * This includes the same nodes as getChildren().
     */ 
    public IAstNode[] getReferencedNodes();
    
    
    /** Accept visitor
     * 
     * @param visitor impl
     * @return TODO
     */
    public int accept(AstVisitor visitor);

    /**
     * Accept a reference
     * @param visitor
     */
    public int acceptReference(AstVisitor visitor);
    
    public ISourceRef getSourceRef();
    public void setSourceRef(ISourceRef sourceRef);

	/**
	 * @return
	 */
	public int getDepth();

	/**
	 * Replace children from the array. 
	 * @param children array which matches length and semantics of {@link #getChildren()}
	 */
	public void replaceChildren(IAstNode[] children);

	/**
	 * Find a node in this tree matching the given node in another tree
	 * @param target
	 * @return IAstNode or null
	 */
	public IAstNode findMatch(IAstNode target);

}

