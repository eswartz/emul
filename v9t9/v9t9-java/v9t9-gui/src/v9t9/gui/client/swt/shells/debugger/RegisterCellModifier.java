/*
  RegisterCellModifier.java

  (c) 2009-2011 Edward Swartz

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

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Item;

import ejs.base.utils.HexUtils;


class RegisterCellModifier implements ICellModifier {
	private StructuredViewer viewer;
	private final int numDigits;

	public RegisterCellModifier(StructuredViewer viewer, int numDigits) {
		this.viewer = viewer;
		this.numDigits = numDigits;
	}

	/**
	 * Returns whether the property can be modified
	 * 
	 * @param element
	 *            the element
	 * @param property
	 *            the property
	 * @return boolean
	 */
	public boolean canModify(Object element, String property) {
		return property.equals("Value");
	}

	/**
	 * Returns the value for the property
	 * 
	 * @param element
	 *            the element
	 * @param property
	 *            the property
	 * @return Object
	 */
	public Object getValue(Object element, String property) {
		IRegister reg = (IRegister) element;
		return numDigits == 2 ? HexUtils.toHex2(reg.getValue()) : HexUtils.toHex4(reg.getValue());
	}

	/**
	 * Modifies the element
	 * 
	 * @param element
	 *            the element
	 * @param property
	 *            the property
	 * @param value
	 *            the value
	 */
	public void modify(Object element, String property, Object value) {
		if (element instanceof Item)
			element = ((Item) element).getData();

		IRegister reg = (IRegister) element;

		try {
			int val = Integer.parseInt(value.toString(), 16);
			
			reg.setValue(val);
			
			viewer.refresh(element);
		} catch (NumberFormatException e) {
			
		}
	}
}