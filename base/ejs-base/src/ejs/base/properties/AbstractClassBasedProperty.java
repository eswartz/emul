/*
  BaseClassProperty.java

  (c) 2010-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;


/**
 * This kind of property reads/writes values to Objects
 * using reflection.
 * @author ejs
 *
 */
public abstract class AbstractClassBasedProperty extends AbstractProperty {
	protected Object obj;
	
	protected AbstractClassBasedProperty(Class<?> type, String name, Object obj) {
		super(null, type, name);
		this.obj = obj;
		this.type = type;
	}
	
	abstract protected Object doGetValue() throws Exception;
	abstract protected void doSetValue(Object value) throws Exception;

	public static Object getCompatibleObject(Class<?> type, Object value) {
		if (value == null)
			return null;
		
		if (value instanceof Double && type == float.class)
			return ((Double) value).floatValue();
		
		// note: value's class will be boxed, so assume a match until use cases prove otherwise
		return value;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.properties.IProperty#getValue()
	 */
	@Override
	public final Object getValue() {
		try {
			return doGetValue();
		} catch (Exception e) {
			throw new RuntimeException("failed to get " + name + " from " + obj, e);
		}
	}
	
	public final void setValue(Object value) {
		try {
			try {
				doSetValue(getCompatibleObject(type, value));
				firePropertyChange();
			} catch (IllegalArgumentException e) {
				setValueFromString(String.valueOf(value));
			}
		} catch (Exception e) {
			throw new RuntimeException("failed to set " + name + " to " + value, e);
		}
	}
	
	public final void setValueFromString(String txt) {
		Object v = PropertyUtils.convertStringToValue(txt, type);
		try {
			doSetValue(v);
			firePropertyChange();
		} catch (Exception e) {
			throw new RuntimeException("failed to set " + name + " to " + v, e);
		}
	}


}
