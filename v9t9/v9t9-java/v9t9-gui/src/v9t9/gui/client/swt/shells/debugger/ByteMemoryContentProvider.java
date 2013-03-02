/*
  ByteMemoryContentProvider.java

  (c) 2009-2012 Edward Swartz

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
		if (range == null)
			return;
		
		//System.out.println(index);
		int addr = index * size;
		MemoryRow row = (MemoryRow) tableViewer.getElementAt(index);
		if (row == null || changes.isTouched(addr, addr + size))
			row = new MemoryRow(addr, range);
		
		tableViewer.replace(row, index);
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