/**
 * 
 */
package org.ejs.coffee.core.jface;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyEditor;
import org.ejs.coffee.core.properties.PropertySource;

/**
 * @author ejs
 *
 */
public class PropertySourceEditor implements IPropertyEditor {

	private final String labelTxt;
	private final PropertySource propertySource;

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IPropertyEditor#createEditor(org.eclipse.swt.widgets.Composite)
	 */
	/**
	 * @param propertySource
	 * @param label 
	 */
	public PropertySourceEditor(PropertySource propertySource, String label) {
		this.propertySource = propertySource;
		this.labelTxt = label;
		// TODO Auto-generated constructor stub
	}

	public Control createEditor(Composite parent) {
		EditGroup group = new EditGroup(parent, SWT.NONE, labelTxt);
		//GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
		GridDataFactory.swtDefaults().applyTo(group);
		for (IProperty property : propertySource.getProperties()) {
			PropertyEditor editor = new PropertyEditor(property);
			editor.createEditor(group);
		}
		
		return group;
	}
}
