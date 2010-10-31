/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;

import v9t9.keyboard.KeyboardState;

/**
 * @author ejs
 *
 */
public class MouseJoystickHandler {

	private final ISwtVideoRenderer renderer;
	private boolean enabled;
	private MouseAdapter mouseButtonListener;
	private MouseMoveListener mouseMoveListener;
	private final KeyboardState keyboardState;
	private Point center;

	/**
	 * @param videoControl
	 */
	public MouseJoystickHandler(final ISwtVideoRenderer renderer, KeyboardState keyboardState) {
		this.renderer = renderer;
		this.keyboardState = keyboardState;
		
		mouseButtonListener = new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (!enabled)
					return;
				if (e.button != 1)
					return;

				System.out.println("DOWN");
				int joy = (e.stateMask & SWT.SHIFT) != 0 ? 2 : 1;
				button(e.button, joy, true);
			}
			@Override
			public void mouseUp(MouseEvent e) {
				if (!enabled)
					return;
				if (e.button != 1)
					return;

				System.out.println("UP");
				int joy = (e.stateMask & SWT.SHIFT) != 0 ? 2 : 1;
				button(e.button, joy, false);
			}
		};
		
		mouseMoveListener = new MouseMoveListener() {
			
			public void mouseMove(MouseEvent e) {
				if (!enabled)
					return;
				int joy = (e.stateMask & SWT.SHIFT) != 0 ? 2 : 1;
				Point loc = renderer.getControl().toDisplay(e.x, e.y);
				move(joy, loc.x, loc.y);
				
				boolean pressed = false;
				int buttons = e.stateMask & SWT.BUTTON_MASK;
				if ((buttons & SWT.BUTTON1) != 0)
					pressed = true;
				button(1, joy, pressed);
			}
		};
		
		renderer.addMouseEventListener(mouseButtonListener);
		renderer.addMouseMotionListener(mouseMoveListener);

	}

	/**
	 * 
	 */
	public void setEnabled(boolean en) {
		enabled = en;
		
		//renderer.getControl().setCapture(enabled);
		
		keyboardState.resetJoystick();
		
		if (enabled) {
			center = renderer.getControl().getDisplay().getCursorLocation();
		}
	}

	/**
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param x
	 * @param y
	 * @param y2 
	 */
	protected void move(int joy, int x, int y) {
		Point diff = new Point(x - center.x, y - center.y);
		
		double delta = 2 * Math.PI / 8;
		double ang = Math.atan2(-diff.y, diff.x);
		
		int quad = (int) ((ang + delta/2 ) / delta + 8) % 8;
		
		//System.out.println(diff + " -> " + ang+"  =  " +quad);
		
		int dx = 0, dy = 0;
		
		switch (quad) {
		case 0:
			dx = 1; dy = 0; break;
		case 1:
			dx = 1; dy = -1; break;
		case 2:
			dx = 0; dy = -1; break;
		case 3:
			dx = -1; dy = -1; break;
		case 4:
			dx = -1; dy = 0; break;
		case 5:
			dx = -1; dy = 1; break;
		case 6:
			dx = 0; dy = 1; break;
		case 7:
			dx = 1; dy = 1; break;
		}
		keyboardState.setJoystick(joy, KeyboardState.JOY_X | KeyboardState.JOY_Y, dx, dy, false, System.currentTimeMillis());
	}

	/**
	 * @param button
	 */
	protected void button(int button, int joy, boolean pressed) {
		//System.out.println(button +"/"+ joy + "/" + pressed);
		keyboardState.setJoystick(joy, KeyboardState.JOY_B, 0, 0, pressed, System.currentTimeMillis());
	}


}
