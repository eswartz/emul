/**
 * 
 */
package v9t9.emulator.clients.builtin.sdl;

import sdljava.event.SDLEvent;
import sdljava.event.SDLKey;
import sdljava.event.SDLKeyboardEvent;
import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.BaseKeyboardHandler;
import v9t9.keyboard.KeyboardState;

/**
 * @author Ed
 *
 */
public class SdlKeyboardHandler extends BaseKeyboardHandler {

	public SdlKeyboardHandler(KeyboardState keyboardState, Machine machine) {
		super(keyboardState, machine);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.KeyboardHandler#scan(v9t9.keyboard.KeyboardState)
	 */
	public void scan(KeyboardState state) {
		// nothing -- all done incrementally
	}

	public void handleEvent(SDLKeyboardEvent event) {
		//System.out.println("sym: " + event.getSym() + "; "
		//		+ (event.getType() == SDLEvent.SDL_KEYDOWN ? "pressed" : "released"));
		
		boolean pressed = event.getType() == SDLEvent.SDL_KEYDOWN;
		
		if (keyboardState.isPasting() && pressed && event.getSym() == SDLKey.SDLK_ESCAPE) {
			keyboardState.cancelPaste();
			return;
		}
		
		char ascii = 0;
		
		if (event.getSym() < 128) {
			ascii = (char) event.getSym();
			if (Character.isLowerCase(ascii))
				ascii = Character.toUpperCase(ascii);
		}
		
		if (ascii == 0 || !keyboardState.postCharacter(pressed, false, (byte) 0, ascii)) {
			byte fctn = KeyboardState.FCTN;
			
			switch (event.getSym()) {

				// shifts
			case SDLKey.SDLK_LSHIFT:
			case SDLKey.SDLK_RSHIFT:
				keyboardState.setKey(pressed, false, KeyboardState.SHIFT, 0);
				break;
			case SDLKey.SDLK_LCTRL:
			case SDLKey.SDLK_RCTRL:
				keyboardState.setKey(pressed, false, KeyboardState.CTRL, 0);
				break;
			case SDLKey.SDLK_LALT:
			case SDLKey.SDLK_RALT:
			case SDLKey.SDLK_LMETA:
			case SDLKey.SDLK_RMETA:
				keyboardState.setKey(pressed, false, KeyboardState.FCTN, 0);
				break;

			case SDLKey.SDLK_CAPSLOCK:
				if (pressed) {
					keyboardState.setAlpha(!keyboardState.getAlpha());
				}
				break;
			case 302:		// actual break -- SDLJava has incorrect SCROLL_LOCK value here
				if (pressed)
					System.exit(0);
				break;
			case SDLKey.SDLK_F1:
			case SDLKey.SDLK_F2:
			case SDLKey.SDLK_F3:
			case SDLKey.SDLK_F4:
			case SDLKey.SDLK_F5:
			case SDLKey.SDLK_F6:
			case SDLKey.SDLK_F7:
			case SDLKey.SDLK_F8:
			case SDLKey.SDLK_F9:
				keyboardState.setKey(pressed, false, fctn, '1' + SDLKey.SDLK_F1 - event.getSym());	
				break;
				
			case SDLKey.SDLK_UP:
				keyboardState.setKey(pressed, false, fctn, 'E');
				break;
			case SDLKey.SDLK_DOWN:
				keyboardState.setKey(pressed, false, fctn, 'X');
				break;
			case SDLKey.SDLK_LEFT:
				keyboardState.setKey(pressed, false, fctn, 'S');
				break;
			case SDLKey.SDLK_RIGHT:
				keyboardState.setKey(pressed, false, fctn, 'D');
				break;
				
				
			//case SWT.DEL:
			//	keyboardState.setKey(pressed, fctnShifted, '1');	
			//	break;
			case SDLKey.SDLK_INSERT:
				keyboardState.setKey(pressed, false, fctn, '2');	
				break;
				
			case SDLKey.SDLK_PAGEUP:
				keyboardState.setKey(pressed, false, fctn, '6'); // (as per E/A and TI Writer)
				break;
			case SDLKey.SDLK_PAGEDOWN:
				keyboardState.setKey(pressed, false, fctn, '4'); // (as per E/A and TI Writer)
				break;

			case SDLKey.SDLK_HOME:
				keyboardState.setKey(pressed, false, fctn, '5');		// BEGIN
				break;
			case SDLKey.SDLK_END:
				keyboardState.setKey(pressed, false, fctn, '0');		// Fctn-0
				break;

			default:
				System.out.println(event.getSym());
			}
		}
		
	}
}
