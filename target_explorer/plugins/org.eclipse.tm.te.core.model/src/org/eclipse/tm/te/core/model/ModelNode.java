/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.model;

import java.util.UUID;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.tm.te.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.core.interfaces.tracing.ITraceIds;
import org.eclipse.tm.te.core.model.interfaces.IContainerModelNode;
import org.eclipse.tm.te.core.model.interfaces.IModelNode;
import org.eclipse.tm.te.core.model.interfaces.IModelNodeProvider;
import org.eclipse.tm.te.core.properties.PropertiesContainer;

/**
 * A common (data) model node implementation.
 * <p>
 * <b>Note:</b> The (data) model node implementation is not thread-safe. Clients requiring
 *              a thread-safe implementation should subclass the properties container and
 *              overwrite {@link #checkThreadAccess()}.
 */
public class ModelNode extends PropertiesContainer implements IModelNode, IModelNodeProvider {
	// Reference to the parent model node
	private IContainerModelNode parent = null;

	// Flag to remember the dirty state of the (data) model node.
	private boolean dirty;
	// Flag to remember the pending state of the (data) model node.
	private boolean pending;

	// Flag to control if property change events are suppressed
	// until the model node is added to a parent container model node.
	protected boolean suppressEventsOnNullParent = true;

	/**
	 * Constructor.
	 */
	public ModelNode() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#getParent()
	 */
	public final IContainerModelNode getParent() {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#getParent(java.lang.Class)
	 */
	public final IContainerModelNode getParent(Class<?> nodeType) {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		if (this.parent != null) {
			if (nodeType.isInstance(this.parent)) {
				return this.parent;
			}
			return this.parent.getParent(nodeType);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#setParent(org.eclipse.tm.te.core.interfaces.nodes.IContainerModelNode)
	 */
	public final void setParent(IContainerModelNode parent) {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		if (this.parent != null) {
			throw new IllegalStateException("Model node already associated with a parent container model node!"); //$NON-NLS-1$
		}
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#move(org.eclipse.tm.te.core.interfaces.nodes.IContainerModelNode)
	 */
	public final void move(IContainerModelNode newParent) {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(newParent);

		// If the node is associated with a parent container, remove the node from
		// the container node non-recursive (keeping all children if being ourself
		// a container model node)
		if (this.parent != null) {
			// Remove the node from the old parent container
			if (!this.parent.contains(this) || this.parent.remove(this, false)) {
				// Unset the parent reference (will enable the add to the new container)
				this.parent = null;
			}
		}

		// Re-add to the new parent. This may cause an
		// IllegalStateException if the previous removal from
		// the old parent container failed.
		newParent.add(this);
		return;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#isVisible()
	 */
	public boolean isVisible() {
		return getBooleanProperty(PROPERTY_IS_VISIBLE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#getError()
	 */
	public String getError() {
		return getStringProperty(PROPERTY_ERROR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#getName()
	 */
	public String getName() {
		String name = (String)super.getProperty(PROPERTY_NAME);
		return name != null ? name : ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.nodes.PropertiesContainer#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		if (PROPERTY_NAME.equals(key)) {
			return getName();
		}
		return super.getProperty(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#getDescription()
	 */
	public String[] getDescription() {
		return new String[]{};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#getImageId()
	 */
	public String getImageId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.nodes.PropertiesContainer#toString()
	 */
	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder(getClass().getName());

		toString.append("{"); //$NON-NLS-1$
		toString.append(super.toString());
		toString.append("}"); //$NON-NLS-1$

		return toString.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.nodes.PropertiesContainer#dropEvent(java.lang.Object, java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected boolean dropEvent(Object source, String key, Object oldValue, Object newValue) {
		boolean drop = super.dropEvent(source, key, oldValue, newValue);
		if (drop) return true;

		// If the parent is null, it must be allowed to fire change events explicitly
		if (parent == null && suppressEventsOnNullParent) {
			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_EVENTS)) {
				CoreBundleActivator.getTraceHandler().trace("Drop change event (null parent)\n\t\t" + //$NON-NLS-1$
															"for eventId = " + key, //$NON-NLS-1$
															0, ITraceIds.TRACE_EVENTS, IStatus.WARNING, this);
			}
			return true;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNodeProvider#getModelNode()
	 */
	public final IModelNode getModelNode() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public boolean contains(ISchedulingRule rule) {
		// We deal only with scheduling rules we know about (as the interface
		// declaration of ISchedulingRule#contains requests).
		if (rule instanceof IModelNode) {
			// The IModelNode itself is an leaf node and cannot have children.
			// Therefore, the IModelNode can contains only itself.
			return rule == this;
		}

		// If we don't know about the scheduling rule, we must return false.
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public boolean isConflicting(ISchedulingRule rule) {
		// We deal only with scheduling rules we know about (as the interface
		// declaration of ISchedulingRule#contains requests).
		if (rule instanceof IModelNode) {
			// The IModelNode itself is an leaf node and cannot have children.
			// Therefore, the IModelNode can conflict only with itself.
			return rule == this;
		}

		// If we don't know about the scheduling rule, we must return false.
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#setDirty(boolean)
	 */
	public final void setDirty(boolean dirty) {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		this.dirty = dirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#isDirty()
	 */
	public final boolean isDirty() {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		return dirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#setPending(boolean)
	 */
	public final void setPending(boolean pending) {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		this.pending = pending;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#isPending()
	 */
	public final boolean isPending() {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		return pending;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.interfaces.nodes.IModelNode#find(java.util.UUID)
	 */
	public IModelNode find(UUID uuid) {
		if (getUUID().equals(uuid)) {
			return this;
		}
		return null;
	}
}
