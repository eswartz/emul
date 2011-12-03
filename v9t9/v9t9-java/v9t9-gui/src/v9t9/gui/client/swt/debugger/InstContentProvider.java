/**
 * 
 */
package v9t9.gui.client.swt.debugger;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

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
		if (insts.size() > 100000) {
			int cutoff = insts.size() / 2;
			if (Display.getCurrent() != null)
				tableViewer.remove(insts.subList(0, cutoff).toArray());
			insts = new ArrayList<InstRow>(insts.subList(cutoff, insts.size()));
		}
		insts.add(row);
		if (Display.getCurrent() != null)
			tableViewer.add(row);
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

	public void removeInstRow(InstRow row) {
		insts.remove(row);
		if (Display.getCurrent() != null)
			tableViewer.remove(row);
	}
	/**
	 * 
	 */
	public void clear() {
		insts.clear();
		if (Display.getCurrent() != null)
			tableViewer.setItemCount(0);
	}
}