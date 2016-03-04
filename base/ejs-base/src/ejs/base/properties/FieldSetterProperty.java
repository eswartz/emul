/*
  FieldProperty.java

  (c) 2010-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This kind of property reads values directly from
 * a Field in an Object but writes them through a #set... method.
 * @author ejs
 *
 */
public class FieldSetterProperty extends AbstractClassBasedProperty {

	
	protected Field field;
	private Method setter;

	public FieldSetterProperty(Class<?> type, String name, Object obj, Field field, Method setter) {
		super(type, name, obj); 
		this.setter = setter;
		this.field = field;
		field.setAccessible(true);
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result
				+ ((field == null) ? 0 : field.hashCode());
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
		FieldSetterProperty other = (FieldSetterProperty) obj;
		if (field == null) {
			if (other.field != null) {
				return false;
			}
		} else if (!field.equals(other.field)) {
			return false;
		}
		if (field == null) {
			if (other.field != null) {
				return false;
			}
		} else if (!field.equals(other.field)) {
			return false;
		}
		if (this.setter == null) {
			if (other.setter != null) {
				return false;
			}
		} else if (!this.setter.equals(other.setter)) {
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
	 * @see ejs.base.properties.AbstractClassBasedProperty#doGetValue()
	 */
	@Override
	protected Object doGetValue() throws Exception {
		return field.get(obj);
	}

	/* (non-Javadoc)
	 * @see ejs.base.properties.BaseClassProperty#doSetValue(java.lang.Object)
	 */
	@Override
	protected void doSetValue(Object value) throws Exception {
		setter.invoke(obj, value);
	}

	public Field getField() {
		return field;
	}
	
	public Method getSetter() {
		return setter;
	}
	public Object getObject() {
		return obj;
	}


}
