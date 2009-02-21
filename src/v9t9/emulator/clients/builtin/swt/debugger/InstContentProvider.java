/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

class InstContentProvider implements ILazyContentProvider {

	private List<InstRow> insts;
	private TableViewer tableViewer;
	
	/**
	 * 
	 */
	public InstContentProvider() {
		insts = new ArrayList<InstRow>(256);
	}
	
	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.tableViewer = (TableViewer) viewer;
		
		// clear
		tableViewer.setItemCount(0);
		insts.clear();
	}

	public void updateElement(int index) {
		if (index < insts.size()) {
			tableViewer.replace(insts.get(index), index);
		}
	}
	
	public void addInstRow(final InstRow row) {
		insts.add(row);
	}
	
	public void refreshTable() {
		//tableViewer.setItemCount(insts.size());
	}
	
	public int getCount() {
		return insts.size();
	}

	/**
	 * @return
	 */
	public Object getLast() {
		return insts.size() > 0 ? insts.get(insts.size() - 1) : null;
	}
}