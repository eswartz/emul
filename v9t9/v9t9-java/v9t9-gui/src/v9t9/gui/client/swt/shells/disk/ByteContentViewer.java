/*
  ByteContentViewer.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.disk;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.ejs.gui.common.FontUtils;

import v9t9.common.files.IEmulatedFile;

/**
 * @author Ed
 *
 */
public class ByteContentViewer extends Composite {

	final int WIDTH = 16;
	
	public static class ByteRow {
		private int address;
		private byte[] content;
		
		public ByteRow(int address, int length) {
			this.address = address;
			this.content = new byte[length];
		}

		public int getAddress() {
			return address;
		}
		
		public byte[] getContent() {
			return content;
		}

		public final byte getByte(int column) {
			return content[column];
		}
		public final char getChar(int column) {
			int b = getByte(column) & 0xff;
			return b >= 32 && b < 127 ? (char)b : '.';
		}
	}
	
	private StackLayout tableLayout;
	private TableViewer byteTableViewer;
	
	private Font tableFont;
	private ByteLabelProvider byteMemoryLabelProvider;
	private Composite tableComposite;
	private int currentAddr;

	public ByteContentViewer(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(2, false));
		
		createUI();
		
		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
//				refreshTask.cancel();		
				tableFont.dispose();
			}
			
		});
	}

	protected void scrollByteViewerToActiveRegion(int lowRange, int hiRange) {
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
		return (addr - currentAddr) / WIDTH;
	}
	protected final int getMemoryColumnIndex(int addr) {
		return (addr - currentAddr) % WIDTH;
	}

	protected void createUI() {
		FontDescriptor fontDescriptor = FontUtils.getFontDescriptor(JFaceResources.getTextFont());
		fontDescriptor = fontDescriptor.increaseHeight(-2);
		tableFont = fontDescriptor.createFont(getDisplay());
		
		tableComposite = new Composite(this, SWT.NONE);
		tableLayout = new StackLayout();
		tableComposite.setLayout(tableLayout);
		
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(tableComposite);
		
		byteMemoryLabelProvider = new ByteLabelProvider(WIDTH);
		
		createByteTableViewer(tableComposite);
		
		tableLayout.topControl = byteTableViewer.getControl();
	}

	protected void createByteTableViewer(Composite parent) {
		byteTableViewer = new TableViewer(parent, SWT.V_SCROLL + SWT.BORDER + SWT.VIRTUAL 
				+ SWT.NO_FOCUS + SWT.FULL_SELECTION);
		
		byteTableViewer.setContentProvider(new ByteContentProvider(WIDTH));
		
		byteTableViewer.setLabelProvider(byteMemoryLabelProvider);
		
				
		final Table table = byteTableViewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(table);
		
		String[] props = new String[1 + WIDTH + 1];
		props[0] = "Addr";
		new TableColumn(table, SWT.CENTER).setText(props[0]);
		for (int i = 0; i < WIDTH; i++) {
			String id = Integer.toHexString(i).toUpperCase();
			props[i + 1] = id;
			new TableColumn(table, SWT.CENTER).setText(id + " ");
		}
		StringBuilder sb = new StringBuilder();
		for (int w = 0; w < WIDTH; w++) {
			sb.append("0123456789ABCDEF".charAt(w&15));
		}
		props[WIDTH+1] = sb.toString();
		new TableColumn(table, SWT.CENTER).setText(props[WIDTH+1]);
		
		table.setFont(tableFont);
		
		GC gc = new GC(table);
		gc.setFont(tableFont);
		int width = gc.stringExtent("FFFFF").x;
		gc.dispose();
		
		table.getColumn(0).setWidth(width);
		for (int i = 1; i <= WIDTH; i++) {
			table.getColumn(i).setWidth(width / 2);
		}
		table.getColumn(WIDTH+1).setWidth(width * 2);
		
		for (TableColumn column : table.getColumns()) {
			column.pack();
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		CellEditor[] editors = new CellEditor[1+WIDTH+WIDTH];
		for (int i = 1; i < WIDTH+1; i++) {
			editors[i] = new TextCellEditor(table);
		}
		
		byteTableViewer.setColumnProperties(props);
//		byteTableViewer.setCellModifier(new ByteMemoryCellModifier(byteTableViewer));
//		byteTableViewer.setCellEditors(editors);
		
		currentAddr = 0;
	}

	public void setFile(IEmulatedFile file) {
		byteTableViewer.setInput(file);
	}
}
