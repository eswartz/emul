/*
  SwtLwjglKeyboardHandler.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

import java.util.ArrayList;
import java.util.List;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class SwtLwjglKeyboardHandler extends SwtKeyboardHandler {

	private static boolean DEBUG = false;
	
	/**
	 * Handle lwjgl input controllers
	 * @author ejs
	 *
	 */
	public interface ControllerHandler {
		Controller getController();
		void setJoystick(int joy, IKeyboardState state);
		boolean isFailedLast();
		void setFailedLast(boolean failedLast);
	}
	
	public static class StupidControllerHandler implements ControllerHandler {
		protected final Controller controller;
		private boolean failedLast;
		private Component leftXAxis;
		private Component rightXAxis;
		private Component leftYAxis;
		private Component rightYAxis;
		private List<Component> leftButtons = new ArrayList<Component>(1);
		private List<Component> rightButtons = new ArrayList<Component>(1);

		/**
		 * 
		 */
		public StupidControllerHandler(Controller controller) {
			this.controller = controller;
			Component[] components = controller.getComponents();
			if (DEBUG) {
				for (Component c : components)
					System.out.println(c);
			}
			leftXAxis = lastOf(components, Identifier.Axis.X);
			rightXAxis = lastOf(components, Identifier.Axis.RX);
			leftYAxis = lastOf(components, Identifier.Axis.Y);
			rightYAxis = lastOf(components, Identifier.Axis.RY);
			if (rightXAxis == null || rightYAxis == null) {
				//rightXAxis = leftXAxis;
				//rightYAxis = leftYAxis;
			}

			Component cand1 = null, cand2 = null;
			for (Component button : controller.getComponents()) {
				if (button.getIdentifier() instanceof Button) {
					if (isButtonFor(button, 1)) {
						leftButtons.add(button);
					}
					else if (isButtonFor(button, 2)) {
						rightButtons.add(button);
					}
					else if (cand1 == null) {
						cand1 = button;
					} else if (cand2 == null) {
						cand2 = button;
					}

				}
			}
			if (leftButtons.isEmpty() && cand1 != null)
				leftButtons.add(cand1);
			if (rightButtons.isEmpty() && cand2 != null)
				rightButtons.add(cand2);
		}

		protected boolean isButtonFor(Component button, int joy) {
			String name = button.getIdentifier().getName();
			return name.toLowerCase().matches(".*(trigger|thumb|" + (joy == 1 ? "left" : "right") + ").*");
		}
		
		private Component lastOf(Component[] cs, Identifier.Axis id) {
			for (int i = cs.length - 1; i >= 0; i--) {
				if (id.equals(cs[i].getIdentifier())) {
					return cs[i];
				}
			}
			return null;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.swt.SwtLwjglKeyboardHandler.ControllerHandler#getController()
		 */
		@Override
		public Controller getController() {
			return controller;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.swt.SwtLwjglKeyboardHandler.ControllerHandler#setJoystick(int, v9t9.keyboard.KeyboardState)
		 */
		@Override
		public void setJoystick(int joy, IKeyboardState state) {
			int x = (int) Math.signum(getXAxis(joy));
			int y = (int) Math.signum(getYAxis(joy));
			boolean fire = getButton(joy);

			state.setJoystick(joy, 
					IKeyboardState.JOY_X | IKeyboardState.JOY_Y | IKeyboardState.JOY_B, 
					x, y, fire);
		}
		
		protected float getXAxis(int joy) {
			Component axis = joy == 1 ? leftXAxis : rightXAxis;
			if (axis == null)
				return 0;
			float xAxis = axis.getPollData();
			if (axis.isAnalog() && Math.abs(xAxis) <= 0.5)
				xAxis = 0;
			return xAxis;
		}
		protected float getYAxis(int joy) {
			Component axis = joy == 1 ? leftYAxis : rightYAxis;
			if (axis == null)
				return 0;
			float yAxis = axis.getPollData();
			if (axis.isAnalog() && Math.abs(yAxis) <= 0.5)
				yAxis = 0;
			return yAxis;
		}
		protected boolean getButton(int joy) {
			boolean fire = false;
			if ((joy == 1 ? leftXAxis : rightXAxis) == null)
				return false;
			
			for (Component button : (joy == 1 ? leftButtons : rightButtons)) {
				fire = button.getPollData() != 0;
				if (fire) {
					if (DEBUG)
						System.out.println(button.getIdentifier());
					break;
				}
			}
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
	
	private ControllerHandler joystick1Handler, joystick2Handler;
	private Runnable scanTask;
	
	public SwtLwjglKeyboardHandler(final IKeyboardState keyboardState, IMachine machine) {
		super(keyboardState, machine);
		
		scanTask = new Runnable() {
			
			@Override
			public void run() {
				scanJoystick(keyboardState, joystick1Handler, 1);
				scanJoystick(keyboardState, joystick2Handler, 2);
				
//				if ((joystick1Handler != null && joystick1Handler.isFailedLast())
//						|| (joystick2Handler != null && joystick2Handler.isFailedLast())) {
//					updateControllers();
//				}
				
			}
		};
		machine.getFastMachineTimer().scheduleTask(scanTask, 10);
		
		updateControllers();
		
		ControllerEnvironment.getDefaultEnvironment().addControllerListener(new ControllerListener() {
			
			@Override
			public void controllerRemoved(ControllerEvent arg0) {
				updateControllers();
			}
			
			@Override
			public void controllerAdded(ControllerEvent arg0) {
				updateControllers();				
			}
		});
	}
	
	/**
	 * 
	 */
	private synchronized void updateControllers() {
		joystick1Handler = joystick2Handler = null;
		
		for (Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
			String name = controller.getName();
			System.out.println("... controller: " + name);

			if (controller.getComponent(Identifier.Axis.X) == null || 
					controller.getComponent(Identifier.Axis.Y) == null ||
					controller.getType() == Controller.Type.MOUSE)
				continue;
				
			System.out.println("Using controller: " + name);
			if (joystick1Handler == null) {
				joystick1Handler = new StupidControllerHandler(controller);
			} else if (joystick2Handler == null) {
				joystick2Handler = new StupidControllerHandler(controller);
				break;
			}
		}
		
	}
	
	/**
	 * @param joystickHandler
	 * @param i
	 */
	private void scanJoystick(IKeyboardState state, ControllerHandler joystickHandler, int joy) {
		if (joystickHandler != null) {
			
			if (joystickHandler.getController().poll()) {
				joystickHandler.setFailedLast(false);
				joystickHandler.setJoystick(joy, state);
			} else {
				if (!joystickHandler.isFailedLast()) {
					// maybe unplugged?
					state.setJoystick(joy, IKeyboardState.JOY_X | IKeyboardState.JOY_Y | IKeyboardState.JOY_B, 
							0, 0, false);
					joystickHandler.setFailedLast(true);
				}
			}
		}
	}

}
