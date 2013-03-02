/*
  ByteContentProvider.java

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
package v9t9.gui.client.swt.shells.disk;

import java.io.IOException;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import v9t9.common.files.EmulatedFile;
import v9t9.gui.client.swt.shells.disk.ByteContentViewer.ByteRow;

/**
 * This provides rows of @width bytes for each index of the memory viewer.
 * @author ejs
 *
 */
class ByteContentProvider implements ILazyContentProvider {

	private TableViewer tableViewer;
	private EmulatedFile file;
	private int width;
	
	public ByteContentProvider(int width) {
		this.width = width;
	}
	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.tableViewer = (TableViewer) viewer;
		
		file = (EmulatedFile) newInput;
		if (file != null) {

			// clear
			tableViewer.setItemCount(0);
			// reset
			tableViewer.setItemCount(file.getFileSize() / width);
		}
	}

	public void updateElement(int index) {
		if (file == null)
			return;
		
		//System.out.println(index);
		int addr = index * width;
		ByteRow row = (ByteRow) tableViewer.getElementAt(index);
		if (row == null) {
			row = new ByteRow(addr, width);
			try {
				file.readContents(row.getContent(), 0, addr, width);
			} catch (IOException e) {
				
			}
		}
		
		tableViewer.replace(row, index);
	}
	
}