/**
 * 
 */
package v9t9.gui.client.swt;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import org.eclipse.swt.widgets.Control;

import v9t9.engine.client.IKeyboardHandler;
import v9t9.engine.keyboard.KeyboardState;
import v9t9.engine.machine.Machine;

/**
 * @author ejs
 *
 */
public class SwtLwjglKeyboardHandler implements IKeyboardHandler, ISwtKeyboardHandler {

	/**
	 * Handle lwjgl input controllers
	 * @author ejs
	 *
	 */
	public interface ControllerHandler {
		Controller getController();
		void setJoystick(int joy, KeyboardState state);
	}
	
	static public class StupidControllerHandler implements ControllerHandler {
		protected final Controller controller;
		private Component leftXAxis;
		private Component rightXAxis;
		private Component leftYAxis;
		private Component rightYAxis;
		/**
		 * 
		 */
		public StupidControllerHandler(Controller controller) {
			this.controller = controller;
			for (Component c : controller.getComponents())
				System.out.println(c);
			leftXAxis = controller.getComponent(Identifier.Axis.X);
			rightXAxis = controller.getComponent(Identifier.Axis.RX);
			leftYAxis = controller.getComponent(Identifier.Axis.Y);
			rightYAxis = controller.getComponent(Identifier.Axis.RY);
			if (rightXAxis == null || rightYAxis == null) {
				rightXAxis = leftXAxis;
				rightYAxis = leftYAxis;
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
		public void setJoystick(int joy, KeyboardState state) {
			int x = (int) Math.signum(getXAxis(joy));
			int y = (int) Math.signum(getYAxis(joy));
			boolean fire = getButton(joy);
			
			state.setJoystick(joy, 
					KeyboardState.JOY_X | KeyboardState.JOY_Y | KeyboardState.JOY_B, 
					x, y, fire, 
					System.currentTimeMillis());
		}
		
		protected float getXAxis(int joy) {
			Component axis = joy == 1 ? leftXAxis : rightXAxis;
			float xAxis = axis.getPollData();
			if (axis.isAnalog() && Math.abs(xAxis) <= 0.5)
				xAxis = 0;
			return xAxis;
		}
		protected float getYAxis(int joy) {
			Component axis = joy == 1 ? leftYAxis : rightYAxis;
			float yAxis = axis.getPollData();
			if (axis.isAnalog() && Math.abs(yAxis) <= 0.5)
				yAxis = 0;
			return yAxis;
		}
		protected boolean getButton(int joy) {
			boolean fire = false;
			for (Component button : controller.getComponents()) {
				if (button.getIdentifier() instanceof Button) {
					if (isButtonFor(button, joy)) {
						fire = button.getPollData() != 0;
						if (fire) {
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
	}
	
	private SwtKeyboardHandler swtKeyboardHandler;
	private ControllerHandler joystickHandler;
	private boolean failedLast;
	
	public SwtLwjglKeyboardHandler(KeyboardState keyboardState, Machine machine) {
		this.swtKeyboardHandler = new SwtKeyboardHandler(keyboardState, machine);
		
		for (Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
			String name = controller.getName();
			System.out.println("... controller: " + name);

			if (controller.getComponent(Identifier.Axis.X) == null || 
					controller.getComponent(Identifier.Axis.Y) == null ||
					controller.getType() == Controller.Type.MOUSE)
				continue;
				
			System.out.println("Using controller: " + name);
			joystickHandler = new StupidControllerHandler(controller);
			break;
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.KeyboardHandler#scan(v9t9.keyboard.KeyboardState)
	 */
	@Override
	public void scan(KeyboardState state) {
		swtKeyboardHandler.scan(state);
		
		if (joystickHandler != null) {
			
			if (joystickHandler.getController().poll()) {
				failedLast = false;
				for (int joy = 1; joy <= 2; joy++) {
					joystickHandler.setJoystick(joy, state);
				}
			} else {
				if (!failedLast) {
					// maybe unplugged?
					state.setJoystick(1, KeyboardState.JOY_X | KeyboardState.JOY_Y | KeyboardState.JOY_B, 
							0, 0, false, System.currentTimeMillis());
					failedLast = true;
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
