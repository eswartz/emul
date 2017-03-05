/*
  ByteContentProvider.java

  (c) 2009-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.disk;

import java.io.IOException;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import v9t9.common.files.IEmulatedFile;
import v9t9.gui.client.swt.shells.disk.ByteContentViewer.ByteRow;

/**
 * This provides rows of @width bytes for each index of the memory viewer.
 * @author ejs
 *
 */
class ByteContentProvider implements ILazyContentProvider {

	private TableViewer tableViewer;
	private IEmulatedFile file;
	private int width;
	
	public ByteContentProvider(int width) {
		this.width = width;
	}
	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.tableViewer = (TableViewer) viewer;
		
		file = (IEmulatedFile) newInput;
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