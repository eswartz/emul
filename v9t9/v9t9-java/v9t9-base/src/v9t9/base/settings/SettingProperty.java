/**
 * 
 */
package v9t9.base.settings;

import v9t9.base.properties.AbstractProperty;
import v9t9.base.properties.PropertyUtils;

/**
 * @author ejs
 *
 */
public class SettingProperty extends AbstractProperty implements ISettingProperty {

	private Object value;
	private final Object defaultValue;
	private String label;
	
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
				System.err.println("Cannot assign setting " + getName() + " from "  + value);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.base.core.properties.IProperty#setValueFromString(java.lang.String)
	 */
	public void setValueFromString(String value) {
		Object obj = PropertyUtils.convertStringToValue(value, type);
		if (obj == null) {
			obj = defaultValue;
			System.err.println("Cannot assign setting " + getName() + " from "  + value);
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

}
