/*
  GetterSetterProperty.java

  (c) 2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

import java.lang.reflect.Method;

/**
 * A property that has a getter/setter pair (rather than direct
 * Field access) 
 * @author ejs
 *
 */
public class GetterSetterProperty extends AbstractProperty {

	
	protected final Method getter, setter;
	protected final Object obj;

	public GetterSetterProperty(IClassPropertyFactory factory, Object obj, 
			Method getter, Method setter,
			Class<?> type, String name) {
		super(factory, obj.getClass(), name); 
				//editorProvider != null ? editorProvider : new FieldPropertyEditorProvider());
		this.obj = obj;
		this.getter = getter;
		if (getter != null) 
			getter.setAccessible(true);
		this.setter = setter;
		if (setter != null) 
			setter.setAccessible(true);
		setType(type);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getter == null) ? 0 : getter.hashCode());
		result = prime * result + ((setter == null) ? 0 : setter.hashCode());
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GetterSetterProperty other = (GetterSetterProperty) obj;
		if (getter == null) {
			if (other.getter != null) {
				return false;
			}
		} else if (!getter.equals(other.getter)) {
			return false;
		}
		if (setter == null) {
			if (other.setter != null) {
				return false;
			}
		} else if (!setter.equals(other.setter)) {
			return false;
		}
		if (this.obj == null) {
			if (other.obj != null) {
				return false;
			}
		} else if (!this.obj.equals(other.obj)) {
			return false;
		}
		return true;
	}



	/* (non-Javadoc)
	 * 
	 */
	/*
	public CellEditor createCellEditor(Composite composite) {
		return new TextCellEditor(composite);
	}
	*/
	
	protected GetterSetterProperty getProperty() { return this; }
	
	/* (non-Javadoc)
	 * 
	 */
	public Object getValue() {
		try {
			return getter.invoke(obj);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/* (non-Javadoc)
	 * 
	 */
	public void setValue(Object value) {
		try {
			if (value instanceof String && type != String.class) {
				setValueFromString((String) value);
			} else if (value instanceof Double && type == float.class) {
				setter.invoke(obj, ((Double) value).floatValue());
				firePropertyChange();
			} else {
				setter.invoke(obj, value);
				firePropertyChange();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setValueFromString(String txt) {
		Object v = PropertyUtils.convertStringToValue(txt, type);
		try {
			setter.invoke(obj, v);
			firePropertyChange();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public Object getObject() {
		return obj;
	}

}
