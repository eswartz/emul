/*
  ListPropertyEditor.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.properties;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.ejs.gui.common.FontUtils;

import ejs.base.properties.IClassPropertyFactory;
import ejs.base.properties.ListFieldProperty;


/**
 * @author ejs
 *
 */
public class ListPropertyEditor implements IPropertyEditor {

	private final ListFieldProperty property;
	private ListViewer viewer;
	protected Object currentElement;
	private Composite editorHolder;

	/**
	 * @param listFieldProperty
	 */
	public ListPropertyEditor(ListFieldProperty property) {
		this.property = property;
	}

	/* (non-Javadoc)
	 * 
	 */
	public IPropertyEditorControl createEditor(Composite parent) {
		final Composite composite = new Composite(parent, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		
		// first column: the items
		createList(composite);
		
		// second column: the editor for the selected item
		editorHolder = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(editorHolder);
		
		parent.getShell().addShellListener(new ShellAdapter() {
			@Override
			public void shellActivated(ShellEvent e) {
				updateEditor();
				((Shell)e.widget).removeShellListener(this);
			}
		});
		
		return new IPropertyEditorControl() {

			@Override
			public Control getControl() {
				return composite;
			}

			@Override
			public void reset() {
				System.err.println(getClass() + " : TODO");
			}
			
		};
	}

	/**
	 * @param composite
	 */
	private void createList(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER_DOT);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		//GridDataFactory.fillDefaults().grab(true, true).minSize(100,-1).applyTo(composite);
		GridDataFactory.fillDefaults().grab(true, true).minSize(50,-1).applyTo(composite);
		
		viewer = new ListViewer(composite);
		GridDataFactory.fillDefaults().grab(true,true).span(1, 4).applyTo(viewer.getControl());
		//GridDataFactory.swtDefaults().span(1, 4).applyTo(viewer.getControl());
		
		if (property.getElementClassFactory() != null) {
			FontDescriptor desc = FontUtils.getFontDescriptor(JFaceResources.getTextFont());
			desc = desc.setHeight(8);
			final Font smallFont = desc.createFont(parent.getShell().getDisplay());
			
			parent.addDisposeListener(new DisposeListener() {
				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
				 */
				public void widgetDisposed(DisposeEvent e) {
					smallFont.dispose();
				}
			});
			
			final Button addButton = new Button(composite, SWT.PUSH);
			addButton.setFont(smallFont);
			GridDataFactory.swtDefaults().minSize(50, -1).applyTo(addButton);
			addButton.setText("+");
			addButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					addNewElement(addButton, property.getElementClassFactory());
				}
			});
			
			final Button removeButton = new Button(composite, SWT.PUSH);
			removeButton.setFont(smallFont);
			GridDataFactory.swtDefaults().minSize(50, -1).applyTo(removeButton);
			removeButton.setText("-");
			removeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (currentElement != null) {
						property.getList().remove(currentElement);
						viewer.remove(currentElement);
						updateList();
					}
				}
			});
			
			final Button upButton = new Button(composite, SWT.PUSH);
			upButton.setFont(smallFont);
			GridDataFactory.swtDefaults().minSize(50, -1).applyTo(upButton);
			upButton.setText("↑");
			upButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (currentElement != null) {
						List<Object> list = property.getList();
						int index = list.indexOf(currentElement);
						if (index > 0) {
							Object el = list.remove(index - 1);
							list.add(index, el);
						}
						viewer.refresh();
						updateList();
					}
				}
			});
			
			final Button downButton = new Button(composite, SWT.PUSH);
			downButton.setFont(smallFont);
			GridDataFactory.swtDefaults().minSize(50, -1).applyTo(downButton);
			downButton.setText("↓");
			downButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (currentElement != null) {
						List<Object> list = property.getList();
						int index = list.indexOf(currentElement);
						if (index < list.size()) {
							Object el = list.remove(index + 1);
							list.add(index, el);
						}
						viewer.refresh();
						updateList();
					}
				}
			});
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {

				public void selectionChanged(SelectionChangedEvent event) {
					Object el = ((IStructuredSelection) event.getSelection()).getFirstElement();
					removeButton.setEnabled(el != null);
				}
				
			});
		}
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				Object el = ((IStructuredSelection) event.getSelection()).getFirstElement();
				currentElement = el;
				updateEditor();
			}
			
		});
		
		viewer.setLabelProvider(new LabelProvider() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			@Override
			public String getText(Object element) {
				if (element instanceof IPropertyEditorProvider)
					return ((IPropertyEditorProvider)element).getLabel(property);
				return super.getText(element);
			}
		});
		viewer.setContentProvider(new ArrayContentProvider());
		List<Object> list = property.getList();
		viewer.setInput(list);
		if (list != null && list.size() > 0) {
			Object el = list.get(0);
			viewer.setSelection(new StructuredSelection(el));
		}
			
	}

	/**
	 * 
	 */
	protected void updateList() {
		//if (property instanceof IPropertyProvider)
		//	((IPropertyProvider) property).updateFromPropertyChange();
		property.firePropertyChange();
	}

	/**
	 * 
	 */
	protected void updateEditor() {
		if (editorHolder != null) {
			if (editorHolder.isDisposed())
				return;
			
			for (Control kid : editorHolder.getChildren())
				kid.dispose();

			if (currentElement != null) {
				if (currentElement instanceof IPropertyEditorProvider) {
					IPropertyEditorProvider pep = (IPropertyEditorProvider) currentElement;
					IPropertyEditor editor = pep.createEditor(property);
					if (editor != null) {
						IPropertyEditorControl control = editor.createEditor(editorHolder);
						GridDataFactory.fillDefaults().grab(true, true).applyTo(control.getControl());
						// TODO: save editor control somewhere
						editorHolder.getShell().layout(true, true);
						//editorHolder.getShell().pack();
					}
				}
			}
		}
	}

	/**
	 * @param factory
	 */
	protected void addNewElement(final Control parent, final IClassPropertyFactory factory) {
		String[] ids = factory.getIds();
		Menu menu = new Menu(parent);
		
		for (String id : ids) {
			final String theId = id;
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(id);
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Object value = factory.create(theId);
					property.getList().add(value);
					viewer.add(value);
					viewer.setSelection(new StructuredSelection(value));
					updateList();
				}
			});
		}		
		
		menu.setVisible(true);
	}

}
