/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.nodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.runtime.interfaces.workingsets.IWorkingSetElement;
import org.eclipse.tm.te.runtime.properties.PropertiesContainer;
import org.eclipse.tm.te.tcf.locator.interfaces.IModelListener;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;


/**
 * Default peer model implementation.
 */
public class PeerModel extends PropertiesContainer implements IPeerModel, IWorkingSetElement {
	// Reference to the parent locator model
	private final ILocatorModel parentModel;

	/**
	 * Constructor.
	 *
	 * @param parent The parent locator model. Must not be <code>null</code>.
	 */
	public PeerModel(ILocatorModel parent) {
		this(parent, null);
	}

	/**
	 * Constructor.
	 *
	 * @param parent The parent locator model. Must not be <code>null</code>.
	 * @param peer The peer or <code>null</code>.
	 */
	public PeerModel(ILocatorModel parent, IPeer peer) {
		super();

		Assert.isNotNull(parent);
		parentModel = parent;

		// Set the default properties before enabling the change events.
		// The properties changed listeners should not be called from the
		// constructor.
		setProperty(IPeerModelProperties.PROP_INSTANCE, peer);

		// Enable change events
		setChangeEventsEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.properties.PropertiesContainer#checkThreadAccess()
	 */
	@Override
	protected final boolean checkThreadAccess() {
	    return Protocol.isDispatchThread();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel#getModel()
	 */
	@Override
	public ILocatorModel getModel() {
		return (ILocatorModel)getAdapter(ILocatorModel.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel#getPeer()
	 */
	@Override
	public IPeer getPeer() {
		return (IPeer)getAdapter(IPeer.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(final Class adapter) {
		// NOTE: The getAdapter(...) method can be invoked from many place and
		//       many threads where we cannot control the calls. Therefore, this
		//       method is the only one which is allowed to call from any thread.
		final Object[] object = new Object[1];
		if (Protocol.isDispatchThread()) {
			object[0] = doGetAdapter(adapter);
		} else {
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					object[0] = doGetAdapter(adapter);
				}
			});
		}
		return object[0] != null ? object[0] : super.getAdapter(adapter);
	}

	/**
	 * Returns an object which is an instance of the given class associated with this object.
	 * Returns <code>null</code> if no such object can be found.
	 * <p>
	 * This method must be called within the TCF dispatch thread!
	 *
	 * @param adapter The adapter class to look up.
	 * @return The adapter or <code>null</code>.
	 */
	protected Object doGetAdapter(Class<?> adapter) {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$

		if (adapter.isAssignableFrom(ILocatorModel.class)) {
			return parentModel;
		}

		Object peer = getProperty(IPeerModelProperties.PROP_INSTANCE);
		if (peer != null && adapter.isAssignableFrom(peer.getClass())) {
			return peer;
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.properties.PropertiesContainer#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder(getClass().getSimpleName());

		if (Protocol.isDispatchThread()) {
			IPeer peer = getPeer();
			buffer.append(": id=" + peer.getID()); //$NON-NLS-1$
			buffer.append(", name=" + peer.getName()); //$NON-NLS-1$
		} else {
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					IPeer peer = getPeer();
					buffer.append(": id=" + peer.getID()); //$NON-NLS-1$
					buffer.append(", name=" + peer.getName()); //$NON-NLS-1$
				}
			});
		}
		buffer.append(", " + super.toString()); //$NON-NLS-1$
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.properties.PropertiesContainer#getProperties()
	 */
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<String, Object>(super.getProperties());
		if (getPeer() != null) properties.putAll(getPeer().getAttributes());
	    return Collections.unmodifiableMap(new HashMap<String, Object>(properties));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.properties.PropertiesContainer#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		Object property = super.getProperty(key);
		if (property == null && getPeer() != null && getPeer().getAttributes().containsKey(key)) {
			property = getPeer().getAttributes().get(key);
		}
		return property;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.properties.PropertiesContainer#postSetProperties(java.util.Map)
	 */
	@Override
	protected void postSetProperties(Map<String, Object> properties) {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(properties);

		if (changeEventsEnabled()) {
			final IModelListener[] listeners = parentModel.getListener();
			if (listeners.length > 0) {
				Protocol.invokeLater(new Runnable() {
					@Override
					@SuppressWarnings("synthetic-access")
					public void run() {
						for (IModelListener listener : listeners) {
							listener.peerModelChanged(parentModel, PeerModel.this);
						}
					}
				});
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.properties.PropertiesContainer#postSetProperty(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void postSetProperty(String key, Object value, Object oldValue) {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(key);

		// Notify registered listeners that the peer changed. Property
		// changes for property slots ending with ".silent" are suppressed.
		if (changeEventsEnabled() && !key.endsWith(".silent")) { //$NON-NLS-1$
			final IModelListener[] listeners = parentModel.getListener();
			if (listeners.length > 0) {
				Protocol.invokeLater(new Runnable() {
					@Override
					@SuppressWarnings("synthetic-access")
					public void run() {
						for (IModelListener listener : listeners) {
							listener.peerModelChanged(parentModel, PeerModel.this);
						}
					}
				});
			}
		}
	}
}
