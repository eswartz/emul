/**
 * 
 */
package org.ejs.gui.properties;

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

import ejs.base.properties.FieldProperty;
import ejs.base.properties.FieldUtils;


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
	public IPropertyEditorControl createEditor(Composite parent) {
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
			final ModifyListener modifyListener = new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					try {
						setValueFromString(text.getText());
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
				
			};
			text.addModifyListener(modifyListener);
			return new IPropertyEditorControl() {

				@Override
				public Control getControl() {
					return text;
				}

				@Override
				public void reset() {
					text.removeModifyListener(modifyListener);
					
					text.setText(property.getString());

					text.addModifyListener(modifyListener);
				}
				
			};
		} else if (Boolean.class.isInstance(value)) {
			final Button check = new Button(parent, SWT.CHECK);
			check.setSelection(Boolean.TRUE.equals(value));
			final SelectionAdapter selectionListener = new SelectionAdapter() {
				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						setValue(check.getSelection());
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			};
			check.addSelectionListener(selectionListener);

			return new IPropertyEditorControl() {

				@Override
				public Control getControl() {
					return check;
				}

				@Override
				public void reset() {
					check.removeSelectionListener(selectionListener);
					
					check.setSelection(property.getBoolean());

					check.addSelectionListener(selectionListener);
				}
				
			};
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
				final SelectionAdapter selectionListener = new SelectionAdapter() {
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
				};
				combo.addSelectionListener(selectionListener);

				return new IPropertyEditorControl() {

					@Override
					public Control getControl() {
						return combo;
					}

					@Override
					public void reset() {
						combo.removeSelectionListener(selectionListener);
						
						for (int i = 0; i < values.length; i++) {
							Enum<?> ef = enumFields[i];
							if (property.getValue() == ef) {
								combo.select(i);
								break;
							}
						}

						combo.addSelectionListener(selectionListener);
					}
					
				};
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
