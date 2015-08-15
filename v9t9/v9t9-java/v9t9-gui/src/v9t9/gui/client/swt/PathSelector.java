/*
  PathSelector.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

import java.net.URISyntaxException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.ejs.gui.common.SwtUtils;

import v9t9.common.files.IPathFileLocator;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * This composite allows editing of paths by adding,
 * removing, reordering.
 * @author ejs
 *
 */
public class PathSelector extends Composite {

	private final IProperty property;
	private final SwtWindow window;
	private final String pathLabel;
	private TableViewer viewer;
	private Composite buttons;
	private Button removeButton;
	private IPropertyListener propertyListener;
	private final IPathFileLocator locator;

	public PathSelector(Composite parent, IPathFileLocator locator, SwtWindow window, String pathLabel, IProperty property) {
		super(parent, SWT.BORDER);
		this.locator = locator;
		this.window = window;
		this.pathLabel = pathLabel;
		this.property = property;
		
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(this);
		
		// left side: the list
		createListTable();
		
		// right side: buttons
		createButtons();
		
		propertyListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				if (!isDisposed()) {
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							viewer.refresh();
						}
					});
				}
			}
		};
		
		property.addListener(propertyListener);
	}

	class PathLabelProvider extends LabelProvider implements ITableFontProvider, ITableColorProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) {
			return super.getText(element) + (!pathExists(element) ? " (not found)" : "");
		}
		protected boolean pathExists(Object element) {
			boolean found = true;
			if (element instanceof String) {
				try {
					found = locator.exists(locator.createURI(element.toString()));
				} catch (URISyntaxException e) {
					found = false;
				}
			}
			return found;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
		 */
		@Override
		public Font getFont(Object element, int columnIndex) {
			boolean found = pathExists(element);
			return found ? null : JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
		 */
		@Override
		public Color getBackground(Object element, int columnIndex) {
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
		 */
		@Override
		public Color getForeground(Object element, int columnIndex) {
			boolean found = pathExists(element);
			return found ? null : getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
		}
	}
	/**
	 * 
	 */
	private void createListTable() {
		viewer = new TableViewer(this, SWT.FULL_SELECTION + SWT.MULTI);
		
		final Table table = viewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		final TableColumn column = new TableColumn(table, SWT.LEFT);
		
		column.setText("Search Locations");


		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				column.pack();
			}
		});
		
		table.addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent e) {
				final Item item = table.getItem(
						viewer.getControl().toControl(new Point(e.x, e.y))
						);
				if (item == null)
					return;
				
				Menu menu = new Menu(viewer.getControl());

				if (item.getData() instanceof String) {
					
					final MenuItem copyItem;
					copyItem = new MenuItem(menu, SWT.NONE);
					copyItem.setText("Copy");
					
					copyItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							Clipboard c = new Clipboard(getDisplay());
							c.setContents(new Object[] { item.getData() }, new Transfer[] { TextTransfer.getInstance() });
						}
						
					});
				}

				SwtUtils.runMenu(null, e.x, e.y, menu);

			}
		});

		
		viewer.addOpenListener(new IOpenListener() {
			
			@Override
			public void open(OpenEvent event) {
				Object oldDir = ((IStructuredSelection) event.getSelection()).getFirstElement();
				
				String dir = window.openDirectorySelectionDialog("Modify " + pathLabel, oldDir.toString());
				if (dir == null || dir.equals(oldDir))
					return;
				
				int index = property.getList().indexOf(oldDir);
				if (index >= 0)
					property.getList().set(index, dir);
				else {
					property.getList().remove(oldDir);
					property.getList().add(dir);
				}
				viewer.refresh();
				
				firePropertyChangeForOthers();
			}
		});
		
		viewer.setLabelProvider(new PathLabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
		
		viewer.setInput(property.getList());
	}

	/**
	 * 
	 */
	private void createButtons() {
		buttons = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(3, 3).applyTo(buttons);
		GridDataFactory.fillDefaults().grab(false, true).minSize(-1, 64).applyTo(buttons);
		
		final Button add = new Button(buttons, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(add);
		add.setText("Add...");
		
		add.addSelectionListener(new SelectionAdapter() {
			String lastDirectory;
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				Object obj = sel.getFirstElement();
				if (obj instanceof String) {
					lastDirectory = (String) obj;
				} else if (lastDirectory == null) {
					if (!property.getList().isEmpty()) {
						lastDirectory = (String) property.getList().get(0);
					} else {
						lastDirectory = ".";
					}
				}
				String dir = window.openDirectorySelectionDialog("Add " + pathLabel, lastDirectory);
				if (dir == null)
					return;
				property.getList().add(dir);
				viewer.add(dir);
				lastDirectory = dir;
				
				firePropertyChangeForOthers();
			}
		});

		removeButton = new Button(buttons, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(removeButton);
		removeButton.setText("Remove");
		removeButton.setEnabled(false);
		
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					for (Object obj : ((IStructuredSelection) viewer.getSelection()).toArray()) {
						property.getList().remove(obj);
						viewer.remove(obj);
					}
					firePropertyChangeForOthers();
				}
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				removeButton.setEnabled(!event.getSelection().isEmpty());
			}
		});

		final Button refresh = new Button(buttons, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(refresh);
		refresh.setText("Rescan");
		
		refresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				property.firePropertyChange();
			}
		});

	}

	/**
	 * 
	 */
	protected void firePropertyChangeForOthers() {
		property.removeListener(propertyListener);
		property.firePropertyChange();
		property.addListener(propertyListener);
	}

}
