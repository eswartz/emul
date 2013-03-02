/*
  InstContentProvider.java

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
	public InstRow getLast() {
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