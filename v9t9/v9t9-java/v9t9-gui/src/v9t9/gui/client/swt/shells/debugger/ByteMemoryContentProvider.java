/*
  ByteMemoryContentProvider.java

  (c) 2009-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.debugger;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * This provides rows of bytes for each index of the memory viewer.
 * @author ejs
 *
 */
class ByteMemoryContentProvider implements ILazyContentProvider {

	private MemoryRange range;
	private MemoryRangeChanges changes;
	private TableViewer tableViewer;
	private int size;
	
	public ByteMemoryContentProvider(int size) {
		this.size = size;
		
	}
	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.tableViewer = (TableViewer) viewer;
		if (changes != null)
			changes.removeMemoryListener();
		
		range = (MemoryRange) newInput;
		if (range != null) {
			changes = new MemoryRangeChanges(range);
			changes.attachMemoryListener();

			// clear
			tableViewer.setItemCount(0);
			// reset
			tableViewer.setItemCount(range.getSize() / size);
		}
	}

	public void updateElement(int index) {
		MemoryRow row = getElementFor(index);
		if (row == null)
			return;
		
		tableViewer.replace(row, index);
	}
	public MemoryRow getElementFor(int index) {
		if (range == null)
			return null;
		
		//System.out.println(index);
		int addr = index * size;
		MemoryRow row = (MemoryRow) tableViewer.getElementAt(index);
		if (row == null || changes.isTouched(addr, addr + size))
			row = new MemoryRow(addr, range);
		
		return row;
	}


	/**
	 * 
	 */
	public void refresh() {
		if (changes != null)
			changes.fetchChanges();
	}
	/**
	 * @return
	 */
	public MemoryRangeChanges getChanges() {
		return changes;
	}
	
}