/**
 * 
 */
package v9t9.gui.properties;

import v9t9.base.properties.FieldProperty;
import v9t9.base.properties.FieldUtils;


/**
 * @author ejs
 *
 */
public class ArrayFieldPropertyEditor extends FieldPropertyEditor {

	public ArrayFieldPropertyEditor(FieldProperty property) {
		super(property);
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.FieldPropertyEditor#setValueFromString(java.lang.String)
	 */
	@Override
	protected void setValueFromString(String txt) {
		Class<?> arrayType = property.getField().getType();
		Object value = FieldUtils.convertStringToValue(txt,
				arrayType.getComponentType());
		setValue(value);
	}

}
