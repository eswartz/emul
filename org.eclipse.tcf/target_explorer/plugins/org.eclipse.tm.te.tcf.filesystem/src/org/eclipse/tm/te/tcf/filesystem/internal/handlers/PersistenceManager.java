/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River)	[360494]Provide an "Open With" action in the pop 
 * 								up menu of file system nodes of Target Explorer.
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tm.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tm.te.tcf.filesystem.internal.preferences.TargetExplorerPreferencePage;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * A facility class to load and save persistent data such including resolved content types, file's
 * properties, and time stamps etc.
 */
public class PersistenceManager {
	// The XML element of unresolvable.
	private static final String ELEMENT_UNRESOLVABLE = "unresolvable"; //$NON-NLS-1$

	// The root element of "unresolvables"
	private static final String ELEMENT_UNRESOLVED = "unresolved"; //$NON-NLS-1$

	// The attribute "contentType" to specify the content type id of the file.
	private static final String ATTR_CONTENT_TYPE = "contentType"; //$NON-NLS-1$

	// The XML element of resolvable.
	private static final String ELEMENT_RESOLVABLE = "resolvable"; //$NON-NLS-1$

	// The root element of "resolvables"
	private static final String ELEMENT_RESOLVED = "resolved"; //$NON-NLS-1$

	// The root element of the memento for content type resolving.
	private static final String CONTENT_TYPE_ROOT = "contentTypes"; //$NON-NLS-1$

	// The XML file name used to store the resolved content types.
	private static final String CONTENT_TYPE_FILE = "contentTypes.xml"; //$NON-NLS-1$

	// The attribute "value"
	private static final String ATTR_VALUE = "value"; //$NON-NLS-1$

	// The attribute "local name" of a qualified name.
	private static final String ATTR_LOCAL_NAME = "localName"; //$NON-NLS-1$

	// The attribute "qualifier" of a qualified name.
	private static final String ATTR_QUALIFIER = "qualifier"; //$NON-NLS-1$

	// The attribute of a node's URL
	private static final String ATTR_URL = "URL"; //$NON-NLS-1$

	// The element "property" to record a file's property
	private static final String ELEMENT_PROPERTY = "property"; //$NON-NLS-1$

	// The element "file" to specify a file's entry.
	private static final String ELEMENT_FILE = "file"; //$NON-NLS-1$

	// The root element of properties.
	private static final String PERSISTENT_ROOT = "properties"; //$NON-NLS-1$

	// Time stamp file used to persist the time stamps of each file.
	private static final String TIMESTAMP_FILE = "timestamps.xml"; //$NON-NLS-1$

	// The file used to store persistent properties of each file.
	private static final String PERSISTENT_FILE = "persistent.xml"; //$NON-NLS-1$

	// The singleton instance.
	private static PersistenceManager instance;

	// The time stamp for each file.
	private Map<URL, Long> timestamps;

	// The persistent properties of the files.
	private Map<URL, Map<QualifiedName, String>> properties;

	// Already known resolved content type of file nodes specified by their URLs.
	private Map<URL, IContentType> resolved;

	// Already known unresolvable file nodes specified by their URLs.
	private Map<URL, URL> unresolved;

	/**
	 * Get the singleton cache manager.
	 * 
	 * @return The singleton cache manager.
	 */
	public static PersistenceManager getInstance() {
		if (instance == null) {
			instance = new PersistenceManager();
		}
		return instance;
	}

	/**
	 * Create a Persistent Manager instance.
	 */
	private PersistenceManager() {
		loadTimestamps();
		loadPersistentProperties();
		loadContentTypes();
	}

	/**
	 * If the node is already considered unresolvable.
	 * 
	 * @param node The file node.
	 * @return true if it is not resolvable or else false.
	 */
	public boolean isUnresovled(FSTreeNode node) {
		return unresolved.get(node.getLocationURL()) != null;
	}

	/**
	 * Get the resolved content type of the node.
	 * 
	 * @param node The file node.
	 * @return the content type of the node if it is resolvable or null.
	 */
	public IContentType getResolved(FSTreeNode node) {
		return resolved.get(node.getLocationURL());
	}

	/**
	 * Add the node and its content type to the resolved list.
	 * 
	 * @param node The file node.
	 * @param contentType Its content type.
	 */
	public void addResovled(FSTreeNode node, IContentType contentType) {
		resolved.put(node.getLocationURL(), contentType);
	}

	/**
	 * Add the node as an unresolvable node.
	 * 
	 * @param node The file node.
	 */
	public void addUnresolved(FSTreeNode node) {
		unresolved.put(node.getLocationURL(), node.getLocationURL());
	}

	/**
	 * If the option of "autosaving" is set to on.
	 * 
	 * @return true if it is auto saving or else false.
	 */
	public boolean isAutoSaving() {
		IPreferenceStore preferenceStore = UIPlugin.getDefault().getPreferenceStore();
		boolean autoSaving = preferenceStore
		                .getBoolean(TargetExplorerPreferencePage.PREF_AUTOSAVING);
		return autoSaving;
	}

	/**
	 * Load the persistent properties from the persistent file in the cache's root directory.
	 */
	private void loadPersistentProperties() {
		IMemento memento = readMemento(PERSISTENT_FILE, PERSISTENT_ROOT);
		properties = Collections.synchronizedMap(new HashMap<URL, Map<QualifiedName, String>>());
		IMemento[] children = memento.getChildren(ELEMENT_FILE);
		if (children != null && children.length > 0) {
			for (IMemento child : children) {
				try {
					String str = child.getString(ATTR_URL);
					URL url = new URL(str);
					Map<QualifiedName, String> nodeProperties = loadFileProperties(child);
					properties.put(url, nodeProperties);
				}
				catch (MalformedURLException e) {
				}
			}
		}
	}

	/**
	 * Load the content type information from the content type file.
	 */
	private void loadContentTypes() {
		IMemento memento = readMemento(CONTENT_TYPE_FILE, CONTENT_TYPE_ROOT);
		resolved = Collections.synchronizedMap(new HashMap<URL, IContentType>());
		unresolved = Collections.synchronizedMap(new HashMap<URL, URL>());
		IMemento mResolved = memento.getChild(ELEMENT_RESOLVED);
		if (mResolved != null) {
			IMemento[] children = mResolved.getChildren(ELEMENT_RESOLVABLE);
			if (children != null && children.length > 0) {
				for (IMemento child : children) {
					try {
						String str = child.getString(ATTR_URL);
						URL url = new URL(str);
						String id = child.getString(ATTR_CONTENT_TYPE);
						IContentType contentType = Platform.getContentTypeManager()
						                .getContentType(id);
						if (contentType != null) {
							resolved.put(url, contentType);
						}
					}
					catch (MalformedURLException e) {
					}
				}
			}
		}
		IMemento mUnresolved = memento.getChild(ELEMENT_UNRESOLVED);
		if (mUnresolved != null) {
			IMemento[] children = mUnresolved.getChildren(ELEMENT_UNRESOLVABLE);
			if (children != null && children.length > 0) {
				for (IMemento child : children) {
					try {
						String str = child.getString(ATTR_URL);
						URL url = new URL(str);
						unresolved.put(url, url);
					}
					catch (MalformedURLException e) {
					}
				}
			}
		}
	}

	/**
	 * Save the content type information to the content type file.
	 */
	private void saveContentTypes() {
		XMLMemento memento = XMLMemento.createWriteRoot(CONTENT_TYPE_ROOT);
		IMemento mResolved = memento.createChild(ELEMENT_RESOLVED);
		for (URL key : resolved.keySet()) {
			IContentType iContentType = resolved.get(key);
			IMemento mResolvable = mResolved.createChild(ELEMENT_RESOLVABLE);
			mResolvable.putString(ATTR_URL, key.toString());
			mResolvable.putString(ATTR_CONTENT_TYPE, iContentType.getId());
		}
		IMemento mUnresolved = memento.createChild(ELEMENT_UNRESOLVED);
		for (URL key : unresolved.keySet()) {
			IMemento mUnresolvable = mUnresolved.createChild(ELEMENT_UNRESOLVABLE);
			mUnresolvable.putString(ATTR_URL, key.toString());
		}
		writeMemento(memento, CONTENT_TYPE_FILE);
	}

	/**
	 * Load a file's properties from the memento node.
	 * 
	 * @param memento The memento node.
	 * @return The properties as a map.
	 */
	private Map<QualifiedName, String> loadFileProperties(IMemento memento) {
		Map<QualifiedName, String> properties = Collections
		                .synchronizedMap(new HashMap<QualifiedName, String>());
		IMemento[] children = memento.getChildren(ELEMENT_PROPERTY);
		if (children != null && children.length > 0) {
			for (IMemento child : children) {
				String qualifier = child.getString(ATTR_QUALIFIER);
				String localName = child.getString(ATTR_LOCAL_NAME);
				QualifiedName name = new QualifiedName(qualifier, localName);
				String value = child.getString(ATTR_VALUE);
				properties.put(name, value);
			}
		}
		return properties;
	}

	/**
	 * Read the memento from a memento file using the specified root element name.
	 * 
	 * @param mementoFile The memento file.
	 * @param mementoRoot The memento's root element name.
	 * @return A memento of this file or an empty memento if the file does not exist.
	 */
	private IMemento readMemento(String mementoFile, String mementoRoot) {
		File location = CacheManager.getInstance().getCacheRoot();
		File stateFile = new File(location, mementoFile);
		if (stateFile.exists()) {
			BufferedReader reader = null;
			try {
				FileInputStream input = new FileInputStream(stateFile);
				reader = new BufferedReader(new InputStreamReader(input, "utf-8")); //$NON-NLS-1$
				IMemento memento = XMLMemento.createReadRoot(reader);
				return memento;
			}
			catch (IOException e) {
			}
			catch (WorkbenchException e) {
			}
			finally {
				if (reader != null) {
					try {
						reader.close();
					}
					catch (Exception e) {
					}
				}
			}
		}
		return XMLMemento.createWriteRoot(mementoRoot);
	}

	/**
	 * Save the time stamps to the persistent file.
	 */
	private void savePersistentProperties() {
		XMLMemento memento = XMLMemento.createWriteRoot(PERSISTENT_ROOT);
		for (URL key : properties.keySet()) {
			Map<QualifiedName, String> nodeProperties = properties.get(key);
			if (!nodeProperties.keySet().isEmpty()) {
				IMemento mFile = memento.createChild(ELEMENT_FILE);
				mFile.putString(ATTR_URL, key.toString());
				saveFileProperties(mFile, nodeProperties);
			}
		}
		writeMemento(memento, PERSISTENT_FILE);
	}

	/**
	 * Save the file's properties to a memento.
	 * 
	 * @param memento The memento object.
	 * @param properties The file properties.
	 */
	private void saveFileProperties(IMemento memento, Map<QualifiedName, String> properties) {
		for (QualifiedName name : properties.keySet()) {
			IMemento mProperty = memento.createChild(ELEMENT_PROPERTY);
			mProperty.putString(ATTR_QUALIFIER, name.getQualifier());
			mProperty.putString(ATTR_LOCAL_NAME, name.getLocalName());
			mProperty.putString(ATTR_VALUE, properties.get(name));
		}
	}

	/**
	 * Write the memento to a memento file.
	 * 
	 * @param memento The memento object.
	 * @param mementoFile The file to write to.
	 */
	private void writeMemento(XMLMemento memento, String mementoFile) {
		OutputStreamWriter writer = null;
		try {
			File location = CacheManager.getInstance().getCacheRoot();
			File stateFile = new File(location, mementoFile);
			FileOutputStream stream = new FileOutputStream(stateFile);
			writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
			memento.save(writer);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				}
				catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Get the file properties of the specified node from the properties map.
	 * 
	 * @param node The file node.
	 * @return The file properties object or empty properties object if it does not exist.
	 */
	public Map<QualifiedName, String> getPersistentProperties(FSTreeNode node) {
		Map<QualifiedName, String> nodeProperties = properties.get(node.getLocationURL());
		if (nodeProperties == null) {
			nodeProperties = Collections.synchronizedMap(new HashMap<QualifiedName, String>());
			properties.put(node.getLocationURL(), nodeProperties);
		}
		return nodeProperties;
	}

	/**
	 * Load the time stamps from the time stamps file in the cache's root directory.
	 */
	private void loadTimestamps() {
		timestamps = Collections.synchronizedMap(new HashMap<URL, Long>());
		File location = CacheManager.getInstance().getCacheRoot();
		File tsFile = new File(location, TIMESTAMP_FILE);
		if (tsFile.exists()) {
			Properties properties = new Properties();
			InputStream input = null;
			try {
				input = new BufferedInputStream(new FileInputStream(tsFile));
				properties.loadFromXML(input);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if (input != null) {
					try {
						input.close();
					}
					catch (IOException e) {
					}
				}
			}
			Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				String value = properties.getProperty(key);
				long timestamp = 0L;
				try {
					timestamp = Long.parseLong(value);
					timestamps.put(new URL(key), Long.valueOf(timestamp));
				}
				catch (Exception nfe) {
				}
			}
		}
	}

	/**
	 * Save the time stamps to the time stamps file.
	 */
	private void saveTimestamps() {
		Properties properties = new Properties();
		for (URL key : timestamps.keySet()) {
			Long timestamp = timestamps.get(key);
			properties.setProperty(key.toString(), timestamp.toString());
		}
		File location = CacheManager.getInstance().getCacheRoot();
		File fTimestamp = new File(location, TIMESTAMP_FILE);
		OutputStream output = null;
		try {
			output = new BufferedOutputStream(new FileOutputStream(fTimestamp));
			properties.storeToXML(output, null);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (output != null) {
				try {
					output.close();
				}
				catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Set the time stamp of the FSTreeNode with the specified location.
	 * 
	 * @param url The FSTreeNode's location URL.
	 * @param timestamp The new base time stamp to be set.
	 */
	public void setBaseTimestamp(URL url, long timestamp) {
		timestamps.put(url, Long.valueOf(timestamp));
	}

	/**
	 * Remove the time stamp entry with the specified URL.
	 * 
	 * @param url The URL key.
	 */
	public void removeBaseTimestamp(URL url) {
		timestamps.remove(url);
	}

	/**
	 * Get the time stamp of the FSTreeNode with the specified location.
	 * 
	 * @param url The FSTreeNode's location URL.
	 * @return The FSTreeNode's base time stamp.
	 */
	public long getBaseTimestamp(URL url) {
		Long timestamp = timestamps.get(url);
		return timestamp == null ? 0L : timestamp.longValue();
	}

	/**
	 * Dispose the cache manager so that it has a chance to save the timestamps and the persistent
	 * properties.
	 */
	public void dispose() {
		saveTimestamps();
		savePersistentProperties();
		saveContentTypes();
	}
}
