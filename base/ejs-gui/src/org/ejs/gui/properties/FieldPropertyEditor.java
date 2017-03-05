/*
  FieldPropertyEditor.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.ejs.gui.common.FontUtils;

import ejs.base.properties.FieldProperty;
import ejs.base.properties.PropertyUtils;
import ejs.base.properties.Range;


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
	 * 
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
			Range range = property.getField().getAnnotation(Range.class);
			if (range == null) {
				final Text text = new Text(parent, SWT.BORDER);
				text.setText(String.valueOf(value));
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
			} else {
				final Composite comp = new Composite(parent, SWT.NONE);
				GridLayoutFactory.fillDefaults().numColumns(2).applyTo(comp);
				
				final Scale scale = new Scale(comp, SWT.HORIZONTAL);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(scale);
				
				final Label label = new Label(comp, SWT.NONE);
				label.setText(String.valueOf(value));
				GridDataFactory.fillDefaults().grab(false, false).
					minSize(FontUtils.measureText(comp.getDisplay(), comp.getFont(), "-100.0!")).
					applyTo(label);
				
				final float add = range.minimum();
				final float mul = (range.maximum() - range.minimum());

				scale.setMinimum(0);
				scale.setMaximum(1000);
				scale.setSelection((int) ((((Number) value).doubleValue() - add) * 1000 / mul));
				
				final SelectionListener selectListener = new SelectionAdapter() {
					/* (non-Javadoc)
					 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
					 */
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							double value = (scale.getSelection() * mul / 1000.) + add; 
							property.setValue(PropertyUtils.convertStringToValue(
									String.valueOf(value), 
									property.getType()));
							label.setText(String.valueOf(value));
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
					
				};
				scale.addSelectionListener(selectListener);
				return new IPropertyEditorControl() {
	
					@Override
					public Control getControl() {
						return comp;
					}
	
					@Override
					public void reset() {
						scale.removeSelectionListener(selectListener);
						
						double value = property.getDouble();
						scale.setSelection((int) ((value - add) * 1000 / mul));
						label.setText(String.valueOf(value));
						
						scale.addSelectionListener(selectListener);
					}
					
				};
				
			}
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
		Object value = PropertyUtils.convertStringToValue(txt, property.getType());
		property.setValue(value);
		//FieldUtils.setValueFromString(property.getField(), property.getObject(), txt);
	}


}
