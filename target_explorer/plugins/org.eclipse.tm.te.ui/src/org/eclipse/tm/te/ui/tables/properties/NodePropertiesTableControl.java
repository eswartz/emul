/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.tables.properties;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.tm.te.ui.WorkbenchPartControl;
import org.eclipse.tm.te.ui.forms.CustomFormToolkit;
import org.eclipse.tm.te.ui.interfaces.IUIConstants;
import org.eclipse.tm.te.ui.nls.Messages;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.Section;


/**
 * Target Explorer: Abstract node properties table control implementation.
 */
public abstract class NodePropertiesTableControl extends WorkbenchPartControl {
	// Reference to the table viewer
	private TableViewer viewer;
	// Reference to the selection changed listener
	private ISelectionChangedListener editorSelectionChangedListener;

	// We remember the sorting order (ascending vs. descending) for each
	// column separately. That way we can come up with the sort order switching
	// correctly if the user changes from one column to the next. If set
	// to Boolean.FALSE, the sort order for the column is descending (default)
	private final Map<TableColumn, Boolean> columnSortOrder = new LinkedHashMap<TableColumn, Boolean>();

	/**
	 * Default node properties table control selection changed listener implementation.
	 * The selection changed listener is registered to the editor tree control.
	 */
	protected class NodePropertiesTableControlSelectionChangedListener implements ISelectionChangedListener {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (getViewer() != null) {
				getViewer().setInput(event.getSelection());
			}
		}
	}

	/**
	 * Constructor.
	 *
	 * @param parentPart The parent workbench part this control is embedded in or <code>null</code>.
	 */
	public NodePropertiesTableControl(IWorkbenchPart parentPart) {
		super(parentPart);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.WorkbenchPartControl#dispose()
	 */
	@Override
	public void dispose() {
		// Dispose the editor tree control selection changed listener
		if (editorSelectionChangedListener != null) {
			ISelectionProvider selectionProvider = (ISelectionProvider)getParentPart().getAdapter(ISelectionProvider.class);
			if (selectionProvider != null) {
				selectionProvider.removeSelectionChangedListener(editorSelectionChangedListener);
				editorSelectionChangedListener = null;
			}
		}

		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.WorkbenchPartControl#setupFormPanel(org.eclipse.swt.widgets.Composite, org.eclipse.tm.te.ui.forms.CustomFormToolkit)
	 */
	@Override
	public void setupFormPanel(Composite parent, CustomFormToolkit toolkit) {
		super.setupFormPanel(parent, toolkit);

		// Create the table viewer
		viewer = doCreateTableViewer(parent);
		// Configure the table viewer
		configureTableViewer(viewer);
		// Configure the table
		configureTable(viewer.getTable(), viewer.getComparator() != null);

		// Register the control as selection listener to the editor control
		ISelectionProvider selectionProvider = getParentPart() != null ? (ISelectionProvider)getParentPart().getAdapter(ISelectionProvider.class) : null;
		if (selectionProvider != null) {
			// Create the selection changed listener instance
			editorSelectionChangedListener = doCreateEditorSelectionChangedListener();
			selectionProvider.addSelectionChangedListener(editorSelectionChangedListener);
		}

		// Prepare popup menu and toolbar
		createContributionItems(viewer);

		// Set the current selection as input
		viewer.setInput(selectionProvider != null ? selectionProvider.getSelection() : null);
	}

	/**
	 * Creates a new editor tree control selection changed listener instance.
	 *
	 * @return The editor tree control selection changed listener instance.
	 */
	protected ISelectionChangedListener doCreateEditorSelectionChangedListener() {
		return new NodePropertiesTableControlSelectionChangedListener();
	}

	/**
	 * Creates the table viewer instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @return The table viewer.
	 */
	protected TableViewer doCreateTableViewer(Composite parent) {
		Assert.isNotNull(parent);

		TableViewer tableViewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);

		return tableViewer;
	}

	/**
	 * Configure the table Viewer.
	 *
	 * @param tableViewer The table viewer. Must not be <code>null</code>.
	 */
	protected void configureTableViewer(TableViewer tableViewer) {
		Assert.isNotNull(tableViewer);

		tableViewer.setLabelProvider(doCreateTableViewerLabelProvider(tableViewer));
		tableViewer.setContentProvider(doCreateTableViewerContentProvider(tableViewer));
		tableViewer.setComparator(doCreateTableViewerComparator(tableViewer));
	}

	/**
	 * Creates the table viewer label provider instance.
	 *
	 * @param viewer The table viewer. Must not be <code>null</code>.
	 * @return The table viewer label provider instance.
	 */
	protected abstract ITableLabelProvider doCreateTableViewerLabelProvider(TableViewer viewer);

	/**
	 * Creates the table viewer content provider instance.
	 *
	 * @param viewer The table viewer. Must not be <code>null</code>.
	 * @return The table viewer content provider instance.
	 */
	protected abstract IStructuredContentProvider doCreateTableViewerContentProvider(TableViewer viewer);

	/**
	 * Creates the table viewer comparator instance.
	 *
	 * @param viewer The table viewer. Must not be <code>null</code>.
	 * @return The table viewer comparator instance or <code>null</code> to turn of sorting.
	 */
	protected ViewerComparator doCreateTableViewerComparator(TableViewer viewer) {
		return null;
	}

	/**
	 * Configure the table.
	 *
	 * @param table The table. Must not be <code>null</code>.
	 * @param sorted Specify <code>true</code> if the table shall support sorting, <code>false</code> otherwise.
	 */
	protected void configureTable(Table table, boolean sorted) {
		Assert.isNotNull(table);

		// Create and configure the table columns
		createTableColumns(table, sorted);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	/**
	 * Create the table columns.
	 *
	 * @param table The table. Must not be <code>null</code>.
	 * @param sorted Specify <code>true</code> if the table shall support sorting, <code>false</code> otherwise.
	 */
	protected void createTableColumns(final Table table, boolean sorted) {
		Assert.isNotNull(table);

		TableColumn sortColumn = null;

		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.NodePropertiesTableControl_column_name_label);
		columnSortOrder.put(column, Boolean.TRUE);
		if (sorted) column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.widget instanceof TableColumn) {
					switchSortColumn(table, (TableColumn)e.widget);
				}
			}
		});
		// The property name is the default sorting column
		sortColumn = column;

		column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.NodePropertiesTableControl_column_value_label);
		columnSortOrder.put(column, Boolean.FALSE);
		if (sorted) column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.widget instanceof TableColumn) {
					switchSortColumn(table, (TableColumn)e.widget);
				}
			}
		});

		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(30));
		tableLayout.addColumnData(new ColumnWeightData(70));
		table.setLayout(tableLayout);

		GridData layoutData = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING);
		table.setLayoutData(layoutData);

		if (sorted) {
			// set the default sort column
			table.setSortColumn(sortColumn);
			table.setSortDirection(columnSortOrder.get(sortColumn).booleanValue() ? SWT.UP : SWT.DOWN);
		}
	}

	/**
	 * Switches the sort order for the given column and set the
	 * new sort order and sort column to the given table.
	 *
	 * @param table The table.
	 * @param column The table column
	 */
	protected final void switchSortColumn(Table table, TableColumn column) {
		if (table == null || table.isDisposed() || column == null || column.isDisposed()) {
			return;
		}
		// Get the current sorting order for the given column
		boolean newSortOrder = !columnSortOrder.get(column).booleanValue();
		// Set sort column and sort direction
		table.setSortColumn(column);
		table.setSortDirection(newSortOrder ? SWT.UP : SWT.DOWN);
		// And update the remembered sort order in the map
		columnSortOrder.put(column, Boolean.valueOf(newSortOrder));

		getViewer().refresh();
	}

	/**
	 * Create the context menu and toolbar groups.
	 *
	 * @param viewer The table viewer. Must not be <code>null</code>.
	 */
	protected void createContributionItems(TableViewer viewer) {
		Assert.isNotNull(viewer);

		// Create the menu manager
		MenuManager manager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		// Attach the menu listener
		manager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		// All items are removed when menu is closing
		manager.setRemoveAllWhenShown(true);
		// Associated with the tree
		viewer.getTable().setMenu(manager.createContextMenu(viewer.getTable()));

		// Register the context menu at the parent workbench part site.
		if (getParentPart() != null && getParentPart().getSite() != null && getContextMenuId() != null) {
			IWorkbenchPartSite site = getParentPart().getSite();
			site.registerContextMenu(getContextMenuId(), manager, viewer);
		}

		// The toolbar is a bit more complicated as we want to have the
		// toolbar placed within the section title.
		createToolbarContributionItem(viewer);
	}

	/**
	 * Returns the controls context menu id.
	 *
	 * @return The context menu id or <code>null</code>.
	 */
	protected String getContextMenuId() {
		return IUIConstants.ID_CONTROL_MENUS_BASE + ".menu.propertiesTable"; //$NON-NLS-1$
	}

	/**
	 * Creates the toolbar within the section parent of the given filtered tree.
	 *
	 * @param viewer The table viewer. Must not be <code>null</code>.
	 */
	protected void createToolbarContributionItem(TableViewer viewer) {
		Assert.isNotNull(viewer);

		// Determine the section parent from the filtered tree
		Composite parent = viewer.getTable().getParent();
		while (parent != null && !(parent instanceof Section)) {
			parent = parent.getParent();
		}

		// We are done here if we cannot find a section parent or the parent is disposed
		if (parent == null || parent.isDisposed()) {
			return;
		}

		// Create the toolbar control
		ToolBar toolbar = new ToolBar(parent, SWT.FLAT | SWT.HORIZONTAL | SWT.RIGHT);

		// The cursor within the toolbar shall change to an hand
		final Cursor handCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);
		// Cursor needs to be explicitly disposed
		toolbar.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (handCursor.isDisposed() == false) {
					handCursor.dispose();
				}
			}
		});

		// If the parent composite is a forms section, set the toolbar
		// as text client to the section header
		if (parent instanceof Section) {
			Section section = (Section)parent;
			// Set the toolbar as text client
			section.setTextClient(toolbar);
		}

		// create the toolbar items
		createToolBarItems(toolbar);
	}

	/**
	 * Create the toolbar items to be added to the toolbar. Override
	 * to add the wanted toolbar items.
	 * <p>
	 * <b>Note:</b> The toolbar items are added from left to right.
	 *
	 * @param toolbar The toolbar to add the toolbar items too. Must not be <code>null</code>.
	 */
	protected void createToolBarItems(ToolBar toolbar) {
		Assert.isNotNull(toolbar);
	}

	/**
	 * Returns the viewer instance.
	 *
	 * @return The viewer instance or <code>null</code>.
	 */
	public Viewer getViewer() {
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (Viewer.class.isAssignableFrom(adapter)) {
			// We have to double check if our real viewer is assignable to
			// the requested Viewer class.
			Viewer viewer = getViewer();
			if (!adapter.isAssignableFrom(viewer.getClass())) {
				viewer = null;
			}
			return viewer;
		} else if (ISelectionListener.class.isAssignableFrom(adapter)) {
			return editorSelectionChangedListener;
		}

		return super.getAdapter(adapter);
	}

}
