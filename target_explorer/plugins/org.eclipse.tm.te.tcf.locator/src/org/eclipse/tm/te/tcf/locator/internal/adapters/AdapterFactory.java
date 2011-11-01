/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.internal.adapters;

import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * Static peers adapter factory implementation.
 */
public class AdapterFactory implements IAdapterFactory {
	// The single instance adapter references
	private final IPersistable mapPersistableAdapter = new MapPersistableAdapter();
	private final IPersistable peerModelPersistableAdapter = new PeerModelPersistableAdapter();

	private static final Class<?>[] CLASSES = new Class[] {
		IPersistable.class
	};

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof Map) {
			if (IPersistable.class.equals(adapterType)) {
				return mapPersistableAdapter;
			}
		}

		if (adaptableObject instanceof IPeerModel) {
			if (IPersistable.class.equals(adapterType)) {
				return peerModelPersistableAdapter;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return CLASSES;
	}

}
