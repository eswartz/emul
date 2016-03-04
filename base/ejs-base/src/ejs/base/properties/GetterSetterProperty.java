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
public class GetterSetterProperty extends AbstractClassBasedProperty {

	
	protected final Method getter, setter;

	public GetterSetterProperty(Class<?> type, String name, Object obj, 
			Method getter, Method setter) {
		super(type, name, obj); 
		this.getter = getter;
		if (getter != null) 
			getter.setAccessible(true);
		this.setter = setter;
		if (setter != null) 
			setter.setAccessible(true);
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
	 * @see ejs.base.properties.AbstractClassBasedProperty#doGetValue()
	 */
	@Override
	protected Object doGetValue() throws Exception {
		return getter != null ? getter.invoke(obj) : null;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.properties.BaseClassProperty#doSetValue(java.lang.Object)
	 */
	@Override
	protected void doSetValue(Object value) throws Exception {
		if (setter != null) 
			setter.invoke(obj, value);
	}

	public Object getObject() {
		return obj;
	}

	/**
	 * @return the getter
	 */
	public Method getGetter() {
		return getter;
	}
	/**
	 * @return the setter
	 */
	public Method getSetter() {
		return setter;
	}
}
