/*
  ModuleSelector.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.modules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.ejs.gui.common.FontUtils;
import org.ejs.gui.common.SwtDialogUtils;

import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.files.IPathFileLocator.IPathChangeListener;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleManager;
import v9t9.common.modules.ModuleDatabase;
import v9t9.common.modules.ModuleInfo;
import v9t9.common.modules.ModuleInfoDatabase;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.client.swt.ISwtVideoRenderer;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.bars.ImageCanvas;
import v9t9.gui.client.swt.imageimport.ImageUtils;
import v9t9.gui.client.swt.shells.IToolShellFactory;
import v9t9.gui.client.swt.shells.LazyImageLoader;
import v9t9.gui.client.swt.shells.LazyImageLoader.ILazyImageAdjuster;
import v9t9.gui.client.swt.shells.LazyImageLoader.ILazyImageLoadedListener;
import v9t9.gui.client.swt.wizards.SetupWizard;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;

/**
 * Dialog for examining, running, and editing modules
 * @author ejs
 *
 */
public class ModuleSelector extends Composite {
	private static final Logger logger = Logger.getLogger(ModuleSelector.class);
	
	public static final String MODULE_SELECTOR_TOOL_ID = "module.selector";
	static String lastFilter;

	private static final String SECTION_MODULE_SELECTOR = "ModuleSelector";
	private static final String SHOW_MISSING_MODULES = "ShowMissingModules";
	private static final String SORT_ENABLED = "SortEnabled";
	private static final String SORT_DIRECTION = "SortDirection";

	final int MAX = 64;

	private static boolean allowEditing = false;
	
	static String NAME_PROPERTY = "name";
	static String[] NAME_PROPERTY_ARRAY = { NAME_PROPERTY };
	static final int NAME_COLUMN = 0;
	static final int FILE_COLUMN = 1;
	

	private ISettingSection dialogSettings; 

	private TreeViewer viewer;
	private TreeColumn nameColumn;
	private IModule selectedModule;
	private Composite buttonBar;
	private final IMachine machine;
	private Button switchButton;
	private Font tableFont;
	private final IModuleManager moduleManager;
	private Text filterText;

	private Map<IModule, Boolean> knownStates = new IdentityHashMap<IModule, Boolean>();
	private Map<IModule, Boolean> currentFetches = new IdentityHashMap<IModule, Boolean>();
	private boolean showMissingModules;
	protected boolean sortModules;
	protected int sortDirection;
	
	private ViewerFilter filteredSearchFilter = new FilteredSearchFilter();
	private final SwtWindow window;
	private boolean wasPaused;
	private ExecutorService executor;
	private ViewerUpdater viewerUpdater;

	private ViewerFilter existingModulesFilter = new ExistingModulesFilter(this);
	protected boolean isEditing;
	protected Collection<URI> dirtyModuleLists = new HashSet<URI>();
	protected boolean isModuleInfoDirty = false;
	//private List<Object> moduleList;
	private Map<URI, Collection<IModule>> moduleMap;
	private LazyImageLoader lazyImageLoader;
	private Image stockModuleImage;
	private URI builtinImagesURI;
	private ILazyImageAdjuster moduleImageResizer;

//	private Button addButton;

	private Image modulesListImage;
	private Image noModuleImage;

	private Map<String, Image> stockImages = new HashMap<String, Image>();

	protected IProperty modDbList;

	private IPathChangeListener pathChangeListener;

	private ModuleNameEditingSupport editingSupport;

	private ArrayList<Object> flatModuleList;

	static class FilteredSearchFilter extends ViewerFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#isFilterProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean isFilterProperty(Object element, String property) {
		return ModuleSelector.NAME_PROPERTY.equals(property);
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (ModuleSelector.lastFilter != null) {
			// note: instanceof excludes "<No module>" entry too
			if (element instanceof URI)
				return true;
			if (false == element instanceof IModule)
				return false;
			IModule mod = (IModule) element;
			String lowSearch = ModuleSelector.lastFilter.toLowerCase();
			return mod.getName().toLowerCase().contains(lowSearch)
					|| mod.getKeywords().contains(lowSearch)
					|| ("auto-start".startsWith(lowSearch) && mod.isAutoStart());
		}
		return true;
	}
}

	/**
	 * @param window 
	 * 
	 */
	public ModuleSelector(Shell shell, IMachine machine_, IModuleManager moduleManager_, SwtWindow window_) {
		super(shell, SWT.NONE);
		this.moduleManager = moduleManager_;
		this.window = window_;
		this.machine = machine_;
		
		modDbList = machine.getSettings().get(IModuleManager.settingUserModuleLists);
		
		executor = Executors.newFixedThreadPool(4);
		
		viewerUpdater = new ViewerUpdater(this);
		
		dialogSettings = machine.getSettings().getMachineSettings().getHistorySettings().findOrAddSection(SECTION_MODULE_SELECTOR);
		
		shell.setText("Module Selector");
		
		wasPaused = machine.setPaused(true);

		GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(this);
		
		
		createSearchFilter();
		
		
		viewer = createTable();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
		
		final Button showUnloadable = new Button(this, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(showUnloadable);
		
		showUnloadable.setText("Show missing modules");
		showUnloadable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showMissingModules = showUnloadable.getSelection();
				dialogSettings.put(SHOW_MISSING_MODULES, showMissingModules);
				viewer.refresh(true);
				updateFilter(lastFilter);
				viewer.expandAll();
			}
		});
		
		showMissingModules = dialogSettings.getBoolean(SHOW_MISSING_MODULES);
		showUnloadable.setSelection(showMissingModules);
		

		if (allowEditing) {
			final Button enableEdit = new Button(this, SWT.CHECK);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(enableEdit);
			
			enableEdit.setText("Edit module list");
			enableEdit.setToolTipText("When enabled, allow editing names and screenshots in user modules");
			enableEdit.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (isEditing) {
						promptSave();
					}
					isEditing = enableEdit.getSelection();
					editingSupport.setCanEdit(isEditing);
					
					viewer.refresh();
				}
			});
		}
		
		buttonBar = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(4).equalWidth(false).applyTo(buttonBar);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonBar);
		
		final Button configureButton = new Button(buttonBar, SWT.PUSH | SWT.NO_FOCUS);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(128, -1).applyTo(configureButton);
		configureButton.setText("Setup...");
		configureButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SetupWizard wizard = new SetupWizard(machine, window, SetupWizard.Page.PATHS);
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				dialog.setPageSize(SWT.DEFAULT, 500);
	        	int ret = dialog.open();
	        	
	        	if (ret == Window.OK)
	        		reloadModules(null);
			}
		});
		
//		addButton = new Button(buttonBar, SWT.PUSH);
//		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(128, -1).applyTo(addButton);
//		
//		addButton.setText("Add...");
//		addButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				ModuleAddDialog dialog = new ModuleAddDialog(window.getShell(), machine, window);
//				int ret = dialog.open();
//				if (ret == Window.OK) {
//					List<String> curDbs = modDbList.getList();
//					final URI moduleDatabaseURI = dialog.getModuleDatabase().toURI();
//					if (!curDbs.contains(dialog.getModuleDatabase().getAbsolutePath())
//							&& !curDbs.contains(moduleDatabaseURI.toString())) {
//						curDbs.add(moduleDatabaseURI.toString());
//						loadModuleList(moduleDatabaseURI.toString());
//						modDbList.setList(curDbs);
//					}
//					
//					reloadModules(moduleDatabaseURI);
//				}
//			}
//		});
//		
//		
		
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
		
		viewerUpdater.start();
		
		pathChangeListener = new IPathChangeListener() {
			
			@Override
			public void pathsChanged() {
				if (!viewer.getTree().isDisposed()) {
					viewer.getTree().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (!viewer.getTree().isDisposed()) {
								synchronized (knownStates) {
									knownStates.clear();
								}
								viewerUpdater.postRefresh();
							}
						}
					});
				}
			}
		};
		
		machine.getRomPathFileLocator().addListener(pathChangeListener);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				machine.setPaused(wasPaused);
				machine.getRomPathFileLocator().removeListener(pathChangeListener);
			}
		});
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				executor.shutdownNow();
				viewerUpdater.interrupt();
				try {
					viewerUpdater.join(1000);
				} catch (InterruptedException e1) {
				}
			}
		});
	
		if (allowEditing) {
			addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					if (isEditing)
						saveModules();
				}
			});
		}
		
	}

	

	/**
	 * 
	 */
	protected void reloadModules(URI moduleDatabaseURI) {
		revertModules();
		synchronized (knownStates) {
			knownStates.clear();
		}
		viewer.refresh(true);
		if (moduleDatabaseURI != null) {
			viewer.expandToLevel(moduleDatabaseURI, 2);
			viewer.setSelection(new StructuredSelection(moduleDatabaseURI));
		} else {
			viewer.expandToLevel(2);
		}
		
	}



	/**
	 * 
	 */
	protected void promptSave() {
		if (!dirtyModuleLists.isEmpty()) {
			boolean doSave = MessageDialog.openQuestion(getShell(), "Module Changes", 
					"Save changes to module list(s)?");
			if (doSave) {
				saveModules();
			} else {
				moduleManager.reloadDatabase();
			}
		}

	}



	/**
	 * 
	 */
	protected void saveModules() {
		if (isModuleInfoDirty && !dirtyModuleLists.isEmpty()) {
			try {
				URL databaseURL = machine.getModuleManager().getModuleInfoDatabase().getDatabaseURL();
				if (!databaseURL.getProtocol().equals("file")) {
					databaseURL = URIUtil.append(dirtyModuleLists.iterator().next(), ModuleInfoDatabase.NAME).toURL(); 
					window.getEventNotifier().notifyEvent(null, Level.WARNING, 
							"Cannot modify original info; saving module info changes to " + databaseURL);
				}
				OutputStream os = new FileOutputStream(databaseURL.getPath());
				machine.getModuleManager().getModuleInfoDatabase().saveModuleInfoAndClose(os);
			} catch (IOException e) {
				window.getEventNotifier().notifyEvent(null, Level.ERROR, "Failed to save module info database: " + e.getMessage());
			}
		}

		for (URI modDbUri : dirtyModuleLists ) {
			saveModules(modDbUri);
		}
		dirtyModuleLists.clear();
		isModuleInfoDirty = false;
	}



	/**
	 * 
	 */
	private void createSearchFilter() {
		
		Composite comp = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(2).equalWidth(false).applyTo(comp);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comp);

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
				viewer.setSelection(new StructuredSelection(moduleManager.getLoadedModules()), true);
			}
		});
	}

	protected void refreshFilters() {
		viewer.setFilters(new ViewerFilter[] { 
				existingModulesFilter,
				filteredSearchFilter
			}
		);
	}

	protected void initFilter(String text) {
		String curText = filterText.getText(); 
		if (curText.isEmpty()) {
			filterText.setText(text != null ? text : "");
			filterText.selectAll();
			filterText.setForeground(null);
		}
		
		refreshFilters();

		updateFilter(text);
		

		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!nameColumn.isDisposed())
					nameColumn.setWidth(viewer.getControl().getSize().x);
				
				final IModule[] loadedModules = moduleManager.getLoadedModules();
				viewer.setSelection(new StructuredSelection(loadedModules), true);
				
				if (loadedModules.length > 0) {
					SwtDialogUtils.revealOncePopulated(machine.getMachineTimer(), 0, 
							viewer, loadedModules[0]);
				}
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
			flatModuleList = null;
			viewer.refresh();
			if (lastFilter != null)
				viewer.expandAll();
		}
	}
	/**
	 * @param moduleManager
	 */
	protected TreeViewer createTable() {
		final TreeViewer viewer = new TreeViewer(this, SWT.READ_ONLY | SWT.BORDER | SWT.FULL_SELECTION);
		

		moduleImageResizer = new ILazyImageAdjuster() {
			
			@Override
			public Image adjustImage(Object element, URI imageURI, Image image) {
				//final boolean moduleLoadable = module == null || isModuleLoadable(module);
				//final String imageKey = imageURI.toString() + (moduleLoadable ? "" : "?grey");

				Rectangle bounds = image.getBounds();
				int sz = Math.max(bounds.width, bounds.height);
				
				if (sz > MAX) {
					sz = MAX;

					Image scaled = ImageUtils.scaleImage(getDisplay(), image, new Point(MAX, MAX), true, true);
					image.dispose();
					
//					if (!moduleLoadable) {
//						Image grey = ImageUtils.convertToGreyscale(display, scaled);
//						scaled.dispose();
//						scaled = grey;
//					}
						
					image = scaled;
				}
				return image;
			}
		};
		
		
		try {
			builtinImagesURI = machine.getRomPathFileLocator().resolveInsideURI(
					machine.getModel().getDataURL().toURI(), 
					"images/");
			logger.info("builtinImagesURI = " + builtinImagesURI);
		} catch (URISyntaxException e3) {
			logger.error("Failed to load stock module image", e3);
		} 
		
		stockModuleImage = loadStockImage("stock_module_missing.png");
		
		lazyImageLoader = new LazyImageLoader(viewer, executor, stockModuleImage);
		
		modulesListImage = EmulatorGuiData.loadImage(getDisplay(), "icons/module_list.png");
		noModuleImage = ImageUtils.scaleImage(getDisplay(),  
				EmulatorGuiData.loadImage(getDisplay(), "icons/stock_no_module.png"),
				new Point(MAX, MAX));
		
		viewer.setUseHashlookup(true);
		viewer.setComparer(new IElementComparer() {
			
			@Override
			public int hashCode(Object element) {
				return element.hashCode();
			}
			
			@Override
			public boolean equals(Object a, Object b) {
				return a == b 
						|| a.equals(b);
			}
		});
		
		Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		GridDataFactory.fillDefaults().grab(true,true).applyTo(tree);

		FontDescriptor desc = FontUtils.getFontDescriptor(
				JFaceResources.getFontRegistry().getBold(
						JFaceResources.DIALOG_FONT));
		tableFont = desc.createFont(getDisplay()); 
		tree.setFont(tableFont);

		nameColumn = new TreeColumn(tree, SWT.LEFT);
		nameColumn.setText("Name");

		final ModuleContentProvider contentProvider = new ModuleContentProvider();
		viewer.setContentProvider(contentProvider);
		
		CellLabelProvider cellLabelProvider = new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement()  instanceof URI) {
					URI uri = (URI) cell.getElement();
					String text = machine.getRomPathFileLocator().splitFileName(uri).second;
					
					cell.setText(text);
					cell.setImage(getModuleListImage());
				} else if (cell.getElement() instanceof IModule) {
					IModule module = (IModule) cell.getElement();
					
					String text = module.getName();
					if (module.isAutoStart())
						text += " (auto-start)";
					cell.setText(text);
					
					ModuleInfo info = module.getInfo();
					cell.setForeground(isModuleLoadable(module) ? null : viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
					cell.setImage(getOrLoadModuleImage(module, module, info != null ? info.getImagePath() : null));
				} else {
					cell.setText(String.valueOf(cell.getElement()));
					cell.setImage(getNoModuleImage());
				}
			}
		};
		
		viewer.setLabelProvider(cellLabelProvider);

		viewer.setColumnProperties(NAME_PROPERTY_ARRAY);
		
		viewer.setComparator(new ViewerComparator() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerComparator#isSorterProperty(java.lang.Object, java.lang.String)
			 */
			@Override
			public boolean isSorterProperty(Object element, String property) {
				return "name".equals(property);
			}
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (!sortModules)
					return 0;
				
				if (e1 instanceof IModule && e2 instanceof IModule) {
					IModule mod1 = (IModule) e1;
					IModule mod2 = (IModule) e2;
					boolean l1 = isModuleLoadable(mod1);
					boolean l2 = isModuleLoadable(mod2);
					if (l1 == l2)
						return sortDirection * mod1.getName().toLowerCase().compareTo(mod2.getName().toLowerCase());
					else if (l1)
						return -1;
					else
						return 1;
				} else if (e1 instanceof IModule) {
					return 1;
				} /* else if (e2 instanceof IModule) */ {
					return -1;
				}
			}	
		});
		
		selectedModule = null;
		
		addIterativeSearch(viewer, tree);

		sortModules = dialogSettings.getBoolean(SORT_ENABLED);
		sortDirection = dialogSettings.getInt(SORT_DIRECTION);
		
		moduleMap = new LinkedHashMap<URI, Collection<IModule>>();
		
		revertModules();
		viewer.setInput(new ModuleInput("No module",  moduleMap));
		viewer.expandToLevel(3);
		
		viewer.setSelection(new StructuredSelection(moduleManager.getLoadedModules()), true);

		tree.addKeyListener(new KeyAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == '\r' || e.keyCode == '\n') {
					switchModule(false);
					e.doit = false;
				}
				else if (e.keyCode == SWT.ARROW_LEFT) {
					if (selectedModule != null) {
						viewer.setSelection(new StructuredSelection(
								contentProvider.getParent(selectedModule)));
					}
				}
			}
		});
		
		TreeViewerColumn nameViewerColumn = new TreeViewerColumn(viewer, nameColumn);
		nameViewerColumn.setLabelProvider(cellLabelProvider);
		
		editingSupport = new ModuleNameEditingSupport(viewer, dirtyModuleLists);
		nameViewerColumn.setEditingSupport(editingSupport);
		

		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				for (Image image : stockImages.values())
					image.dispose();
				stockImages.clear();
				if (modulesListImage != null)
					modulesListImage.dispose();
				if (noModuleImage != null)
					noModuleImage.dispose();

				if (tableFont != null)
					tableFont.dispose();
				/*for (Image image : loadedImages.values()) {
					image.dispose();
				}*/
			}
		});

		lazyImageLoader.addListener(new ILazyImageLoadedListener() {
			
			@Override
			public void imageLoaded(Object element, URI imageURI, Image image) {
				if (element != null)
					viewerUpdater.post(element);
			}
		});
		

		return viewer;
	}



	/**
	 * 
	 */
	private void revertModules() {
		dirtyModuleLists.clear();
		moduleMap.clear();
		List<String> modDbs = modDbList.getList();
		modDbs = new ArrayList<String>(modDbs);		// copy -- no change intended
		//modDbs.add(0, moduleManager.getStockDatabaseURL().toString());
		for (String modDb : modDbs) {
			loadModuleList(modDb);
		}
		
//		moduleList = new ArrayList<Object>(Arrays.asList(moduleManager.getModules()));
//		moduleList.add(0, "<No module>");
//		viewer.setInput(moduleList);
		
	}



	/**
	 * @param modDb
	 */
	protected void loadModuleList(String modDb) {
		try {
			URI databaseURI = machine.getRomPathFileLocator().findFile(modDb);
			try {
				List<IModule> mods = moduleManager.readModules(databaseURI);
				moduleMap.put(databaseURI, mods);
			} catch (IOException e3) {
				moduleMap.put(databaseURI, new ArrayList<IModule>(1));
				machine.getEventNotifier().notifyEvent(null, Level.WARNING, 
						e3.getMessage());
			}
		} catch (Exception e3) {
			machine.getEventNotifier().notifyEvent(null, Level.WARNING, 
					e3.getMessage());
		}
	}



	/**
	 * @param viewer
	 * @param tree
	 * @param realModules
	 */
	protected void addIterativeSearch(final TreeViewer viewer, Tree tree) {
		tree.addKeyListener(new KeyAdapter() {
			StringBuilder search = new StringBuilder();
			int index = 0;
			IModule[] modules;
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (modules == null) {
					modules = moduleManager.getModules();
				}
				if (e.keyCode == '\b') {
					search.setLength(0);
					index = 0;
					e.doit = e.keyCode != '\b';
					modules = moduleManager.getModules();
				}
				else if (e.keyCode == SWT.ARROW_DOWN) {
					index++;
					e.doit = false;
				}
				else if (e.keyCode == SWT.ARROW_UP) {
					index--;
					e.doit = false;
				}
				else if (e.character >= 32 && e.character < 127) {
					search.append(e.character);
					e.doit = false;
				}
				else {
					return;
				}
				
				if (search.length() > 0) {
					int end = (index + modules.length - 1) % modules.length;
					for (int i = index; i != end; i = (i + 1) % modules.length) {
						IModule m = modules[i];
						String lowSearch = search.toString().toLowerCase();
						if (m.getName().toLowerCase().contains(lowSearch)
								|| m.getKeywords().contains(lowSearch)) {
							viewer.setSelection(new StructuredSelection(m), true);
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
		nameColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				e.doit = false;
				if (!sortModules) {
					sortModules = true;
					sortDirection = 1;
				} else {
					if (sortDirection == 1)
						sortDirection = -1;
					else
						sortModules = false;
				}
				dialogSettings.put(SORT_ENABLED, sortModules);
				dialogSettings.put(SORT_DIRECTION, sortDirection);
				flatModuleList = null;
				viewer.refresh();
			}
		});
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (false == obj instanceof IModule) {
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
				if (false == obj instanceof IModule) {
					selectedModule = null;
					if (!switchButton.isDisposed())
						switchButton.setEnabled(true);
					switchModule(false);
				}
				else if (obj instanceof IModule) {
					selectedModule = (IModule) obj;
					if (!switchButton.isDisposed())
						switchButton.setEnabled(true);
					switchModule(false);
				} else {
					if (!switchButton.isDisposed())
						switchButton.setEnabled(false);
				}
			}
		});
		
		viewer.getControl().addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent e) {
				final TreeItem item = viewer.getTree().getItem(
						viewer.getTree().toControl(new Point(e.x, e.y))
						);
				if (item != null && item.getData() instanceof IModule) {
					final IModule module = (IModule) item.getData();

					final Menu menu = new Menu(viewer.getControl());
					
					if (isEditing && window.getVideoRenderer() instanceof ISwtVideoRenderer) {
						final MenuItem mitem;
						mitem = new MenuItem(menu, SWT.NONE);
						mitem.setText("Assign module image from screenshot...");
						
						mitem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								assignModuleImage(module);
							}

						});
					}

					if (isEditing) {
						final MenuItem dlitem;
						dlitem = new MenuItem(menu, SWT.NONE);
						dlitem.setText("Delete entry");
						
						dlitem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								dirtyModuleLists.add(module.getDatabaseURI());
								moduleManager.removeModule(module);
								moduleMap.get(module.getDatabaseURI()).remove(module);
								viewer.remove(module);
							}
							
						});
					}
					
					final MenuItem ditem;
					ditem = new MenuItem(menu, SWT.NONE);
					ditem.setText("Module details...");
					
					ditem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							menu.dispose();
							showModuleDetails(module);
						}
					});

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
				else if (item != null && item.getData() instanceof URI) {
					final URI uri = (URI) item.getData();

					final Menu menu = new Menu(viewer.getControl());

					final MenuItem infoitem;
					infoitem = new MenuItem(menu, SWT.NONE);
					infoitem.setText(uri.toString());
					infoitem.setEnabled(false);
					
					final MenuItem copyitem;
					copyitem = new MenuItem(menu, SWT.NONE);
					copyitem.setText("Copy module list path");
						
					copyitem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							Clipboard clipboard = new Clipboard(getDisplay());
							TextTransfer textTransfer = TextTransfer.getInstance();
							URLTransfer urlTransfer = URLTransfer.getInstance();
							Transfer[] transfers = new Transfer[]{textTransfer,urlTransfer};
							Object[] data;
							data = new Object[]{uri.getPath(),uri.toString()};
							clipboard.setContents(data, transfers);
							clipboard.dispose();
						}
						
					});

					
					final MenuItem remitem;
					remitem = new MenuItem(menu, SWT.NONE);
					remitem.setText("Remove list");
						
					remitem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							if (dirtyModuleLists.contains(uri)) {
								boolean doSave = MessageDialog.openQuestion(getShell(), "Module Changes", 
										"Save changes to module list?");
								if (doSave) {
									saveModules(uri);
								} 
								dirtyModuleLists.remove(uri);
							}
							List<Object> list = modDbList.getList();
							list.remove(uri.toString());
							try {
								list.remove(new File(uri).toString());
							} catch (IllegalArgumentException ia) {
								// ignore
							}
							modDbList.setList(list);
							dirtyModuleLists.remove(uri);
							moduleMap.remove(uri);
							viewer.remove(uri);
						}
						
					});

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
		
		viewer.getTree().addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ARROW_NEXT) {
					traverseTree(1);
				} else if (e.detail == SWT.TRAVERSE_PAGE_NEXT) {
					traverseTree(8);
				} else if (e.detail == SWT.TRAVERSE_ARROW_PREVIOUS) {
					traverseTree(-1);
				} else if (e.detail == SWT.TRAVERSE_PAGE_PREVIOUS) {
					traverseTree(-8);
				} 
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
					e.doit = false;
					
					if (viewer.getTree().getItemCount() > 0 && selectedModule != null) {
						switchModule(false);
					}
				}
			}
		});

		filterText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				updateFilter(filterText.getText());
				
				if (lastFilter != null && viewer.getTree().getItemCount() > 0) {
					viewer.setSelection(new StructuredSelection(viewer.getTree().getItems()[0].getData()), true);
				}
			}
		});
		
	}

	/**
	 * @param i
	 */
	protected void traverseTree(int step) {
		List<Object> flatList = getFlatModuleList();
		
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		int index = 0;
		if (!sel.isEmpty()) {
			index = flatList.indexOf(sel.getFirstElement());
		}
		int newIndex = Math.max(0, Math.min(flatList.size() - 1, index + step));
		if (newIndex >= 0 && newIndex < flatList.size()) {
			viewer.setSelection(new StructuredSelection(flatList.get(newIndex)));
		}
	}

	protected List<Object> getFlatModuleList() {
		if (flatModuleList == null) {
			flatModuleList = new ArrayList<Object>();
			ITreeContentProvider cp = ((ITreeContentProvider) viewer.getContentProvider());
			addToList(cp, viewer.getFilters(), null, viewer.getInput(), flatModuleList);
		}
		return flatModuleList;
	}


	private void addToList(ITreeContentProvider cp, ViewerFilter[] filters,
			Object parent, Object ent, List<Object> flatList) {
		boolean excl = false;
		for (ViewerFilter filt : filters) {
			if (!filt.select(viewer, parent, ent)) {
				excl = true;
				break;
			}
		}
		if (!excl) {
			flatList.add(ent);
		}
		Object[] kids = cp.getChildren(ent);
		if (kids == null)
			return;
		for (Object kid : kids) {
			addToList(cp, filters, ent, kid, flatList);
		}
	}



	/**
	 * @param softReset if true, just reset CPU, else if false, reset whole machine 
	 * 
	 */
	protected void switchModule(boolean softReset) {
		try {
			if (softReset)
				machine.getCpu().reset();
			else
				machine.reset();			
			
			moduleManager.switchModule(selectedModule);

			if (!isDisposed())
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
	public TreeViewer getViewer() {
		return viewer;
	}
	
	/**
	 * @return the selectedModule
	 */
	public IModule getSelectedModule() {
		return selectedModule;
	}
	
	/**
	 * @param machine2
	 * @param buttonBar2
	 * @return
	 */
	public static IToolShellFactory getToolShellFactory(final IMachine machine,
			final ImageCanvas buttonBar,
			final SwtWindow window) {
		 return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "ModuleWindowBounds";
				behavior.centering = Centering.INSIDE;
				behavior.centerOverControl = buttonBar;
				behavior.dismissOnClickOutside = true;
			}
			public Control createContents(Shell shell) {
				return new ModuleSelector(shell, machine, machine.getModuleManager(), window);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}
	
	Image getOrLoadModuleImage(final Object element, final IModule module, String imagePath) {
		URI imageURI = null;
		
		// see if user has an entry
		
		if (imagePath != null) {
			imageURI = getImageURI(imagePath);
		}
		
		if (imageURI == null) {
			return loadStockImage(
					module != null ? 
							(isModuleLoadable(module) ? "stock_module.png" : "stock_module_missing.png")
							: "stock_no_module.png");
		}

		Image image = lazyImageLoader.findOrLoadImage(element, imageURI, moduleImageResizer);
		//System.out.println(System.currentTimeMillis() + "... " + module + ": " + image);
		return image;
	}



	/**
	 * @param imagePath
	 * @param imagesURI
	 * @return
	 */
	protected URI getImageURI(String imagePath) {
		URI imagesURI = builtinImagesURI;

		URI imageURI;
		imageURI = machine.getRomPathFileLocator().findFile(imagePath);

		if (imageURI == null) {
			// look inside distribution
			
				imageURI = machine.getRomPathFileLocator().resolveInsideURI(
						imagesURI,
						imagePath);
				if (!machine.getRomPathFileLocator().exists(imageURI))
					imageURI = null;
			
		}
		return imageURI;
	}


	/**
	 * @param string
	 * @return
	 */
	private Image loadStockImage(String string) {
		Image stock = stockImages .get(string);
		if (stock == null) {
			stock = EmulatorGuiData.loadImage(getDisplay(), "icons/" + string);
			stockImages.put(string, stock);
		}
		return stock;
	}



	/**
	 * @param module
	 * @return
	 */
	protected boolean isModuleLoadable(final IModule module) {
		if (module == null)
			return false;
		
		synchronized (knownStates) {
			Boolean known = knownStates.get(module);
			if (known != null)
				return known;
		}
		
		// this can take a long time
		synchronized (currentFetches) {
			if (currentFetches.containsKey(module))
				return false; 	// not known yet
			currentFetches.put(module, true);
		
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					Boolean state;
					
					synchronized (knownStates) {
						state = knownStates.get(module);
					}
					if (state == null) {
						try {
							moduleManager.getModuleMemoryEntries(module);
							synchronized (knownStates) {
								knownStates.put(module, Boolean.TRUE);
							}
						} catch (NotifyException e) {
							//System.out.println(e.toString());
							synchronized (knownStates) {
								knownStates.put(module, Boolean.FALSE);
							}
						}
					} 

					synchronized (currentFetches) {
						currentFetches.remove(module);
					}

					// notify viewer of change (label or content)
					viewerUpdater.post(module);
				}
			};
			
			executor.submit(runnable);
		}
		
		return false;	// not known yet
	}

	private void assignModuleImage(IModule module) {
		String defDir = null;
		try {
			defDir = machine.getModel().getDataURL().toURI().getPath();
		} catch (URISyntaxException e) {
			
		}
		String imagePath = module.getName().toLowerCase().replace(' ', '_');
		String targFile = window.openFileSelectionDialog("Save screenshot...", 
				defDir,
				"images/" + imagePath + ".png",
				true,
				new String[] { "*.png|PNG" });
		if (targFile != null) {
			ImageData data = ((ISwtVideoRenderer) window.getVideoRenderer()).getPlainScreenshotImageData();
			try {
				ImageIO.write(ImageUtils.convertToBufferedImage(data).image, "png", new File(targFile));
				
				ModuleInfo info = module.getInfo();
				if (info == null) {
					info = ModuleInfo.createForModule(module);
					module.setInfo(info);
				}
				info.setImagePath(targFile.substring(targFile.lastIndexOf(File.separatorChar) + 1));
				lazyImageLoader.resetImage(getImageURI(info.getImagePath()));
				viewer.update(module, null);

				machine.getModuleManager().getModuleInfoDatabase().register(module);
				isModuleInfoDirty = true;
				
				dirtyModuleLists.add(module.getDatabaseURI());
			} catch (IOException e) {
				window.getEventNotifier().notifyEvent(null, Level.ERROR, "Failed to save image: " + e.getMessage());
			}
		}
	}

	/**
	 * @param module
	 */
	private void saveModules(URI moduleList) {
		try {
			OutputStream os = machine.getRomPathFileLocator().createOutputStream(moduleList);
			try {
				ModuleDatabase.saveModuleListAndClose(machine.getMemory(), os, 
						moduleList, Arrays.asList(moduleManager.getModules()));
			} catch (NotifyException e) {
				window.getEventNotifier().notifyEvent(e.getEvent());
			}
		} catch (IOException e) {
			window.getEventNotifier().notifyEvent(null, Level.ERROR, "Failed to save module database: " + e.getMessage());
		}
		
	}

	private void showModuleDetails(IModule module) {
		ModuleInfoDialog dialog = new ModuleInfoDialog(this, window.getShell(), module);
		
		dialog.open();
	}


	/**
	 * @return the machine
	 */
	public IMachine getMachine() {
		return machine;
	}



	/**
	 * @return the showMissingModules
	 */
	public boolean isShowMissingModules() {
		return showMissingModules;
	}

	public ISettingSection getDialogSettings() {
		return dialogSettings;
	}

	public Image getModuleListImage() {
		return modulesListImage;
	}
	public Image getNoModuleImage() {
		return noModuleImage;
	}



	/**
	 * 
	 */
	public void firstRefresh() {
		initFilter(ModuleSelector.lastFilter);
		hookActions();
	}

}
