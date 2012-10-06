/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.io.IOException;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import v9t9.common.files.EmulatedFile;
import v9t9.gui.client.swt.shells.ByteContentViewer.ByteRow;

/**
 * This provides rows of 16 bytes for each index of the memory viewer.
 * @author ejs
 *
 */
class ByteContentProvider implements ILazyContentProvider {

	private TableViewer tableViewer;
	private EmulatedFile file;
	
	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.tableViewer = (TableViewer) viewer;
		
		file = (EmulatedFile) newInput;
		if (file != null) {

			// clear
			tableViewer.setItemCount(0);
			// reset
			tableViewer.setItemCount(file.getFileSize() / 16);
		}
	}

	public void updateElement(int index) {
		if (file == null)
			return;
		
		//System.out.println(index);
		int addr = index * 16;
		ByteRow row = (ByteRow) tableViewer.getElementAt(index);
		if (row == null) {
			row = new ByteRow(addr, 16);
			try {
				file.readContents(row.getContent(), 0, addr, 16);
			} catch (IOException e) {
				
			}
		}
		
		tableViewer.replace(row, index);
	}
	
}