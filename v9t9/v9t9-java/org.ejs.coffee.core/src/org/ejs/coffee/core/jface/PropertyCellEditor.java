/**
 * 
 */
package org.ejs.coffee.core.jface;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyEditor;

/**
 * @author ejs
 *
 */
public class PropertyCellEditor extends CellEditor {

	private Composite holder;
	private IProperty property;
	private IPropertyEditor editor;
	private Control editorControl;

	public PropertyCellEditor(Composite parent) {
		super(parent, SWT.NONE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.CellEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createControl(Composite parent) {
		holder = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(holder);
	
		holder.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
            	PropertyCellEditor.this.focusLost();
            }
        });
		
		return holder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.CellEditor#doGetValue()
	 */
	@Override
	protected Object doGetValue() {
		return property;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.CellEditor#doSetFocus()
	 */
	@Override
	protected void doSetFocus() {
		if (editorControl != null) {
			editorControl.setFocus();
			if (editorControl instanceof Text)
				((Text)editorControl).selectAll();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.CellEditor#doSetValue(java.lang.Object)
	 */
	@Override
	protected void doSetValue(Object value) {
		property = (IProperty) value;
		if (editor == null) {
			editor = property.createEditor();
			if (editor == null)
				return;
			editorControl = editor.createEditor(holder).getControl();
		}
	}

}
