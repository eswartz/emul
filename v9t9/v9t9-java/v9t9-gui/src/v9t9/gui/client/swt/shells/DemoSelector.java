/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Text;
import org.ejs.gui.common.FontUtils;
import org.ejs.gui.common.SwtDialogUtils;

import v9t9.common.demos.IDemo;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoManager;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.bars.ImageBar;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;

/**
 * @author ejs
 *
 */
public class DemoSelector extends Composite {
	public static final String DEMO_SELECTOR_TOOL_ID = "demo.selector";
	
	private static final String SECTION_DEMO_PLAYER = "DemoPlayer";
	private static final String SORT_ENABLED = "SortEnabled";
	private static final String SORT_DIRECTION = "SortDirection";
	private static final String SORT_COLUMN = "SortColumn";

	
	private static String NAME_PROPERTY = "name";
	private static String DESCR_PROPERTY = "descr";
	private static String DATE_PROPERTY = "date";
	private static String[] NAME_PROPERTY_ARRAY = { NAME_PROPERTY, DESCR_PROPERTY, DATE_PROPERTY };
	static final int NAME_COLUMN = 0;
	static final int DESCR_COLUMN = 1;
	static final int DATE_COLUMN = 2;
	static final int FILE_COLUMN = 3;

	protected static final int TEXT_MARGIN = 4;
	
	private TreeViewer viewer;
	private TreeColumn nameColumn;
	private TreeColumn dateColumn;
	private TreeColumn descrColumn;
	private IDemo selectedDemo;
	private Composite buttonBar;
	private final IMachine machine;
	private Button playButton;
	private Font nameColumnFont;

	private IDemoHandler demoHandler;

	private Text filterText;

	private String lastFilter;

	private boolean sortDemos = true;
	private int sortDirection = 1;
	private int sortColumn = NAME_COLUMN;
	
	private IDemoManager demoManager;


	private final class DescrViewerColumnLabelProvider extends StyledCellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			if (cell.getElement()  instanceof IDemo && cell.getColumnIndex() == DESCR_COLUMN) {
				String string = (String) ((IDemo) cell.getElement()).getDescription();
				Rectangle bounds = cell.getBounds();
				GC gc = new GC(getDisplay()); 
				StringBuilder sb = new StringBuilder();
				
				String[] words = string.split(" ", 0);
				
				int curLineSize = 0;
				int index = 0;
				while (index < words.length) {
					Point extent = gc.textExtent(words[index]);
					if (curLineSize > 0 && curLineSize + extent.x >= bounds.width) {
						sb.append('\n');
						curLineSize = 0;
					}
					curLineSize += extent.x;
					sb.append(words[index]);
					sb.append(' ');
					index++;
				}
				
				cell.setText(sb.toString());
				gc.dispose();
			}
		}
	}

	class FilteredSearchFilter extends ViewerFilter {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerFilter#isFilterProperty(java.lang.Object, java.lang.String)
		 */
		@Override
		public boolean isFilterProperty(Object element, String property) {
			return NAME_PROPERTY.equals(property);
		}
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (parentElement.getClass().equals(Object.class))
				return true;
			if (lastFilter != null) {
				return element instanceof IDemo && ((IDemo) element).getName().toLowerCase().contains(
						lastFilter.toLowerCase());
			}
			return true;
		}
	}
	
	private ViewerFilter filteredSearchFilter = new FilteredSearchFilter();

	private ISettingSection dialogSettings;

	private Button browseButton;

	private IProperty recordedPathProperty;

	public DemoSelector(Shell shell, IMachine machine_, IDemoManager demoManager_, SwtWindow window_) {
		super(shell, SWT.NONE);
		this.demoHandler = machine_.getDemoHandler();
		this.demoManager = demoManager_;
		this.machine = machine_;
		
		dialogSettings = machine.getSettings().getWorkspaceSettings().getHistorySettings().
				findOrAddSection(SECTION_DEMO_PLAYER);

		demoManager.reload();
		
		recordedPathProperty = Settings.get(machine, IDemoManager.settingRecordedDemosPath);

		
		shell.setText("Demo Player");
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		createSearchFilter();
		
		
		viewer = createTable();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());

		
		buttonBar = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(3).equalWidth(false).applyTo(buttonBar);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonBar);

		browseButton = new Button(buttonBar, SWT.PUSH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(128, -1).applyTo(browseButton);

		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filename = SwtDialogUtils.openFileSelectionDialog(
						getShell(),
						"Select demo file", 
						selectedDemo != null && selectedDemo.getURI().getScheme().equals("file") ?
								new File(selectedDemo.getURI()).getParent() : null,
						null, false,
						IDemoManager.DEMO_EXTENSIONS);
				if (filename != null) {
					File playFile = new File(filename);
					String parent = playFile.getParentFile().toURI().toString();
					IProperty searchPath = Settings.get(machine, IDemoManager.settingUserDemosPath);
					if (!searchPath.getList().contains(parent)) {
						searchPath.getList().add(parent);
						searchPath.firePropertyChange();
						
						demoManager.reload();
						viewer.refresh();
					}

					if (!isDisposed())
						getShell().dispose();
					
					try {
						demoHandler.startPlayback(playFile.toURI());
					} catch (NotifyException ex) {
						machine.getEventNotifier().notifyEvent(ex.getEvent());
					}
				}
			}
		});
		browseButton.setEnabled(true);
		
		
		Label filler = new Label(buttonBar, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(filler);

		
		playButton = new Button(buttonBar, SWT.PUSH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(128, -1).applyTo(playButton);
		
		playButton.setText("Play demo");
		playButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				playDemo();
			}
		});
		playButton.setEnabled(false);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
			}
		});
		
		initFilter(lastFilter);
		
		hookActions();
		

		//ViewerColumn nameViewerColumn = new TreeViewerColumn(viewer, nameColumn);
		//nameViewerColumn.setLabelProvider(new NameViewerColumnLabelProvider());

		getDisplay().asyncExec(new Runnable() {
			public void run() {
				restoreSelection();
			}
		});
		

		
	}

	/**
	 * 
	 */
	protected void restoreSelection() {
		selectedDemo = null;
		
		URI last = null;
		if ((last = demoHandler.getPlaybackURI()) == null)
			last = demoHandler.getRecordingURI();
		
		if (last != null) {
			for (IDemo demo : demoManager.getDemos()) {
				if (demo.getURI().equals(last)) {
					selectedDemo = demo;
//					viewer.setSelection(new TreeSelection(
//							new TreePath(new Object[] { selectedDemo })), 
//							true);
					viewer.setSelection(new StructuredSelection(selectedDemo), true);
				}
			}
		}
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
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (lastFilter != null && lastFilter.length() > 0) {
					initFilter(null);
					filterText.setFocus();
					viewer.setSelection(new StructuredSelection(demoManager.getDemos()), true);
				}
			}
		});
	}

	protected void refreshFilters() {
		viewer.setFilters(new ViewerFilter[] { 
				filteredSearchFilter
			}
		);
	}

	protected void initFilter(String text) {
		if (text == null || text.length() == 0) {
			filterText.setText("Search...");
			filterText.setForeground(getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		} else {
			String curText = filterText.getText(); 
			if (curText.equals("Search...") || curText.isEmpty()) {
				filterText.setText(text);
				filterText.selectAll();
				filterText.setForeground(null);
			}
		}
		
		refreshFilters();

		updateFilter(text);
		

		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (viewer.getControl().isDisposed())
					return;
				
				int width = viewer.getControl().getSize().x;
				
				if (!nameColumn.isDisposed())
					nameColumn.setWidth(width / 3);
				if (!dateColumn.isDisposed())
					dateColumn.setWidth(width / 4);
				
				if (!descrColumn.isDisposed()) {
					descrColumn.setWidth(width / 4);
					viewer.refresh();
				}
				
				final IDemo[] demos = demoManager.getDemos();
				viewer.setSelection(new StructuredSelection(demos), true);
				
				if (selectedDemo != null) {
					SwtDialogUtils.revealOncePopulated(
							machine.getMachineTimer(), 500, 
							viewer, selectedDemo);
				}
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
	 * @param demoManager
	 */
	protected TreeViewer createTable() {
		final TreeViewer viewer = new TreeViewer(this, SWT.READ_ONLY | SWT.BORDER | SWT.FULL_SELECTION);
		
		viewer.setComparer(new IElementComparer() {
			
			@Override
			public int hashCode(Object element) {
				return System.identityHashCode(element);
			}
			
			@Override
			public boolean equals(Object a, Object b) {
				return a == b || a.equals(b);
			}
		});
		
		Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		GridDataFactory.fillDefaults().grab(true,true).applyTo(tree);

		FontDescriptor desc = FontUtils.getFontDescriptor(JFaceResources.getBannerFont());
		nameColumnFont = desc.createFont(getDisplay()); 

		nameColumn = new TreeColumn(tree, SWT.LEFT);
		nameColumn.setText("Name");
		descrColumn = new TreeColumn(tree, SWT.LEFT);
		descrColumn.setText("Description");
		dateColumn = new TreeColumn(tree, SWT.LEFT);
		dateColumn.setText("Date");

		viewer.setContentProvider(new DemoTreeContentProvider());
		viewer.setLabelProvider(new DemoTreeLabelProvider());

		viewer.setColumnProperties(NAME_PROPERTY_ARRAY);
		
		viewer.setComparator(new ViewerComparator() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerComparator#isSorterProperty(java.lang.Object, java.lang.String)
			 */
			@Override
			public boolean isSorterProperty(Object element, String property) {
				return NAME_PROPERTY_ARRAY[sortColumn].equals(property);
			}
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (!sortDemos)
					return 0;
				
				if (e1 instanceof IDemo && e2 instanceof IDemo) {
					IDemo dem1 = (IDemo) e1;
					IDemo dem2 = (IDemo) e2;
					boolean l1 = isDemoLoadable(dem1);
					boolean l2 = isDemoLoadable(dem2);
					if (l1 == l2) {
						int diff;
						if (sortColumn == NAME_COLUMN) {
							diff = dem1.getName().toLowerCase().compareTo(dem2.getName().toLowerCase());
						} else if (sortColumn == DATE_COLUMN) {
							try {
								long d1 = demoManager.getDemoLocator().getLastModified(dem1.getURI());
								long d2 = demoManager.getDemoLocator().getLastModified(dem2.getURI());
								diff = (int) Math.signum(d1 - d2);
							} catch (IOException e) {
								diff = dem1.getName().toLowerCase().compareTo(dem2.getName().toLowerCase());
							}
						} else /*if (sortColumn == DESCR_COLUMN)*/ {
							diff = dem1.getDescription().toLowerCase().compareTo(dem2.getDescription().toLowerCase());
							
						}
						return sortDirection * diff;
					} else if (l1) {
						return -1;
					} else {
						return 1;
					}
				} else if (e1 instanceof IDemo) {
					return 1;
				} /* else if (e2 instanceof IDemo) */ {
					return -1;
				}
			}	
		});
		
		
		addIterativeSearch(viewer, tree);

		sortDemos = dialogSettings.getBoolean(SORT_ENABLED);
		sortDirection = dialogSettings.getInt(SORT_DIRECTION);
		sortColumn = dialogSettings.getInt(SORT_COLUMN);

		viewer.setInput(new Object());
		
		tree.addKeyListener(new KeyAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == '\r' || e.keyCode == '\n') {
					playDemo();
					e.doit = false;
				}
			}
		});
		

		final ViewerColumn descrViewerColumn = new TreeViewerColumn(viewer, descrColumn);
		final DescrViewerColumnLabelProvider descrLabelProvider = new DescrViewerColumnLabelProvider();
		descrViewerColumn.setLabelProvider(descrLabelProvider);
		descrColumn.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						viewer.refresh();
					}
				});
			}
		});
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (nameColumnFont != null)
					nameColumnFont.dispose();
			}
		});
		
		return viewer;
	}

	/**
	 * @param viewer
	 * @param control
	 * @param realModules
	 */
	protected void addIterativeSearch(final StructuredViewer viewer, Control control) {
		control.addKeyListener(new KeyAdapter() {
			StringBuilder search = new StringBuilder();
			int index = 0;
			IDemo[] demos;
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (demos == null) {
					demos = demoManager.getDemos();
				}
				if (e.keyCode == '\b') {
					search.setLength(0);
					index = 0;
					e.doit = e.keyCode != '\b';
					demos = demoManager.getDemos();
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
					int end = (index + demos.length - 1) % demos.length;
					for (int i = index; i != end; i = (i + 1) % demos.length) {
						IDemo m = demos[i];
						if (m.getName().toLowerCase().contains(search.toString().toLowerCase())) {
							viewer.setSelection(new StructuredSelection(m), true);
							index = i;
							break;
						}
					}
				}
			}
		});
	}

	class SortHandler extends SelectionAdapter {
		private final int column;
		public SortHandler(int column) {
			this.column = column;
			
		}
		@Override
		public void widgetSelected(SelectionEvent e) {
			e.doit = false;
			
			if (!sortDemos || sortColumn != column) {
				sortDemos = true;
				sortDirection = 1;
				sortColumn = column;
			} else {
				if (sortDirection == 1) {
					sortDirection = -1;
				} else {
					sortDemos = false;
				}
			}
			dialogSettings.put(SORT_ENABLED, sortDemos);
			dialogSettings.put(SORT_DIRECTION, sortDirection);
			dialogSettings.put(SORT_COLUMN, sortColumn);
			viewer.refresh();
		}
	}
	/**
	 * 
	 */
	protected void hookActions() {
		nameColumn.addSelectionListener(new SortHandler(NAME_COLUMN));
		dateColumn.addSelectionListener(new SortHandler(DATE_COLUMN));
		descrColumn.addSelectionListener(new SortHandler(DESCR_COLUMN));
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (obj instanceof String) {
					selectedDemo = null;
					playButton.setEnabled(true);
				}
				else if (obj instanceof IDemo) {
					selectedDemo = (IDemo) obj;
					playButton.setEnabled(true);
				} else {
					playButton.setEnabled(false);
				}
			}
		});
		viewer.addOpenListener(new IOpenListener() {
			
			public void open(OpenEvent event) {
				Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (obj instanceof String) {
					selectedDemo = null;
					if (!playButton.isDisposed())
						playButton.setEnabled(true);
					playDemo();
				}
				else if (obj instanceof IDemo) {
					selectedDemo = (IDemo) obj;
					if (!playButton.isDisposed())
						playButton.setEnabled(true);
					playDemo();
				} else {
					if (!playButton.isDisposed())
						playButton.setEnabled(false);
				}
			}
		});
		
		viewer.getControl().addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent e) {
				final Item item = viewer.getTree().getItem(
						viewer.getControl().toControl(new Point(e.x, e.y))
						);
				if (item == null)
					return;
				

				Menu menu = new Menu(viewer.getControl());

				if (item.getData() instanceof URI) {

					final URI uri = (URI) item.getData();
					final String filePathStr = uri.getScheme().equals("file") ? new File(uri.getPath()).getAbsolutePath() : null;
					
					final MenuItem dlitem;
					dlitem = new MenuItem(menu, SWT.NONE);
					dlitem.setText("Remove directory from search paths");
					
					dlitem.addSelectionListener(new SelectionAdapter() {
						@SuppressWarnings("unchecked")
						@Override
						public void widgetSelected(SelectionEvent e) {
							for (IProperty prop : demoManager.getDemoLocator().getSearchPathProperties()) {
								if (prop.getValue() instanceof List<?>) {
									List<String> list = (List<String>) prop.getValue();
									if (list.remove(uri.toString()) || list.remove(filePathStr)) {
										prop.firePropertyChange();
										viewer.refresh();
									}
								}
							}
						}
						
					});
					
					if (recordedPathProperty.getString().equals(uri.toString())) {
						dlitem.setEnabled(false);
					}
				}
				else if (item.getData() instanceof IDemo) {
					final IDemo demo = (IDemo) item.getData();

					if (demo.getURI().getScheme().equals("file")) {
						final MenuItem dlitem;
						dlitem = new MenuItem(menu, SWT.NONE);
						dlitem.setText("Delete entry from disk");
						
						dlitem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								boolean ok = MessageDialog.openConfirm(
										getShell(), "Delete?", "Are you sure you want to delete:\n"
										+ demo.getURI());
								if (!ok)
									return;
								
								if (new File(demo.getURI()).delete()) {
									demoManager.removeDemo(demo);
									viewer.remove(demo);
								} else {
									MessageDialog.openError(getShell(), "Delete failed", 
											"Could not delete the demo from disk:\n" + demo.getURI());
								}
							}
							
						});
					}
					
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
					
					if (viewer.getTree().getItemCount() > 0 && selectedDemo != null) {
						playDemo();
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
	 * 
	 */
	protected void playDemo() {
		try {
			demoHandler.startPlayback(selectedDemo.getURI());
			if (!isDisposed())
				getShell().dispose();
		} catch (NotifyException e) {
			machine.notifyEvent(e.getEvent());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public Viewer getViewer() {
		return viewer;
	}
	
	public IDemo getSelectedDemo() {
		return selectedDemo;
	}
	
	class DemoTreeContentProvider implements ITreeContentProvider {

		@Override
		public void dispose() {
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			return demoManager.getDemoLocator().getSearchURIs();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof URI) {
				List<IDemo> demos = new ArrayList<IDemo>();
				for (IDemo demo : demoManager.getDemos()) {
					if (demo.getParentURI().equals(parentElement)) {
						demos.add(demo);
					}
				}
				return (IDemo[]) demos.toArray(new IDemo[demos.size()]);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		@Override
		public Object getParent(Object element) {
			if (element instanceof IDemo)
				return ((IDemo) element).getParentURI();
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		@Override
		public boolean hasChildren(Object element) {
			return element instanceof URI;
		}
		
	}
	
	class DemoTreeLabelProvider extends LabelProvider implements ITableLabelProvider,
		ITableColorProvider, ITableFontProvider {

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
			if (element instanceof String && columnIndex == NAME_COLUMN)
				return element.toString();
			if (element instanceof URI && columnIndex == NAME_COLUMN) {
				if (element instanceof URI && element.toString().equals(recordedPathProperty.getString())) {
					return element.toString() + " (recording path)";
				}

				return element.toString();
			}
			if (!(element instanceof IDemo)) {
				return null;
			}
			IDemo demo = (IDemo) element;
			switch (columnIndex) {
			case NAME_COLUMN: return demo.getName();
			case DATE_COLUMN: {
				try {
					Date date = new Date(demoManager.getDemoLocator().getLastModified(demo.getURI())); 
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DAY_OF_MONTH, -7);
					if (cal.getTime().compareTo(date) >= 0)
						return DateFormat.getDateInstance().format(date);
					else
						return DateFormat.getDateTimeInstance().format(date);
				} catch (IOException e) {
					return "";
				}
			}
			case DESCR_COLUMN: return demo.getDescription();
			case FILE_COLUMN: return demo.getURI().toString();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
		 */
		@Override
		public Color getForeground(Object element, int columnIndex) {
			if (element instanceof URI && element.toString().equals(recordedPathProperty.getString())) {
				return getDisplay().getSystemColor(SWT.COLOR_BLUE);
			}
			if (!(element instanceof IDemo))
				return null;
			
			IDemo demo = (IDemo) element;
			if (!isDemoLoadable(demo))
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

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
		 */
		@Override
		public Font getFont(Object element, int columnIndex) {
			if (columnIndex == NAME_COLUMN)
				return nameColumnFont;
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
				behavior.boundsPref = "DemoWindowBounds";
				behavior.centering = Centering.OUTSIDE;
				behavior.dismissOnClickOutside = false;
			}
			public Control createContents(Shell shell) {
				return new DemoSelector(shell, machine, machine.getDemoManager(), window);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}
	
	protected boolean isDemoLoadable(final IDemo demo) {
		if (demo == null)
			return false;
		
		return demoHandler.isDemoSupported(demo.getURI());
	}

	/**
	 * @return the machine
	 */
	public IMachine getMachine() {
		return machine;
	}


}
