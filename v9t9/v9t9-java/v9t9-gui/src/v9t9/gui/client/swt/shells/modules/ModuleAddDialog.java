/*
  ModuleAddDialog.java

  (c) 2013 Ed Swartz

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
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.ejs.gui.common.DirectoryDialogHelper;

import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleManager;
import v9t9.common.modules.ModuleDatabase;
import v9t9.gui.client.swt.SwtWindow;
import ejs.base.properties.IProperty;
import ejs.base.settings.DialogSettingsWrapper;
import ejs.base.settings.ISettingSection;

/**
 * @author ejs
 *
 */
public class ModuleAddDialog extends StatusDialog {

	private static final String SECTION_MODULE_ADDER = "module.adder";
	
	private static final String LAST_MODULE_DIR = "last.dir";
	private static final String LAST_DB_FILE = "last.db";
	
	private IMachine machine;
	private Text dirText;
	private Combo dbSelector;
	
	private File dir;
	private CheckboxTableViewer discoveredList;
	private List<IModule> selectedModules = new ArrayList<IModule>();
	private List<IModule> discoveredModules = new ArrayList<IModule>();
	private File dbFile;

	private ISettingSection settings;

	private TableViewerColumn nameColumn;

	private TableViewerColumn filesColumn;

	/**
	 * @param parentShell
	 * @param window 
	 * @param machine 
	 */
	protected ModuleAddDialog(Shell parentShell, IMachine machine, SwtWindow window) {
		super(parentShell);
		this.machine = machine;
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		settings = machine.getSettings().getMachineSettings().getHistorySettings().findOrAddSection(SECTION_MODULE_ADDER);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText("Add Modules");
		super.configureShell(newShell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
	 */
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return new DialogSettingsWrapper(settings);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		Label label;
		label = new Label(composite, SWT.WRAP);
		label.setText("V9t9 can scan a directory for module ROMs and save a list for the selector.");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
		
		/*spacer*/ new Label(composite, SWT.NONE);

		Composite threeColumns = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(threeColumns);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(threeColumns);
		
		createBrowseRow(threeColumns);

		createDiscoveredList(threeColumns);
		
		createDatabaseRow(threeColumns);

		boolean restored = false;
		String last = settings.get(LAST_MODULE_DIR);
		if (last != null) {
			dirText.setText(last);
			restored = true;
		}
		
		last = settings.get(LAST_DB_FILE);
		if (last != null) {
			dbSelector.setText(last);
			restored = true;
		}

		if (restored) {
			refresh();
		}
		
		composite.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (dbFile != null)
					settings.put(LAST_DB_FILE, dbFile.toString());
				if (dir != null)
					settings.put(LAST_MODULE_DIR, dir.toString());
			}
		});

		return composite;
	}

	static class DiscoveredModuleLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			IModule module = (IModule) element;
			if (columnIndex == 0) {
				return module.getName();
			} else {
				StringBuilder sb = new StringBuilder();
				for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
					if (info.getFilename() != null) {
						if (sb.length() > 0)
							sb.append(", ");
						sb.append(info.getFilename());
					}
					if (info.getFilename2() != null) {
						if (sb.length() > 0)
							sb.append(", ");
						sb.append(info.getFilename2());
					}
				}
				return sb.toString();
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
	
	/**
	 * @param composite
	 */
	private void createDiscoveredList(Composite composite) {
		Composite listArea = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(listArea);
		GridDataFactory.fillDefaults().grab(true, true).span(3, 1).applyTo(listArea);
		
		Label label;
		
		/*spacer*/ label = new Label(listArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(label);
		
		label = new Label(listArea, SWT.NONE);
		label.setText("Modules found:");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(label);
		
		discoveredList = CheckboxTableViewer.newCheckList(listArea, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(discoveredList.getControl());
		
		discoveredList.getTable().setHeaderVisible(true);
		
		nameColumn = new TableViewerColumn(discoveredList, SWT.LEFT | SWT.RESIZE);
		nameColumn.getColumn().setText("Name");
		
		filesColumn = new TableViewerColumn(discoveredList, SWT.LEFT | SWT.RESIZE);
		filesColumn.getColumn().setText("Files");
		
		discoveredList.setLabelProvider(new DiscoveredModuleLabelProvider());
		discoveredList.setContentProvider(new ArrayContentProvider());
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
		
		final Button selectUniqueButton = new Button(buttons, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(selectUniqueButton);
		selectUniqueButton.setText("Select Unique");

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
		selectUniqueButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectUniqueModules();
				validate();
			}
		});
		
		discoveredList.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					selectedModules.add((IModule) event.getElement());
				} else {
					selectedModules.remove((IModule) event.getElement());
				}
				validate();
			}
		});
	}

	/**
	 * @param composite
	 */
	protected void createBrowseRow(Composite browseRow) {
		Label label;
		
		label = new Label(browseRow, SWT.WRAP);
		label.setText("Directory:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);
		
		dirText = new Text(browseRow, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(dirText);
		
		dirText.setToolTipText("Specify a directory containing e.g. *.bin files.");
		
		final Button browseButton = new Button(browseRow, SWT.PUSH);
		browseButton.setText("Browse...");
		GridDataFactory.fillDefaults().hint(128, -1).applyTo(browseButton);
		
		
		dirText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		dirText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == '\r') {
					e.doit = false;
					refresh();
				}
			}
		});
		dirText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!new File(dirText.getText()).equals(dbFile))
					refresh();
			}
		});
		
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
				DirectoryDialogHelper.setFilterPathToExistingDirectory(dialog, dirText.getText());
				dialog.setText("Browse Module Directory");
				String path = dialog.open();
				if (path != null) {
					dirText.setText(path);
					refresh();
				}
			}
		});
	}
	/**
	 * @param composite
	 */
	protected void createDatabaseRow(Composite dbRow) {
		Label label;
		
		label = new Label(dbRow, SWT.WRAP);
		label.setText("List file:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(label);
		
		dbSelector = new Combo(dbRow, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(dbSelector);
		dbSelector.setToolTipText("Specify a file to record the list of discovered modules.");
		
		final Button browseButton = new Button(dbRow, SWT.PUSH);
		browseButton.setText("Browse...");
		GridDataFactory.fillDefaults().hint(128, -1).applyTo(browseButton);
		
		dbSelector.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setText("Select Module List");
				dialog.setFileName("modules.xml");
				dialog.setFilterExtensions(new String[] { "*.xml" });
				dialog.setFilterNames(new String[] { "Module Lists (*.xml)" });
				String path = dialog.open();
				if (path != null) {
					dbSelector.add(path);
					dbSelector.select(dbSelector.indexOf(path));
					
					refresh();
				}
			}
		});
		
		IProperty modList = machine.getSettings().get(IModuleManager.settingUserModuleLists);
		List<String> mods = modList.getList();
		dbFile = null;
		for (String mod : mods) {
			if (dbFile == null)
				dbFile = new File(mod);
			dbSelector.add(mod);
		}
	}

	protected void validate() {
		IStatus status = null;
		
		boolean valid = true;
		
		String dbTextStr = dbSelector.getText();
		dbFile = new File(dbTextStr);
		if (status == null) {
			if (dbTextStr.isEmpty()) {
				status = createStatus(IStatus.INFO, "Select a module list file");
				valid = false;
			} else {
				if (!dbFile.isFile()) {
					status = createStatus(IStatus.WARNING, "Module list will be created");
				}
			}
		}

		String dirTextStr = dirText.getText();
		if (dirTextStr.isEmpty()) {
			if (!dbFile.exists())
				status = createStatus(IStatus.INFO, "Enter or browse to a directory");
			else
				status = createStatus(IStatus.INFO, "Leave directory blank to add module list");
		} else {
			File dir = new File(dirTextStr);
			if (!dir.isDirectory()) {
				status = createStatus(IStatus.ERROR, "Directory does not exist");
				valid = false;
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
		
		updateStatus(status == null ? Status.OK_STATUS : status);
		if (getButton(OK) != null)
			getButton(OK).setEnabled(valid);
		
	}
	/**
	 * @param error
	 * @param string
	 * @return
	 */
	private IStatus createStatus(int error, String string) {
		return new Status(error, "v9t9-gui", string);
	}

	/**
	 * 
	 */
	protected void refresh() {
		discoveredModules.clear();
		selectedModules.clear();
		
		dir = new File(dirText.getText());
		URI databaseURI = dbFile != null ? dbFile.toURI() : URI.create("modules.xml");
		
		Collection<IModule> ents = machine.scanModules(databaseURI, dir);
		discoveredModules.addAll(ents);

		discoveredList.setAllChecked(false);
		discoveredList.refresh();
		
		selectUniqueModules();
		
		nameColumn.getColumn().pack();
		filesColumn.getColumn().pack();
		
		validate();
	}
	
	/**
	 * 
	 */
	private void selectUniqueModules() {
		URI thisDB = dbFile != null ? dbFile.toURI() : null;
		selectedModules.clear();
		for (IModule module : discoveredModules) {
			IModule match = machine.getModuleManager().findModuleByName(module.getName(), false);
			if (match == null || match.getDatabaseURI().equals(thisDB)) {
				discoveredList.setChecked(module, true);
				selectedModules.add(module);
			}
		}
	}

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
	@Override
	protected void okPressed() {
		File dbase = getModuleDatabase();
		if (dbase != null) {
			if (!saveModuleList(dbase))
				return;
		}
		
		super.okPressed();
	}

	/**
	 * @param dbase
	 */
	protected boolean saveModuleList(File dbase) {
		if (dbase.exists()) {
			if (false == MessageDialog.openConfirm(getShell(), "File exists", 
					"The file " + dbase + " already exists.  Overwrite?")) {
				return false;
			}
		}
		try {
			try {
				ModuleDatabase.saveModuleListAndClose(machine.getMemory(),
						new FileOutputStream(dbase),
						null,
						selectedModules);
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
