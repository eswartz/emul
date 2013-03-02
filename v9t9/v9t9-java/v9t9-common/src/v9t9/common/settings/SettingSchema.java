/*
  SettingSchema.java

  (c) 2011-2013 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
	
	public IProperty createSetting() {
		IProperty prop = new SettingSchemaProperty(this);
		return prop;
	}

	/**
	 * @return
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	
}
