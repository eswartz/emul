/**
 * 
 */
package org.ejs.coffee.core.jface;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyEditor;
import org.ejs.coffee.core.properties.IPropertyEditorControl;
import org.ejs.coffee.core.properties.IPropertySource;

/**
 * @author ejs
 *
 */
public class PropertySourceEditor implements IPropertyEditor {

	private final String labelTxt;
	private final IPropertySource propertySource;

	public PropertySourceEditor(IPropertySource propertySource, String label) {
		this.propertySource = propertySource;
		this.labelTxt = label;
	}

	public EditGroup createEditor(Composite parent) {
		EditGroup group = new EditGroup(parent, SWT.NONE, labelTxt);
		//GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
		GridDataFactory.swtDefaults(). applyTo(group);
		for (IProperty property : propertySource.getProperties()) {
			if (property.isHidden())
				continue;
			PropertyEditor editor = new PropertyEditor(property);
			IPropertyEditorControl editorControl = editor.createEditor(group);
			group.registerControl(property, editorControl);
		}
		
		return group;
	}
}
