/*
  IKeyboardListener.java

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

import java.util.Collection;

import v9t9.common.keyboard.IKeyboardMapping.PhysKey;

/**
 * Reports changes in high-level keyboard changes
 * @author ejs
 *
 */
public interface IKeyboardListener {

	/** A key was pressed or released.
	 * @param key the keyboard character (see KeyboardConstants#KEY_...)
	 * @param pressed true for press, false for release     
	 */
	void keyEvent(Collection<Integer> keys, boolean pressed);
	

	/** A physical key was pressed or released.
	 * @param key the physical key identifier (@see {@link PhysKey#getIdentifier()})
	 * @param pressed true for press, false for release     
	 */
	void physKeyEvent(Collection<Integer> keys, boolean pressed);

	/**
	 * Joystick state changed.
	 * @param num
	 * @param mask
	 * @see IKeyboardState#JOY_UP_R
	 * @see IKeyboardState#JOY_DOWN_R
	 * @see IKeyboardState#JOY_LEFT_R
	 * @see IKeyboardState#JOY_RIGHT_R
	 * @see IKeyboardState#JOY_FIRE_R
	 */
	void joystickChangeEvent(int num, byte mask);
}
