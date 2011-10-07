/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.model;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.tm.te.runtime.events.EventManager;
import org.eclipse.tm.te.runtime.model.interfaces.IContainerModelNode;
import org.eclipse.tm.te.runtime.model.interfaces.IModelNode;

/**
 * A common (data) model container node implementation.
 * <p>
 * <b>Note:</b> The (data) model node implementation is not thread-safe. Clients requiring
 *              a thread-safe implementation should subclass the properties container and
 *              overwrite {@link #checkThreadAccess()}.
 */
public class ContainerModelNode extends ModelNode implements IContainerModelNode {
	// Note: Do _not_ use sorted sets/trees here! The trees get not resorted if the element state
	// changes. We may loose the possibility to find the element again within the tree!
	private final List<IModelNode> childList = new ArrayList<IModelNode>();

	// Lock to use for synchronization purpose
	private final Lock childListLock = new ReentrantLock();

	// empty array
	public static final IModelNode[] EMPTY_MODEL_NODE_ARRAY = new IModelNode[0];

	/**
	 * Constructor.
	 */
	public ContainerModelNode() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.nodes.IContainerModelNode#getChildren()
	 */
	@Override
	public IModelNode[] getChildren() {
		return internalGetChildren();
	}

	/**
	 * Return the real child list.
	 */
	protected final IModelNode[] internalGetChildren() {
		// Create the list that will hold the copy (non-deep copy)
		List<IModelNode> children = new ArrayList<IModelNode>();
		try {
			// Acquire the lock while copying the child references
			childListLock.lock();
			// Add the children to the returned list copy
			children.addAll(childList);
		} finally {
			// Release the lock
			childListLock.unlock();
		}
		return children.toArray(new IModelNode[children.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.nodes.IContainerModelNode#getChildren(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getChildren(Class<T> instanceOf) {
		// Create the list that will hold the found children being
		// a instance of the given class
		List<T> children = new ArrayList<T>();
		try {
			// Acquire the lock while copying the child references
			childListLock.lock();
			// Walk through all the children and check for the class
			for (IModelNode child : childList) {
				if (instanceOf.isInstance(child)) {
					children.add((T)child);
				}
			}
		} finally {
			// Release the look
			childListLock.unlock();
		}

		return children;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.nodes.IContainerModelNode#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		boolean hasChildren = false;
		try { childListLock.lock(); hasChildren = !childList.isEmpty(); } finally { childListLock.unlock(); }
		return hasChildren;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.nodes.IContainerModelNode#add(org.eclipse.tm.te.runtime.interfaces.nodes.IModelNode)
	 */
	@Override
	public boolean add(IModelNode node) {
		if (node != null) {
			try {
				childListLock.lock();
				// set the parent if not set otherwise before.
				if (node.getParent() == null) {
					node.setParent(this);
				}
				else {
					Assert.isTrue(node.getParent() == this, "Attempt to add child node to " + getName() + " with this != node.getParent()!!!"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				childList.add(node);
			} finally {
				childListLock.unlock();
			}

			EventObject event = newEvent(this, NOTIFY_ADDED, null, new IModelNode[] { node });
			if (event != null) EventManager.getInstance().fireEvent(event);

			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.nodes.IContainerModelNode#remove(org.eclipse.tm.te.runtime.interfaces.nodes.IModelNode, boolean)
	 */
	@Override
	public boolean remove(IModelNode node, boolean recursive) {
		if (node instanceof IContainerModelNode && recursive) {
			IContainerModelNode container = (IContainerModelNode)node;
			container.clear();
		}

		boolean removed = false;
		// Removes the given node from this container
		try { childListLock.lock(); removed = childList.remove(node); } finally { childListLock.unlock(); }
		// Unlink the parent and fire the removed notification if the element got removed
		if (removed) {
			EventObject event = newEvent(this, NOTIFY_REMOVED, new IModelNode[] { node }, null);
			if (event != null) EventManager.getInstance().fireEvent(event);
		}

		return removed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.nodes.IContainerModelNode#removeAll(java.lang.Class)
	 */
	@Override
	public <T> boolean removeAll(Class<T> nodeType) {
		boolean removed = false;
		List<T> children;

		try {
			childListLock.lock();
			children = getChildren(nodeType);
			removed |= childList.removeAll(children);
		} finally {
			childListLock.unlock();
		}

		if (removed) {
			EventObject event = newEvent(this, NOTIFY_REMOVED, children, null);
			if (event != null) EventManager.getInstance().fireEvent(event);
		}

		return removed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.nodes.IContainerModelNode#clear()
	 */
	@Override
	public boolean clear() {
		boolean removed = false;

		boolean changed = setChangeEventsEnabled(false);

		try {
			childListLock.lock();
			IModelNode[] children = internalGetChildren();
			for (IModelNode element : children) {
				removed |= remove(element, true);
			}
		} finally {
			childListLock.unlock();
		}

		if (changed) setChangeEventsEnabled(true);

		return removed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.nodes.IContainerModelNode#size()
	 */
	@Override
	public int size() {
		return childList.size();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.nodes.IContainerModelNode#contains(org.eclipse.tm.te.runtime.interfaces.nodes.IModelNode)
	 */
	@Override
	public boolean contains(IModelNode node) {
		if (node != null) {
			try {
				childListLock.lock();
				return childList.contains(node);
			} finally {
				childListLock.unlock();
			}
		}
		return false;
	}

	/**
	 * Returns weather the scheduling rule of container model nodes is considering
	 * children or not. If recursive scheduling rule locking is enabled, than any job
	 * having a child of this container model node as scheduling rule, will conflict
	 * with this container model node.
	 *
	 * @return <code>True</code> if recursive scheduling rule locking is enabled, <code>false</code> otherwise.
	 */
	protected boolean isSchedulingLockedRecursivly() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.nodes.ModelNode#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean contains(ISchedulingRule rule) {
		// We deal only with scheduling rules we know about (as the interface
		// declaration of ISchedulingRule#contains requests) and if recursive
		// scheduling rule locking is on.
		if (isSchedulingLockedRecursivly() && rule instanceof IModelNode) {
			// Iterate through the children and if one of the children
			// contains the given scheduling rule, this container model
			// node contains the given scheduling rule as well.
			try {
				childListLock.lock();
				IModelNode[] children = internalGetChildren();
				for (IModelNode child : children) {
					if (child.contains(rule)) {
						return true;
					}
				}
			} finally {
				childListLock.unlock();
			}
		}

		return super.contains(rule);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.nodes.ModelNode#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		// We deal only with scheduling rules we know about (as the interface
		// declaration of ISchedulingRule#contains requests) and if recursive
		// scheduling rule locking is on.
		if (isSchedulingLockedRecursivly() && rule instanceof IModelNode) {
			// Iterate through the children and if one of the children
			// is conflicting with the given scheduling rule, this
			// container model node is conflicting the given scheduling
			// rule as well.
			try {
				childListLock.lock();
				IModelNode[] children = internalGetChildren();
				for (IModelNode child : children) {
					if (child.isConflicting(rule)) {
						return true;
					}
				}
			} finally {
				childListLock.unlock();
			}
		}

		return super.isConflicting(rule);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.nodes.ModelNode#find(java.util.UUID)
	 */
	@Override
	public IModelNode find(UUID uuid) {
		IModelNode find = super.find(uuid);
		if (find != null) return find;

		for (IModelNode child : childList) {
			find = child.find(uuid);
			if (find != null) {
				return find;
			}
		}

		return find;
	}
}
