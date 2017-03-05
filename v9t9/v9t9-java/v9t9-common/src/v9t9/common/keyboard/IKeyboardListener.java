/*
  IKeyboardListener.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
