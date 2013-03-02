/*
  InstLabelProvider.java

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

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import v9t9.common.cpu.IInstructionEffectLabelProvider;
import v9t9.common.cpu.IInstructionEffectLabelProvider.Column;


public class InstLabelProvider extends BaseLabelProvider 
	implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {

	private Column[] columns;

	public InstLabelProvider(IInstructionEffectLabelProvider effectProvider) {
		this.columns = effectProvider.getColumns();
	}
	
	public int getColumnCount() {
		return columns.length;
	}
	
	public String getColumnLabel(int columnIndex) {
		return columns[columnIndex].label;
	}
	
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		InstRow row = (InstRow)element;
		if (row == null || columnIndex >= getColumnCount()) return null;
		return columns[columnIndex].getText(row.getBefore(), row.getAfter());
	}

	@Override
	public Color getForeground(Object element, int columnIndex) {
		return null;
	}
	
	@Override
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}
	
	@Override
	public Font getFont(Object element, int columnIndex) {
		return null;
	}

	/**
	 * @return
	 */
	public Column[] getColumns() {
		return columns;
	}
}