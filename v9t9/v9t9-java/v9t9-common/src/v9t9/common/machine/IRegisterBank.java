/*
  IRegisterBank.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.machine;

import ejs.base.properties.IPersistable;

/**
 * This represents a set of registers controlling a single
 * entity, which may exist in multiple copies.
 * @author ejs
 *
 */
public interface IRegisterBank extends IPersistable {
	/** Get the identifier for the voice, in register naming */
	String getId();
	/**
	 * Get the name or description of the voice
	 */
	String getName();
	
	/** Get base register number for voice */
	int getBaseRegister();
	/** Get register count for voice */
	int getRegisterCount();

	int getRegister(int reg);
	void setRegister(int reg, int newValue);
}
