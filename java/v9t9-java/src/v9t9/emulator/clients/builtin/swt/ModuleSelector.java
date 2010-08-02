/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import v9t9.emulator.clients.builtin.NotifyException;
import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.cpu.Cpu9900;
import v9t9.engine.modules.IModule;

/**
 * @author ejs
 *
 */
public class ModuleSelector extends Composite {

	private TableViewer viewer;
	private TableColumn nameColumn;
	private IModule selectedModule;
	private Composite buttonBar;
	private final Machine machine;
	private Button switchButton;

	/**
	 * 
	 */
	public ModuleSelector(Shell shell, Machine machine) {
		super(shell, SWT.NONE);
		
		shell.setText("Module Selector");
		
		this.machine = machine;
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		Label label = new Label(this, SWT.WRAP);
		label.setText("Select a module:");
		GridDataFactory.swtDefaults().grab(true, false).applyTo(label);
		
		viewer = new TableViewer(this, SWT.READ_ONLY | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
		
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		GridDataFactory.fillDefaults().grab(true,true).applyTo(table);
		
		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Name");

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ModuleTableLabelProvider());
		
		selectedModule = null;
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (obj instanceof String) {
					selectedModule = null;
					switchButton.setEnabled(true);
				}
				else if (obj instanceof IModule) {
					selectedModule = (IModule) obj;
					switchButton.setEnabled(true);
				} else {
					switchButton.setEnabled(false);
				}
			}
		});
		viewer.addOpenListener(new IOpenListener() {
			
			public void open(OpenEvent event) {
				Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (obj instanceof String) {
					selectedModule = null;
					switchButton.setEnabled(true);
				}
				else if (obj instanceof IModule) {
					selectedModule = (IModule) obj;
					switchButton.setEnabled(true);
					switchModule();
				} else {
					switchButton.setEnabled(false);
				}
			}
		});
		
		
		buttonBar = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(buttonBar);
		
		switchButton = new Button(buttonBar, SWT.PUSH);
		switchButton.setText("Switch module");
		switchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				switchModule();
			}
		});
		switchButton.setEnabled(false);
		
		IModule[] realModules = machine.getModuleManager().getModules();
		Object[] modulesPlusEmpty = new Object[realModules.length + 1];
		modulesPlusEmpty[0] = "<No module>";
		System.arraycopy(realModules, 0, modulesPlusEmpty, 1, realModules.length);
		viewer.setInput(modulesPlusEmpty);
		final IModule[] loadedModules = machine.getModuleManager().getLoadedModules();
		viewer.setSelection(new StructuredSelection(loadedModules));
		if (loadedModules.length > 0) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.reveal(loadedModules[0]);
				}
			});
		}

		nameColumn.pack();
	}

	/** 
	 * 
	 */
	protected void switchModule() {
		try {
			machine.getModuleManager().switchModule(selectedModule);
			machine.getCpu().setPin(Cpu9900.PIN_RESET);

			getShell().dispose();
		} catch (NotifyException e) {
			ErrorDialog.openError(getShell(), "Failed to load", 
					"Failed to load all the entries from the module",
					new Status(IStatus.ERROR, "v9t9", e.getMessage(), e));
		}
		
			
		
	}

	/**
	 * @return the viewer
	 */
	public Viewer getViewer() {
		return viewer;
	}
	
	/**
	 * @return the selectedModule
	 */
	public IModule getSelectedModule() {
		return selectedModule;
	}
	
	static class ModuleTableLabelProvider extends LabelProvider implements ITableLabelProvider {

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
			if (element instanceof String)
				return element.toString();
			if (!(element instanceof IModule)) {
				return null;
			}
			IModule module = (IModule) element;
			switch (columnIndex) {
			case 0: return module.getName();
			}
			return null;
		}
		
	}
}
