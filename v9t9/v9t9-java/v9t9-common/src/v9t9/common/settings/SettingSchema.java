/*
  SettingSchema.java

  (c) 2011-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.settings;

import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public class SettingSchema {
	private String name;
	private Object defaultValue;
	private final String label;
	private final String description;
	private final String context;
	private final Class<?> klass;
	private String editorId;
	

	public SettingSchema(String context, String name, String label, String description,
			String editorId,
			Object storage, Class<?> klass) {
		this.context = context;
		this.name = name;
		this.label = label;
		this.description = description;
		this.editorId = editorId;
		this.defaultValue = storage;
		this.klass = klass;
	}
	public SettingSchema(String context, String name, String label, String description,
			Object storage, Class<?> klass) {
		this.context = context;
		this.name = name;
		this.label = label;
		this.description = description;
		this.defaultValue = storage;
		this.klass = klass;
	}

	public SettingSchema(String context, String name, String label, String description,
			Object storage) {
		this(context, name, label, description, storage, null);
	}

	public SettingSchema(String context, String name, Class<?> klass, Object value) {
		this(context, name, null, null, value, klass);
	}


	public SettingSchema(String context, String name, Object defaultValue) {
		this(context, name, null, null, defaultValue);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SettingSchema: " + getName();
	}
	
	
	public String getName() {
		return name;
	}
	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
	public String getContext() {
		return context;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @return the klass
	 */
	public Class<?> getKlass() {
		return klass;
	}

	/**
	 * @return
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 *  This tells how to create editor UI for the setting.
	 * By default, the type of the property's value is used to create UI.
	 * @return <code>null</code> or id (e.g. DeviceEditorIdConstants)
	 */
	public String getEditorId() {
		return editorId;
	}
	
	
	public IProperty createSetting() {
		IProperty prop = new SettingSchemaProperty(this);
		return prop;
	}
}
