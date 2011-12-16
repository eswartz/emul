/**
 * 
 */
package v9t9.gui.properties;

import ejs.base.properties.FieldProperty;
import ejs.base.properties.PropertyUtils;


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
		Object value = PropertyUtils.convertStringToValue(txt,
				arrayType.getComponentType());
		setValue(value);
	}

}
