/**
 * 
 */
package v9t9.gui.client.swt.shells;

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
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleManager;
import v9t9.common.modules.MemoryEntryInfo;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.client.swt.bars.ImageBar;
import v9t9.gui.common.FontUtils;

/**
 * @author ejs
 *
 */
public class ModuleSelector extends Composite {
	public static final String MODULE_SELECTOR_TOOL_ID = "module.selector";
	private static String lastFilter;

	static final int IMAGE_COLUMN = 0;
	static final int NAME_COLUMN = 1;
	static final int FILE_COLUMN = 2;
	
	private TableViewer viewer;
	private TableColumn imageColumn;
	private TableColumn nameColumn;
	private IModule selectedModule;
	private Composite buttonBar;
	private final IMachine machine;
	private Button switchButton;
	private Font tableFont;
	private final IModuleManager moduleManager;
	private Button scanButton;
	private Text filterText;
	protected int visibleCount;

	
	/**
	 * This filter only allows through module entries for
	 * which all the ROM segments exist.
	 * @author ejs
	 *
	 */
	class ExistingModulesFilter extends ViewerFilter {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (!(element instanceof IModule))
				return true;
			
			IModule module = (IModule) element;
			try {
				moduleManager.getModuleMemoryEntries(module);
				return true;
			} catch (NotifyException e) {
				System.out.println(e.toString());
				return false;
			}
		}
		
	}
	
	private ViewerFilter existingModulesFilter = new ExistingModulesFilter();
	
	class FilteredSearchFilter extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (lastFilter != null) {
				// note: instanceof excludes "<No module>" entry too
				return element instanceof IModule && ((IModule) element).getName().toLowerCase().contains(
						lastFilter.toLowerCase());
			}
			return true;
		}
	}
	
	private ViewerFilter filteredSearchFilter = new FilteredSearchFilter();

	/**
	 * 
	 */
	public ModuleSelector(Shell shell, IMachine machine, IModuleManager moduleManager) {
		super(shell, SWT.NONE);
		this.moduleManager = moduleManager;
		
		shell.setText("Module Selector");
		
		this.machine = machine;
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		//Label label = new Label(this, SWT.WRAP);
		//label.setText("Select a module:");
		//GridDataFactory.swtDefaults().grab(true, false).applyTo(label);
		
		createSearchFilter();
		
		
		viewer = createTable();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
		
		buttonBar = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(3).equalWidth(false).applyTo(buttonBar);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonBar);
		

		scanButton = new Button(buttonBar, SWT.PUSH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(128, -1).applyTo(scanButton);
		scanButton.setText("Scan...");
		scanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		scanButton.setEnabled(false);

		
		Label filler = new Label(buttonBar, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(filler);

		
		switchButton = new Button(buttonBar, SWT.PUSH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(128, -1).applyTo(switchButton);
		
		switchButton.setText("Switch module");
		switchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				switchModule((e.stateMask & SWT.SHIFT) != 0);
			}
		});
		switchButton.setEnabled(false);
		
		

		hookActions();

		initFilter(lastFilter);

	}

	/**
	 * 
	 */
	private void createSearchFilter() {
		
		Composite comp = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(2).equalWidth(false).applyTo(comp);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comp);

		filterText = new Text(comp, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(filterText);
		
		filterText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (lastFilter == null) {
					filterText.setForeground(null);
					lastFilter = "";
					if (e.keyCode == e.character) {
						filterText.setText("");
					}
				}
				
				if (e.keyCode == SWT.ARROW_DOWN) {
					viewer.getTable().setFocus();
				}
				
				if (e.keyCode == '\r') {
					viewer.getTable().setFocus();
					e.doit = false;
					
					if (viewer.getTable().getItemCount() == 1) {
						selectedModule = (IModule) viewer.getTable().getItems()[0].getData();
						switchModule(false);
					}
				}
			}
		});

		filterText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				updateFilter(filterText.getText());
			}
		});
		
		final Button clearButton = new Button(comp, SWT.PUSH | SWT.NO_FOCUS);
		clearButton.setImage(EmulatorGuiData.loadImage(getDisplay(), "icons/icon_search_clear.png"));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(false, false).applyTo(clearButton);

		clearButton.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				initFilter(null);
			}
		});
	}

	protected void initFilter(String text) {
		if (text == null || text.length() == 0) {
			filterText.setText("Search...");
			filterText.setForeground(getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		}  else {
			filterText.setText(text);
			filterText.selectAll();
			filterText.setForeground(null);
		}
		
		viewer.setFilters(new ViewerFilter[] { 
				existingModulesFilter,
				filteredSearchFilter
			}
		);

		updateFilter(text);
		

		getDisplay().asyncExec(new Runnable() {
			public void run() {
				nameColumn.pack();
			}
		});

	}

	/**
	 * @param text
	 */
	protected void updateFilter(final String text) {
		if (text == null || text.isEmpty()) {
			lastFilter = null;
		} else {
			lastFilter = text;
		}
		viewer.refresh();
	}

	/**
	 * @param moduleManager
	 */
	protected TableViewer createTable() {
		final TableViewer viewer = new TableViewer(this, SWT.READ_ONLY | SWT.BORDER);
		
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		GridDataFactory.fillDefaults().grab(true,true).applyTo(table);
		
		/*
		if (table.getFont().getFontData()[0].getHeight() < 12) {
			FontDescriptor desc = FontUtils.getFontDescriptor(JFaceResources.getDefaultFont());
			tableFont = desc.setHeight(12).createFont(getDisplay()); 
			table.setFont(tableFont);
		}
		*/

		FontDescriptor desc = FontUtils.getFontDescriptor(JFaceResources.getBannerFont());
		tableFont = desc.createFont(getDisplay()); 
		table.setFont(tableFont);

		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (tableFont != null)
					tableFont.dispose();
			}
		});
		
		imageColumn = new TableColumn(table, SWT.LEFT);
		imageColumn.setText("");
		
		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Name");

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ModuleTableLabelProvider());
		
		selectedModule = null;
		final IModule[] realModules = moduleManager.getModules();
		
		if (realModules.length > 0) {
			addIterativeSearch(viewer, table, realModules);
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

		table.addKeyListener(new KeyAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == '\r' || e.keyCode == '\n') {
					switchModule(false);
					e.doit = false;
				}
			}
		});
		
		return viewer;
	}

	/**
	 * @param viewer
	 * @param table
	 * @param realModules
	 */
	protected void addIterativeSearch(final TableViewer viewer, Table table,
			final IModule[] realModules) {
		table.addKeyListener(new KeyAdapter() {
			StringBuilder search = new StringBuilder();
			int index = 0;
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == '\b' || e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_UP) {
					search.setLength(0);
					index = 0;
					e.doit = e.keyCode != '\b';
				}
				else if (e.character >= 32 && e.character < 127) {
					search.append(e.character);
					e.doit = false;
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


	/**
	 * 
	 */
	protected void hookActions() {
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
			case IMAGE_COLUMN: return null;
			case NAME_COLUMN: return module.getName();
			case FILE_COLUMN: {
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

	/**
	 * @param machine2
	 * @param buttonBar2
	 * @return
	 */
	public static IToolShellFactory getToolShellFactory(final IMachine machine,
			final ImageBar buttonBar) {
		 return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "ModuleWindowBounds";
				behavior.centering = Centering.INSIDE;
				behavior.centerOverControl = buttonBar;
				behavior.dismissOnClickOutside = true;
			}
			public Control createContents(Shell shell) {
				return new ModuleSelector(shell, machine, machine.getModuleManager());
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}
}
