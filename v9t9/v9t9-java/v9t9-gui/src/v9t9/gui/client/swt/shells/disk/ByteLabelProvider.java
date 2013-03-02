/*
  ByteLabelProvider.java

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

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import v9t9.gui.client.swt.shells.disk.ByteContentViewer.ByteRow;
import ejs.base.utils.HexUtils;


class ByteLabelProvider extends BaseLabelProvider implements ITableLabelProvider, ITableColorProvider {
	private int width;

	public ByteLabelProvider(int width) {
		this.width = width;
	}
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		ByteRow row = (ByteRow)element;
		if (columnIndex == 0)
			return HexUtils.toHex4(row.getAddress());
		else if (columnIndex <= width)
			return HexUtils.toHex2(row.getByte(columnIndex - 1));
		else {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < width; i++) {
				builder.append(row.getChar(i));
			}
			return builder.toString();
		}
	}

	public Color getForeground(Object element, int columnIndex) {
//		if (columnIndex >= 1 && columnIndex <= width
//				&& ((columnIndex - 1) / 2) % 2 == 0)
//			return alternatingWordForeground;
		return null;
	}
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}
}