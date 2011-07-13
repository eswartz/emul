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
 * Target Explorer: Base class for verify listener.
 */
public abstract class VerifyListener implements org.eclipse.swt.events.VerifyListener {

	// arguments (binary coded)
	public static final int NO_ATTR = 0;
	// next attribute should start with 2^0

	// binary coded arguments
	private int attributes;

	/**
	 * Constructor
	 * @param attributes The validator attributes.
	 */
	public VerifyListener(int attributes) {
		setAttributes(attributes);
	}

	/**
	 * Set the attributes for the validator.
	 * @param attributes The validator attributes.
	 */
	public void setAttributes(int attributes) {
		this.attributes = attributes;
	}

	/**
	 * Add an attribute.
	 * @param attribute The validator attribute to add.
	 */
	public void addAttribute(int attribute) {
		if (!isAttribute(attribute)) {
			this.attributes |= attribute;
		}
	}

	/**
	 * Remove an attribute.
	 * @param attribute The validator attribute to remove.
	 */
	public void delAttribute(int attribute) {
		if (isAttribute(attribute)) {
			this.attributes -= attribute;
		}
	}

	/**
	 * Returns the attributes.
	 * @return
	 */
	public int getAttributes() {
		return attributes;
	}

	/**
	 * Returns true if the argument is set.
	 * @param attribute The argument to ask for.
	 * @return
	 */
	public boolean isAttribute(int attribute) {
		return isAttribute(attribute, attributes);
	}

	/**
	 * Returns true is argument is set.
	 * This static method can be used in the constructor or other static methods
	 * to check attributes.
	 * @param attribute The attribute to ask for
	 * @param attributes The binary coded attribute list
	 * @return
	 */
	public static boolean isAttribute(int attribute, int attributes) {
		return ((attributes & attribute) == attribute);
	}
}
