/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

class MemoryContentProvider implements ILazyContentProvider {

	MemoryRange range;
	private TableViewer tableViewer;
	
	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.tableViewer = (TableViewer) viewer;
		if (range != null)
			range.removeMemoryListener();
		
		range = (MemoryRange) newInput;
		if (range != null) {
			range.attachMemoryListener();

			// clear
			tableViewer.setItemCount(0);
			// reset
			tableViewer.setItemCount(range.getSize() / 16);
		}
	}

	public void updateElement(int index) {
		if (range == null)
			return;
		
		//System.out.println(index);
		int addr = index * 16;
		MemoryRow row = (MemoryRow) tableViewer.getElementAt(index);
		if (row == null)
			row = new MemoryRow(addr, range);
		
		tableViewer.replace(row, index);
	}
	
}