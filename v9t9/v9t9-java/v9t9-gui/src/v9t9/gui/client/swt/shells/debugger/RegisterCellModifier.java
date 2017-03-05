/*
  RegisterCellModifier.java

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