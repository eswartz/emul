/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import javax.imageio.ImageIO;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.DialogSettingsWrapper;
import ejs.base.settings.ISettingSection;
import ejs.base.settings.SettingProperty;
import ejs.base.utils.HexUtils;
import ejs.base.utils.Pair;

import v9t9.common.client.IVideoRenderer;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.files.DataFiles;
import v9t9.common.files.PathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.StoredMemoryEntryInfo;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleManager;
import v9t9.common.modules.MemoryEntryInfo;
import v9t9.common.settings.Settings;
import v9t9.common.video.IVdpCanvas;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.client.swt.ISwtVideoRenderer;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.bars.ImageBar;
import v9t9.gui.client.swt.imageimport.ImageClipDecorator;
import v9t9.gui.client.swt.imageimport.ImageLabel;
import v9t9.gui.client.swt.imageimport.ImageUtils;
import v9t9.gui.common.FontUtils;

/**
 * @author ejs
 *
 */
public class ModuleSelector extends Composite {
	public static final String MODULE_SELECTOR_TOOL_ID = "module.selector";
	private static String lastFilter;

	private static final String SECTION_MODULE_SELECTOR = "ModuleSelector";
	private static final String SHOW_MISSING_MODULES = "ShowMissingModules";
	
	private static boolean allowRecordImages = true;
	
	static final int NAME_COLUMN = 0;
	static final int FILE_COLUMN = 1;
	

	private ISettingSection dialogSettings; 

	private TableViewer viewer;
	private TableColumn nameColumn;
	private IModule selectedModule;
	private Composite buttonBar;
	private final IMachine machine;
	private Button switchButton;
	private Font tableFont;
	private final IModuleManager moduleManager;
	private Text filterText;

	private Map<IModule, Boolean> knownStates = new HashMap<IModule, Boolean>();
	private boolean showMissingModules;
	protected boolean sortModules;
	protected int sortDirection;
	
	private static Map<String, Image> loadedImages = new HashMap<String, Image>();
	
	/**
	 * Not used -- UI is too complex 
	 * @author ejs
	 *
	 */
	final class ScreenshotSelectorDialog extends Dialog {
		private static final String SECTION_SCREEN_SHOTS = "ScreenShots";
		private ImageLabel imageLabel;
		private IProperty clipProperty = new SettingProperty("clip", new java.awt.Rectangle());
		private Image screenshot;
		private ImageLabel renderedImageLabel;
		private Image renderedImage;

		private ScreenshotSelectorDialog(Shell parentShell) {
			super(parentShell);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#setShellStyle(int)
		 */
		@Override
		protected void setShellStyle(int newShellStyle) {
			super.setShellStyle(newShellStyle | SWT.RESIZE);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
		 */
		@Override
		protected IDialogSettings getDialogBoundsSettings() {
			return new DialogSettingsWrapper(dialogSettings.findOrAddSection(SECTION_SCREEN_SHOTS));
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
		 */
		@Override
		protected Point getInitialSize() {
			return new Point(400, 300);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			
			Composite sideBySide = new Composite(composite, SWT.NONE);
			GridLayoutFactory.fillDefaults().numColumns(3).applyTo(sideBySide);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(sideBySide);
			
			imageLabel = new ImageLabel(sideBySide, SWT.BORDER);
			IVideoRenderer renderer = window.getVideoRenderer();
			IVdpCanvas canvas = renderer.getCanvas();
			GridDataFactory.fillDefaults().grab(true, true)
						.hint(canvas.getVisibleWidth(), canvas.getVisibleHeight())
						.applyTo(imageLabel);

			Label sep = new Label(sideBySide, SWT.VERTICAL | SWT.SHADOW_IN);
			GridDataFactory.fillDefaults().grab(false, true).applyTo(sep);
			
			renderedImageLabel = new ImageLabel(sideBySide, SWT.BORDER);
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(false, false)
				.minSize(32, 32).hint(32, 32)
				.applyTo(renderedImageLabel);
			
			renderedImage = new Image(getDisplay(), 32, 32);
			renderedImageLabel.setImage(renderedImage);
			
			screenshot = new Image(getDisplay(), ((ISwtVideoRenderer) renderer).getScreenshotImageData());
			imageLabel.setImage(screenshot);
			
			updateRenderedImage();
			
			new ImageClipDecorator(imageLabel, clipProperty, new IPropertyListener() {
				
				@Override
				public void propertyChanged(IProperty property) {
					updateRenderedImage();						
				}
			}, new ImageClipDecorator.IBoundsUpdater() {
				public Rectangle update(Rectangle rect) {
					int sz = Math.max(rect.width, rect.height);
					// make power-of-two
					while ((sz & (sz - 1)) != 0)
						sz &= (sz - 1);
					rect.width = rect.height = sz;
					
					return rect;
				}
			});
			
			addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					screenshot.dispose();
				}
			});
			
			Label message = new Label(composite, SWT.NONE | SWT.WRAP);
			message.setText("Select a portion of the screenshot to use as the module icon.");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(message);
			
			return composite;
		}

		private void updateRenderedImage() {
			java.awt.Rectangle rect = (java.awt.Rectangle) clipProperty.getValue();
			if (rect == null || rect.isEmpty()) {
				Rectangle bounds = screenshot.getBounds();
				rect = new java.awt.Rectangle(0, 0, bounds.width, bounds.height);
			}
			
			GC gc = new GC(renderedImage);
			Rectangle rbounds = renderedImage.getBounds();
			gc.drawImage(screenshot, rect.x, rect.y, rect.width, rect.height, 0, 0, rbounds.width, rbounds.height);
			gc.dispose();
			
			renderedImageLabel.redraw();
		}
	}

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
			if (showMissingModules || !(element instanceof IModule))
				return true;
			
			IModule module = (IModule) element;
			return isModuleLoadable(module);
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
	
	class ViewerUpdater extends Thread { 

		private Queue<Object> elements = new LinkedBlockingDeque<Object>();
		
		public void post(Object element) {
			elements.add(element);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			final List<Object> avail = new ArrayList<Object>();
			while (true) {

				while (!elements.isEmpty()) {
					final Object element = elements.poll();
					avail.add(element);
				}
				if (!avail.isEmpty() && !getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {
						private boolean firstRefresh = true;

						public void run() {
							if (!viewer.getControl().isDisposed()) {
								viewer.update(avail.toArray(), null);
								// the node may have been filtered out
								viewer.setFilters(new ViewerFilter[] { 
										existingModulesFilter,
										filteredSearchFilter
									}
								);
								
								if (firstRefresh) {
									firstRefresh = false;
									
									initFilter(lastFilter);
	
									hookActions();
								}								
								
								avail.clear();
							}
						}
					});
				}
				
				// dleay to gather more changes at once
				try {
					Thread.sleep(750);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}
	private ViewerFilter filteredSearchFilter = new FilteredSearchFilter();
	private final SwtWindow window;
	private IProperty pauseProperty;
	private boolean wasPaused;
	private PathFileLocator pathFileLocator;
	private ExecutorService executor;
	private ViewerUpdater viewerUpdater;

	/**
	 * @param window 
	 * 
	 */
	public ModuleSelector(Shell shell, IMachine machine, IModuleManager moduleManager, SwtWindow window) {
		super(shell, SWT.NONE);
		this.moduleManager = moduleManager;
		this.window = window;
		
		executor = Executors.newFixedThreadPool(8);
		
		viewerUpdater = new ViewerUpdater();
		
		dialogSettings = machine.getSettings().getWorkspaceSettings().getHistorySettings().findOrAddSection(SECTION_MODULE_SELECTOR);
		
		shell.setText("Module Selector");
		
		this.machine = machine;
		
		pathFileLocator = machine.getMemoryEntryFactory().getPathFileLocator();
		
		pauseProperty = Settings.get(machine, IMachine.settingPauseMachine);
		wasPaused = pauseProperty.getBoolean();
		pauseProperty.setBoolean(true);

		moduleManager.reload();
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		
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
				viewer.refresh();
				dialogSettings.put(SHOW_MISSING_MODULES, showMissingModules);
			}
		});
		
		showMissingModules = dialogSettings.getBoolean(SHOW_MISSING_MODULES);
		showUnloadable.setSelection(showMissingModules);
		
		buttonBar = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(3).equalWidth(false).applyTo(buttonBar);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonBar);
		
		/*
		scanButton = new Button(buttonBar, SWT.PUSH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(128, -1).applyTo(scanButton);
		scanButton.setText("Scan...");
		scanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		scanButton.setEnabled(false);
		*/
		
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
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				executor.shutdownNow();
				viewerUpdater.interrupt();
				try {
					viewerUpdater.join();
				} catch (InterruptedException e1) {
				}
			}
		});
		
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
		
		final Button clearButton = new Button(comp, SWT.PUSH | SWT.NO_FOCUS);
		clearButton.setImage(EmulatorGuiData.loadImage(getDisplay(), "icons/icon_search_clear.png"));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(false, false).applyTo(clearButton);

		clearButton.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (lastFilter != null && lastFilter.length() > 0) {
					initFilter(null);
					filterText.setFocus();
					viewer.setSelection(new StructuredSelection(moduleManager.getLoadedModules()), true);
				}
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
				if (!nameColumn.isDisposed())
					nameColumn.pack();
				
				final IModule[] loadedModules = moduleManager.getLoadedModules();
				viewer.setSelection(new StructuredSelection(loadedModules));
				
				// workaround: GTK does not realize the elements for a while
				final TimerTask task = new TimerTask() {
					TimerTask xx = this;
					/* (non-Javadoc)
					 * @see java.util.TimerTask#run()
					 */
					@Override
					public void run() {
						if (isDisposed()) {
							xx.cancel();
							return;
						}
							
						getDisplay().asyncExec(new Runnable() {
							public void run() {
								boolean cancel = false;
								if (isDisposed() || loadedModules.length == 0) {
									cancel = true;
								}
								if (loadedModules.length > 0 && viewer.getTable().getItem(0).getBounds().height > 0) {
									viewer.reveal(loadedModules[0]);
									cancel = true;
								}
								if (cancel) {
									xx.cancel();
								}
							}
						});
					}
				};
				machine.getMachineTimer().scheduleAtFixedRate(task, 0, 500);
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
				/*for (Image image : loadedImages.values()) {
					image.dispose();
				}*/
				pauseProperty.setBoolean(wasPaused);
			}
		});
		
		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Name");

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ModuleTableLabelProvider());
		
		viewer.setComparator(new ViewerComparator() {
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
						return sortDirection * mod1.getName().compareTo(mod2.getName());
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
		final IModule[] realModules = moduleManager.getModules();
		
		if (realModules.length > 0) {
			addIterativeSearch(viewer, table, realModules);
		}
		
		Object[] modulesPlusEmpty = new Object[realModules.length + 1];
		modulesPlusEmpty[0] = "<No module>";
		System.arraycopy(realModules, 0, modulesPlusEmpty, 1, realModules.length);
		viewer.setInput(modulesPlusEmpty);
		
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
							viewer.setSelection(new StructuredSelection(m), true);
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
		nameColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!sortModules) {
					sortModules = true;
					sortDirection = 1;
				} else {
					if (sortDirection == 1)
						sortDirection = -1;
					else
						sortModules = false;
				}
				viewer.refresh();
			}
		});
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
		
		viewer.getTable().addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent e) {
				final TableItem item = viewer.getTable().getItem(
						viewer.getTable().toControl(new Point(e.x, e.y))
						);
				if (item != null && item.getData() instanceof IModule) {
					Menu menu = new Menu(viewer.getTable());
					
					final MenuItem mitem;
					if (allowRecordImages && window.getVideoRenderer() instanceof ISwtVideoRenderer) {
						mitem = new MenuItem(menu, SWT.NONE);
						mitem.setText("Assign module image from screenshot...");
						
						mitem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								assignModuleImage((IModule) item.getData());
							}

						});
					}

					final MenuItem ditem;
					ditem = new MenuItem(menu, SWT.NONE);
					ditem.setText("Module details...");
					
					ditem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							showModuleDetails((IModule) item.getData());
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
					
					if (viewer.getTable().getItemCount() > 0 && selectedModule != null) {
						switchModule(false);
					}
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
	
	class ModuleTableLabelProvider extends LabelProvider implements ITableLabelProvider,
		ITableColorProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			if (!(element instanceof IModule)) {
				if (columnIndex == NAME_COLUMN)
					return getOrLoadModuleImage(element, null, null, null); 
			}
			IModule module = (IModule) element;
			switch (columnIndex) {
			case NAME_COLUMN: 
				{
					return getOrLoadModuleImage(element, module, module.getImageURL(), module.getImagePath());
				}
			default:
				return null;
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof String && columnIndex == NAME_COLUMN)
				return element.toString();
			if (!(element instanceof IModule)) {
				return null;
			}
			IModule module = (IModule) element;
			switch (columnIndex) {
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

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
		 */
		@Override
		public Color getForeground(Object element, int columnIndex) {
			if (!(element instanceof IModule))
				return null;
			
			IModule module = (IModule) element;
			if (!isModuleLoadable(module))
				return getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
			
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
		 */
		@Override
		public Color getBackground(Object element, int columnIndex) {
			return null;
		}
	}

	/**
	 * @param machine2
	 * @param buttonBar2
	 * @return
	 */
	public static IToolShellFactory getToolShellFactory(final IMachine machine,
			final ImageBar buttonBar,
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
	
	private Image getOrLoadModuleImage(final Object element, final IModule module, URL imageURL, String imagePath) {
		if (imageURL == null) {
			if (imagePath != null) {
				File file = DataFiles.resolveFile(Settings.getSettings(machine), imagePath);
				if (file != null && file.exists()) {
					try {
						imageURL = file.toURI().toURL();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (imageURL == null) {
			try {
				imageURL = new URL(machine.getModel().getDataURL(), 
						module != null ? 
								(isModuleLoadable(module) ? "images/stock_module.png" : "images/stock_module_missing.png")
								: "images/stock_no_module.png");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		final String imageKey = imageURL.toString() + (module == null || isModuleLoadable(module) ? "" : "?grey");
		
		Image image;
		synchronized (loadedImages) {
			
			final URL theImageURL = imageURL;
			image = loadedImages.get(imageKey);
			if (image == null) {
				Runnable runnable = new Runnable() {
					
					@Override
					public void run() {

						//long start = System.currentTimeMillis(); 
						Image image = null;
						InputStream is = null;
						try {
							is = theImageURL.openStream();
							image = new Image(getDisplay(), is);
							
							Rectangle bounds = image.getBounds();
							int sz = Math.max(bounds.width, bounds.height);
							
							final int MAX = 64;
							if (sz > MAX) {
								sz = MAX;

								Image scaled = ImageUtils.scaleImage(getDisplay(), image, new Point(MAX, MAX), true, true);
								image.dispose();
								
								if (module != null && !isModuleLoadable(module)) {
									Image grey = ImageUtils.convertToGreyscale(getDisplay(), scaled);
									scaled.dispose();
									scaled = grey;
								}
									
								image = scaled;
							}
							
							synchronized (loadedImages) {
								loadedImages.put(imageKey, image);

								viewerUpdater.post(element);
							}
							
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								is.close();
							} catch (IOException e) {
							}
							//long end = System.currentTimeMillis();
							//System.out.println("... image load+scale took " + (end - start));
						}
					}
				};
				
				executor.submit(runnable);
				
			}
		}
		
		//System.out.println(System.currentTimeMillis() + "... " + module + ": " + image);
		return image;
	}


	/**
	 * @param module
	 * @return
	 */
	protected boolean isModuleLoadable(final IModule module) {
		if (module == null)
			return false;
		
		Boolean known = knownStates.get(module);
		if (known != null)
			return known;
		
		// this can take a long time
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				Boolean state;
				
				state = knownStates.get(module);
				if (state == null) {
					try {
						moduleManager.getModuleMemoryEntries(module);
						synchronized (knownStates) {
							knownStates.put(module, Boolean.TRUE);
						}
					} catch (NotifyException e) {
						System.out.println(e.toString());
						synchronized (knownStates) {
							knownStates.put(module, Boolean.FALSE);
						}
					}
				}
				
				// notify viewer of change (label or content)
				viewerUpdater.post(module);
			}
		};
		
		executor.submit(runnable);
		return false;
	}

	private void assignModuleImage(IModule module) {
		String defDir = null;
		try {
			defDir = machine.getModel().getDataURL().toURI().getPath();
		} catch (URISyntaxException e) {
			
		}
		String targFile = window.openFileSelectionDialog("Save screenshot...", 
				defDir,
				"images/" + module.getName().toLowerCase().replace(' ', '_') + ".png",
				true,
				new String[] { ".png|PNG" });
		if (targFile != null) {
			ImageData data = ((ISwtVideoRenderer) window.getVideoRenderer()).getScreenshotImageData();
			try {
				ImageIO.write(ImageUtils.convertToBufferedImage(data).first, "png", new File(targFile));
			} catch (IOException e) {
				window.getEventNotifier().notifyEvent(null, Level.ERROR, "Failed to save image: " + e.getMessage());
			}
		}
	}

	static class ErrorTreeNode extends TreeNode {

		public ErrorTreeNode(Object value) {
			super(value);
		}
		
	}
	static class InfoTreeNode extends TreeNode {
		
		public InfoTreeNode(Object value) {
			super(value);
		}
		
	}
	static class ModuleInfoTreeLabelProvider extends BaseLabelProvider implements ITableLabelProvider,
		ITableColorProvider, ITableFontProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object nodeElement, int columnIndex) {
			TreeNode treeNode = (TreeNode) nodeElement;
			Object element = treeNode.getValue();
			switch (columnIndex) {
			case 0:
				if (element instanceof IProperty)
					return "Search Path Property";
				if (element instanceof IModule)
					return "Module Entries";
				if (element instanceof MemoryEntryInfo)
					return "Expected Properties";
				if (element instanceof StoredMemoryEntryInfo)
					return split(((StoredMemoryEntryInfo) element).uri.getPath()).second;
				if (element instanceof Map.Entry)
					return ((Map.Entry<?, ?>) element).getKey().toString();
				if (element instanceof Pair)
					return ((Pair<?, ?>) element).first.toString();
				if (element instanceof URI)
					return ((URI) element).toString();
				return element.toString();
			case 1:
				if (element instanceof IProperty)
					return ((IProperty) element).getName();
				if (element instanceof IModule)
					return null;
				if (element instanceof String)
					return element.toString();
				if (element instanceof MemoryEntryInfo)
					return null;
				if (element instanceof StoredMemoryEntryInfo)
					return split(((StoredMemoryEntryInfo) element).uri.getPath()).first;
				if (element instanceof Map.Entry)
					return ((Map.Entry<?, ?>) element).getValue().toString();
				if (element instanceof Pair)
					return ((Pair<?, ?>) element).second.toString();
				if (element instanceof URI) {
					return treeNode instanceof ErrorTreeNode ? "missing" : "present";
				}
				return null;
			}
			return null;
		}

		/**
		 * @param path
		 * @return
		 */
		private Pair<String, String> split(String path) {
			int idx = path.lastIndexOf('/');
			if (idx >= 0)
				return new Pair<String, String>(path.substring(0, idx), path.substring(idx+1));
			else
				return new Pair<String, String>("", path);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
		 */
		@Override
		public Color getForeground(Object element, int columnIndex) {
			return element instanceof ErrorTreeNode 
			? Display.getDefault().getSystemColor(SWT.COLOR_RED) : null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
		 */
		@Override
		public Color getBackground(Object element, int columnIndex) {
			return null;
		}
		

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
		 */
		@Override
		public Font getFont(Object element, int columnIndex) {
			if (columnIndex == 0 && element instanceof InfoTreeNode) 
				return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
			
			if (columnIndex == 1 && ((TreeNode) element).getValue() instanceof IProperty)
				return JFaceResources.getTextFont();
			
			return null;
		}
		
		
	}
	
	final class ModuleInfoDialog extends Dialog {
		private final IModule module;

		private ModuleInfoDialog(Shell parentShell, IModule module) {
			super(parentShell);
			this.module = module;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#setShellStyle(int)
		 */
		@Override
		protected void setShellStyle(int newShellStyle) {
			super.setShellStyle(newShellStyle | SWT.RESIZE | SWT.TOOL | SWT.CLOSE);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
					true);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
		}

		protected IDialogSettings getDialogBoundsSettings() {
			return new DialogSettingsWrapper(dialogSettings.findOrAddSection("ModuleInfo"));
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
		 */
		@Override
		protected Point getInitialSize() {
			return super.getInitialSize();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			///////////
			
			CLabel title = new CLabel(composite, SWT.NONE);
			title.setText(module.getName());
			title.setFont(JFaceResources.getHeaderFont());
			
			title.setImage(getOrLoadModuleImage(null, module, module.getImageURL(), module.getImagePath()));
			
			GridDataFactory.fillDefaults().grab(true, false).applyTo(title);
			
			///////////
			Label sep = new Label(composite, SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(sep);

			///////////
			
			CLabel summary = new CLabel(composite, SWT.WRAP);
			if (isModuleLoadable(module)) {
				summary.setText("All module files resolved");
				summary.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_INFORMATION));
			} else {
				summary.setText("One or more module files are missing");
				summary.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_ERROR));
			}
			GridDataFactory.fillDefaults().grab(true, false).applyTo(summary);

			///////////
			
			final TreeViewer viewer = new TreeViewer(composite, SWT.BORDER);
			
			Tree tree = viewer.getTree();
			tree.setHeaderVisible(true);
			tree.setLinesVisible(true);
			
			GridDataFactory.fillDefaults().grab(true,true).applyTo(tree);

			final TreeColumn nameColumn = new TreeColumn(tree, SWT.RIGHT);
			final TreeColumn infoColumn = new TreeColumn(tree, SWT.LEFT);

			TreeNodeContentProvider contentProvider = new TreeNodeContentProvider();
			viewer.setContentProvider(contentProvider);
			viewer.setLabelProvider(new ModuleInfoTreeLabelProvider());
			
			viewer.setInput(createModuleContent());
			
			composite.getDisplay().asyncExec(new Runnable() {
				public void run() {
					viewer.expandToLevel(2);
					nameColumn.pack();
					infoColumn.pack();
				}
			});
			
			composite.addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
				}
			});
			
			return composite;
		}

		/**
		 * @param module2
		 * @return
		 */
		private Object createModuleContent() {
			List<TreeNode> kids = new ArrayList<TreeNode>();

			
			TreeNode moduleDatabase = new TreeNode(new Pair<String, String>(
					"Module Defined By", module.getDatabaseURI().toString()));
			
			kids.add(moduleDatabase);

			TreeNode memInfoNode = new TreeNode(module);
			MemoryEntryInfo[] infos = module.getMemoryEntryInfos();
			
			List<TreeNode> memNodes = new ArrayList<TreeNode>();
			for (MemoryEntryInfo info : infos) {
				if (!info.isBanked()) {
					addMemoryInfoNode(memNodes, info,
							info.getName(), info.getFilename(), info.getOffset());
				} else {
					addMemoryInfoNode(memNodes, info, 
							info.getName() + " (bank 0)", 
							info.getFilename(), 
							info.getOffset());

					addMemoryInfoNode(memNodes, info, 
							info.getName() + " (bank 1)", 
							info.getFilename2(), 
							info.getOffset2());
				}
			}
			memInfoNode.setChildren(memNodes.toArray(new TreeNode[memNodes.size()]));
			kids.add(memInfoNode);

			for (IProperty prop : pathFileLocator.getSearchPathProperties()) {
				kids.add(makeTreeNode(prop));
			}
			

			return (TreeNode[]) kids.toArray(new TreeNode[kids.size()]);
		}

		protected void addMemoryInfoNode(
				List<TreeNode> memNodes, MemoryEntryInfo info,
				String name, String filename, int offset) {
			StoredMemoryEntryInfo storedInfo;
			try {
				storedInfo = StoredMemoryEntryInfo.resolveStoredMemoryEntryInfo(
						pathFileLocator, getMachine().getSettings(), 
						getMachine().getMemory(), info, 
						name, filename, offset);
				memNodes.add(makeTreeNode(storedInfo));
			} catch (IOException e) {
				TreeNode errorNode = new ErrorTreeNode(new Pair<String, String>(filename,
						e instanceof FileNotFoundException ? "File not found on search paths" : e.getMessage()));
				TreeNode[] kids = new TreeNode[] {
						makeTreeNode(info),
						};
				errorNode.setChildren(kids);
				memNodes.add(errorNode);
			}
		}
		

		private TreeNode makeTreeNode(StoredMemoryEntryInfo info) {
			TreeNode node = new TreeNode(info);
			TreeNode[] kids = new TreeNode[3];
			kids[0] = new TreeNode(new Pair<String, String>("Location", info.uri.toString()));
			kids[1] = new TreeNode(new Pair<String, String>("File Size", ""+info.filesize));
			kids[2] = makeTreeNode(info.info);
			node.setChildren(kids);
			return node;
		}


		private TreeNode makeTreeNode(MemoryEntryInfo info) {
			TreeNode node = new TreeNode(info);
			Map<String, Object> props = info.getProperties();
			List<TreeNode> kids = new ArrayList<TreeNode>();
			for (Map.Entry<String, Object> entry : props.entrySet()) {
				if (entry.getKey().equals(MemoryEntryInfo.CLASS))
					continue;
				if (entry.getKey().equals(MemoryEntryInfo.ADDRESS) 
						|| entry.getKey().equals(MemoryEntryInfo.OFFSET)
						|| entry.getKey().equals(MemoryEntryInfo.OFFSET2)
						)
					kids.add(new TreeNode(new Pair<String, String>(entry.getKey(), 
							">" + HexUtils.toHex4(((Number) entry.getValue()).intValue()))));
				else if (entry.getKey().equals(MemoryEntryInfo.SIZE)) {
					int size = ((Number) entry.getValue()).intValue();
					kids.add(new TreeNode(new Pair<String, String>(entry.getKey(),
								size == 0 ? "any size" : 
									(size < 0 ? "at most " : "") + ">" + HexUtils.toHex4(size))  ));
				}
				else
					kids.add(new TreeNode(entry));
			}
			node.setChildren(kids.toArray(new TreeNode[kids.size()]));
			return node;
		}

		private TreeNode makeTreeNode(IProperty pathProperty) {
			TreeNode node = new TreeNode(pathProperty);
			List<TreeNode> kids = new ArrayList<TreeNode>();
			if (pathProperty.getValue() instanceof List) {
				if (!pathProperty.getList().isEmpty()) {
					for (Object path : pathProperty.getList()) {
						kids.add(createPathNode(path));
					}
				} else {
					kids.add(new InfoTreeNode(new Pair<String, String>("Empty", "")));				
				}
			} else {
				kids.add(createPathNode(pathProperty.getValue()));
			}
			node.setChildren((TreeNode[]) kids.toArray(new TreeNode[kids.size()]));
			return node;
		}

		/**
		 * @param kids
		 * @param idx
		 * @param path
		 * @return
		 */
		protected TreeNode createPathNode(Object path) {
			try {
				URI uri = pathFileLocator.createURI(path.toString());
				return pathFileLocator.exists(uri) ? new TreeNode(uri) : new ErrorTreeNode(uri);
			} catch (URISyntaxException e) {
				return new ErrorTreeNode(new Pair<String, String>(path.toString(), e.getMessage()));
			}
		}
	}
	
	private void showModuleDetails(IModule module) {
		ModuleInfoDialog dialog = new ModuleInfoDialog(getShell(), module);
		
		dialog.open();
	}


	/**
	 * @return the machine
	 */
	public IMachine getMachine() {
		return machine;
	}


}
