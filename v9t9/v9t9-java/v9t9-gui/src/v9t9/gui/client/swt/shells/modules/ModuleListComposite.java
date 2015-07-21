/*
  ModuleListComposite.java

  (c) 2013-2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.modules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleDetector;
import v9t9.common.modules.IModuleManager;
import v9t9.common.modules.ModuleDatabase;
import v9t9.common.settings.Settings;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.client.swt.SwtWindow;
import ejs.base.properties.IProperty;
import ejs.base.settings.DialogSettingsWrapper;
import ejs.base.settings.ISettingSection;

/**
 * This composite lets the user select which detected modules
 * should be added to a module list database. 
 * @author ejs
 *
 */
public class ModuleListComposite extends Composite {

	public interface IStatusListener {
		void statusUpdated(IStatus status);
	}
	
	private static final String SECTION_MODULE_MANAGER = "module.manager";
	
	private static final String SHOW_ALL = "show.all";
	private static final String LAST_DB_FILE = "last.db";

	private IMachine machine;
	private ComboViewer dbSelector;
	
	private CheckboxTableViewer viewer;

	private List<IModule> discoveredModules = new ArrayList<IModule>();
	
	/** current database */
	private File dbFile;
	
	private Map<File, List<IModule>> origModuleMap = new HashMap<File, List<IModule>>();
	private Map<File, List<IModule>> selectedModuleMap = new LinkedHashMap<File, List<IModule>>();
	private List<IModule> selectedModules = null;

	private ISettingSection settings;

	private TableViewerColumn checkedColumn;
	private TableViewerColumn nameColumn;

	private IStatus status;

	protected boolean showAllModules;

	private URI nonameDatabaseURI = URI.create("modules.xml");

	private Button showAllButton;

	private IStatusListener statusListener;

	private ModuleNameEditingSupport editingSupport;

	private ArrayList<URI> dirtyModuleLists;


	private ColumnComparator comparator;

	private DiscoveredModuleLabelProvider labelProvider;

	class FilteredSearchFilter extends ViewerFilter {

		@Override
		public boolean isFilterProperty(Object element, String property) {
			return true;
		}
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (lastFilter != null) {
				if (false == element instanceof IModule)
					return true;
				IModule mod = (IModule) element;
				String lowSearch = lastFilter.toLowerCase();
				return mod.getName().toLowerCase().contains(lowSearch)
						|| mod.getKeywords().contains(lowSearch)
						|| ("auto-start".startsWith(lowSearch) && mod.isAutoStart());
			}
			return true;
		}
	}

	private ViewerFilter filteredSearchFilter = new FilteredSearchFilter();
	private Text filterText;
	protected String lastFilter;

	public ModuleListComposite(Composite parent, IMachine machine, SwtWindow window,
			IStatusListener statusListener) {
		super(parent, SWT.NONE);
		this.statusListener = statusListener;
		
		GridLayoutFactory.fillDefaults().margins(3, 3).applyTo(this);
		
		this.machine = machine;
		
		settings = machine.getSettings().getMachineSettings().getHistorySettings().findOrAddSection(SECTION_MODULE_MANAGER);

		Composite composite = this;
		
		/*spacer*/ new Label(composite, SWT.NONE);

		Composite dbRow = createDatabaseRow(composite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(dbRow);
		
		Composite tableComp = createTableViewer(composite);
		GridDataFactory.fillDefaults().grab(true, true).hint(-1, 500).applyTo(tableComp);

		boolean wasShowAll = settings.getBoolean(SHOW_ALL);
		showAllButton.setSelection(wasShowAll);

		String last = settings.get(LAST_DB_FILE);

		File sel = null;
		IProperty modList = machine.getSettings().get(IModuleManager.settingUserModuleLists);
		List<String> mods = modList.getList();
		for (String mod : mods) {
			URI uri = machine.getRomPathFileLocator().findFile(mod);
			if (uri != null) {
				File file = null;
				try {
					file = new File(uri);
					List<IModule> origModules = machine.getModuleManager().readModules(uri);
					selectedModuleMap.put(file, origModules);
					origModuleMap.put(file, new ArrayList<IModule>(origModules));
					if (sel == null || (last != null && file.equals(new File(last)))) {
						sel = file;
					}
				} catch (IllegalArgumentException e) {
					// ignore
				} catch (IOException e1) {
					e1.printStackTrace();
					if (file != null)
						selectedModuleMap.put(file, null);
				}
			}
		}
		
			
		dbSelector.setLabelProvider(new LabelProvider());
		dbSelector.setContentProvider(new ArrayContentProvider());
		
		dbSelector.setInput(selectedModuleMap.keySet());
		
		if (sel == null) {
			File defaultFile = new File(machine.getSettings().getUserSettings().getConfigDirectory()
					+ "modules.xml");
			setDbFile(defaultFile);
			
			dbSelector.add(defaultFile);
			
			sel = defaultFile;
		}
			
		refresh();
		
		dbSelector.setSelection(new StructuredSelection(sel));
		
		composite.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (dbFile != null)
					settings.put(LAST_DB_FILE, dbFile.toString());
			}
		});

	}

	protected IDialogSettings getDialogBoundsSettings() {
		return new DialogSettingsWrapper(settings);
	}

	class DiscoveredModuleLabelProvider extends BaseLabelProvider implements ITableLabelProvider, IFontProvider {
		public DiscoveredModuleLabelProvider() {
		}
		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IModule) {
				if (columnIndex == 0) {
					return "";
				}
				return ((IModule) element).getName();
			}
			return "";
		}
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		@Override
		public Font getFont(Object element) {
			if (element instanceof IModule) {
				IModule stock = machine.getModuleManager().findStockModuleByMd5(((IModule) element).getMD5());
				if (stock != null && false == stock.getName().equals(((IModule) element).getName())) 
					return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
			}
			return null;
		}
	}
	
	public class ColumnComparator extends ViewerComparator {
		private int propertyIndex;
		private int direction = 1;

		public ColumnComparator() {
			this.propertyIndex = 0;
			direction = 1;
		}

		public int getDirection() {
			return direction == 1 ? SWT.DOWN : SWT.UP;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// flip between ascending/descending/none
				if (direction == 0)
					direction = 1;
				else
					direction = -direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = 1;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (direction == 0)
				return 0;
			
			int rc;
			if (propertyIndex == 0) {
				// checkbox
				boolean sel1 = selectedModules.contains(e1);
				boolean sel2 = selectedModules.contains(e2);
				rc = (sel1 ? 0 : 1) - (sel2 ? 0 : 1);
			}
			else {
				String lab1 = labelProvider.getColumnText(e1, propertyIndex);
				String lab2 = labelProvider.getColumnText(e2, propertyIndex);
				
				rc = lab1.compareToIgnoreCase(lab2);
			}
			
			// If descending order, flip the direction
			if (direction < 0) {
				rc = -rc;
			}
			return rc;
		}

	}

	private Composite createSearchFilter(Composite parent) {
		
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(2).equalWidth(false).applyTo(comp);

		filterText = new Text(comp, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		filterText.setMessage("Search...");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(filterText);
		
		filterText.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				filterText.selectAll();				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				filterText.selectAll();				
			}
		});
		
		final Button clearButton = new Button(comp, SWT.PUSH | SWT.NO_FOCUS);
		clearButton.setImage(EmulatorGuiData.loadImage(getDisplay(), "icons/icon_search_clear.png"));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(false, false).applyTo(clearButton);

		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				initFilter(null);
				filterText.setFocus();
				filterText.setText("");
			}
		});
		
		filterText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (viewer.getControl().isDisposed())
					return;

				if (lastFilter == null) {
					filterText.setForeground(null);
					lastFilter = "";
					filterText.setText("");
				}
				
				if (e.keyCode == SWT.ARROW_DOWN) {
					viewer.getControl().setFocus();
				}
				
				if (e.keyCode == '\r') {
					viewer.getControl().setFocus();
//					e.doit = false;
				}
			}
		});

		filterText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				updateFilter(filterText.getText());
				
				if (lastFilter != null && viewer.getTable().getItemCount() > 0) {
					viewer.setSelection(new StructuredSelection(viewer.getTable().getItems()[0].getData()), true);
				}
			}
		});

		return comp;
	}
	
	protected void refreshFilters() {
		viewer.setFilters(new ViewerFilter[] { 
				filteredSearchFilter
			}
		);
	}

	private void initFilter(String text) {
		String curText = filterText.getText(); 
		if (curText.isEmpty()) {
			filterText.setText(text != null ? text : "");
			filterText.selectAll();
			filterText.setForeground(null);
		}
		
		refreshFilters();

		updateFilter(text);

		// re-apply checked state, which is lost
		// see https://www.eclipse.org/forums/index.php/t/403879/
		if (selectedModules != null)
			viewer.setCheckedElements(selectedModules.toArray());

		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!nameColumn.getColumn().isDisposed())
					nameColumn.getColumn().setWidth(viewer.getControl().getSize().x);

				filterText.setFocus();
			}
		});

	}

	protected void updateFilter(final String text) {
		String prev = lastFilter;
		if (text == null || text.isEmpty() || text.equals("Search...")) {
			lastFilter = null;
		} else {
			lastFilter = text;
		}
		if (lastFilter != prev && (lastFilter == null || ! lastFilter.equals(prev))) {
			viewer.refresh();
		}
	}

	/**
	 * @param composite
	 */
	private Composite createTableViewer(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);

		GridDataFactory.fillDefaults().grab(true, true).hint(-1, 500).applyTo(composite);
		Label label;
		
		/*spacer*/ label = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(label);
		
		label = new Label(composite, SWT.NONE);
		label.setText("Select modules for list:");
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(label);
		
		Composite listArea = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(0, 0).numColumns(2).applyTo(listArea);
		GridDataFactory.fillDefaults().grab(true, true).span(3, 1).indent(12, 0).applyTo(listArea);

		Composite c = createSearchFilter(listArea);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(c);
		label = new Label(listArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(label);
		
		viewer = CheckboxTableViewer.newCheckList(listArea, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).hint(-1, 400).applyTo(viewer.getControl());
		
		viewer.getTable().setHeaderVisible(true);
		
		comparator = new ColumnComparator();
		viewer.setComparator(comparator);
		
		checkedColumn = new TableViewerColumn(viewer, SWT.LEFT | SWT.RESIZE);
		checkedColumn.getColumn().addSelectionListener(createColumnSelectionListener(checkedColumn.getColumn(), 0));
		checkedColumn.getColumn().setText("Use");
		
		nameColumn = new TableViewerColumn(viewer, SWT.LEFT | SWT.RESIZE);
		nameColumn.getColumn().addSelectionListener(createColumnSelectionListener(nameColumn.getColumn(), 1));
		nameColumn.getColumn().setText("Name");

		comparator.direction = 1;
		comparator.propertyIndex = 1;
		
		labelProvider = new DiscoveredModuleLabelProvider();
		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(new ArrayContentProvider());
		
		dirtyModuleLists = new ArrayList<URI>();
		editingSupport = new ModuleNameEditingSupport(machine.getModuleManager(), viewer, dirtyModuleLists);
		nameColumn.setEditingSupport(editingSupport);
		editingSupport.setCanEdit(true);
		
		viewer.setInput(discoveredModules);
		
		Composite buttons = new Composite(listArea, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(buttons);
		GridDataFactory.swtDefaults().grab(false, false).span(1, 1).applyTo(buttons);
		
		final Button selectAllButton = new Button(buttons, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(selectAllButton);
		selectAllButton.setText("Select All");
		
		final Button selectNoneButton = new Button(buttons, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(selectNoneButton);
		selectNoneButton.setText("Select None");
		
		final Button resetButton = new Button(buttons, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(resetButton);
		resetButton.setText("Reset");

		selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setAllChecked(true);
				selectedModules.clear();
				
				selectedModules.addAll(discoveredModules);
				validate();
			}
		});
		selectNoneButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setAllChecked(false);
				selectedModules.clear();
				validate();
			}
		});
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				reset();
			}
		});
		
		viewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (selectedModules != null) {
					if (event.getElement() instanceof IModule) {
						IModule module = (IModule) event.getElement();
						if (event.getChecked()) {
							selectedModules.add(module);
						} else {
							selectedModules.remove(module);
						}
					}
				}
				validate();
			}
		});
		
		viewer.getControl().addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent e) {
				final TableItem item = viewer.getTable().getItem(
						viewer.getTable().toControl(new Point(e.x, e.y))
						);
				if (item != null && item.getData() instanceof IModule) {
					final IModule module = (IModule) item.getData();
					
					final IModule stock = machine.getModuleManager().findStockModuleByMd5(module.getMD5());

					final Menu menu = new Menu(viewer.getControl());
					
					final MenuItem ditem;
					ditem = new MenuItem(menu, SWT.NONE);
					ditem.setText("Module details...");
					
					ditem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							menu.dispose();
							ModuleInfoDialog dialog = new ModuleInfoDialog(machine, null, getShell(), module);
							dialog.open();
						}
					});
					
					if (stock != null && false == module.getName().equals(stock.getName())) {
						final MenuItem nitem;
						nitem = new MenuItem(menu, SWT.NONE);
						nitem.setText("Reset from stock module");
						
						nitem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								menu.dispose();
								module.setName(stock.getName());
								module.setAutoStart(stock.isAutoStart());
								module.getKeywords().addAll(stock.getKeywords());
								viewer.refresh(module);
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
						if (!getDisplay().readAndDispatch())
							getDisplay().sleep();
					}
					
				}
			}
		});
		
		

		showAllButton = new Button(listArea, SWT.CHECK);
		GridDataFactory.fillDefaults().indent(0, 6).grab(true, false).applyTo(showAllButton);
		
		showAllButton.setText("Show all modules");
		showAllButton.setToolTipText("When enabled, show all modules, even those recorded in other lists");
		showAllButton.setSelection(showAllModules);
		showAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showAllModules = showAllButton.getSelection();
				refresh();
			}
		});
		
		initFilter(null);
		return composite;
	}

	private SelectionListener createColumnSelectionListener(final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	/**
	 * @param composite
	 */
	protected Composite createDatabaseRow(Composite parent) {
		Composite dbRow = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(4).spacing(2, 0).applyTo(dbRow);
		
		Label label;
		
		label = new Label(dbRow, SWT.WRAP);
		label.setText("List file: ");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);
		
		dbSelector = new ComboViewer(dbRow, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(dbSelector.getControl());
		dbSelector.getControl().setToolTipText("Specify a file to record the list of discovered modules.");
		
		dbSelector.getControl().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				File db = new File(dbSelector.getCombo().getText());
				setDbFile(db);
				dbSelector.setInput(selectedModuleMap.keySet());
				dbSelector.setSelection(new StructuredSelection(dbFile));
			}
		});
		dbSelector.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty()) {
					return;
				}
				Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
				
				File dbFile = (File) o;
				if (dbFile == ModuleListComposite.this.dbFile)
					return;
				
				setDbFile(dbFile);
				
				validate();
				reset();
			}
		});

		final Button browseButton = new Button(dbRow, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setToolTipText("Locate and add a new modules.xml file.");
		GridDataFactory.fillDefaults().hint(128, -1).applyTo(browseButton);
		
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.SAVE);
				dialog.setText("Select Module List");
				dialog.setFileName("modules.xml");
				dialog.setFilterExtensions(new String[] { "*.xml" });
				dialog.setFilterNames(new String[] { "Module Lists (*.xml)" });
				String path = dialog.open();
				if (path != null) {
					File db = new File(path);
					setDbFile(db);
					reset();
					dbSelector.refresh();
					dbSelector.setSelection(new StructuredSelection(db));
				}
			}
		});
		

		final Button removeButton = new Button(dbRow, SWT.PUSH);
		removeButton.setImage(EmulatorGuiData.loadImage(getDisplay(), "icons/icon_clear.gif"));
		removeButton.setToolTipText("Remove the current modules.xml file from V9t9 (does not delete)");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(false, false).applyTo(removeButton);

		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedModuleMap.remove(dbFile);
				if (selectedModuleMap.isEmpty()) {
					setDbFile(null);
				} else {
					setDbFile(selectedModuleMap.keySet().iterator().next());
				}
				
				reset();
				dbSelector.refresh();
				if (dbFile != null)
					dbSelector.setSelection(new StructuredSelection(dbFile));
				
				validate();
			}
		});

		return dbRow;
	}

	/**
	 * @param mod
	 */
	private void setDbFile(File dbFile) {
		this.dbFile = dbFile;
		
		List<IModule> selected;
		if (dbFile != null) {
			selected = selectedModuleMap.get(dbFile);
			if (selected == null) {
				if (dbFile.exists() && machine.getModuleManager() != null) {
					try {
						selected = machine.getModuleManager().readModules(dbFile.toURI());
						origModuleMap.put(dbFile, new ArrayList<IModule>(selected));
					} catch (IOException e) {
					}
				}
				if (selected == null) {
					selected = new ArrayList<IModule>();
				}
				selectedModuleMap.put(dbFile, selected);
			}
		} else {
			selected = new ArrayList<IModule>();
		}
		
		//System.out.println(dbFile + " => " + selected);
		selectedModules = selected;
		
	}

	protected boolean validate() {
		status = null;
		
		boolean valid = true;
		
//		String dbTextStr = dbSelector.getText();
//		setDbFile(dbTextStr);
		
		if (status == null) {
			if (dbFile == null) {
				status = createStatus(IStatus.INFO, "Select a module list file");
				valid = false;
			} else {
				if (!dbFile.isFile()) {
					status = createStatus(IStatus.WARNING, "Module list will be created; "
							+ "selected " +
							selectedModules.size() + " of " +
							discoveredModules.size() + " modules"
							);
				}
			}
		}

		if (status == null) {
			if (discoveredModules.isEmpty()) {
				status = createStatus(IStatus.WARNING, "No modules recognized");
			} else {
				status = createStatus(IStatus.INFO, "Selected " +
						selectedModules.size() + " of " +
						discoveredModules.size() + " modules");
			}
		}
		
		if (statusListener != null)
			statusListener.statusUpdated(status);
		
		return valid;
//		updateStatus(status == null ? Status.OK_STATUS : status);
//		if (getButton(OK) != null)
//			getButton(OK).setEnabled(valid);
		
	}

	private IStatus createStatus(int error, String string) {
		return new Status(error, "v9t9-gui", string);
	}

	public void refresh() {
		discoveredModules.clear();
		
		URI databaseURI = dbFile != null ? dbFile.toURI() : nonameDatabaseURI;
		
		IModuleDetector detector = machine.getModuleDetector();
		for (URI uri : machine.getRomPathFileLocator().getSearchURIs()) {
			File dir;
			try {
				dir = new File(uri);
			} catch (IllegalArgumentException e) {
				// ignore
				continue;
			}
					
			detector.scan(dir);
		}

		for (Map.Entry<String, List<IModule>> ent : detector.gatherDuplicatesByMD5().entrySet()) {
			IModule module = ent.getValue().get(0);
			
			IModule exist = machine.getModuleManager().findModuleByNameAndHash(module.getName(), module.getMD5());
			if (exist == null) {
				module.removePathsFromFiles(machine.getRomPathFileLocator());
				discoveredModules.add(module);
			} else if (exist.getDatabaseURI().equals(databaseURI)) {
				// keep my version in case name,etc. were edited
				discoveredModules.add(exist);
			} else if (showAllModules) {
				discoveredModules.add(exist);
			}
		}
		
		viewer.setAllChecked(false);
		viewer.refresh();
		
		checkedColumn.getColumn().pack();
		nameColumn.getColumn().pack();
		
		reset();
		
		filterText.setFocus();
	}
	
	public IStatus getStatus() {
		return status;
	}

	protected void reset() {
		if (selectedModules == null)
			return;
		
		List<IModule> orig = origModuleMap.get(dbFile);
		if (orig == null) {
			populateUnique();
		} else {
			// select the modules previously assigned to this database,
			// sorting them to the top
			selectedModules.clear();
			
			Map<String, IModule> discMap = new HashMap<String, IModule>();
			for (IModule disc : discoveredModules) {
				discMap.put(disc.getMD5(), disc);
			}
			
			for (IModule module : orig) {
				boolean found = false;
				IModule disc = discMap.get(module.getMD5());
				if (disc != null) {
					disc.setName(module.getName()); 	// preserve user edits
					disc.setDatabaseURI(dbFile.toURI());
					selectedModules.add(disc);
					disc.removePathsFromFiles(machine.getRomPathFileLocator());
					found = true;
				}
				if (!found) {
					module = module.copy();
					module.removePathsFromFiles(machine.getRomPathFileLocator());
					module.setDatabaseURI(dbFile.toURI());
					selectedModules.add(module);
					if (!discoveredModules.contains(module)) 
						discoveredModules.add(module);
				}
			}
		}
		
		viewer.refresh();
		viewer.setCheckedElements(selectedModules.toArray());
		
		validate();
	}
		
	/**
	 * Select all the modules not already selected, e.g.
	 * to populate a new module database.
	 */
	protected void populateUnique() {
		if (selectedModules == null)
			return;
		
		selectedModules.clear();
		
		for (IModule module : discoveredModules) {
			IModule exist = machine.getModuleManager().findModuleByNameAndHash(
					module.getName(), module.getMD5());
			if (exist == null) {
				selectedModules.add(module);
			}
		}

		viewer.refresh();
		viewer.setCheckedElements(selectedModules.toArray());
		
		validate();
	}
	
//	private void selectThisDatabaseModules() {
//		URI thisDB = dbFile != null ? dbFile.toURI() : nonameDatabaseURI;
//		selectedModules.clear();
//		for (IModule module : discoveredModules) {
//			if (module.getDatabaseURI().equals(thisDB)) {
//				selectedModules.add(module);
//			}
//		}
//		treeViewer.setCheckedElements(selectedModules.toArray());
//	}
	
	public File getModuleDatabase() {
		return dbFile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
//	@Override
//	protected void okPressed() {
//		File dbase = getModuleDatabase();
//		if (dbase != null) {
//			if (!saveModuleList(dbase))
//				return;
//		}
//		
//		super.okPressed();
//	}

	public boolean saveModuleLists() {
		for (Map.Entry<File, List<IModule>> ent : selectedModuleMap.entrySet()) {
			if (!saveModuleList(ent.getKey())) {
				return false;
			}
		}
		
		IProperty moduleList = Settings.get(machine, IModuleManager.settingUserModuleLists);
		List<String> ents = moduleList.getList();

		ents.clear();
		for (Map.Entry<File, List<IModule>> ent : selectedModuleMap.entrySet()) {
			File moduleDB = ent.getKey();
			URI uri = machine.getRomPathFileLocator().findFile(moduleDB.getName());
			if (uri != null && uri.equals(moduleDB.toURI())) {
				ents.add(moduleDB.getName());
			} else {
				ents.add(moduleDB.getAbsolutePath());
			}
		}
		moduleList.setList(ents);
		
		machine.getModuleManager().reloadDatabase();
		return true;
	}
	
	protected boolean saveModuleList(File dbase) {
		// see if any changes...
		List<IModule> newList = selectedModuleMap.get(dbase);
		if (newList == null)
			return true;
		
		if (!dirtyModuleLists.contains(dbase.toURI()) && newList.equals(origModuleMap.get(dbase))) {
			return true;
		}
		
		
		if (dbase.exists()) {
			if (!dbase.canWrite()) {
				if (false == MessageDialog.openQuestion(getShell(), "Cannot write file", 
						"The module list has changes but '"+dbase+"' is read-only.  Make writeable and overwrite?")) {
					return true;
				}
				dbase.setWritable(true);
			}
			else if (false == MessageDialog.openQuestion(getShell(), "File exists", 
					"V9t9 detected changes to the file '" + dbase + "'.  Overwrite it?")) {
				return true;
			}
		}
		try {
			try {
				dbase.getParentFile().mkdirs();
				dbase.renameTo(new File(dbase.getParentFile(), dbase.getName() + "~"));
				ModuleDatabase.saveModuleListAndClose(machine.getMemory(),
						new FileOutputStream(dbase),
						null,
						newList);
			} catch (IOException ex) {
				throw new NotifyException(this, "Failed to create list file: " + dbase, ex);
			}
		} catch (NotifyException e1) {
			e1.printStackTrace();
			String msg = e1.getMessage();
			if (e1.getCause() != null)
				msg += "\n\n" + e1.getCause(); 
						
			MessageDialog.openError(getShell(), "Cannot write", msg);
			return false;
		}
		return true;
	}
	
}
