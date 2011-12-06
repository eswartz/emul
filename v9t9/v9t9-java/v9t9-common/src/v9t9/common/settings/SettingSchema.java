/**
 * 
 */
package v9t9.common.settings;

import v9t9.base.settings.SettingProperty;

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
	
	
	public String getName() {
		return name;
	}
	
	public String getContext() {
		return context;
	}
	
	public SettingProperty createSetting() {
		SettingProperty prop;
		if (klass != null) {
			// FIXME
			prop = new SettingProperty(name, klass, defaultValue);
			prop.setDescription(description);
		}
		else
			prop = new SettingProperty(name, label, description, defaultValue);
		return prop;
	}

	
}
