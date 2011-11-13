/**
 * 
 */
package org.ejs.coffee.core.jface;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.ejs.coffee.core.properties.IClassPropertyFactory;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyEditor;
import org.ejs.coffee.core.properties.IPropertyEditorControl;
import org.ejs.coffee.core.properties.IPropertyProvider;

/**
 * @author ejs
 *
 */
public class PropertyEditor  {

	private final IProperty property;
	private IPropertyEditorControl theFieldEditorControl;

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IPropertyEditor#createEditor(org.eclipse.swt.widgets.Composite)
	 */
	/**
	 * @param propertySource
	 * @param label 
	 */
	public PropertyEditor(IProperty property) {
		this.property = property;
		// TODO Auto-generated constructor stub
	}

	public IPropertyEditorControl createEditor(EditGroup group) {
		if (!(property.getValue() instanceof IPropertyProvider)) {
			return createFormattedEditor(group, group.getContainer(), property);
		}
		else {
			return createFormattedEditor(group, group.getSubcontainer(), property);
		}
	}
	
	protected IPropertyEditorControl createFormattedEditor(final EditGroup group, Composite composite, final IProperty property) {
		final Composite holder = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(holder);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(holder);
		
		final Link link;
		link = new Link(holder, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(link);

		if (property.getClassFactory() != null) {
			link.setText("<a href=\"new\">" + property.getLabel() + "</a>");
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewProperty(holder, link, property, property.getClassFactory());
				}
			});
		} else {
			link.setText(property.getLabel());
		}
		
		
		IPropertyEditorControl editor = setupEditor(holder, property);
		return editor;
	}

	/**
	 * @param parent
	 * @param property
	 */
	private IPropertyEditorControl setupEditor(Composite parent, IProperty property) {
		if(theFieldEditorControl != null) {
			theFieldEditorControl.getControl().dispose();
			theFieldEditorControl = null;
		}
		IPropertyEditor editor = property.createEditor();
		if (editor == null) {
			return null;
		}
		IPropertyEditorControl control = editor.createEditor(parent);
		if (control == null) {
			return null;
		}
		GridDataFactory.fillDefaults().grab(true, false).minSize(50, -1).applyTo(control.getControl());
		
		theFieldEditorControl = control;
		
		return control;		
	}

	/**
	 * @param parent 
	 * @param property
	 */
	protected void createNewProperty(final Composite parent, final Control label, final IProperty property, final IClassPropertyFactory factory) {
		String[] ids = factory.getIds();
		Menu menu = new Menu(label);
		
		for (String id : ids) {
			final String theId = id;
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(id);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Object value = factory.create(theId);
					property.setValue(value);
					setupEditor(parent, property);
					parent.getShell().pack();
				}
			});
		}
		
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText("Delete");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				property.setValue(null);
				if (theFieldEditorControl != null) {
					theFieldEditorControl.getControl().dispose();
					theFieldEditorControl = null;
				}
				parent.getShell().pack();
			}
		});
		
		menu.setVisible(true);
	}

}
