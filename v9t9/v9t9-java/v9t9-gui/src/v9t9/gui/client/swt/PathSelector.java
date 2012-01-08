/**
 * 
 */
package v9t9.gui.client.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ejs.base.properties.IProperty;

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

	public PathSelector(Composite parent, SwtWindow window, String pathLabel, IProperty property) {
		super(parent, SWT.BORDER);
		this.window = window;
		this.pathLabel = pathLabel;
		this.property = property;
		
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(this);
		
		// left side: the list
		createListTable();
		
		// right side: buttons
		createButtons();
	}

	/**
	 * 
	 */
	private void createListTable() {
		viewer = new TableViewer(this);
		
		final Table table = viewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		final TableColumn column = new TableColumn(table, SWT.LEFT);
		
		column.setText("Location");


		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				column.pack();
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
				property.firePropertyChange();
			}
		});
		
		viewer.setLabelProvider(new LabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
		
		viewer.setInput(property.getList());
	}

	/**
	 * 
	 */
	private void createButtons() {
		buttons = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1). applyTo(buttons);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(buttons);
		
		final Button add = new Button(buttons, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(add);
		add.setText("Add...");
		
		add.addSelectionListener(new SelectionAdapter() {
			String lastDirectory = property.getList().isEmpty() ? "." : (String) property.getList().get(0);
			@Override
			public void widgetSelected(SelectionEvent e) {
				String dir = window.openDirectorySelectionDialog("Add " + pathLabel, lastDirectory);
				if (dir == null)
					return;
				property.getList().add(dir);
				viewer.add(dir);
				lastDirectory = dir;
				property.firePropertyChange();
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
					property.firePropertyChange();
				}
			}
		});
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				removeButton.setEnabled(!event.getSelection().isEmpty());
			}
		});

	}

}
