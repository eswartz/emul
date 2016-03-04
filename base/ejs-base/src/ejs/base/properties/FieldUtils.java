/*
  FieldUtils.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * @author ejs
 *
 */
public class FieldUtils {

	public static Object getArrayValue(Field field, int index, Object obj) {
		if (!(field.getType().isArray())) {
			throw new IllegalArgumentException();
		}
		Object array;
		try {
			array = field.get(obj);
		} catch (Exception e1) {
			return null;
		}
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
			Object array;
			try {
				array = field.get(obj);
			} catch (Exception e1) {
				return;
			}

			Array.set(array, index, value);
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
		Object value = PropertyUtils.convertStringToValue(txt, arrayType.getComponentType());
		setArrayValue(field, index, obj, value);
	}

	/**
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static Field fetchField(Object obj, String fieldName) throws IllegalArgumentException {
		Class<?> klass = obj.getClass();
		while (klass != null) {
			try {
				Field field = klass.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException e) {
				klass = klass.getSuperclass();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		throw new IllegalArgumentException(fieldName);
	}
	
}
