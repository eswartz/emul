/*
  DiskBrowseDialog.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.disk;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.files.Catalog;
import v9t9.common.files.CatalogEntry;
import v9t9.common.files.EmulatedFile;
import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.shells.FileContentDialog;

public class DiskBrowseDialog extends Dialog {
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_SIZE = 1;
	private static final int COLUMN_TYPE = 2;
	private static final int COLUMN_RECLEN = 3;
	private static final int COLUMN_PROT = 4;
	public static class CatalogLabelProvider extends LabelProvider implements ITableLabelProvider {


		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			CatalogEntry entry = (CatalogEntry) element;
			switch(columnIndex) {
			case COLUMN_NAME:
				return entry.fileName;
			case COLUMN_SIZE:
				return "" + entry.secs;
			case COLUMN_TYPE:
				return entry.type;
			case COLUMN_RECLEN:
				return entry.type.equals("PROGRAM") ? "" :  "" + entry.recordLength;
			case COLUMN_PROT:
				return entry.isProtected ? "P" : "";
			}
			return null;
		}
		
	}

	static class CatalogSorter extends ViewerComparator {
		private static final int ASCENDING = 0;

		private static final int DESCENDING = 1;

		private int column;

		private int direction;

		/**
		 * Does the sort. If it's a different column from the previous sort, do
		 * an ascending sort. If it's the same column as the last sort, toggle
		 * the sort direction.
		 * 
		 * @param column
		 */
		public void doSort(int column) {
			if (column == this.column) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.column = column;
				direction = ASCENDING;
			}
		}

		/**
		 * Compares the object for sorting
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {
			int rc = 0;
			CatalogEntry p1 = (CatalogEntry) e1;
			CatalogEntry p2 = (CatalogEntry) e2;

			// Determine which column and do the appropriate sort
			switch (column) {
			case COLUMN_NAME:
				rc = p1.fileName.compareTo(p2.fileName);
				break;
			case COLUMN_SIZE:
				rc = p1.secs - p2.secs;
				break;
			case COLUMN_RECLEN:
				rc = p1.recordLength - p2.recordLength;
				break;
			case COLUMN_TYPE:
				rc = p1.type.compareTo(p2.type);
				break;
			case COLUMN_PROT:
				rc = (p1.isProtected ? 1 : 0) - (p2.isProtected ? 1 : 0);
				break;
			}

			// If descending order, flip the direction
			if (direction == DESCENDING)
				rc = -rc;

			return rc;
		}
	}

	private final List<CatalogEntry> entries;
	private final Catalog catalog;
	private final IMachine machine;
	{
		setShellStyle(getShellStyle() & ~(SWT.APPLICATION_MODAL + SWT.SYSTEM_MODAL) | SWT.RESIZE | SWT.MODELESS);
	}

	public DiskBrowseDialog(Shell parentShell,
			IMachine machine,
			Catalog catalog) {
		super(parentShell);
		this.machine = machine;
		this.entries = catalog.getEntries();
		this.catalog = catalog;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Catalog of " + catalog.deviceName);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(400, 600);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		Label label = new Label(composite, SWT.WRAP);
		label.setText(catalog.deviceName + "\n\nName: " + catalog.volumeName + "; Total: " + catalog.totalSectors + "; Used: " + catalog.usedSectors);
		GridDataFactory.fillDefaults().grab(true,false).applyTo(label);
		
		final TableViewer viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new CatalogLabelProvider());

		final CatalogSorter comparator = new CatalogSorter();
		viewer.setComparator(comparator);
		
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		
		GridDataFactory.fillDefaults().grab(true,true).applyTo(viewer.getControl());
		
		TableColumn fileColumn = new TableColumn(table, SWT.LEFT);
		fileColumn.setText("Filename");
		setSortListener(viewer, fileColumn, COLUMN_NAME);
		TableColumn sizeColumn = new TableColumn(table, SWT.LEFT);
		sizeColumn.setText("Sectors");
		setSortListener(viewer, sizeColumn, COLUMN_SIZE);
		TableColumn typeColumn = new TableColumn(table, SWT.LEFT);
		typeColumn.setText("Type");
		setSortListener(viewer, typeColumn, COLUMN_TYPE);
		TableColumn lenColumn = new TableColumn(table, SWT.LEFT);
		lenColumn.setText("RecLen");
		setSortListener(viewer, lenColumn, COLUMN_RECLEN);
		TableColumn protColumn = new TableColumn(table, SWT.CENTER);
		protColumn.setText("Prot?");
		setSortListener(viewer, protColumn, COLUMN_PROT);
		
		viewer.setInput(entries);
		
		fileColumn.pack();
		sizeColumn.pack();
		typeColumn.pack();
		lenColumn.pack();
		protColumn.pack();
		///
		
		table.addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent e) {
				final Item item = viewer.getTable().getItem(
						viewer.getControl().toControl(new Point(e.x, e.y))
						);
				if (item == null)
					return;

				Menu menu = new Menu(viewer.getControl());

				if (item.getData() instanceof CatalogEntry) {

					final CatalogEntry entry = (CatalogEntry) item.getData();
					

					final MenuItem pasteFullPathItem;
					pasteFullPathItem = new MenuItem(menu, SWT.NONE);
					pasteFullPathItem.setText("Paste path into emulator");
					
					pasteFullPathItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							doPaste(entry, true);
						}
					});
					

					final MenuItem pasteNameItem;
					pasteNameItem = new MenuItem(menu, SWT.NONE);
					pasteNameItem.setText("Paste filename into emulator");
					
					pasteNameItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							doPaste(entry, false);
						}
					});
					
					
					final MenuItem viewItem;
					viewItem = new MenuItem(menu, SWT.NONE);
					viewItem.setText("View content");
					
					viewItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							doViewContent(entry.getFile());
						}
					});
					
				}

				if (menu.getItemCount() == 0) {
					menu.dispose();
					return;
				}
				
				menu.setLocation(e.x, e.y);
				menu.setVisible(true);
				
				while (!menu.isDisposed() && menu.isVisible()) {
					if (!getShell().getDisplay().readAndDispatch())
						getShell().getDisplay().sleep();
				}

			}
		});

		return composite;
	}

	protected void doPaste(CatalogEntry entry, boolean full) {
		String filePath;
		if (full)
			filePath = catalog.deviceName + "." + entry.fileName;
		else
			filePath = entry.fileName;
		machine.notifyEvent( 
				Level.INFO, "Pasting '" + filePath + "'");
				
		machine.getKeyboardHandler().pasteText(filePath);
	}
	private void setSortListener(final TableViewer viewer, TableColumn column, final int columnName) {
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((CatalogSorter) viewer.getComparator()).doSort(columnName);
				viewer.refresh();
			}
		});
		
	}
	
	protected void doViewContent(EmulatedFile file) {
		FileContentDialog dialog = new FileContentDialog(getShell());
		dialog.setFile(file);
		dialog.open();
	}

}