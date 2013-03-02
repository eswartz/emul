/*
  PropertyUtils.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

import java.lang.reflect.Array;

/**
 * @author ejs
 *
 */
public class PropertyUtils {
	
	public static void setValueFromString(IProperty property, String txt) {
		Class<?> klass = property.getType();
		Object v = convertStringToValue(txt, klass);
		property.setValue(v);
	}

	public static Object convertStringToValue(String txt, Class<?> klass) {
		Object v = null;
		try {
			if (klass.equals(Double.class) || klass.equals(Double.TYPE))
				v = Double.parseDouble(txt);
			else if (klass.equals(Float.class) || klass.equals(Float.TYPE))
				v = Float.parseFloat(txt);
			else if (klass.equals(Integer.class) || klass.equals(Integer.TYPE))
				v = Integer.parseInt(txt);
			else if (klass.equals(String.class))
				v = txt;
			else if (klass.equals(Boolean.class) || klass.equals(Boolean.TYPE))
				v = Boolean.parseBoolean(txt);
			else if (klass.isEnum()) {
				for (Object val : klass.getEnumConstants()) {
					if (val.toString().equals(txt)) {
						v = val;
						break;
					}
				}
				if (v == null) {
					new IllegalStateException("invalid enum " + txt + " for " + klass).printStackTrace();
				}
				
			}
			else {
				throw new IllegalStateException("not handled: " + klass);
			}
			return v;
		} catch (NumberFormatException e2) {
			return null;
		}
	}

	public static Object getArrayValue(IProperty property, int index) {
		if (!(property.getType().isArray())) {
			throw new IllegalArgumentException();
		}
		Object array = property.getValue();
		if (array == null)
			return null;
		try {
			return Array.get(array, index);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void setArrayValue(IProperty property, int index, Object value) {
		if (!(property.getType().isArray())) {
			throw new IllegalArgumentException();
		}
		try {
			Array.set(property.getValue(), index, value);
			property.firePropertyChange();
		} catch (IllegalArgumentException e) {
			// likely setting a primitive; ignore
			if (value != null)
				e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param field
	 * @param index
	 * @param obj
	 * @param txt
	 */
	public static void setArrayValueFromString(IProperty property, int index,
			String txt) {
		if (!(property.getType().isArray())) {
			throw new IllegalArgumentException();
		}
		Class<?> arrayType = property.getType();
		Object value = convertStringToValue(txt, arrayType.getComponentType());
		setArrayValue(property, index, value);
	}
	
}
