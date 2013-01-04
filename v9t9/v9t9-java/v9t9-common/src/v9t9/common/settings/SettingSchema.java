/**
 * 
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
