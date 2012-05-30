/**
 * 
 */
package v9t9.gui.client.swt;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;

import org.eclipse.swt.widgets.Control;

import v9t9.common.client.IKeyboardHandler;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class SwtLwjglKeyboardHandler implements IKeyboardHandler, ISwtKeyboardHandler {

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
					x, y, fire, 
					System.currentTimeMillis());
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
	
	private SwtKeyboardHandler swtKeyboardHandler;
	private ControllerHandler joystick1Handler, joystick2Handler;
	
	public SwtLwjglKeyboardHandler(IKeyboardState keyboardState, IMachine machine) {
		this.swtKeyboardHandler = new SwtKeyboardHandler(keyboardState, machine);
		
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

	@Override
	public void scan(IKeyboardState state) {
		swtKeyboardHandler.scan(state);
		
		scanJoystick(state, joystick1Handler, 1);
		scanJoystick(state, joystick2Handler, 2);
		
//		if ((joystick1Handler != null && joystick1Handler.isFailedLast())
//				|| (joystick2Handler != null && joystick2Handler.isFailedLast())) {
//			updateControllers();
//		}
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
							0, 0, false, System.currentTimeMillis());
					joystickHandler.setFailedLast(true);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISwtKeyboardHandler#init(org.eclipse.swt.widgets.Control)
	 */
	@Override
	public void init(Control control) {
		swtKeyboardHandler.init(control);
	}

}
