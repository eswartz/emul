/**
 * 
 */
package org.ejs.coffee.core.properties;

/**
 * @author ejs
 *
 */
public class SettingProperty extends AbstractProperty {

	private Object value;
	private final Object defaultValue;
	private String label;
	
	public SettingProperty(String name, String label, String description, Object value, IPropertyEditorProvider editorProvider) {
		super(null, value.getClass(), name, editorProvider);
		this.label = label;
		this.defaultValue = value;
		this.value = value;
	}
	public SettingProperty(String name, Class<?> klass, Object value) {
		super(null, klass, name, null);
		this.label = name;
		this.defaultValue = value;
		this.value = value;
	}
	public SettingProperty(String name, String label, String description, Object value) {
		this(name, label, description, value, null);
	}

	public SettingProperty(String name, Object value) {
		this(name, name, null, value, null);
	}

	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.AbstractProperty#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IProperty#getValue()
	 */
	public Object getValue() {
		return value;
	}
	
	public boolean isEnabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IProperty#setValue(java.lang.Object)
	 */
	public void setValue(Object value) {
		boolean incompatible = value != null && !type.isAssignableFrom(value.getClass());
		if (value != null && !incompatible) {
			if (!this.value.equals(value)) {
				this.value = value;
				firePropertyChange();
			}
		} else {
			if (!this.value.equals(defaultValue)) {
				this.value = defaultValue;
				firePropertyChange();
			}
			if (incompatible)
				System.err.println("Cannot assign setting " + getName() + " from "  + value);
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IProperty#setValueFromString(java.lang.String)
	 */
	public void setValueFromString(String value) {
		Object obj = PropertyUtils.convertStringToValue(value, type);
		if (obj == null) {
			obj = defaultValue;
			System.err.println("Cannot assign setting " + getName() + " from "  + value);
		}
		setValue(obj);
	}

}
