/**
 * 
 */
package v9t9.gui.client.swt;

import java.text.MessageFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import v9t9.common.events.NotifyException;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleManager;
import v9t9.common.modules.MemoryEntryInfo;

/**
 * @author ejs
 *
 */
public class ModuleSelector extends Composite {

	private TableViewer viewer;
	private TableColumn nameColumn;
	private IModule selectedModule;
	private Composite buttonBar;
	private final IMachine machine;
	private Button switchButton;
	private TableColumn fileColumn;
	private Font tableFont;
	private final IModuleManager moduleManager;

	/**
	 * 
	 */
	public ModuleSelector(Shell shell, IMachine machine, IModuleManager moduleManager) {
		super(shell, SWT.NONE);
		this.moduleManager = moduleManager;
		
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
		
		if (table.getFont().getFontData()[0].getHeight() < 12) {
			FontDescriptor desc = FontUtils.getFontDescriptor(JFaceResources.getDefaultFont());
			tableFont = desc.setHeight(12).createFont(getDisplay()); 
			table.setFont(tableFont);
		}
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (tableFont != null)
					tableFont.dispose();
			}
		});
		
		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Name");

		fileColumn = new TableColumn(table, SWT.LEFT);
		fileColumn.setText("File");

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ModuleTableLabelProvider());
		
		selectedModule = null;
		final IModule[] realModules = moduleManager.getModules();
		
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
					switchModule(false);
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
				switchModule((e.stateMask & SWT.SHIFT) != 0);
			}
		});
		switchButton.setEnabled(false);
		

		if (realModules.length > 0) {
			table.addKeyListener(new KeyAdapter() {
				StringBuilder search = new StringBuilder();
				int index = 0;
				
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.keyCode == '\b') {
						search.setLength(0);
						index = 0;
						e.doit = false;
					}
					else if (e.character >= 32 && e.character < 127) {
						search.append(e.character);
						e.doit = false;
					}
					else if (e.keyCode == '\r' || e.keyCode == '\n') {
						switchModule(false);
						e.doit = false;
						return;
					}
					else {
						return;
					}
					
					if (search.length() > 0) {
						int end = (index + realModules.length - 1) % realModules.length;
						for (int i = index; i != end; i = (i + 1) % realModules.length) {
							IModule m = realModules[i];
							if (m.getName().toLowerCase().contains(search.toString().toLowerCase())) {
								viewer.setSelection(new StructuredSelection(m));
								viewer.reveal(m);
								index = i;
								break;
							}
						}
					}
				}
			});
		}
		
		
		Object[] modulesPlusEmpty = new Object[realModules.length + 1];
		modulesPlusEmpty[0] = "<No module>";
		System.arraycopy(realModules, 0, modulesPlusEmpty, 1, realModules.length);
		viewer.setInput(modulesPlusEmpty);
		final IModule[] loadedModules = moduleManager.getLoadedModules();
		viewer.setSelection(new StructuredSelection(loadedModules));
		if (loadedModules.length > 0) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.reveal(loadedModules[0]);
				}
			});
		}

		nameColumn.pack();
		fileColumn.pack();
	}

	/**
	 * @param softReset if true, just reset CPU, else if false, reset whole machine 
	 * 
	 */
	protected void switchModule(boolean softReset) {
		try {
			moduleManager.switchModule(selectedModule);
			if (softReset)
				machine.getCpu().reset();
			else
				machine.reset();

			getShell().dispose();
		} catch (NotifyException e) {
			machine.notifyEvent(Level.ERROR,
					MessageFormat.format("Failed to load all the entries from the module ''{0}''\n\n{1}",
							selectedModule.getName(), e.getMessage()));
		} catch (Throwable e) {
			e.printStackTrace();
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
			case 1: {
				for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
					Object v = info.getProperties().get(MemoryEntryInfo.FILENAME);
					if (v != null)
						return v.toString();
				}
			}
			}
			return null;
		}
		
	}
}
