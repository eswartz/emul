/**
 * 
 */
package org.ejs.coffee.core.jface;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.ejs.coffee.core.properties.FieldProperty;
import org.ejs.coffee.core.properties.FieldUtils;
import org.ejs.coffee.core.properties.IPropertyEditor;
import org.ejs.coffee.core.properties.IPropertyEditorProvider;

/**
 * @author ejs
 *
 */
public class FieldPropertyEditor implements
		IPropertyEditor {

	protected final FieldProperty property;

	public FieldPropertyEditor(FieldProperty property) {
		this.property = property;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.generator.IPropertySource#createEditor(org.eclipse.swt.widgets.Composite, java.lang.String)
	 */
	public Control createEditor(Composite parent) {
		if (property == null)
			return null;
		
		Object value = getValue();
		if (value == null)
			return null;
		
		if (value instanceof IPropertyEditorProvider) {
			IPropertyEditor editor = ((IPropertyEditorProvider) value).createEditor(property);
			if (editor != null)
				return editor.createEditor(parent);
		}
		
		final Class<?> klass = property.getField().getType();
		if (Number.class.isInstance(value)) {
			final Text text = new Text(parent, SWT.BORDER);
			text.setText("" + value);
			text.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					setValueFromString(text.getText());
				}
				
			});
			return text;
		} else if (Boolean.class.isInstance(value)) {
			final Button check = new Button(parent, SWT.CHECK);
			check.setSelection(Boolean.TRUE.equals(value));
			check.addSelectionListener(new SelectionAdapter() {
				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				@Override
				public void widgetSelected(SelectionEvent e) {
					setValue(check.getSelection());
				}
			});
			return check;
		} else if (Enum.class.isAssignableFrom(klass)) {
			final Combo combo = new Combo(parent, SWT.READ_ONLY);
			final Enum<?>[] enumFields= (Enum<?>[]) klass.getEnumConstants();
			if (enumFields != null) {
				final String[] values = new String[enumFields.length] ;
				for (int i = 0; i < values.length; i++) {
					Enum<?> ef = enumFields[i];
					values[i] = ef.toString();
				}
				combo.setItems(values);
				combo.setText(value.toString());
				combo.addSelectionListener(new SelectionAdapter() {
					/* (non-Javadoc)
					 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
					 */
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							setValue(enumFields[combo.getSelectionIndex()]);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				});
				return combo;
			}
		}
			
		return null;
	}

	/**
	 * @param selection
	 */
	protected void setValue(Object value) {
		property.setValue(value);
	}

	protected Object getValue() {
		return property.getValue();
	}

	protected void setValueFromString(final String txt) {
		
		FieldUtils.setValueFromString(property.getField(), property.getObject(), txt);
	}


}
