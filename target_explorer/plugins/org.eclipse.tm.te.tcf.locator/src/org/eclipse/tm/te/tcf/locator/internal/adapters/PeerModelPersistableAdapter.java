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

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Path;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * Persistable implementation handling peer attributes.
 */
public class PeerModelPersistableAdapter implements IPersistable {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable#getStorageID()
	 */
	@Override
	public String getStorageID() {
		return "org.eclipse.tm.te.tcf.locator.persistence"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable#getURI(java.lang.Object)
	 */
    @Override
	public URI getURI(final Object data) {
		Assert.isNotNull(data);

		URI uri = null;

		// Only peer model objects are supported
		if (data instanceof IPeerModel) {
			// Get the file path the peer model has been created from
			final String[] path = new String[1];
			if (Protocol.isDispatchThread()) {
				path[0] = ((IPeerModel)data).getPeer().getAttributes().get("Path"); //$NON-NLS-1$
			} else {
				Protocol.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						path[0] = ((IPeerModel)data).getPeer().getAttributes().get("Path"); //$NON-NLS-1$
					}
				});
			}

			if (path[0] != null && !"".equals(path[0].trim())) { //$NON-NLS-1$
				uri = new Path(path[0].trim()).toFile().toURI();
			}
		}

		return uri;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable#exportFrom(java.lang.Object)
	 */
    @Override
	public Map<String, Object> exportFrom(Object data) throws IOException {
		Assert.isNotNull(data);

		Map<String, Object> result = null;

		// Only peer model objects are supported
		if (data instanceof IPeerModel) {
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.persistence.interfaces.IPersistable#importTo(java.lang.Object, java.util.Map)
	 */
	@Override
	public void importTo(Object data, Map<String, Object> external) throws IOException {
		Assert.isNotNull(data);
		Assert.isNotNull(external);

		// Only peer model objects are supported
		if (data instanceof IPeerModel) {
		}
	}

}
