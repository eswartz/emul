/**
 * 
 */
package org.ejs.coffee.core.properties;

import org.ejs.coffee.core.jface.FieldPropertyEditor;


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
