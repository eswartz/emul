/**
 * 
 */
package v9t9.common.settings;

import v9t9.common.client.ISettingsHandler;
import ejs.base.settings.SettingProperty;


/**
 * @author ejs
 *
 */
public class SettingSchemaProperty extends SettingProperty {

	private SettingSchema schema;
	
	public SettingSchemaProperty(SettingSchema schema) {
		super(schema.getName(), 
				schema.getLabel(),
				schema.getDescription(),
				schema.getKlass() != null ? schema.getKlass() : 
			schema.getDefaultValue().getClass(), schema.getDefaultValue());
		this.schema = schema;
	}

	public SettingSchemaProperty(String name, Object defaultValue) {
		this(new SettingSchema(ISettingsHandler.TRANSIENT, name, defaultValue));
	}
	public SettingSchemaProperty(String name, Class<?> klass, Object defaultValue) {
		this(new SettingSchema(ISettingsHandler.TRANSIENT, name, klass, defaultValue));
	}

	public SettingSchema getSchema() {
		return schema;
	}

}
