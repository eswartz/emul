/**
 * 
 */
package v9t9.gui.client.swt;

import net.java.games.input.Component;
import net.java.games.input.Component.POV;
import net.java.games.input.Controller;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.keyboard.JoystickRole;

/**
 * Handle controllers whose values map to 
 * @author ejs
 *
 */
public class SimpleControllerHandler implements IControllerHandler {
	protected final Controller controller;
	protected final Component component;
	protected final JoystickRole role;
	
	private boolean failedLast;

	/**
	 * 
	 */
	public SimpleControllerHandler(Controller controller, Component component, JoystickRole role) {
		this.controller = controller;
		this.component = component;
		this.role = role;
		
		failedLast = false;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.SwtLwjglKeyboardHandler.ControllerHandler#getController()
	 */
	@Override
	public Controller getController() {
		return controller;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.IControllerHandler#getComponent()
	 */
	@Override
	public Component getComponent() {
		return component;
	}
	
	/**
	 * @return the role
	 */
	public JoystickRole getRole() {
		return role;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.SwtLwjglKeyboardHandler.ControllerHandler#setJoystick(int, v9t9.keyboard.KeyboardState)
	 */
	@Override
	public void setJoystick(int joy, IKeyboardState state) {
		switch (role) {
		case IGNORE:
			return;
			
		case X_AXIS: {
			int x = (int) Math.signum(getAxis());
			state.setJoystick(joy, 
					IKeyboardState.JOY_X, 
					x, 0, false);
			break;
		}
		case Y_AXIS: {
			int y = (int) Math.signum(getAxis());
			state.setJoystick(joy, 
					IKeyboardState.JOY_Y, 
					0, y, false);
			break;
		}
	
		case BUTTON: {
			boolean fire = getButton();
			state.setJoystick(joy, 
					IKeyboardState.JOY_B, 
					0, 0, fire);
			break;
		}
		
		case POV: {
			float value = getAxis();
			int y = 0;
			int x = 0;
			if (value == POV.UP_LEFT) {
				y = -1;
				x = -1;
			} else if (value == POV.UP) {
				y = -1;
				x = 0;
			} else if (value == POV.UP_RIGHT) {
				y = -1;
				x = 1;
			} else if (value == POV.RIGHT) {
				y = 0;
				x = 1;
			} else if (value == POV.DOWN_RIGHT) {
				y = 1;
				x = 1;
			} else if (value == POV.DOWN) {
				y = 1;
				x = 0;
			} else if (value == POV.DOWN_LEFT) {
				y = -1;
				x = 1;
			} else if (value == POV.LEFT) {
				y = 0;
				x = -1;
			}
			state.setJoystick(joy, IKeyboardState.JOY_X | IKeyboardState.JOY_Y, x, y, false);
		}
		}
	}
	
	protected float getAxis() {
		if (component == null)
			return 0;
		float axis = component.getPollData();
		if (component.isAnalog() && Math.abs(axis) <= 0.5)
			axis = 0;
		return axis;
	}
	protected boolean getButton() {
		if (component == null)
			return false;

		boolean fire = component.getPollData() != 0;
		return fire;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.SwtLwjglKeyboardHandler.ControllerHandler#isFailedLast()
	 */
	@Override
	public boolean isFailedLast() {
		return failedLast;
	}
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.SwtLwjglKeyboardHandler.ControllerHandler#setFailedLast(boolean)
	 */
	@Override
	public void setFailedLast(boolean failedLast) {
		this.failedLast = failedLast;
	}
}