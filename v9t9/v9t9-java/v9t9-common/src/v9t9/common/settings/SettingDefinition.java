/**
 * 
 */
package v9t9.common.settings;

import v9t9.base.settings.SettingProperty;

/**
 * @author ejs
 *
 */
public class SettingDefinition {
	private String name;
	private Object defaultValue;
	private final String label;
	private final String description;
	private final String context;
	
	
	public SettingDefinition(String context, String name, String label, String description,
			Object storage) {
		this.context = context;
		this.name = name;
		this.label = label;
		this.description = description;
		this.defaultValue = storage;
	}


	public SettingDefinition(String context, String name, Object defaultValue) {
		this(context, name, null, null, defaultValue);
	}
	
	
	public String getName() {
		return name;
	}
	
	public String getContext() {
		return context;
	}
	
	public SettingProperty createSetting() {
		return new SettingProperty(name, label, description, defaultValue);
	}

	
}
