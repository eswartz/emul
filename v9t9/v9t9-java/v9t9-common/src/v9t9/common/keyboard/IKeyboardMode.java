/*
  IKeyboardMode.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.keyboard;

import java.util.Map;

import v9t9.common.keyboard.IKeyboardMapping.PhysKey;
import ejs.base.utils.Pair;

/**
 * Provides a dynamic interpretation of the keyboard mapping
 * for the machine
 * @author ejs
 *
 */
public interface IKeyboardMode {
	String getId();
	/**
	 * Displayed name of mode
	 * @return
	 */
	String getLabel();
	
	/**
	 * Get the keycode mappings for the given shift and lock mask
	 * @param shiftLockMask applied shifts or locks (0 for default state)
	 * @return map of each key to keycode and label (no entry for a PhysKey means nothing 
	 * special versus some shiftLockMask with fewer 'on' bits)
	 */
	Map<PhysKey, Pair<Integer, String>> getShiftLockMaskMap(byte shiftLockMask);
	/**
	 * @param shiftLockMask
	 * @param key
	 * @return
	 */
	int getKeycode(byte shiftLockMask, PhysKey key);

}
