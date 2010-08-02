/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

class InstLabelProvider extends BaseLabelProvider implements ITableLabelProvider, ITableColorProvider {

	public InstLabelProvider() {
	}
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		InstRow row = (InstRow)element;
		if (row == null) return null;
		switch (columnIndex) {
		case 0:
			return row.getAddress();
		case 1:
			return row.getInst();
		case 2:
			return row.getOp1();
		case 3:
			return row.getOp2();
		}
		return null;
	}

	public Color getForeground(Object element, int columnIndex) {
		return null;
	}
	
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}
}