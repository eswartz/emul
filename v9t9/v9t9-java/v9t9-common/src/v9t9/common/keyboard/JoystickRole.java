/*
  JoystickRole.java

  (c) 2017 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.keyboard;

public enum JoystickRole {
	/** Don't use this component */
	IGNORE,
	/** Map a -1...1 value to X axis */ 
	X_AXIS,
	/** Map a -1...1 value to Y axis */ 
	Y_AXIS,
	/** Map a value to X and Y axes */ 
	DIRECTIONAL,
	/** Map a button to X axis left */ 
	LEFT,
	/** Map a button to X axis right */ 
	RIGHT,
	/** Map a button to Y axis up */ 
	UP,
	/** Map a button to Y axis down */ 
	DOWN,
	/** Map a button to Fire */ 
	BUTTON
}