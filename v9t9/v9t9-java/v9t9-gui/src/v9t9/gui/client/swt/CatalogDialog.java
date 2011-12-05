package v9t9.gui.client.swt;

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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import v9t9.base.settings.SettingProperty;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.files.Catalog;
import v9t9.common.files.CatalogEntry;
import v9t9.common.machine.IMachine;

final class CatalogDialog extends Dialog {
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
	private final SettingProperty setting;
	private final IMachine machine;
	{
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MODELESS);
	}

	public CatalogDialog(Shell parentShell,
			IMachine machine,
			List<CatalogEntry> entries, 
			Catalog catalog, SettingProperty setting) {
		super(parentShell);
		this.machine = machine;
		this.entries = entries;
		this.catalog = catalog;
		this.setting = setting;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Catalog of " + setting.getName());
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
		label.setText(setting.getString() + "\n\nName: " + catalog.volumeName + "; Total: " + catalog.totalSectors + "; Used: " + catalog.usedSectors);
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
		

		final boolean[] isShifted = { false };
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				isShifted[0] = (e.stateMask & SWT.SHIFT) != 0;
				TableItem item = table.getItem(new Point(e.x, e.y));
				if (item != null && item.getData() instanceof CatalogEntry) {
					CatalogEntry entry = (CatalogEntry)item.getData();
					String filePath;
					if (isShifted[0])
						filePath = entry.fileName;
					else
						filePath = setting.getName() + "." + entry.fileName;
					machine.getClient().getEventNotifier().notifyEvent(entry, 
							Level.INFO, "Pasting '" + filePath + "'");
							
					machine.getKeyboardState().pasteText(filePath);
				}
			}
		});

		label = new Label(composite, SWT.WRAP);
		label.setText("Double-click to paste path (shift for filename)");
		GridDataFactory.fillDefaults().grab(true,false).applyTo(label);
		
		
		return composite;
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
}