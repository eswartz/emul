/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.List;


/**
 * @author ejs
 *
 */
public interface IAstNodeList <T extends IAstNode> extends IAstNode {
	IAstNodeList<T> copy();
	/** modifiable list of contents.  If you add or remove items, you must manually reparent them */
	List<T> list();
	int nodeCount();
	IAstNode[] getNodes();
	T[] getNodes(Class<T> baseClass);
	
	void add(T node);
	/** Add new node before another.  If node is null, add at end. */
	void addBefore(T newNode, T node);
	/** Add new node after another.  If node is null, add at start */
	void addAfter(T node, T newNode);
	
	void add(int idx, T node);
	
	void remove(T node);
	
	T getFirst();
	T getLast();
	
}
