/**
 * 
 */
package v9t9.base.properties;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * @author ejs
 *
 */
public class FieldUtils {
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
				new Exception("Setting " + field.getName() + " in " + obj + " with " + value,
						e).printStackTrace();
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

	public static void setValueFromString(Field field, Object obj, String txt) {
		Class<?> klass = field.getType();
		Object v = PropertyUtils.convertStringToValue(txt, klass);
		setValue(field, obj, v);
		
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
		Object value = PropertyUtils.convertStringToValue(txt, arrayType.getComponentType());
		setArrayValue(field, index, obj, value);
	}
	
}
