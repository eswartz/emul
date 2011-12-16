/**
 * 
 */
package v9t9.gui.properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertySource;


/**
 * @author ejs
 *
 */
public class PropertySourceEditor implements IPropertyEditor {

	private final String labelTxt;
	private final IPropertySource propertySource;
	private final IPropertyEditorProvider propertyEditorProvider;

	public PropertySourceEditor(IPropertyEditorProvider propertyEditorProvider,
			IPropertySource propertySource, String label) {
		this.propertyEditorProvider = propertyEditorProvider;
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
			PropertyEditorHolder editor = new PropertyEditorHolder(property,
					propertyEditorProvider);
			IPropertyEditorControl editorControl = editor.createEditor(group);
			group.registerControl(property, editorControl);
		}
		
		return group;
	}
}
