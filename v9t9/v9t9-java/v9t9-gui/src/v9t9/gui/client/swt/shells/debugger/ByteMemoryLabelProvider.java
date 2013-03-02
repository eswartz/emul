/*
  ByteMemoryLabelProvider.java

  (c) 2009-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.debugger;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import ejs.base.utils.HexUtils;


class ByteMemoryLabelProvider extends BaseLabelProvider implements ITableLabelProvider, ITableColorProvider {
	private Color changedMemoryBackground;
	private Color alternatingWordForeground;
	private ByteMemoryContentProvider contentProvider;

	public ByteMemoryLabelProvider(Color alternatingWordForeground, Color changedMemoryBackground,
			ByteMemoryContentProvider contentProvider) {
		this.alternatingWordForeground = alternatingWordForeground;
		this.changedMemoryBackground = changedMemoryBackground;
		this.contentProvider = contentProvider;
	}
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		MemoryRow row = (MemoryRow)element;
		if (columnIndex == 0)
			return HexUtils.toHex4(row.getAddress());
		else if (columnIndex <= 16)
			return HexUtils.toHex2(row.getByte(columnIndex - 1));
		else {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < 16; i++) {
				builder.append(row.getChar(i));
			}
			return builder.toString();
		}
	}

	public Color getForeground(Object element, int columnIndex) {
		if (columnIndex >= 1 && columnIndex <= 16
				&& ((columnIndex - 1) / 2) % 2 == 0)
			return alternatingWordForeground;
		return null;
	}
	
	public Color getBackground(Object element, int columnIndex) {
		MemoryRow row = (MemoryRow)element;
		Color color = null;
		if (columnIndex > 0 && columnIndex <= 16) {
			Integer addr = row.getAddress() + columnIndex - 1;
			
			if (contentProvider.getChanges().getAndResetChanged(addr)) {
				color = changedMemoryBackground;
			}
		}
		return color;
	}
}