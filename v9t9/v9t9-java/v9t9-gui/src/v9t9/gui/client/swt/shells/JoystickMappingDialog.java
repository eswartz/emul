/*
  ControllerMappingDialog.java

  (c) 2017 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import v9t9.common.keyboard.ControllerConfig;
import v9t9.common.keyboard.JoystickRole;
import v9t9.common.keyboard.ControllerConfig.ParseException;
import v9t9.common.keyboard.ControllerIdentifier;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.SwtLwjglKeyboardHandler;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.bars.ImageCanvas;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;

/**
 * @author ejs
 *
 */
public class JoystickMappingDialog extends Composite implements IPropertyListener {
	public static final String CONTROLLER_MAPPING_TOOL_ID = "controller.mapping.dialog";
	public static final String SECTION_CONTROLLER_MAPPINGS = "lwjglControllerMappings";

	private static final String SORT_ENABLED = "SortEnabled";
	private static final String SORT_DIRECTION = "SortDirection";
	private static final String SORT_COLUMN = "SortColumn";

	private static String CONTROLLER_PROPERTY = "controller";
	private static String NAME_PROPERTY = "name";
	private static String INDEX_PROPERTY = "index";
	private static String VALUE_PROPERTY = "value";
	private static String JOY_PROPERTY = "joyst";
	private static String ROLE_PROPERTY = "role";
	
	private static String[] NAME_PROPERTY_ARRAY = { CONTROLLER_PROPERTY, NAME_PROPERTY, INDEX_PROPERTY, VALUE_PROPERTY, JOY_PROPERTY, ROLE_PROPERTY };
	static final int CONTROLLER_COLUMN = 0;
	static final int NAME_COLUMN = 1;
	static final int INDEX_COLUMN = 2;
	static final int VALUE_COLUMN = 3;
	static final int JOY_COLUMN = 4;
	static final int ROLE_COLUMN = 5;

	private final IMachine machine;

	private ISettingSection dialogSettings;

	private CTabFolder folder;
	private CTabItem guiItem;
	private CTabItem editItem;
	private TableViewer viewer;
	private Text editor1, editor2;
	private Composite buttonBar;
	
	private boolean sortDemos = true;
	private int sortDirection = 1;
	private int sortColumn = CONTROLLER_COLUMN;
	
	private Button rescanButton;
	private TableColumn ctrlColumn;
	private TableColumn nameColumn;
	private TableColumn indexColumn;
	private TableColumn valueColumn;
	private TableColumn joyColumn;
	private TableColumn roleColumn;
	private CLabel statusLabel;
	private Runnable scanTask;

	private IProperty joystickRescanProperty;
	private IProperty joystick1ConfigProperty;
	private IProperty joystick2ConfigProperty;
	
	private Map<ControllerIdentifier, Integer> joyFor = new HashMap<ControllerIdentifier, Integer>();
	private ControllerConfig fullConfig;
	private ControllerConfig config1, config2;
	
	private long lastUpdateTime;
	private Map<ControllerIdentifier, Float> lastValues = Collections.synchronizedMap(new HashMap<ControllerIdentifier, Float>());
	private TreeMap<Long, ControllerIdentifier> changedTimes = new TreeMap<Long, ControllerIdentifier>();
	

	public JoystickMappingDialog(Shell shell, IMachine machine_) {
		super(shell, SWT.NONE);
		this.machine = machine_;
		
		dialogSettings = machine.getSettings().getMachineSettings().getHistorySettings().
				findOrAddSection(SECTION_CONTROLLER_MAPPINGS);

		joystickRescanProperty = Settings.get(machine, SwtLwjglKeyboardHandler.settingJoystickRescan);

		joystick1ConfigProperty = Settings.get(machine, SwtLwjglKeyboardHandler.settingJoystick1Config);
		joystick2ConfigProperty = Settings.get(machine, SwtLwjglKeyboardHandler.settingJoystick2Config);

		shell.setText("Controller Mapper");
		
		GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(this);
		
		/////////
		
		Composite labelAndSelector = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(labelAndSelector);
		GridDataFactory.fillDefaults().applyTo(labelAndSelector);

		Label label = new Label(labelAndSelector, SWT.NONE);
		label.setText("Joystick: ");
		label.setFont(JFaceResources.getBannerFont());
		
		////////

		folder = new CTabFolder(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(folder);
		
		guiItem = new CTabItem(folder, SWT.NONE);
		guiItem.setText("Interactive");

		Composite tableComp = new Composite(folder, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(tableComp);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComp);
		
		viewer = createTable(tableComp);
		guiItem.setControl(tableComp);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());

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
				
				if (e1 instanceof ControllerIdentifier && e2 instanceof ControllerIdentifier) {
					ControllerIdentifier id1 = (ControllerIdentifier) e1;
					ControllerIdentifier id2 = (ControllerIdentifier) e2;

					int diff = 0;
					switch (sortColumn) {
					case CONTROLLER_COLUMN:
						diff = id1.controllerName.compareToIgnoreCase(id2.controllerName);
						break;
					case NAME_COLUMN:
						diff = id1.name.compareToIgnoreCase(id2.name);
						break;
					case INDEX_COLUMN:
						diff = id1.controllerName.compareToIgnoreCase(id2.controllerName);
						if (diff == 0)
							diff = id1.index - id2.index;
						break;
					case JOY_COLUMN: {
						Integer j1 = joyFor.get(id1); 
						Integer j2 = joyFor.get(id2); 
						diff = (j1 != null ? j1 : 0) - (j2 != null ? j2 : 0);
						break;
					}
					case ROLE_COLUMN: {
						diff = fullConfig.find(id1).ordinal() - fullConfig.find(id2).ordinal();
						break;
					}
					}
					return sortDirection * diff;
				} else if (e1 instanceof ControllerIdentifier) {
					return 1;
				} /* else if (e2 instanceof IDemo) */ {
					return -1;
				}
			}	
		});


		Text text = new Text(tableComp, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.NO_FOCUS);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
		text.setBackground(getBackground());
		text.setText("Use any connected controllers to see which components "
				+ "the sticks and buttons align with above."
				+ "\n\n"
				+ "Change the "	+ "'" + joyColumn.getText() + "' and "
				+ "'" + roleColumn.getText() + "' entries to remap those inputs "
				+ "to the directions or buttons of TI joystick #1 and #2."
				+ "\n\n"
				+ "(NOTE: you must restart V9t9 to detect controllers plugged or unplugged while running.)"
				);
		////
		
		editItem = new CTabItem(folder, SWT.NONE);
		editItem.setText("Edit");
		
		Composite editComp = new Composite(folder, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(editComp);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(editComp);
		
		editItem.setControl(editComp);
		
		label = new Label(editComp, SWT.NONE);
		label.setText("Joystick #1 Configuration");
		
		editor1 = new Text(editComp, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		editor1.setEditable(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(editor1);
		
		label = new Label(editComp, SWT.NONE);
		label.setText("Joystick #2 Configuration");
		
		editor2 = new Text(editComp, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		editor2.setEditable(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(editor2);
		
		statusLabel = new CLabel(editComp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(statusLabel);
		
		
		//////////

		buttonBar = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(3).equalWidth(false).applyTo(buttonBar);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonBar);

		rescanButton = new Button(buttonBar, SWT.PUSH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(rescanButton);

//		rescanButton.setImage(EmulatorGuiData.loadImage(getDisplay(), "icons/refresh.png"));
		rescanButton.setText("Revert to Default");
		rescanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean ret = MessageDialog.openConfirm(
						getShell(), 
						"Reset?", 
						"This will throw away any customizations and reset mappings to default.\n\nContinue?");
				if (ret) {
					joystickRescanProperty.setBoolean(true);
				}
			}
		});
		rescanButton.setEnabled(true);
		
		
		Label filler = new Label(buttonBar, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(filler);


		Map<Controller, Map<ControllerIdentifier, Component>> cmap = SwtLwjglKeyboardHandler.getSupportedControllerComponents();
		final Controller[] controllers = cmap.keySet().toArray(new Controller[cmap.size()]);
		final Map<Component, ControllerIdentifier> ids = new IdentityHashMap<Component, ControllerIdentifier>();

		// unified config
		fullConfig = new ControllerConfig();
		config1 = new ControllerConfig();
		config2 = new ControllerConfig();
		try {
			config1.fromString(joystick1ConfigProperty.getString());
			fullConfig.mergeFrom(config1);
		} catch (ControllerConfig.ParseException e) {
		}
		try {
			config2.fromString(joystick2ConfigProperty.getString());
			fullConfig.mergeFrom(config2);
		} catch (ControllerConfig.ParseException e) {
			
		}
		
		for (Controller controller : controllers) {
			for (Entry<ControllerIdentifier, Component> ent : cmap.get(controller).entrySet()) {
				ControllerIdentifier id = ent.getKey();
				ids.put(ent.getValue(), id);
				
				if (config1.getMap().containsKey(id)) {
					joyFor.put(id, 1);
				} else if (config2.getMap().containsKey(id)) {
					joyFor.put(id, 2);
				} else {
					joyFor.put(id, 0);
					fullConfig.map(id, JoystickRole.IGNORE);
				}
			}
		}

		// add any missing components to the full config
		
		scanTask = new Runnable() {

			private Runnable updater = null;
			
			private Set<ControllerIdentifier> changed = new HashSet<ControllerIdentifier>();
			
			@Override
			public void run() {
				
				changed.clear();
				
				for (Controller controller : controllers) {
					controller.poll();
				}
				
				for (Entry<Component, ControllerIdentifier> ent : ids.entrySet()) {
					ControllerIdentifier id = ent.getValue();
					Component component = ent.getKey();
						
					float data = ((int) Math.round(component.getPollData() * 8)) / 8.0f;
//						float data = component.getPollData();
					if (!lastValues.containsKey(id) || lastValues.get(id) != data) {
						changed.add(id);
					}
					lastValues.put(id, data);
				}

				synchronized (changedTimes) {
					boolean any = false;
					
					final long now = System.currentTimeMillis();
					for (ControllerIdentifier id : changed) {
						changedTimes.put(now, id);
						any = true;
					}
					
					if (any || lastUpdateTime < now + 3000) {
						if (updater == null) {
							updater = new Runnable() {
								@Override
								public void run() {
									if (isDisposed())
										return;
									
									Set<ControllerIdentifier> changed;
									synchronized (changedTimes) {
										updater = null;
										
										SortedMap<Long, ControllerIdentifier> stale = changedTimes.headMap(lastUpdateTime - 1000);
										SortedMap<Long, ControllerIdentifier> nowChanged = changedTimes.tailMap(lastUpdateTime);

										changed = new HashSet<ControllerIdentifier>(nowChanged.values());
										changed.addAll(stale.values());
				
										stale.clear();
									}
//									System.out.println(changed);
									viewer.update( changed.toArray(), null); // new String[] { JOY_PROPERTY, VALUE_PROPERTY });
									
									lastUpdateTime = now;
								}
							};
							Display.getDefault().asyncExec(updater);
						}
					}
				}
				
			}
			
		};
		machine.getFastMachineTimer().scheduleTask(scanTask, 5);
		
		joystick1ConfigProperty.addListener(this);
		joystick2ConfigProperty.addListener(this);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				joystick1ConfigProperty.removeListener(JoystickMappingDialog.this);
				joystick2ConfigProperty.removeListener(JoystickMappingDialog.this);
				
				machine.getFastMachineTimer().cancelTask(scanTask);
			}
		});
	
		
		hookActions();
		
		folder.setSelection(guiItem);
	
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				String text1 = joystick1ConfigProperty.getString();
				String text2 = joystick2ConfigProperty.getString();
				
				viewer.setInput(fullConfig);
				
				editor1.setText(text1);
				editor2.setText(text2);
			}
		});
	}

	/**
	 * 
	 */
	protected void updateEditors() {
		joystick1ConfigProperty.setString(config1.toString());
		joystick2ConfigProperty.setString(config2.toString());
	}

	protected TableViewer createTable(Composite parent) {
		final TableViewer viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		GridDataFactory.fillDefaults().grab(true,true).applyTo(table);

		ctrlColumn = new TableColumn(table, SWT.LEFT);
		ctrlColumn.setText("Controller");
		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Component");
		indexColumn = new TableColumn(table, SWT.LEFT);
		indexColumn.setText("Index");
		valueColumn = new TableColumn(table, SWT.LEFT);
		valueColumn.setText("Live Value");
		joyColumn = new TableColumn(table, SWT.LEFT);
		joyColumn.setText("Joystick");
		roleColumn = new TableColumn(table, SWT.LEFT);
		roleColumn.setText("Role");

		viewer.setColumnProperties(NAME_PROPERTY_ARRAY);
		
		viewer.setContentProvider(new ControllerIdentifierContentProvider());
		viewer.setLabelProvider(new ControllerIdentifierLabelProvider());
		
		CellEditor[] editors = new CellEditor[NAME_PROPERTY_ARRAY.length];
		editors[JOY_COLUMN] = new FriendlyComboBoxCellEditor(table,
				new String[] { "<none>", "1", "2" }, 
				SWT.READ_ONLY);
		
		String[] jsStrs = new String[JoystickRole.values().length];
		for (int i = 0; i < jsStrs.length; i++) {
			jsStrs[i] = JoystickRole.values()[i].toString();
		}
		editors[ROLE_COLUMN] = new FriendlyComboBoxCellEditor(table, 
				jsStrs, 
				SWT.READ_ONLY);
		
		viewer.setCellEditors(editors);
		viewer.setCellModifier(new JoystickRoleCellModifier());

		FocusCellOwnerDrawHighlighter highlighter = new FocusCellOwnerDrawHighlighter(viewer);
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewer, highlighter);
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(viewer);
	
		TableViewerEditor.create(viewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
		
		viewer.setUseHashlookup(true);
		
		
		addControlListener(new ControlAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
			 */
			@Override
			public void controlResized(ControlEvent e) {

				getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (isDisposed())
							return;
						
						int width = viewer.getControl().getSize().x - 6;
						
						ctrlColumn.setWidth(width / 4);
						nameColumn.setWidth(width / 8);
						indexColumn.setWidth(width / 8);
						valueColumn.setWidth(width / 8);
						joyColumn.setWidth(width / 8);
						roleColumn.setWidth(width / 4);

					}
				});
			}
		});

		//viewer.setInput(new Object());
		
		return viewer;
	}
	

	/**
	 * @author ejs
	 *
	 */
	private final class FriendlyComboBoxCellEditor extends ComboBoxCellEditor {
		/**
		 * @param parent
		 * @param items
		 * @param style
		 */
		private FriendlyComboBoxCellEditor(Composite parent, String[] items,
				int style) {
			super(parent, items, style);
		}

		protected void setAndApply(CCombo combo, int index) {
			// must set the selection before getting value
			combo.select(index);
			doSetValue(index);
			
			Object newValue = doGetValue();
			markDirty();
			boolean isValid = isCorrect(newValue);
			setValueValid(isValid);

		}

		protected void keyReleaseOccured(KeyEvent keyEvent) {
			CCombo combo = (CCombo) getControl();
			
			if (keyEvent.keyCode == SWT.PAGE_UP) {
				setAndApply(combo, 0);
			}
			else if (keyEvent.keyCode == SWT.PAGE_DOWN) {
				setAndApply(combo, combo.getItemCount() - 1);
			}
			super.keyReleaseOccured(keyEvent);
		}
	}

	/**
	 * @author ejs
	 *
	 */
	private final class JoystickRoleCellModifier implements ICellModifier {
		protected ControllerIdentifier getId(Object element) {
			if (element instanceof TableItem)
				element = ((TableItem) element).getData();
			if (element instanceof ControllerIdentifier)
				return (ControllerIdentifier) element;
			return null;
		}

		@Override
		public void modify(Object element, String property, Object value) {
			ControllerIdentifier id = getId(element);
			if (JOY_PROPERTY.equals(property)) {
				Integer oldFor = joyFor.get(id);
				Integer newFor = (Integer) value;
				joyFor.put(id, newFor);
				
				if (oldFor != newFor) {
					// unmap from old joystick
					if (oldFor == 1) {
						config1.remove(id);
					}
					else if (oldFor == 2) {
						config2.remove(id);
					}
					
					// place current value in new
					if (newFor == 1) {
						config1.map(id, fullConfig.find(id));
					}
					else if (newFor == 2) {
						config2.map(id, fullConfig.find(id));
					}

					updateEditors();
					viewer.update(id, null);
				}
			}
			else if (ROLE_PROPERTY.equals(property)) {
				JoystickRole role = JoystickRole.values()[(Integer) value];
				fullConfig.map(id, role);
				
				Integer curFor = joyFor.get(id);

				if (curFor != null) {
					if (curFor == 1) {
						config1.map(id, role);
					}
					else if (curFor == 2) {
						config2.map(id, role);
					}
				}
				updateEditors();
				viewer.update(id, null);
			}
		}

		@Override
		public Object getValue(Object element, String property) {
			ControllerIdentifier id = getId(element);
			if (JOY_PROPERTY.equals(property)) {
				Integer j = joyFor.get(id);
				if (j == null)
					j = 0;
				return j;
			}
			else if (ROLE_PROPERTY.equals(property)) {
				return fullConfig.find(id).ordinal();
			}
			return null;
		}

		@Override
		public boolean canModify(Object element, String property) {
			return JOY_PROPERTY.equals(property) || ROLE_PROPERTY.equals(property);
		}
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
				viewer.getTable().setSortColumn(viewer.getTable().getColumn(column));
				viewer.getTable().setSortDirection(SWT.DOWN);
			} else {
				if (sortDirection == 1) {
					sortDirection = -1;
					viewer.getTable().setSortDirection(SWT.UP);
				} else {
					sortDemos = false;
					viewer.getTable().setSortColumn(null);
					viewer.getTable().setSortDirection(SWT.NONE);
				}
			}
			dialogSettings.put(SORT_ENABLED, sortDemos);
			dialogSettings.put(SORT_DIRECTION, sortDirection);
			dialogSettings.put(SORT_COLUMN, sortColumn);
			viewer.refresh();
		}
	}

	protected void hookActions() {
		
		ctrlColumn.addSelectionListener(new SortHandler(CONTROLLER_COLUMN));
		nameColumn.addSelectionListener(new SortHandler(NAME_COLUMN));
		indexColumn.addSelectionListener(new SortHandler(INDEX_COLUMN));
		valueColumn.addSelectionListener(new SortHandler(VALUE_COLUMN));
		joyColumn.addSelectionListener(new SortHandler(JOY_COLUMN));
		roleColumn.addSelectionListener(new SortHandler(ROLE_COLUMN));

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
			}
		});
		viewer.addOpenListener(new IOpenListener() {
			
			public void open(OpenEvent event) {
			}
		});
		
		editor1.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateFromText(joystick1ConfigProperty, config1, editor1.getText());
			}
		});
		editor1.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				updateFromText(joystick1ConfigProperty, config1, editor1.getText());
			}
		});
		editor2.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateFromText(joystick2ConfigProperty, config2, editor2.getText());
			}
		});
		editor2.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				updateFromText(joystick2ConfigProperty, config2, editor2.getText());
			}
		});
		
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.properties.IPropertyListener#propertyChanged(ejs.base.properties.IProperty)
	 */
	@Override
	public void propertyChanged(IProperty property) {
		populateTable();
		if (property == joystick1ConfigProperty) {
			editor1.setText(property.getString());
		}
		else if (property == joystick2ConfigProperty) {
			editor2.setText(property.getString());
		}
	}
	
	protected void updateFromText(IProperty joystickProperty, ControllerConfig config, String text) {
		statusLabel.setImage(null);
		statusLabel.setText("");
		
		config.clear();
		
		try {
			config.fromString(text);
			
			Map<ControllerIdentifier, JoystickRole> map = fullConfig.getMap();
			fullConfig.clear();
			fullConfig.mergeFrom(config1);
			fullConfig.mergeFrom(config2);
			
			// restore unmapped entries
			for (Entry<ControllerIdentifier, Integer> ent : joyFor.entrySet()) {
				ControllerIdentifier id = ent.getKey();
				if ((ent.getValue() == null || ent.getValue() == 0)
						&& fullConfig.find(id) == JoystickRole.IGNORE) {
					JoystickRole role = map.get(id);
					fullConfig.map(id, role != null ? role : JoystickRole.IGNORE);
				}
			}
		} catch (ParseException e) {
			folder.setSelection(editItem);
			statusLabel.setImage(getDisplay().getSystemImage(SWT.ICON_ERROR));
			statusLabel.setText(e.getMessage());
		}

		populateTable();
		
		joystickProperty.removeListener(this);
		joystickProperty.setString(text);
		joystickProperty.addListener(this);
	}

	protected void populateTable() {
		viewer.setInput(fullConfig);
		viewer.refresh();
	}

	static class ControllerIdentifierContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		/* (non-Javadoc)IPropertyListener
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			return ((ControllerConfig) inputElement).getMap().keySet().toArray();
		}

	}
	
	class ControllerIdentifierLabelProvider extends LabelProvider implements ITableLabelProvider,
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
			if (!(element instanceof ControllerIdentifier))
				return String.valueOf(element);
			
			ControllerIdentifier id = (ControllerIdentifier) element;
			
			switch (columnIndex) {
			case CONTROLLER_COLUMN:
				return id.controllerName;
			case NAME_COLUMN:
				return id.name;
			case INDEX_COLUMN:
				return Integer.toString(id.index);
			case VALUE_COLUMN: {
				synchronized (lastValues) {
					Float val = lastValues.get(id);
					return val != null ? Float.toString(val) : "";
				}
			}
			case JOY_COLUMN: {
				Integer j = joyFor.get(id);
				return String.valueOf(j != null && j > 0 ? j : "<none>");
			}
			case ROLE_COLUMN:
				return String.valueOf(fullConfig.getMap().get(id));
			}
			return "";
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
		 */
		@Override
		public Color getForeground(Object element, int columnIndex) {
			Integer j = joyFor.get(element);
			if (j == null || j == 0) {
				return getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
		 */
		@Override
		public Color getBackground(Object element, int columnIndex) {
			if (changedTimes.values().contains(element))
				return getDisplay().getSystemColor(SWT.COLOR_YELLOW);
			
			else if (columnIndex != JOY_COLUMN && columnIndex != ROLE_COLUMN)
				return getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
				
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
		 */
		@Override
		public Font getFont(Object element, int columnIndex) {
			return null;
		}
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
				behavior.boundsPref = "ControllerWindowBounds";
				behavior.centering = Centering.CENTER;
				behavior.dismissOnClickOutside = false;
				behavior.defaultBounds = new Rectangle(0, 0, 800, 700);
			}
			public Control createContents(Shell shell) {
				return new JoystickMappingDialog(shell, machine);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}

	/**
	 * @return the machine
	 */
	public IMachine getMachine() {
		return machine;
	}


}
