/**
 * 
 */
package v9t9.gui.properties;

import ejs.base.properties.FieldProperty;
import ejs.base.properties.IProperty;


/**
 * @author ejs
 *
 */
public class FieldPropertyEditorProvider implements IPropertyEditorProvider {

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IPropertyEditorProvider#createEditor()
	 */
	public IPropertyEditor createEditor(IProperty property) {
		return new FieldPropertyEditor((FieldProperty) property);
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IPropertyEditorProvider#getLabel()
	 */
	public String getLabel(IProperty property) {
		return property.getLabel();
	}

}
