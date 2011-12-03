/**
 * 
 */
package v9t9.gui.properties;

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

import v9t9.base.properties.IProperty;
import v9t9.base.properties.IPropertyProvider;

/**
 * @author ejs
 *
 */
public class PropertyEditorHolder  {

	private final IProperty property;
	private IPropertyEditorControl theFieldEditorControl;
	private final IPropertyEditorProvider propertyEditorProvider;

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IPropertyEditor#createEditor(org.eclipse.swt.widgets.Composite)
	 */
	/**
	 * @param propertyEditorProvider 
	 * @param propertySource
	 * @param label 
	 */
	public PropertyEditorHolder(IProperty property, IPropertyEditorProvider propertyEditorProvider) {
		this.property = property;
		this.propertyEditorProvider = propertyEditorProvider;
	}

	public IPropertyEditorControl createEditor(EditGroup group) {
		if (!(property.getValue() instanceof IPropertyProvider)) {
			return createFormattedEditor(group, group.getContainer());
		}
		else {
			return createFormattedEditor(group, group.getSubcontainer());
		}
	}
	
	protected IPropertyEditorControl createFormattedEditor(final EditGroup group, Composite composite) {
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
					createNewProperty(holder, link);
				}
			});
		} else {
			link.setText(property.getLabel());
		}
		
		
		IPropertyEditorControl editor = setupEditor(holder);
		return editor;
	}

	/**
	 * @param parent
	 */
	private IPropertyEditorControl setupEditor(Composite parent) {
		if(theFieldEditorControl != null) {
			theFieldEditorControl.getControl().dispose();
			theFieldEditorControl = null;
		}
		//IPropertyEditor editor = property.createEditor();
		IPropertyEditor editor = propertyEditorProvider.createEditor(property);
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
	 */
	protected void createNewProperty(final Composite parent, final Control label) {
		String[] ids = property.getClassFactory().getIds();
		Menu menu = new Menu(label);
		
		for (String id : ids) {
			final String theId = id;
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(id);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Object value = property.getClassFactory().create(theId);
					property.setValue(value);
					setupEditor(parent);
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
