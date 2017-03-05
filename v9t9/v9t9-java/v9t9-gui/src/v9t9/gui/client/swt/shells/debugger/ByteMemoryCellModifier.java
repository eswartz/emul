/*
  ByteMemoryCellModifier.java

  (c) 2009-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.debugger;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Item;

import ejs.base.utils.HexUtils;


class ByteMemoryCellModifier implements ICellModifier {
	private StructuredViewer viewer;

	public ByteMemoryCellModifier(StructuredViewer viewer) {
		this.viewer = viewer;
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
		MemoryRow row = (MemoryRow) element;
		try {
			int offset = Integer.parseInt(property, 16);
			return row.getRange().canModify(row.getAddress() + offset);
		} catch (NumberFormatException e) {
			return false;
		}
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
		MemoryRow row = (MemoryRow) element;
		
		try {
			int offset = Integer.parseInt(property, 16);
			return HexUtils.toHex2(row.getByte(offset));
		} catch (NumberFormatException e) {
			return null;
		}
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

		MemoryRow row = (MemoryRow) element;

		try {
			int offset = Integer.parseInt(property, 16);
			byte byt = (byte) Integer.parseInt(value.toString(), 16);
			
			row.putByte(offset, byt);
			
			viewer.refresh(element);
		} catch (NumberFormatException e) {
			
		}
	}
}