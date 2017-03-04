/**
 * 
 */
package v9t9.gui.client.swt;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import v9t9.common.keyboard.IKeyboardState;

public interface IControllerHandler {
	Controller getController();
	Component getComponent();
	void setJoystick(int joy, IKeyboardState state);
	boolean isFailedLast();
	void setFailedLast(boolean failedLast);
}