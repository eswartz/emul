/*
  DecodedTableLabelProvider.java

  (c) 2012 Edward Swartz

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

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import ejs.base.utils.HexUtils;

/**
 * @author ejs
 *
 */
public class DecodedTableLabelProvider extends BaseLabelProvider implements
		ITableLabelProvider {

	private final ILabelProvider contentLabelProvider;
	private final int chunkSize;

	/**
	 * @param contentLabelProvider 
	 * 
	 */
	public DecodedTableLabelProvider(ILabelProvider contentLabelProvider, int chunkSize) {
		this.contentLabelProvider = contentLabelProvider;
		this.chunkSize = chunkSize;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		DecodedRow row = (DecodedRow) element;
		IDecodedContent content = row.getContent();
		if (columnIndex == 0)
			 return HexUtils.toHex4(content.getAddr());
		if (columnIndex == 1) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < content.getSize(); i++) {
				if (i > 0 && i % chunkSize == 0)
					sb.append(' ');
				sb.append(HexUtils.toHex2(
						row.getBytes()[i]));
			}
			return sb.toString();
		}
		return contentLabelProvider.getText(content);
	}

}
