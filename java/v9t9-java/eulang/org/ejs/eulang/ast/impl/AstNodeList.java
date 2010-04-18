/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstSymbolExpr;


/**
 * @author ejs
 *
 */
public class AstNodeList<T extends IAstNode> extends AstNode implements IAstNodeList<T> {

	private List<T> list = new ArrayList<T>();
	private Class<T> theClass;
	/**
	 * 
	 */
	public AstNodeList(Class<T> theClass) {
		this.theClass = theClass;
	}
	protected AstNodeList(List<T> copyList) {
		list = copyList;
		for (T node : list) {
			node.setParent(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstNodeList<T> copy(IAstNode copyParent) {
		List<T> copyList = new ArrayList<T>();
		for (T t : list) {
			copyList.add(doCopy(t, copyParent));
		}
		AstNodeList<T> newList = new AstNodeList<T>(copyList);
		newList.theClass = theClass;
		return fixup(this, newList);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 22;
		result = prime * result + ((list == null) ? 0 : list.hashCode());
		return result;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstNodeList<T> other = (AstNodeList) obj;
		if (list == null) {
			if (other.list != null)
				return false;
		} else if (!list.equals(other.list))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "LIST [" + list.size() + "]";
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNodeList#nodeCount()
	 */
	@Override
	public int nodeCount() {
		return list.size();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNodeList#list()
	 */
	@Override
	public List<T> list() {
		return list;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNodeList#getNodes()
	 */
	@Override
	public IAstNode[] getNodes() {
		return (IAstNode[]) list.toArray(new IAstNode[list.size()]);
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNodeList#getNodes()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T[] getNodes(Class<T> baseClass) {
		T[] array = (T[]) Array.newInstance(baseClass, list.size());
		return (T[]) list.toArray(array);
	}

	public void add(T node) {
		list.add(node);
		this.reparent(null, node);
	}
	
	public void add(int idx, T node) {
		list.add(idx, node);
		this.reparent(null, node);
	}
	
	
	public void addAfter(T node, T newNode) {
		int idx = newNode != null ? list.indexOf(newNode) : 0;
		list.add(idx, node);
		this.reparent(null, node);
	} 
	public void addBefore(T newNode, T node) {
		int idx = newNode != null ? list.indexOf(newNode) : list.size();
		list.add(idx, node);
		this.reparent(null, node);
	} 
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return (IAstNode[]) list.toArray(new IAstNode[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (theClass == null || !theClass.isAssignableFrom(another.getClass()))
			throw new ClassCastException();
		for (ListIterator<T> iterator = list.listIterator(); iterator.hasNext();) {
			T node = (T) iterator.next();
			if (node == existing) {
				if (another == null) {
					iterator.remove();
				} else {
					iterator.set((T) another);
					another.setParent(this);
				}
				return;
			}
		}
		throw new IllegalArgumentException();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNodeList#getFirst()
	 */
	@Override
	public T getFirst() {
		if (list.size() > 0)
			return list.get(0);
		else
			return null;
	}
	public T getLast() {
		if (list.size() > 0)
			return list.get(list.size() - 1);
		else
			return null;
		
	}
	/**
	 * @param symCopy
	 * @return
	 */
	public static <T extends IAstNode> IAstNodeList<T> singletonList(Class<T> theClass, T symCopy) {
		IAstNodeList<T> list = new AstNodeList<T>(theClass);
		list.add(symCopy);
		list.setSourceRef(symCopy.getSourceRef());
		return list;
	}
}
