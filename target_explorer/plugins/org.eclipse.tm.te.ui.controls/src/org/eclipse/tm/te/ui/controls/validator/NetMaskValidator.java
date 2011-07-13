/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls.validator;

/**
 * Target Explorer: Netmask validator.
 */
public class NetMaskValidator extends NameOrIPValidator {

	/**
	 * Constructor.
	 *
	 * @param attributes Attributes for the validator.
	 */
	public NetMaskValidator(int attributes) {
		super(ATTR_IP);
		if (isAttribute(ATTR_MANDATORY, attributes)) {
			addAttribute(ATTR_MANDATORY);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.NameOrIPValidator#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String netMask) {
		if (super.isValid(netMask)) {
			// check the netmask
			String[] bytes = netMask.split("."); //$NON-NLS-1$
			int oldByte = 255;
			int actByte;
			for (int i=0; i<bytes.length; i++) {
				actByte = Integer.parseInt(bytes[i]);
				if ((oldByte < 255 &&  actByte > 0) ||
					(oldByte == 255 && !isValidNetMaskByte(actByte))) {
					return false;
				}
				oldByte = actByte;
			}
			return true;
		}
		return false;
	}

	/**
	 * Check one byte of a netmask if valid.
	 * A valid netmask byte should match the regular expression 1*0* and
	 * so can only be a value in the set {0,128,192,224,240,248,252,254,255}.
	 */
	private boolean isValidNetMaskByte(int netMaskByte) {
		switch (netMaskByte) {
			case 0:
			case 128:
			case 192:
			case 224:
			case 240:
			case 248:
			case 252:
			case 254:
			case 255:
				return true;
			default:
				return false;
		}
	}
}
