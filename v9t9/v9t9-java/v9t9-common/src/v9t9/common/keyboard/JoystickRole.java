/**
 * 
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