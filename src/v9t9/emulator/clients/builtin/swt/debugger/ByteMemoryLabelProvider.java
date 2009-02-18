/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;

import java.util.Set;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import v9t9.utils.Utils;

class ByteMemoryLabelProvider extends BaseLabelProvider implements ITableLabelProvider, ITableColorProvider {
	private Color changedMemoryBackground;

	public ByteMemoryLabelProvider(Color changedMemoryBackground) {
		this.changedMemoryBackground = changedMemoryBackground;
	}
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		MemoryRow row = (MemoryRow)element;
		if (columnIndex == 0)
			return Utils.toHex4(row.getAddress());
		else if (columnIndex <= 16)
			return Utils.toHex2(row.getByte(columnIndex - 1));
		else {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < 16; i++) {
				builder.append(row.getChar(i));
			}
			return builder.toString();
		}
	}

	public Color getForeground(Object element, int columnIndex) {
		return null;
	}
	
	public Color getBackground(Object element, int columnIndex) {
		MemoryRow row = (MemoryRow)element;
		Color color = null;
		if (columnIndex > 0 && columnIndex <= 16) {
			Integer addr = row.getAddress() + columnIndex - 1;
			
			if (row.getRange().getAndResetChanged(addr)) {
				color = changedMemoryBackground;
			}
		}
		return color;
	}
}