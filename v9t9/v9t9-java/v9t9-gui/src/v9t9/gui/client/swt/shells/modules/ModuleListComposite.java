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
import java.util.Arrays;
import java.util.Collection;
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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;

import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleDetector;
import v9t9.common.modules.IModuleManager;
import v9t9.common.modules.ModuleDatabase;
import v9t9.common.settings.Settings;
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
	
	private CheckboxTableViewer discoveredList;
	private List<IModule> discoveredModules = new ArrayList<IModule>();
	private File dbFile;
	
	private Map<File, List<IModule>> origModuleMap = new HashMap<File, List<IModule>>();
	private Map<File, List<IModule>> selectedModuleMap = new LinkedHashMap<File, List<IModule>>();
	private List<IModule> selectedModules = null;

	private ISettingSection settings;

	private TableViewerColumn nameColumn;

	private TableViewerColumn filesColumn;

	private IStatus status;

	protected boolean showAllModules;

	private URI nonameDatabaseURI = URI.create("modules.xml");

	private Button showAllButton;

	private IStatusListener statusListener;

	private ModuleNameEditingSupport editingSupport;

	private ArrayList<URI> dirtyModuleLists;

	private HashMap<IModule, String> discoveredFiles;

	private ColumnComparator comparator;

	public ModuleListComposite(Composite parent, IMachine machine, SwtWindow window,
			IStatusListener statusListener) {
		super(parent, SWT.NONE);
		this.statusListener = statusListener;
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		this.machine = machine;
		
		settings = machine.getSettings().getMachineSettings().getHistorySettings().findOrAddSection(SECTION_MODULE_MANAGER);

		discoveredFiles = new HashMap<IModule, String>();
		
		Composite composite = this;
		
		/*spacer*/ new Label(composite, SWT.NONE);

		Composite threeColumns = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(threeColumns);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(threeColumns);
		
		createDatabaseRow(threeColumns);
		
		createDiscoveredList(threeColumns);

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
			
		dbSelector.setSelection(new StructuredSelection(sel));
		
		refresh();
		
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

	class DiscoveredModuleLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		public DiscoveredModuleLabelProvider() {
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			IModule module = (IModule) element;
			if (columnIndex == 0) {
				return module.getName();
			} else {
				return discoveredFiles.get(module);
			}
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
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
				// Same column as last sort; toggle the direction
				direction = -direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = 1;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			IModule p1 = (IModule) e1;
			IModule p2 = (IModule) e2;
			int rc = 0;
			switch (propertyIndex) {
			case 0:
				rc = p1.getName().compareTo(p2.getName());
				break;
			case 1:
			{
				String dis1 = discoveredFiles.get(p1);
				String dis2 = discoveredFiles.get(p2);
				rc = dis1.compareTo(dis2);
				break;
			}
			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction < 0) {
				rc = -rc;
			}
			return rc;
		}

	}

	/**
	 * @param composite
	 */
	private void createDiscoveredList(Composite composite) {

		Label label;
		
		/*spacer*/ label = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(label);
		
		label = new Label(composite, SWT.NONE);
		label.setText("Select modules for list:");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(label);
		
		Composite listArea = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(listArea);
		GridDataFactory.fillDefaults().grab(true, true).span(3, 1).indent(12, 0).applyTo(listArea);
		
		discoveredList = CheckboxTableViewer.newCheckList(listArea, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(discoveredList.getControl());
		
		discoveredList.getTable().setHeaderVisible(true);
		

		comparator = new ColumnComparator();
		discoveredList.setComparator(comparator);
		
		nameColumn = new TableViewerColumn(discoveredList, SWT.LEFT | SWT.RESIZE);
		nameColumn.getColumn().addSelectionListener(createColumnSelectionListener(nameColumn.getColumn(), 0));
		nameColumn.getColumn().setText("Name");
		
		filesColumn = new TableViewerColumn(discoveredList, SWT.LEFT | SWT.RESIZE);
		filesColumn.getColumn().addSelectionListener(createColumnSelectionListener(filesColumn.getColumn(), 1));
		filesColumn.getColumn().setText("Files");
		
		discoveredList.setLabelProvider(new DiscoveredModuleLabelProvider());
		discoveredList.setContentProvider(new ArrayContentProvider());
		
		dirtyModuleLists = new ArrayList<URI>();
		editingSupport = new ModuleNameEditingSupport(discoveredList, dirtyModuleLists);
		nameColumn.setEditingSupport(editingSupport);
		editingSupport.setCanEdit(true);
		
		
		discoveredList.setInput(discoveredModules);
		
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
				discoveredList.setAllChecked(true);
				selectedModules.clear();
				selectedModules.addAll(discoveredModules);
				validate();
			}
		});
		selectNoneButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				discoveredList.setAllChecked(false);
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
		
		discoveredList.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				IModule module = (IModule) event.getElement();
				if (event.getChecked()) {
					selectedModules.add(module);
				} else {
					selectedModules.remove(module);
				}
				validate();
			}
		});
		

		showAllButton = new Button(listArea, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(showAllButton);
		
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

	}

	private SelectionListener createColumnSelectionListener(final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				discoveredList.getTable().setSortDirection(dir);
				discoveredList.getTable().setSortColumn(column);
				discoveredList.refresh();
			}
		};
		return selectionAdapter;
	}

	/**
	 * @param composite
	 */
	protected void createDatabaseRow(Composite dbRow) {
		Label label;
		
		label = new Label(dbRow, SWT.WRAP);
		label.setText("List file:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);
		
		dbSelector = new ComboViewer(dbRow, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(dbSelector.getControl());
		dbSelector.getControl().setToolTipText("Specify a file to record the list of discovered modules.");
		
		final Button browseButton = new Button(dbRow, SWT.PUSH);
		browseButton.setText("Browse...");
		GridDataFactory.fillDefaults().hint(128, -1).applyTo(browseButton);
		
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
				setDbFile((File) o);
				
				validate();
				reset();
			}
		});
		
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
		
	}

	/**
	 * @param mod
	 */
	private void setDbFile(File dbFile) {
		this.dbFile = dbFile;
		
		List<IModule> selected = selectedModuleMap.get(dbFile);
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
		
		System.out.println(dbFile + " => " + selected);
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
		discoveredFiles.clear();
		
		URI databaseURI = dbFile != null ? dbFile.toURI() : nonameDatabaseURI;
		
		for (URI uri : machine.getRomPathFileLocator().getSearchURIs()) {
			File dir;
			try {
				dir = new File(uri);
			} catch (IllegalArgumentException e) {
				// ignore
				continue;
			}
					
			IModuleDetector detector = machine.createModuleDetector(databaseURI);
			Collection<IModule> ents = detector.scan(dir);
			
			for (IModule module : ents) {
				IModule exist = machine.getModuleManager().findModuleByName(module.getName(), true);
				if (exist == null) {
					module.removePathsFromFiles(machine.getRomPathFileLocator());
					discoveredModules.add(module);
				} else if (exist.getDatabaseURI().equals(databaseURI)) {
					if (Arrays.equals(exist.getMemoryEntryInfos(), module.getMemoryEntryInfos()))
						discoveredModules.add(exist);
					else
						discoveredModules.add(module);	// in case detection changed
				} else if (showAllModules) {
					discoveredModules.add(exist);
				}
				
				StringBuilder sb = new StringBuilder();
				for (File file : module.getUsedFiles(machine.getRomPathFileLocator())) {
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(file.getName());
				}
				discoveredFiles.put(module, sb.toString());
				

			}
		}

		discoveredList.setAllChecked(false);
		discoveredList.refresh();
		
		nameColumn.getColumn().pack();
		filesColumn.getColumn().pack();
		
		reset();
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
			selectedModules.clear();
			List<IModule> matched = new ArrayList<IModule>();
			for (IModule module : orig) {
				boolean found = false;
				for (IModule disc : discoveredModules.toArray(new IModule[discoveredModules.size()])) {
					if (module.getName().equals(disc.getName())) {
						selectedModules.add(disc);
						disc.removePathsFromFiles(machine.getRomPathFileLocator());
						discoveredModules.remove(disc);
						matched.add(disc);
						found = true;
						break;
					}
				}
				if (!found) {
					module.removePathsFromFiles(machine.getRomPathFileLocator());
					matched.add(module);
					selectedModules.add(module);
				}
			}
			discoveredModules.addAll(0, matched);
		}
		
		discoveredList.refresh();
		discoveredList.setCheckedElements(selectedModules.toArray());
		
		validate();
	}
		
	public void populateUnique() {
		if (selectedModules == null)
			return;
		
		selectedModules.clear();
		
		for (IModule module : discoveredModules) {
			IModule exist = machine.getModuleManager().findModuleByName(module.getName(), true);
			if (exist == null) {
				selectedModules.add(module);
			}
		}

		discoveredList.refresh();
		discoveredList.setCheckedElements(selectedModules.toArray());
		
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
//		discoveredList.setCheckedElements(selectedModules.toArray());
//	}
	
	/**
	 * @return the discoveredModules
	 */
	public List<IModule> getDiscoveredModules() {
		return discoveredModules;
	}

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
