/**
 * 
 */
package org.ejs.coffee.core.properties;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * @author ejs
 *
 */
public class FieldUtils {
	/**
	 * @param field2
	 * @param text
	 */
	public static void setValueFromString(Field field, Object obj, String txt) {
		Class<?> klass = field.getType();
		Object v = convertStringToValue(txt, klass);
		setValue(field, obj, v);
		
	}

	public static Object convertStringToValue(String txt, Class<?> klass) {
		Object v;
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
			else {
				throw new IllegalStateException("not handled: " + klass);
			}
			return v;
		} catch (NumberFormatException e2) {
			return null;
		}
	}
	
	/**
	 * @param text
	 */
	public static void setValue(Field field, Object obj, Object value) {
		try {
			//field.setAccessible(true);
			field.set(obj, value);
			//if (obj instanceof IPropertyProvider)
			//	((IPropertyProvider) obj).updateFromPropertyChange();
			
		} catch (Exception e) {
			if  (value != null)
				new Exception("Setting " + field.getName() + " in " + obj + " with " + value).printStackTrace();
		}
	}

	public static Object getValue(Field field, Object obj) {
		try {
			//field.setAccessible(true);
			return field.get(obj);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Object getArrayValue(Field field, int index, Object obj) {
		if (!(field.getType().isArray())) {
			throw new IllegalArgumentException();
		}
		Object array = getValue(field, obj);
		if (array == null)
			return null;
		try {
			return Array.get(array, index);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void setArrayValue(Field field, int index, Object obj, Object value) {
		if (!(field.getType().isArray())) {
			throw new IllegalArgumentException();
		}
		try {
			Array.set(getValue(field, obj), index, value);
			//if (obj instanceof IPropertyProvider)
			//	((IPropertyProvider) obj).updateFromPropertyChange();
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
	public static void setArrayValueFromString(Field field, int index,
			Object obj, String txt) {
		if (!(field.getType().isArray())) {
			throw new IllegalArgumentException();
		}
		Class<?> arrayType = field.getType();
		Object value = convertStringToValue(txt, arrayType.getComponentType());
		setArrayValue(field, index, obj, value);
	}
	
}
