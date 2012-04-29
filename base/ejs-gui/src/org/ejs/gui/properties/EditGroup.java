/**
 * 
 */
package org.ejs.gui.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import ejs.base.properties.IProperty;


/**
 * @author ejs
 *
 */
public class EditGroup extends Composite implements IPropertyEditorControl {

	private Group group;
	private Composite subgroup;
	private HashMap<IProperty, IPropertyEditorControl> propertyMap;

	/**
	 * @param parent
	 * @param style
	 * @param label 
	 */
	public EditGroup(Composite parent, int style, String labelTxt) {
		super(parent, style);
		
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);
		
		group = new Group(this, SWT.NONE);
		group.setText(labelTxt);
		
		//GridDataFactory.fillDefaults().grab(true, true).minSize(100,100).applyTo(group);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(6, 6).applyTo(group);

		propertyMap = new HashMap<IProperty, IPropertyEditorControl>();
	}

	@Override
	public Control getControl() {
		return group;
	}
	
	/**
	 * @return
	 */
	public Composite getContainer() {
		return group;
	}

	public Composite getSubcontainer() {
		if (subgroup == null) {

			subgroup = new Composite(this, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(subgroup);
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(subgroup);
		}
		return subgroup;
	}

	public void registerControl(IProperty property, IPropertyEditorControl control) {
		propertyMap.put(property, control);
	}
	/**
	 * 
	 */
	public void clear() {
		for (Control kid : group.getChildren()) {
			kid.dispose();
		}
		subgroup = null;
	}

	@Override
	public void reset() {
		for (Map.Entry<IProperty, IPropertyEditorControl> entry : propertyMap.entrySet()) {
			IPropertyEditorControl ctrl = entry.getValue();
			if (ctrl != null) 
				ctrl.reset();
		}
	}
}
