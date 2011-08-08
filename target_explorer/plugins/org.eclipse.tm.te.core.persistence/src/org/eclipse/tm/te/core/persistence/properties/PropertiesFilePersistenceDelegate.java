/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.persistence.properties;

import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.tm.te.core.interfaces.nodes.IPropertiesContainer;
import org.eclipse.tm.te.core.persistence.AbstractPersistenceDelegate;

/**
 * Target Explorer: Properties file persistence delegate implementation.
 * <p>
 * The persistence delegates reads and writes a simple grouped properties file format.
 */
public class PropertiesFilePersistenceDelegate extends AbstractPersistenceDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.persistence.interfaces.IPersistenceDelegate#write(org.eclipse.core.runtime.IPath, org.eclipse.tm.te.core.interfaces.nodes.IPropertiesContainer)
	 */
	public void write(IPath path, IPropertiesContainer data) throws IOException {
		Assert.isNotNull(data);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.persistence.interfaces.IPersistenceDelegate#read(org.eclipse.core.runtime.IPath)
	 */
	public IPropertiesContainer read(IPath path) throws IOException {
		return null;
	}
}
