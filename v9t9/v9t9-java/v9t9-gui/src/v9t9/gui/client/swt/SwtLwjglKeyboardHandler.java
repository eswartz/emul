/*
  SwtLwjglKeyboardHandler.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

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
	
	static public class StupidControllerHandler implements ControllerHandler {
		protected final Controller controller;
		private boolean failedLast;
		private Component leftXAxis;
		private Component rightXAxis;
		private Component leftYAxis;
		private Component rightYAxis;
		/**
		 * 
		 */
		public StupidControllerHandler(Controller controller) {
			this.controller = controller;
			if (DEBUG) {
				for (Component c : controller.getComponents())
					System.out.println(c);
			}
			leftXAxis = controller.getComponent(Identifier.Axis.X);
			rightXAxis = controller.getComponent(Identifier.Axis.RX);
			leftYAxis = controller.getComponent(Identifier.Axis.Y);
			rightYAxis = controller.getComponent(Identifier.Axis.RY);
			if (rightXAxis == null || rightYAxis == null) {
				//rightXAxis = leftXAxis;
				//rightYAxis = leftYAxis;
			}
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
			for (Component button : controller.getComponents()) {
				if (button.getIdentifier() instanceof Button) {
					if (isButtonFor(button, joy)) {
						fire = button.getPollData() != 0;
						if (fire) {
							if (DEBUG)
								System.out.println(button.getIdentifier());
							break;
						}
					}
				}
			}
			return fire;
		}
		
		/**
		 * @param button  
		 * @param joy 
		 */
		protected boolean isButtonFor(Component button, int joy) {
			String name = button.getIdentifier().getName();
			return name.toLowerCase().matches(".*(trigger|thumb|" + (joy == 1 ? "left" : "right") + "|1|3|5|7).*");
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
		System.out.println("java.library.path=" + System.getProperty("java.library.path"));
		
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
