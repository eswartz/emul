/*
  IKeyboardMode.java

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
