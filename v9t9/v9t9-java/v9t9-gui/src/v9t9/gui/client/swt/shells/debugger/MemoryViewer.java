/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ejs.base.properties.IPersistable;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.HexUtils;
import ejs.base.utils.Pair;

import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryListener;
import v9t9.gui.EmulatorGuiData;
import v9t9.gui.common.FontUtils;

/**
 * @author Ed
 *
 */
public class MemoryViewer extends Composite implements IPersistable {

	private TableViewer byteTableViewer;
	private ComboViewer entryViewer;
	private final IMemory memory;
	private TimerTask refreshTask;
	protected MemoryRange currentRange;
	private Button refreshButton;
	protected boolean autoRefresh;
	private Button pinButton;
	private boolean pinMemory;
	private Button filterButton;
	private boolean filterMemory;
	private Font tableFont;

	public MemoryViewer(Composite parent, int style, IMemory memory, final Timer timer) {
		super(parent, style);
		this.memory = memory;

		setLayout(new GridLayout(2, false));
		
		createTable();
		
		memory.addListener(new IMemoryListener() {

			public void physicalMemoryMapChanged(IMemoryEntry entry) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						refreshEntryCombo();
						
					}
				});
			}
			public void logicalMemoryMapChanged(IMemoryEntry entry) {
				
			}
			
		});
		
		refreshTask = new TimerTask() {

			@Override
			public void run() {
				if (timer != null && autoRefresh && !isDisposed())
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							refreshViewer();
						}
					});
			}
			
		};
		timer.schedule(refreshTask, 0, 250);
		
		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				refreshTask.cancel();		
				tableFont.dispose();
			}
			
		});
	}

	/* (non-Javadoc)
	 * @see ejs.base.properties.IPersistable#saveState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection section) {
		section.put("Range", currentRange != null ? currentRange.toString() : null);
		section.put("AutoRefresh", autoRefresh);
		section.put("PinMemory", pinMemory);
		section.put("FilterMemory", filterMemory);
		
		TableItem item = byteTableViewer.getTable().getItem(new Point(0, 0));
		if (item != null && item.getData() instanceof MemoryRow) {
			section.put("FirstRowAddr", ((MemoryRow) item.getData()).getAddress());
		}
		// I can't seem to directly query the last row in the visible height of the table,
		// hence this loop...
		for (int y = 0; y < 256; y++) {
			item = byteTableViewer.getTable().getItem(new Point(0, byteTableViewer.getTable().getItemHeight() * y)); 
			if (item != null && item.getData() instanceof MemoryRow) {
				section.put("LastRowAddr", ((MemoryRow) item.getData()).getAddress());
			} else {
				break;
			}
		}
	}
	/* (non-Javadoc)
	 * @see ejs.base.properties.IPersistable#loadState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection section) {
		if (section == null)
			return;
		String range = section.get("Range");
		if (range != null)
			currentRange = MemoryRange.fromString(memory, range);
		autoRefresh = section.getBoolean("AutoRefresh");
		pinMemory = section.getBoolean("PinMemory");
		filterMemory = section.getBoolean("FilterMemory");
		
		final int visLoAddr = section.getInt("FirstRowAddr");
		final int visHiAddr = section.getInt("LastRowAddr");
		
		if (entryViewer != null) {
			final long timeout = System.currentTimeMillis() + 1000;
			final ControlListener resetRangeListener = new ControlAdapter() {
				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
				 */
				@Override
				public void controlResized(ControlEvent e) {
					if (System.currentTimeMillis() < timeout)
						scrollToActiveRegion(visLoAddr, visHiAddr);
					else
						byteTableViewer.getTable().removeControlListener(this);
				}
			}; 
			
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					refreshButton.setSelection(autoRefresh);
					pinButton.setSelection(pinMemory);
					filterButton.setSelection(filterMemory);
							

					// apply filter
					entryViewer.refresh();
					
					if (currentRange != null) {
						entryViewer.setSelection(new StructuredSelection(currentRange.getEntry()));
						changeCurrentRange(currentRange);
						byteTableViewer.getTable().addControlListener(resetRangeListener);
					}
					
				}
			});
			
		}
	}
	
	

	protected void scrollToActiveRegion(int lowRange, int hiRange) {
		int row = getMemoryRowIndex(lowRange);
		int visibleRows = byteTableViewer.getTable().getSize().y 
			/ byteTableViewer.getTable().getItemHeight();
		int endRow = getMemoryRowIndex(hiRange);
		
		Object elementAt;
		
		if (false) {
			if (byteTableViewer.getContentProvider() instanceof ILazyContentProvider) {
				try {
					((ILazyContentProvider)byteTableViewer.getContentProvider()).updateElement(row);
				} catch (Exception e) {
					// can throw if it's not gonna be visible
				}
			}
			elementAt = byteTableViewer.getElementAt(row);
			if (elementAt != null) {
				byteTableViewer.reveal(elementAt);
			}
		}
		if (visibleRows >= endRow - row) {
			if (byteTableViewer.getContentProvider() instanceof ILazyContentProvider) {
				try {
					((ILazyContentProvider)byteTableViewer.getContentProvider()).updateElement(endRow);
				} catch (Exception e) {
					// can throw if it's not gonna be visible	
				}
			}
			elementAt = byteTableViewer.getElementAt(endRow);
			if (elementAt != null) {
				byteTableViewer.reveal(elementAt);
			}
		}
		
	}

	protected final int getMemoryRowIndex(int addr) {
		return (addr - currentRange.addr) / 16;
	}
	protected final int getMemoryColumnIndex(int addr) {
		return (addr - currentRange.addr) % 16;
	}

	static class MemoryEntryLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			IMemoryEntry entry = (IMemoryEntry) element;
			return entry.getName() + " (" 
				+ entry.getDomain().getName() + " >" + HexUtils.toHex4((entry.getAddr() + entry.getAddrOffset())) + ")";
		}
	}
	protected void createTable() {
		entryViewer = new ComboViewer(this, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.NO_FOCUS);
		entryViewer.setContentProvider(new ArrayContentProvider());
		entryViewer.setLabelProvider(new MemoryEntryLabelProvider());
		entryViewer.setFilters(new ViewerFilter[] {
			new ViewerFilter() {

				@Override
				public boolean select(Viewer viewer, Object parentElement,
						Object element) {
					IMemoryEntry entry = (IMemoryEntry) element;
					if (!entry.hasReadAccess())
						return false;
					if (filterMemory && !entry.hasWriteAccess())
						return false;
					return true;
				}
				
			}
		});
		entryViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
				MemoryRange range;
				if (element instanceof IMemoryEntry) {
					range = new MemoryRange((IMemoryEntry) element);
				} else if (element instanceof MemoryRange) {
					range = (MemoryRange) element;
				} else {
					return;
				}
				changeCurrentRange(range);
			}
			
		});
		
		Composite buttonBar = new Composite(this, SWT.NO_FOCUS);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(buttonBar);
		buttonBar.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		filterButton = new Button(buttonBar, SWT.TOGGLE);
		filterButton.setImage(EmulatorGuiData.loadImage(getDisplay(), "icons/filter.png"));
		filterButton.setSize(24, 24);
		filterMemory = true;
		filterButton.setSelection(true);
		filterButton.setToolTipText("View only RAM entries if set");
		filterButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				filterMemory = filterButton.getSelection();
				entryViewer.refresh();
			}
		});

		
		refreshButton = new Button(buttonBar, SWT.TOGGLE);
		refreshButton.setImage(EmulatorGuiData.loadImage(getDisplay(), "icons/refresh.png"));
		refreshButton.setSize(24, 24);
		autoRefresh = true;
		refreshButton.setSelection(true);
		refreshButton.setToolTipText("Automatically refresh memory view if set");
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				autoRefresh = refreshButton.getSelection();
			}
		});
		
		pinButton = new Button(buttonBar, SWT.TOGGLE);
		pinButton.setImage(EmulatorGuiData.loadImage(getDisplay(), "icons/pin.png"));
		pinButton.setSize(24, 24);
		pinMemory = false;
		pinButton.setSelection(false);
		pinButton.setToolTipText("Pin view to scroll position if set, else, scroll to active memory");
		pinButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pinMemory = pinButton.getSelection();
			}
		});

		
		byteTableViewer = new TableViewer(this, SWT.V_SCROLL + SWT.BORDER + SWT.VIRTUAL + SWT.NO_FOCUS + SWT.FULL_SELECTION);
		byteTableViewer.setContentProvider(new MemoryContentProvider());
		byteTableViewer.setLabelProvider(new ByteMemoryLabelProvider(
				new Color(getDisplay(), new RGB(64, 64, 128)),
				getDisplay().getSystemColor(SWT.COLOR_RED)
				));
		
		refreshEntryCombo();
				
		final Table table = byteTableViewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(table);
		
		String[] props = new String[1 + 16 + 1];
		props[0] = "Addr";
		new TableColumn(table, SWT.CENTER).setText(props[0]);
		for (int i = 0; i < 16; i++) {
			String id = Integer.toHexString(i).toUpperCase();
			props[i + 1] = id;
			new TableColumn(table, SWT.CENTER).setText(id + " ");
		}
		props[17] = "0123456789ABCDEF";
		new TableColumn(table, SWT.CENTER).setText(props[17]);
		
		FontDescriptor fontDescriptor = FontUtils.getFontDescriptor(JFaceResources.getTextFont());
		fontDescriptor = fontDescriptor.increaseHeight(-2);
		tableFont = fontDescriptor.createFont(getDisplay());
		table.setFont(tableFont);
		
		GC gc = new GC(table);
		gc.setFont(tableFont);
		int width = gc.stringExtent("FFFF").x;
		gc.dispose();
		
		table.getColumn(0).setWidth(width);
		for (int i = 1; i <= 16; i++) {
			table.getColumn(i).setWidth(width / 2);
		}
		table.getColumn(17).setWidth(width * 2);
		
		for (TableColumn column : table.getColumns()) {
			column.pack();
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		CellEditor[] editors = new CellEditor[1+16+16];
		for (int i = 1; i < 17; i++) {
			editors[i] = new TextCellEditor(table);
		}
		
		byteTableViewer.setColumnProperties(props);
		byteTableViewer.setCellModifier(new ByteMemoryCellModifier(byteTableViewer));
		byteTableViewer.setCellEditors(editors);
		
		addTableContextMenu(table);
		
		table.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseHover(MouseEvent e) {
				TableItem item = table.getItem(new Point(e.x, e.y));
				if (item != null) {
					for (int i = 0; i <= 16; i++) {
						Rectangle bounds = item.getTextBounds(i);
						if (e.x >= bounds.x && e.x < bounds.x + bounds.width) {
							MemoryRange range = (MemoryRange) byteTableViewer.getInput();
							MemoryRow row = (MemoryRow) item.getData();
							if (i > 0) i--;
							int addr = row.getAddress() + i;
							
							// see if the address's symbol is known
							String descr = ">" + HexUtils.toHex4(addr);
							String symbol = getSymbolFor(range, addr);
							if (symbol != null) {
								descr += " = " + symbol;
							} else if ((addr & 1) != 0) {
								// hovering over byte?
								addr &= ~1;
								symbol = getSymbolFor(range, addr);
								if (symbol != null) {
									descr += " = " + symbol;
								}
							}
							
							// see if we can look up the word AT the address
							addr = range.getEntry().flatReadWord(addr);
							symbol = getSymbolFor(range, addr);
							if (symbol != null) {
								descr += "\n= >" + HexUtils.toHex4(addr) + " = " + symbol;
							}
							
							table.setToolTipText(descr);
							return;
						}
					}
				}
				setToolTipText(null);
			}

			private String getSymbolFor(MemoryRange range, int addr) {
				IMemoryEntry entry = range.getEntry();
				if (entry.getAddr() <= addr && entry.getAddr() + entry.getSize() > addr) {
					Pair<String, Short> info = entry.lookupSymbolNear((short) addr, 0x1000);
					if (info != null) {
						return info.first + (info.second == addr ? "" : " + " + HexUtils.toHex4(addr - info.second));
					}
				}
				String symbols = null;
				for (IMemoryEntry e : entry.getDomain().getFlattenedMemoryEntries()) {
					Pair<String, Short> info = e.lookupSymbolNear((short) addr, 0x100);
					if (info != null) {
						String sym = info.first + (info.second == addr ? "" : " + " + HexUtils.toHex4(addr - info.second));
						if (symbols != null)
							symbols += ", " + sym;
						else
							symbols = sym;
					}
				}
				return symbols;
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				setToolTipText(null);
			}
			@Override
			public void mouseExit(MouseEvent e) {
				setToolTipText(null);
			}
		});
	}


	private void addTableContextMenu(final Table table) {
		Menu menu = new Menu(table);
		MenuItem item;
		item = new MenuItem(menu, SWT.NONE);
		item.setText("Set start range");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentRange == null)
					return;
				int addr = table.getSelectionIndex() * 16;
				restrictRange(currentRange.getAddress() + addr, currentRange.getAddress() + currentRange.getSize());
			}
		});
		item = new MenuItem(menu, SWT.NONE);
		item.setText("Set end range");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentRange == null)
					return;
				int endAddr = (table.getSelectionIndex() + 1) * 16;
				restrictRange(currentRange.getAddress(), currentRange.getAddress() + endAddr);
			}
		});
		
		item = new MenuItem(menu, SWT.NONE);
		item.setText("Clear range");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentRange == null)
					return;
				changeCurrentRange(new MemoryRange(currentRange.getEntry()));
			}
		});
		table.setMenu(menu);
	}

	protected void changeCurrentRange(MemoryRange range) {
		byteTableViewer.getTable().setLayoutDeferred(true);
		byteTableViewer.setInput(range);
		currentRange = range;
		//for (TableColumn column : byteTableViewer.getTable().getColumns())
		//	column.pack();
		byteTableViewer.getTable().setLayoutDeferred(false);
		//MemoryViewer.this.getShell().layout(true, true);
		//MemoryViewer.this.getShell().pack();
		
	}

	protected void restrictRange(int addr, int endAddr) {
		MemoryRange range = new MemoryRange(
				currentRange.getEntry(),
				addr,
				endAddr - addr);
		changeCurrentRange(range);
	}

	private void refreshEntryCombo() {
		if (!entryViewer.getControl().isDisposed()) {
			List<IMemoryEntry> allEntries = new ArrayList<IMemoryEntry>();
			for (IMemoryDomain domain : memory.getDomains()) {
				allEntries.addAll(Arrays.asList(domain.getFlattenedMemoryEntries()));
			}
			entryViewer.setInput(allEntries.toArray());
		}
	}

	protected void refreshViewer() {
		if (!isDisposed() && isVisible() && currentRange != null) {
			int lowRange, hiRange;
			synchronized (currentRange) {
				lowRange = currentRange.getLowTouchRange();
				hiRange = currentRange.getHiTouchRange();
				if (lowRange <= hiRange) {
					if (!byteTableViewer.getTable().isDisposed()) {
						if (pinMemory) {
							byteTableViewer.setSelection(null);
						}
						/*
						for (int addr = lowRange; addr < hiRange; addr += 16) {
							int row = (lowRange - currentEntry.addr) / 16;
							Object elementAt = byteTableViewer.getElementAt(row);
							if (elementAt != null) {
								byteTableViewer.refresh(elementAt);
								if (addr == lowRange && !pinMemory) {
									byteTableViewer.reveal(elementAt);
								}
							}
						}*/
						
						if (!pinMemory) {
							scrollToActiveRegion(lowRange, hiRange);
						}
						byteTableViewer.refresh();
						currentRange.clearTouchRange();
					}
				}
			}
		}
	}
	
}
