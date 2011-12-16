/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.stepper;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId;

/**
 * A stepper attributes utility provides a set of static methods
 * to access the attributes of a step.
 */
public class StepperAttributeUtil {
	/**
	 * Get the full qualified key to get or set data in the data.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @return The full qualified key.
	 */
	protected final static String getFullQualifiedKey(String key, IFullQualifiedId fullQualifiedId, Object data) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		return (fullQualifiedId != null ? fullQualifiedId.toString() : "") + key; //$NON-NLS-1$
	}

	/**
	 * Returns the properties container for the given data object.
	 *
	 * @param data The data object or <code>null</code>.
	 * @return The properties container or <code>null</code> if the container cannot be determined.
	 */
	private static IPropertiesContainer getPropertiesContainer(Object data) {
		if (data instanceof IPropertiesContainer) {
			return (IPropertiesContainer) data;
		}
		if (data instanceof IAdaptable) {
			return (IPropertiesContainer)((IAdaptable)data).getAdapter(IPropertiesContainer.class);
		}
		return null;
	}

	/**
	 * Get a property from the data. If the value is not stored within the full qualified id, the
	 * value stored within the parent id will be returned.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @return The property value or <code>null</code> if either the data has no property container
	 *         or the property is not set.
	 */
	public final static Object getProperty(String key, IFullQualifiedId fullQualifiedId, Object data) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		IPropertiesContainer container = getPropertiesContainer(data);
		if (container == null) {
			return null;
		}
		if (fullQualifiedId == null || container
		                .getProperty(getFullQualifiedKey(key, fullQualifiedId, data)) != null) {
			return container.getProperty(getFullQualifiedKey(key, fullQualifiedId, data));
		}
		return container.getProperty(getFullQualifiedKey(key, fullQualifiedId.getParentId(), data));
	}

	/**
	 * Get a string property from the data. If the value is not stored within the full qualified id,
	 * the value stored within the parent id will be returned.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @return The string property value or <code>null</code> if either the data has no property
	 *         container or the property is not set.
	 */
	public final static String getStringProperty(String key, IFullQualifiedId fullQualifiedId, Object data) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		IPropertiesContainer container = getPropertiesContainer(data);
		if (container == null) {
			return null;
		}
		if (fullQualifiedId == null || container.getProperty(getFullQualifiedKey(key, fullQualifiedId, data)) != null) {
			return container.getStringProperty(getFullQualifiedKey(key, fullQualifiedId, data));
		}
		return container.getStringProperty(getFullQualifiedKey(key, fullQualifiedId.getParentId(), data));
	}

	/**
	 * Get a boolean property from the data. If the value is not stored within the full qualified
	 * id, the value stored within the parent id will be returned.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @return The boolean property value or <code>false</code> if either the data has no property
	 *         container or the property is not set.
	 */
	public final static boolean getBooleanProperty(String key, IFullQualifiedId fullQualifiedId, Object data) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		IPropertiesContainer container = getPropertiesContainer(data);
		if (container == null) {
			return false;
		}
		if (fullQualifiedId == null || container.getProperty(getFullQualifiedKey(key, fullQualifiedId, data)) != null) {
			return container.getBooleanProperty(getFullQualifiedKey(key, fullQualifiedId, data));
		}
		return container.getBooleanProperty(getFullQualifiedKey(key, fullQualifiedId.getParentId(), data));
	}

	/**
	 * Get a int property from the data.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @return The int property value or <code>-1</code> if either the data has no property
	 *         container or the property is not set.
	 */
	public final static int getIntProperty(String key, IFullQualifiedId fullQualifiedId, Object data) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		IPropertiesContainer container = getPropertiesContainer(data);
		if (container == null) {
			return -1;
		}
		if (fullQualifiedId == null || container.getProperty(getFullQualifiedKey(key, fullQualifiedId, data)) != null) {
			return container.getIntProperty(getFullQualifiedKey(key, fullQualifiedId, data));
		}
		return container.getIntProperty(getFullQualifiedKey(key, fullQualifiedId.getParentId(), data));
	}

	/**
	 * Check if a property is set.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @return <code>true</code> if a property value is set.
	 */
	public final static boolean isPropertySet(String key, IFullQualifiedId fullQualifiedId, Object data) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		IPropertiesContainer container = getPropertiesContainer(data);
		if (container == null) {
			return false;
		}
		return container.getProperty(getFullQualifiedKey(key, fullQualifiedId, data)) != null;
	}

	/**
	 * Set a property value to the data.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @param value The new value.
	 * @return <code>true</code> if the value was set.
	 */
	public final static boolean setProperty(String key, IFullQualifiedId fullQualifiedId, Object data, Object value) {
		return setProperty(key, fullQualifiedId, data, value, false);
	}

	/**
	 * Set a property value to the data and optional share it through the parent full qualified id.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @param value The new value.
	 * @param share When <code>true</code>, the value is also stored within the parent full
	 *            qualified id to share the value with other steps within the same parent (group).
	 * @return <code>true</code> if the value was set.
	 */
	public final static boolean setProperty(String key, IFullQualifiedId fullQualifiedId, Object data, Object value, boolean share) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		IPropertiesContainer container = getPropertiesContainer(data);
		if (container == null) {
			return false;
		}
		if (share && fullQualifiedId != null) {
			container.setProperty(getFullQualifiedKey(key, fullQualifiedId.getParentId(), data), value);
		}
		return container.setProperty(getFullQualifiedKey(key, fullQualifiedId, data), value);
	}

	/**
	 * Set a boolean property value to the data.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @param value The new boolean value.
	 * @return <code>true</code> if the value was set.
	 */
	public final static boolean setProperty(String key, IFullQualifiedId fullQualifiedId, Object data, boolean value) {
		return setProperty(key, fullQualifiedId, data, value, false);
	}

	/**
	 * Set a boolean property value to the data and optional share it through the parent full
	 * qualified id.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @param value The new boolean value.
	 * @param share When <code>true</code>, the value is also stored within the parent full
	 *            qualified id to share the value with other steps within the same parent (group).
	 * @return <code>true</code> if the value was set.
	 */
	public final static boolean setProperty(String key, IFullQualifiedId fullQualifiedId, Object data, boolean value, boolean share) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		IPropertiesContainer container = getPropertiesContainer(data);
		if (container == null) {
			return false;
		}
		if (share && fullQualifiedId != null) {
			container.setProperty(getFullQualifiedKey(key, fullQualifiedId.getParentId(), data), value);
		}
		return container.setProperty(getFullQualifiedKey(key, fullQualifiedId, data), value);
	}

	/**
	 * Set a int property value to the data.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @param value The new int value.
	 * @return <code>true</code> if the value was set.
	 */
	public final static boolean setProperty(String key, IFullQualifiedId fullQualifiedId, Object data, int value) {
		return setProperty(key, fullQualifiedId, data, value, false);
	}

	/**
	 * Set an int property value to the data and optional share it through the parent full qualified
	 * id.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @param value The new int value.
	 * @param share When <code>true</code>, the value is also stored within the parent full
	 *            qualified id to share the value with other steps within the same parent (group).
	 * @return <code>true</code> if the value was set.
	 */
	public final static boolean setProperty(String key, IFullQualifiedId fullQualifiedId, Object data, int value, boolean share) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		IPropertiesContainer container = getPropertiesContainer(data);
		if (container == null) {
			return false;
		}
		if (share && fullQualifiedId != null) {
			container.setProperty(getFullQualifiedKey(key, fullQualifiedId.getParentId(), data), value);
		}
		return container.setProperty(getFullQualifiedKey(key, fullQualifiedId, data), value);
	}
}
