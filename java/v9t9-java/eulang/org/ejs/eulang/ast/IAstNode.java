/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.ISourceRef;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.TypeException;



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
    
    /** Fully copy the node */
    IAstNode copy();
    
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
    
    /** Change the parent to the given node. */
    public void reparent(IAstNode node);

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
     * @return {@link AstVisitor#PROCESS_xxx} flag
     */
    public int accept(AstVisitor visitor);

    /**
     * Accept a reference
     * @param visitor
     */
    public int acceptReference(AstVisitor visitor);
    
    public ISourceRef getSourceRef();
    public void setSourceRef(ISourceRef sourceRef);
    public void setSourceRefTree(ISourceRef sourceRef);

	/**
	 * @return
	 */
	public int getDepth();

	/**
	 * Find a node in this tree matching the given node in another tree
	 * @param target
	 * @return IAstNode or null
	 */
	public IAstNode findMatch(IAstNode target);

	/**
	 * Get the nearest enclosing scope (may be this)
	 * @return
	 */
	IScope getOwnerScope();
	
	/**
	 * Replace the given child node with another.
	 */
	void replaceChild(IAstNode existing, IAstNode another);
	
	/** 
	 * Ensure that the node's type is sensible
	 * @param typeEngine 
	 * @throws TypeException TODO
	 */
	void validateType(TypeEngine typeEngine) throws TypeException;
	/** 
	 * Ensure that the node's child types are sensible for this node
	 * @param typeEngine 
	 * @throws TypeException TODO
	 */
	void validateChildTypes(TypeEngine typeEngine) throws TypeException;
	
	void uniquifyIds();
}

