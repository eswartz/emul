/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.connection;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.te.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.core.connection.interfaces.IConnectionType;
import org.eclipse.tm.te.core.connection.interfaces.IConnectionTypeConstants;
import org.eclipse.tm.te.core.utils.text.StringUtil;
import org.eclipse.tm.te.runtime.nls.Messages;
import org.eclipse.tm.te.runtime.properties.PropertiesContainer;

/**
 * Connection type implementation.
 */
public class ConnectionType extends PropertiesContainer implements IConnectionType {

	/**
	 * Initialize the default connection type properties.
	 * <p>
	 * <b>Note:</b> This method is called from {@link #setInitializationData(IConfigurationElement, String, Object)}.
	 */
	protected void initDefaultProperties() {
		setProperty(IConnectionTypeConstants.PROPERTY_DEFINING_BUNDLE, null);
		setProperty(IConnectionTypeConstants.PROPERTY_ID, null);
		setProperty(IConnectionTypeConstants.PROPERTY_LABEL, ""); //$NON-NLS-1$
		setProperty(IConnectionTypeConstants.PROPERTY_SHORTNAME, ""); //$NON-NLS-1$
		setProperty(IConnectionTypeConstants.PROPERTY_DESCRIPTION, ""); //$NON-NLS-1$
		setProperty(IConnectionTypeConstants.PROPERTY_CATEGORY_ID, null);
		setProperty(IConnectionTypeConstants.PROPERTY_SUPPORTS_EARLY_FINISH, false);
		setProperty(IConnectionTypeConstants.PROPERTY_ENABLED, true);
		setProperty(IConnectionTypeConstants.PROPERTY_LAST_INVALID_CAUSE, null);
		setProperty(IConnectionTypeConstants.PROPERTY_CONNECT_IMMEDIATELY, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
    @SuppressWarnings("unchecked")
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		// Initialize the default properties first
		initDefaultProperties();

		// read the connection type attributes from the configuration element and apply
		if (config != null && config.getName().equals("connectionType")) { //$NON-NLS-1$

			// Set the id if an id attribute is specified and the id had been not
			// yet set by the connection type implementation class.
			// Throws an exception if the id is empty or null.
			String id = config.getAttribute("id"); //$NON-NLS-1$
			if (getId() == null && id != null && id.length() > 0) {
				setProperty(IConnectionTypeConstants.PROPERTY_ID, id);
			} else {
				throw createMissingMandatoryAttributeException("id", config.getContributor().getName()); //$NON-NLS-1$
			}

			// Initialize the label field by reading the <label> extension attribute if present.
			String label = config.getAttribute("label"); //$NON-NLS-1$
			if (label != null && label.length() > 0) {
				setProperty(IConnectionTypeConstants.PROPERTY_LABEL, label);
			} else {
				throw createMissingMandatoryAttributeException("label", config.getContributor().getName()); //$NON-NLS-1$
			}

			// Initialize the description field by reading the "<description>" extension child element if present.
			IConfigurationElement[] children = config.getChildren("description"); //$NON-NLS-1$
			// Only one description element is allow. All other will be ignored
			if (children.length > 0) {
				IConfigurationElement description = children[0];
				String value = description.getValue();
				setProperty(IConnectionTypeConstants.PROPERTY_DESCRIPTION, value != null ? value.trim() : ""); //$NON-NLS-1$
			}

			// Get the enabled attribute
			String isEnabled = config.getAttribute("isEnabled"); //$NON-NLS-1$

			// We allow to overwrite the enabled attribute from the plugin.xml
			// via a .options file property:
			//
			// The options file key is: <contribution plugin>/connectionType/<id>/enabled = true
			StringBuilder debugKey = new StringBuilder(config.getContributor().getName());
			debugKey.append("/connectionType/"); //$NON-NLS-1$
			debugKey.append(id.replaceAll("\\s", "_")); //$NON-NLS-1$ //$NON-NLS-2$
			debugKey.append("/enabled"); //$NON-NLS-1$

			if (Boolean.parseBoolean(Platform.getDebugOption(debugKey.toString()))) {
				isEnabled = "true"; //$NON-NLS-1$
			}

			// Apply the enabled attribute
			if (isEnabled != null && isEnabled.length() > 0 && (isEnabled.equalsIgnoreCase("true") || isEnabled.equalsIgnoreCase("false"))) { //$NON-NLS-1$ //$NON-NLS-2$
				setProperty(IConnectionTypeConstants.PROPERTY_ENABLED, Boolean.valueOf(isEnabled).booleanValue());
			}

			// Get the short name attribute
			String shortName = config.getAttribute("shortName"); //$NON-NLS-1$
			if (shortName != null && shortName.length() > 0) {
				setProperty(IConnectionTypeConstants.PROPERTY_SHORTNAME, shortName);
			}

			// Get the new connection wizard category id
			String categoryId = config.getAttribute("categoryId"); //$NON-NLS-1$
			if (categoryId != null && categoryId.length() > 0) {
				setProperty(IConnectionTypeConstants.PROPERTY_CATEGORY_ID, categoryId);
			}
		}

		// Check the initialization data object. Can be either a string or a hash table.
		if (data instanceof String) {
			// Tokenize the string and take the pairs as properties
			String[] params = StringUtil.tokenize((String)data, 0, false);
			for (int i = 0; i < params.length - 1; i++) {
				// Parameter key is the first parameter
				String paramKey = params[i];
				// Parameter value is the second parameter
				String paramValue = (i + 1) < params.length ? params[++i] : null;
				// Don't apply a property with the same name if already set (protects
				// explicit attributes like "shortName", "isEnabled" or "categoryId").
				if (paramKey != null && getProperty(paramKey) != null) continue;
				// Store it
				if (paramKey != null) setProperty(paramKey, paramValue);
			}
		}
		else if (data instanceof Map<?,?>) {
			// Just copy the map content to the properties
			Map<String, String> params = (Map<String, String>)data;
			for (String paramKey : params.keySet()) {
				// If the short name is already set, don't overwrite it
				if (IConnectionTypeConstants.PROPERTY_SHORTNAME.equals(paramKey) && getProperty(paramKey) != null) continue;
				// Store it
				if (paramKey != null) setProperty(paramKey, params.get(paramKey));
			}
		}
	}

	/**
	 * Creates a new {@link CoreException} to be thrown if a mandatory extension attribute
	 * is missing.
	 *
	 * @param attributeName The attribute name. Must be not <code>null</code>.
	 * @param extensionId The extension id. Must be not <code>null</code>.
	 *
	 * @return The {@link CoreException} instance.
	 */
	protected CoreException createMissingMandatoryAttributeException(String attributeName, String extensionId) {
		Assert.isNotNull(attributeName);
		Assert.isNotNull(extensionId);

		return new CoreException(new Status(IStatus.ERROR,
		                					CoreBundleActivator.getUniqueIdentifier(),
		                					0,
		                					NLS.bind(Messages.Extension_error_missingRequiredAttribute, attributeName, extensionId),
		                					null));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension#getId()
	 */
	@Override
    public final String getId() {
		return getStringProperty(IConnectionTypeConstants.PROPERTY_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension#getLabel()
	 */
	@Override
    public final String getLabel() {
		return getStringProperty(IConnectionTypeConstants.PROPERTY_LABEL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.interfaces.extensions.IExecutableExtension#getDescription()
	 */
	@Override
    public final String getDescription() {
		return getStringProperty(IConnectionTypeConstants.PROPERTY_DESCRIPTION);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.connection.interfaces.IConnectionType#isEnabled()
	 */
	@Override
    public boolean isEnabled() {
		return getBooleanProperty(IConnectionTypeConstants.PROPERTY_ENABLED) /*&& ConnectionTypeBindingsManager.getInstance().isConnectionTypeEnabled(getId())*/;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.connection.interfaces.IConnectionType#isValid()
	 */
	@Override
    public boolean isValid() {
		setProperty(IConnectionTypeConstants.PROPERTY_LAST_INVALID_CAUSE, null);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		// If the requested adapter is a connection service type,
		// forward to the connection type service manager
//		if (IConnectionTypeService.class.isAssignableFrom(adapter)) {
//			IConnectionTypeService service = ConnectionTypeServiceManager.getInstance().getService(getId(), adapter);
//			if (service != null) return service;
//		}
		return super.getAdapter(adapter);
	}

}
