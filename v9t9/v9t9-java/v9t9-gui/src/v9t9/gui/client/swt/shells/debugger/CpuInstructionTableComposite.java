/*
  CpuInstructionTableComposite.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.gui.client.swt.shells.debugger;


import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.ejs.gui.common.FontUtils;

import v9t9.common.cpu.IInstructionEffectLabelProvider;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class CpuInstructionTableComposite extends CpuInstructionComposite {
	private Table instTable;
	private Font smallerFont;
	private Font tableFont;
	protected int pageSize = 1;
	private InstLabelProvider instLabelProvider;

	public CpuInstructionTableComposite(Composite parent, int style, IMachine machine_) {
		super(parent, style, machine_);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		///
		instTable = new Table(this, SWT.BORDER + SWT.VIRTUAL + SWT.NO_FOCUS + SWT.FULL_SELECTION);
//		instTable.setLabelProvider(new InstLabelProvider(
//				//getDisplay().getSystemColor(SWT.COLOR_RED)
//				));
		instLabelProvider = new InstLabelProvider(machine.getCpu().createInstructionEffectLabelProvider());
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(instTable);
		
		FontDescriptor fontDescriptor = FontUtils.getFontDescriptor(JFaceResources.getTextFont());
		//fontDescriptor = fontDescriptor.increaseHeight(-2);
		tableFont = fontDescriptor.createFont(getDisplay());
		FontDescriptor smallerFontDescriptor = fontDescriptor.increaseHeight(-2);
		smallerFont = smallerFontDescriptor.createFont(getDisplay());
		
		instTable.setFont(smallerFont);
		
		GC gc = new GC(getDisplay());
		gc.setFont(smallerFont);
		int charWidth = gc.stringExtent("M").x;
		gc.dispose();

		TableColumn column;

		for (IInstructionEffectLabelProvider.Column col : instLabelProvider.getColumns()) {
			column = new TableColumn(instTable, SWT.LEFT);
			column.setText(col.label);
			column.setMoveable(true);
			column.setWidth(charWidth * (col.width + 2));
		}

		instTable.setHeaderVisible(true);
		instTable.setLinesVisible(true);
		
		start();
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#setupEvents()
	 */
	@Override
	public void setupEvents() {
		instTable.addListener(SWT.Resize, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				pageSize = instTable.getClientArea().height / instTable.getItemHeight() - 1;
			}
		});
		instTable.addListener(SWT.SetData, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				if (pageSize == 0)
					return;
				
				TableItem item = (TableItem) event.item;
				int index = event.index;
				
				int start;
				int end;
				Object[] rows;
				synchronized (instHistory) {
					start = Math.min(instHistory.size(), index / pageSize * pageSize);
					end = Math.min (start + pageSize, Math.min(instHistory.size(), instTable.getItemCount ()));
					rows = instHistory.subList(start, end).toArray();
				}
				int columnCount = instTable.getColumnCount();
				for (int i = start; i < end; i++) {
					item = instTable.getItem (i);
					InstRow row = (InstRow) rows[i - start];
					for (int c = 0; c < columnCount; c++) {
						item.setText(c, instLabelProvider.getColumnText(row, c));
					}
				}
			}
		});
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				tableFont.dispose();
				smallerFont.dispose();

			}
		});
	}
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#go()
	 */
	@Override
	public void go() {
		//instTable.setItemCount(MAX_INST_HISTORY);
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#flush(java.util.LinkedList)
	 */
	@Override
	public void flush() {
		int count;
		synchronized (instHistory) {
			count = instHistory.size();
		}
		instTable.setItemCount(count);
		instTable.setSelection(count - 1); //new int[] { count - 1 });
	}
	
	protected void resizeTable() {
		for (TableColumn c : instTable.getColumns()) {
			c.pack();
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.debugger.CpuInstructionComposite#clear()
	 */
	@Override
	public void clear() {
		super.clear();
	}

}
