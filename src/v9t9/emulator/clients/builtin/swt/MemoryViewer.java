/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import v9t9.emulator.hardware.V9t9;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryListener;
import v9t9.engine.memory.MemoryDomain.MemoryWriteListener;
import v9t9.utils.Utils;

/**
 * @author Ed
 *
 */
public class MemoryViewer extends Composite {

	private TableViewer byteTableViewer;
	private ComboViewer entryViewer;
	private final Memory memory;
	protected int lowRange;
	protected int hiRange;
	private Timer timer;
	private TimerTask refreshTask;
	private MemoryWriteListener memoryWriteListener;
	protected MemoryEntry currentEntry;
	private Button refreshButton;
	protected boolean autoRefresh;
	private Button pinButton;
	private boolean pinMemory;

	public MemoryViewer(Composite parent, int style, Memory memory) {
		super(parent, style);
		this.memory = memory;
		
		setLayout(new GridLayout(2, false));
		
		createByteTable();
		
		memory.addListener(new MemoryListener() {

			public void physicalMemoryMapChanged(MemoryEntry entry) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						refreshEntryCombo();
						
					}
				});
			}
			public void logicalMemoryMapChanged(MemoryEntry entry) {
				
			}
			
		});
		
		timer = new Timer();
		refreshTask = new TimerTask() {

			@Override
			public void run() {
				if (autoRefresh && !isDisposed())
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (lowRange < hiRange && !isDisposed() && isVisible()) {
								if (!byteTableViewer.getTable().isDisposed()) {
									synchronized (byteTableViewer) {
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
											int row = (lowRange - currentEntry.addr) / 16;
											if (byteTableViewer.getContentProvider() instanceof ILazyContentProvider) {
												try {
													((ILazyContentProvider)byteTableViewer.getContentProvider()).updateElement(row);
												} catch (Exception e) {
													
												}
											}
											Object elementAt = byteTableViewer.getElementAt(row);
											if (elementAt != null) {
												byteTableViewer.reveal(elementAt);
											}
											int visibleRows = byteTableViewer.getTable().getSize().y 
												/ byteTableViewer.getTable().getItemHeight();
											int endRow = (hiRange - currentEntry.addr) / 16;
											if (visibleRows >= endRow - row) {
												if (byteTableViewer.getContentProvider() instanceof ILazyContentProvider) {
													try {
														((ILazyContentProvider)byteTableViewer.getContentProvider()).updateElement(endRow);
													} catch (Exception e) {
														
													}
												}
												elementAt = byteTableViewer.getElementAt(endRow);
												if (elementAt != null) {
													byteTableViewer.reveal(elementAt);
												}
											}
										}
										byteTableViewer.refresh();
										lowRange = 0xffff;
										hiRange = 0;
									}
								}
							}
						}
					});
			}
			
		};
		timer.scheduleAtFixedRate(refreshTask, 0, 250);
		
	}

	@Override
	public void dispose() {
		refreshTask.cancel();
		timer.cancel();
		super.dispose();
	}
	
	protected void createByteTable() {
		entryViewer = new ComboViewer(this, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.NO_FOCUS);
		entryViewer.setContentProvider(new ArrayContentProvider());
		entryViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				MemoryEntry entry = (MemoryEntry) element;
				return entry.getName() + "(" + Utils.toHex4(entry.addr + entry.addrOffset) + ")";
			}
		});
		entryViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof MemoryEntry && e2 instanceof MemoryEntry) {
					MemoryEntry me1 = (MemoryEntry) e1;
					MemoryEntry me2 = (MemoryEntry) e2;
					int isRam1 = me1.hasWriteAccess() ? 0 : 1;
					int isRam2 = me2.hasWriteAccess() ? 0 : 1;
					return isRam1 - isRam2;
				}
				return super.compare(viewer, e1, e2);
			}
		});
		
		entryViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				MemoryEntry entry = (MemoryEntry) ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (entry != null) {
					byteTableViewer.getTable().setLayoutDeferred(true);
					byteTableViewer.setInput(entry);
					for (TableColumn column : byteTableViewer.getTable().getColumns())
						column.pack();
					byteTableViewer.getTable().setLayoutDeferred(false);
					MemoryViewer.this.getShell().pack();
				}
			}
			
		});
		
		Composite buttonBar = new Composite(this, SWT.NO_FOCUS);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(buttonBar);
		buttonBar.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		refreshButton = new Button(buttonBar, SWT.TOGGLE);
		refreshButton.setImage(new Image(getDisplay(), V9t9.getDataFile("icons/refresh.png").getAbsolutePath()));
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
		pinButton.setImage(new Image(getDisplay(), V9t9.getDataFile("icons/pin.png").getAbsolutePath()));
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

		
		byteTableViewer = new TableViewer(this, SWT.V_SCROLL + SWT.BORDER + SWT.VIRTUAL + SWT.NO_FOCUS);
		byteTableViewer.setContentProvider(new MemoryContentProvider());
		byteTableViewer.setLabelProvider(new ByteMemoryLabelProvider());
		
		refreshEntryCombo();
				
		Table table = byteTableViewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(table);
		
		String[] props = new String[1 + 16 + 1];
		props[0] = "Addr";
		new TableColumn(table, SWT.CENTER).setText(props[0]);
		for (int i = 0; i < 16; i++) {
			String id = Integer.toHexString(i).toUpperCase();
			props[i + 1] = id;
			new TableColumn(table, SWT.CENTER | SWT.NO_FOCUS).setText(id);
		}
		props[17] = "0123456789ABCDEF";
		new TableColumn(table, SWT.NO_FOCUS | SWT.CENTER).setText(props[17]);
		
		// hmmm... FontRegister.createFont() is busted
		Font textFont = JFaceResources.getTextFont();
		FontData[] fontData = textFont.getFontData();
		textFont.dispose();
		int len = 0;
		while (len < fontData.length && fontData[len] != null) 
			len++;
		FontData[] fontData2 = new FontData[len];
		System.arraycopy(fontData, 0, fontData2, 0, len);
		///
		
		FontDescriptor fontDescriptor = FontDescriptor.createFrom(fontData2);
		fontDescriptor = fontDescriptor.increaseHeight(-2);
		table.setFont(fontDescriptor.createFont(getDisplay()));
		
		for (int i = 0; i < 18; i++) {
			table.getColumn(i).pack();
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

		memoryWriteListener = new MemoryWriteListener() {

			public void changed(MemoryEntry entry, int addr) {
				if (currentEntry.domain == entry.domain && currentEntry.contains(addr)) {
					synchronized (byteTableViewer) {
						lowRange = Math.min(lowRange, addr);
						hiRange = Math.max(hiRange, addr);
					}
				}
			}
			
		};
	}

	private void refreshEntryCombo() {
		if (!entryViewer.getControl().isDisposed()) {
			List<MemoryEntry> allEntries = new ArrayList<MemoryEntry>();
			for (MemoryDomain domain : memory.getDomains()) {
				allEntries.addAll(Arrays.asList(domain.getFlattenedMemoryEntries()));
			}
			entryViewer.setInput(allEntries.toArray());
		}
	}

	static class MemoryRow {
		private final MemoryEntry entry;
		private int baseaddr;

		public MemoryRow(int baseaddr, MemoryEntry entry) {
			this.entry = entry;
			this.baseaddr = baseaddr;
		}

		

		public int getAddress() {
			return baseaddr + entry.addr;
		}

		public int getByte(int column) {
			if (baseaddr + column < entry.size)
				return entry.flatReadByte(baseaddr + entry.addr + column);
			else
				return 0;
		}

		public char getChar(int column) {
			int b = getByte(column) & 0xff;
			return b > 32 && b < 127 ? (char)b : '.';
		}



		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + baseaddr;
			result = prime * result + ((entry == null) ? 0 : entry.hashCode());
			return result;
		}



		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			MemoryRow other = (MemoryRow) obj;
			if (baseaddr != other.baseaddr) {
				return false;
			}
			if (entry == null) {
				if (other.entry != null) {
					return false;
				}
			} else if (!entry.equals(other.entry)) {
				return false;
			}
			return true;
		}
		
		
	}
	class MemoryContentProvider implements ILazyContentProvider {

		MemoryEntry entry;
		private TableViewer tableViewer;
		
		public void dispose() {
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.tableViewer = (TableViewer) viewer;
			if (entry != null)
				entry.domain.setWriteListener(null);
			
			entry = (MemoryEntry) newInput;
			if (entry != null) {
				// clear
				tableViewer.setItemCount(0);
				// reset
				tableViewer.setItemCount(entry.size / 16);
				entry.domain.setWriteListener(memoryWriteListener);
				currentEntry = entry;
			}
		}

		public void updateElement(int index) {
			if (entry == null)
				return;
			
			//System.out.println(index);
			int addr = index * 16;
			MemoryRow row = (MemoryRow) tableViewer.getElementAt(index);
			if (row == null)
				row = new MemoryRow(addr, entry);
			
			tableViewer.replace(row, index);
		}
		
	}
	
	class ByteMemoryLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			MemoryRow row = (MemoryRow)element;
			if (columnIndex == 0)
				return Utils.toHex4(row.getAddress());
			else if (columnIndex <= 16)
				return Utils.toHex2(row.getByte(columnIndex - 1));
			else {
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < 16; i++) {
					builder.append(row.getChar(i));
				}
				return builder.toString();
			}
		}

	}

	class ByteMemoryCellModifier implements ICellModifier {
		private StructuredViewer viewer;

		public ByteMemoryCellModifier(StructuredViewer viewer) {
			this.viewer = viewer;
		}

		/**
		 * Returns whether the property can be modified
		 * 
		 * @param element
		 *            the element
		 * @param property
		 *            the property
		 * @return boolean
		 */
		public boolean canModify(Object element, String property) {
			return currentEntry.hasWriteAccess();
		}

		/**
		 * Returns the value for the property
		 * 
		 * @param element
		 *            the element
		 * @param property
		 *            the property
		 * @return Object
		 */
		public Object getValue(Object element, String property) {
			MemoryRow row = (MemoryRow) element;
			
			int offset = Integer.parseInt(property, 16);
			return Utils.toHex2(row.getByte(offset));
		}

		/**
		 * Modifies the element
		 * 
		 * @param element
		 *            the element
		 * @param property
		 *            the property
		 * @param value
		 *            the value
		 */
		public void modify(Object element, String property, Object value) {
			if (element instanceof Item)
				element = ((Item) element).getData();

			MemoryRow row = (MemoryRow) element;
			
			int addr = row.getAddress();
			int offset = Integer.parseInt(property, 16);

			byte byt = (byte) Integer.parseInt(value.toString(), 16);
			
			currentEntry.flatWriteByte(addr + offset, byt);
			
			viewer.refresh(element);
		}
	}
	
	}
