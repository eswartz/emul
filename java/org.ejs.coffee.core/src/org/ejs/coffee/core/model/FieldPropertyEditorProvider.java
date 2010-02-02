/**
 * 
 */
package org.ejs.coffee.core.model;

import org.ejs.coffee.core.jface.FieldPropertyEditor;
import org.ejs.coffee.core.jface.IPropertyEditor;


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
		return property.getName();
	}

}
