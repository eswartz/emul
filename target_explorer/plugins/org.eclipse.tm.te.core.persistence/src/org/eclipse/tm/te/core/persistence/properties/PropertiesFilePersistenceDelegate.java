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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
		Assert.isNotNull(path);
		Assert.isNotNull(data);

		// The incoming path has to be an absolute path
		if (!path.isAbsolute()) {
			throw new IOException("Not Absolute"); //$NON-NLS-1$
		}

		// If the file extension is no set, default to "properties"
		if (path.getFileExtension() == null) {
			path = path.addFileExtension("properties"); //$NON-NLS-1$
		}

		// Create the writer object
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path.toFile()), "UTF-8")); //$NON-NLS-1$
		try {
			// Write the first level of attributes
			writeMap(writer, "core", data.getProperties()); //$NON-NLS-1$
		} finally {
			writer.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.persistence.interfaces.IPersistenceDelegate#delete(org.eclipse.core.runtime.IPath)
	 */
	public boolean delete(IPath path) throws IOException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.persistence.interfaces.IPersistenceDelegate#read(org.eclipse.core.runtime.IPath)
	 */
	public IPropertiesContainer read(IPath path) throws IOException {
		return null;
	}

	/**
	 * Write the map data.
	 *
	 * @param writer The writer. Must not be <code>null</code>.
	 * @param section The section name. Must not be <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	protected void writeMap(BufferedWriter writer, String section, Map<String, Object> data) throws IOException {
		Assert.isNotNull(writer);
		Assert.isNotNull(section);
		Assert.isNotNull(data);

		// Will contain the list of map keys where the value is a map type itself.
		List<String> childMapKeys = new ArrayList<String>();
		// Will contain the list of map keys where the value is not an map type.
		List<String> childKeys = new ArrayList<String>();

		// Get all the map keys and filter the map type values
		for (String key : data.keySet()) {
			if (data.get(key) instanceof Map) childMapKeys.add(key);
			else childKeys.add(key);
		}

		// Sort both lists
		Collections.sort(childMapKeys);
		Collections.sort(childKeys);

		// If the child key list is not empty, write the section
		if (!childKeys.isEmpty()) {
			// Write the header
			writer.write("[" + section.trim() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.newLine();

			for (String key : childKeys) {
				writer.write('\t');
				writer.write(key);
				writer.write(" = "); //$NON-NLS-1$

				Object value = data.get(key);
				if (value instanceof List) {
					writer.write(Arrays.deepToString(((List<?>)value).toArray()));
				} else {
					writer.write(value.toString());
				}

				writer.newLine();
			}
		}

		// If there are map type values, write them now
		if (!childMapKeys.isEmpty()) {
			writer.newLine();

			for (String key : childMapKeys) {
				// Calculate the section name
				String newSection = "core".equals(section) ? key.trim() : section + "." + key.trim(); //$NON-NLS-1$ //$NON-NLS-2$
				// Write it
				writeMap(writer, newSection, (Map<String, Object>)data.get(key));
			}
		}
	}
}
