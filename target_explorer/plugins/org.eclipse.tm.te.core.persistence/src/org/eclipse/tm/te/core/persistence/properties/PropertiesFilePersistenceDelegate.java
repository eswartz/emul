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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.tm.te.core.interfaces.nodes.IPropertiesContainer;
import org.eclipse.tm.te.core.nodes.PropertiesContainer;
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
		Assert.isNotNull(path);

		// The incoming path has to be an absolute path
		if (!path.isAbsolute()) {
			throw new IOException("Not Absolute"); //$NON-NLS-1$
		}

		// If the file extension is no set, default to "properties"
		if (path.getFileExtension() == null) {
			path = path.addFileExtension("properties"); //$NON-NLS-1$
		}

		return path.toFile().delete();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.persistence.interfaces.IPersistenceDelegate#read(org.eclipse.core.runtime.IPath)
	 */
	public IPropertiesContainer read(IPath path) throws IOException {
		Assert.isNotNull(path);

		IPropertiesContainer data = new PropertiesContainer();
		Map<String, Object> coreSection = new HashMap<String, Object>();

		// Create the reader object
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile()), "UTF-8")); //$NON-NLS-1$
		try {
			read(reader, coreSection);
		} finally {
			reader.close();
		}
		data.setProperties(coreSection);

		return !data.getProperties().isEmpty() ? data : null;
	}

	/**
	 * Write the map data.
	 *
	 * @param writer The writer. Must not be <code>null</code>.
	 * @param section The section name. Must not be <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 */
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
			// Write a new line except it is the "core" section
			if (!"core".equals(section)) writer.newLine(); //$NON-NLS-1$

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
			for (String key : childMapKeys) {
				// Calculate the section name
				String newSection = "core".equals(section) ? key.trim() : section + "." + key.trim(); //$NON-NLS-1$ //$NON-NLS-2$
				// Write it
				writeMap(writer, newSection, (Map<String, Object>)data.get(key));
			}
		}
	}

	private static Pattern SECTION = Pattern.compile("\\s*\\[([^\\]]+)\\]\\s*"); //$NON-NLS-1$
	private static Pattern PROPERTY = Pattern.compile("\\s*(.+\\s*=\\s*.+)"); //$NON-NLS-1$

	/**
	 * Read the data.
	 *
	 * @param reader The reader. Must not be <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 */
	protected void read(BufferedReader reader, Map<String, Object> data) throws IOException {
		Assert.isNotNull(reader);
		Assert.isNotNull(data);

		// The sections by name for easier access.
		// The "core" section is the incoming data object
		Map<String, Map<String, Object>> sections = new HashMap<String, Map<String, Object>>();
		sections.put("core", data); //$NON-NLS-1$

		String currentSection = "core"; //$NON-NLS-1$
		String line = reader.readLine();
		while (line != null) {
			Matcher matcher = SECTION.matcher(line);
			if (matcher.matches()) {
				currentSection = matcher.group(1).toLowerCase();
				if (sections.get(currentSection) == null) {
					sections.put(currentSection, new HashMap<String, Object>());
				}
			} else {
				matcher = PROPERTY.matcher(line);
				if (matcher.matches()) {
					String property = matcher.group(1);
					String[] pieces = property.split("=", 2); //$NON-NLS-1$
					Map<String, Object> section = sections.get(currentSection);
					section.put(pieces[0].trim(), pieces[1].trim());
				}
			}

			line = reader.readLine();
		}

		// Recreate the sections hierarchy
		for (String sectionName : sections.keySet()) {
			if ("core".equals(sectionName)) continue; //$NON-NLS-1$
			Map<String, Object> section = sections.get(sectionName);
			if (sectionName.contains(".")) { //$NON-NLS-1$
				// Split the section name and recreate the missing hierarchy
				String[] pieces = sectionName.split("\\."); //$NON-NLS-1$
				Map<String, Object> parentSection = data;
				for (String subSectionName : pieces) {
					if ("core".equals(subSectionName)) continue; //$NON-NLS-1$

					if (sectionName.endsWith(subSectionName)) {
						parentSection.put(subSectionName, section);
					} else {
						Map<String, Object> subSection = (Map<String, Object>)parentSection.get(subSectionName);
						if (subSection == null) {
							subSection = new HashMap<String, Object>();
							parentSection.put(subSectionName, subSection);
						}
						parentSection = subSection;
					}
				}
			} else {
				// Place it into the root object, but check if it may exist
				Map<String, Object> oldSection = (Map<String, Object>)data.get(sectionName);
				if (oldSection != null) oldSection.putAll(section);
				else data.put(sectionName, section);
			}
		}
	}
}
