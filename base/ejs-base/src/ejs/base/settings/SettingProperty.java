/*
  SettingProperty.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;

import ejs.base.properties.AbstractProperty;
import ejs.base.properties.PropertyUtils;


/**
 * @author ejs
 *
 */
public class SettingProperty extends AbstractProperty implements ISettingProperty {

	private static final Logger logger = Logger.getLogger(SettingProperty.class);
	
	private Object value;
	private final Object defaultValue;
	private final String label;
	
	public SettingProperty(String name, String label, String description, Class<?> klass, Object value) {
		super(null, klass != null ? klass : value.getClass(), name);
		this.label = label;
		this.defaultValue = value;
		this.value = value;
		setDescription(description);
	}
	public SettingProperty(String name, String label, String description, Object value) {
		super(null, value.getClass(), name);
		this.label = label;
		this.defaultValue = value;
		this.value = value;
		setDescription(description);
	}
	public SettingProperty(String name, Class<?> klass, Object value) {
		super(null, klass, name);
		this.label = name;
		this.defaultValue = value;
		this.value = value;
	}

	public SettingProperty(String name, Object value) {
		this(name, name, null, value);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.base.core.properties.AbstractProperty#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.base.core.properties.IProperty#getValue()
	 */
	public Object getValue() {
		return value;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.base.settings.ISettingProperty#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.base.core.properties.IProperty#setValue(java.lang.Object)
	 */
	public void setValue(Object value) {
		boolean incompatible = value != null && !type.isAssignableFrom(value.getClass());
		if (incompatible && defaultValue != null && Collection.class.isAssignableFrom(defaultValue.getClass())) {
			// make collection
			try {
				if (value.getClass().isArray()) {
					Collection<?> coll = new ArrayList<Object>(Arrays.asList((Object[]) value));
					value = coll;
				}
				incompatible = false;
			} catch (ClassCastException e) {
			}
		}
		if (value != null && !incompatible) {
			if (this.value == null || !this.value.equals(value)) {
				this.value = value;
				firePropertyChange();
			}
		} else {
			if (this.value != null && !this.value.equals(defaultValue)) {
				this.value = defaultValue;
				firePropertyChange();
			}
			if (incompatible)
				logger.error("Cannot assign setting " + getName() + " from "  + value);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.base.core.properties.IProperty#setValueFromString(java.lang.String)
	 */
	public void setValueFromString(String value) {
		Object obj = PropertyUtils.convertStringToValue(value, type);
		if (obj == null) {
			obj = defaultValue;
			logger.error("Cannot assign setting " + getName() + " from "  + value);
		}
		setValue(obj);
	}
	/* (non-Javadoc)
	 * @see v9t9.base.settings.ISettingProperty#isDefault()
	 */
	@Override
	public boolean isDefault() {
		return value == null || value.equals(defaultValue);
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.settings.ISettingProperty#resetToDefault()
	 */
	@Override
	public void resetToDefault() {
		if (defaultValue != null)
			setValue(defaultValue);
	}

}
