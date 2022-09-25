package v9t9.machine.ti99.machine;

import java.util.Arrays;

import v9t9.common.machine.IMachine;
import v9t9.engine.keyboard.KeyboardState;

import static v9t9.common.keyboard.KeyboardConstants.*;

public class KeyboardState994A extends KeyboardState {
	/** entries as 0x[row:4][column:4] */
	static final char x9901ToLatin[] = {
		// see >1707 in GROM
    	/* 0x00 */   0xff, 'X', 'C', 'V', 'B', 'Z', 0xff, 0xff,
    	/* 0x10 */   KEY_CONTROL, 'W', 'E', 'R', 'T', 'Q', 0xff, 0xff,
    	/* 0x20 */   KEY_SHIFT, 'S', 'D', 'F', 'G', 'A', 0xff, 0xff,
    	/* 0x30 */   KEY_ALT, '2', '3', '4', '5', '1', 0xff, 0xff,
    	/* 0x40 */   0xff, '9', '8', '7', '6', '0', 0xff, 0xff,
    	/* 0x50 */  13, 'O', 'I', 'U', 'Y', 'P', 0xff, 0xff,
    	/* 0x60 */ ' ', 'L', 'K', 'J', 'H', ';', 0xff, 0xff,
    	/* 0x70 */ '=', '.', ',', 'M', 'N', '/', 0xff, 0xff,
    };
    
	public KeyboardState994A(IMachine machine) {
		super(machine);
		 /* CRU rows and columns */
	    SHIFT_R = 2;
	    SHIFT_C = 0;
	    FCTN_R = 3;
	    FCTN_C = 0;
	    CTRL_R = 1;
	    CTRL_C = 0;

	    JOY_FIRE_R = 7;
		JOY_LEFT_R = 6;
		JOY_RIGHT_R = 5;
		JOY_DOWN_R = 4;
		JOY_UP_R = 3;
		
		JOY1_C = 6;
		JOY2_C = 7;
		
	    /*  NOTE: 47 = '/' in Latin-1 corresponds to the US keyboard key '/'
	        and '?', but on the TI keyboard, 0x75 this is the key for '/' and
	        '-'.  The target-specific code must trap '-', '/', '?', '_'
	        and should use FCTN+I for '?'.*/
	    
    	Arrays.fill(latinto9901, (byte) -1);
    	for (int i = 0; i < 64; i++) {
    		char ch = x9901ToLatin[i]; 
    		if (ch != 0xff) {
    			latinto9901[ch] = (byte) (((i & 0xf8) << 1) | (i & 0x7)); 
    		}
    	}
    	
		registerMapping(KEY_TAB, KEY_CONTROL, 'I');
		registerMapping(KEY_EXCLAMATION, KEY_SHIFT, '1');
		registerMapping(KEY_AT, KEY_SHIFT, '2');
		registerMapping(KEY_POUND, KEY_SHIFT, '3');
		registerMapping(KEY_DOLLAR, KEY_SHIFT, '4');
		registerMapping(KEY_PERCENT, KEY_SHIFT, '5');
		registerMapping(KEY_CIRCUMFLEX, KEY_SHIFT, '6');
		registerMapping(KEY_AMPERSAND, KEY_SHIFT, '7');
		registerMapping(KEY_ASTERISK, KEY_SHIFT, '8');
		registerMapping(KEY_OPEN_PARENTHESIS, KEY_SHIFT, '9');
		registerMapping(KEY_CLOSE_PARENTHESIS, KEY_SHIFT, '0');
		registerMapping(KEY_PLUS, KEY_SHIFT, '=');
		registerMapping(KEY_LESS, KEY_SHIFT, ',');
		registerMapping(KEY_GREATER, KEY_SHIFT, '.');
		registerMapping(KEY_COLON, KEY_SHIFT, ';');
		registerMapping(KEY_BACK_QUOTE, KEY_ALT, 'C');
		registerMapping(KEY_TILDE, KEY_ALT, 'W');
		registerMapping(KEY_MINUS, KEY_SHIFT, '/');
		registerMapping(KEY_UNDERSCORE, KEY_ALT, 'U');
		registerMapping(KEY_OPEN_BRACKET, KEY_ALT, 'R');
		registerMapping(KEY_OPEN_BRACE, KEY_ALT, 'F');
		registerMapping(KEY_CLOSE_BRACKET, KEY_ALT, 'T');
		registerMapping(KEY_CLOSE_BRACE, KEY_ALT, 'G');
		registerMapping(KEY_QUOTE, KEY_ALT, 'P');
		registerMapping(KEY_SINGLE_QUOTE, KEY_ALT, 'O');
		registerMapping(KEY_QUESTION, KEY_ALT, 'I');
		registerMapping(KEY_BACK_SLASH, KEY_ALT, 'Z');
		registerMapping(KEY_BAR, KEY_ALT, 'A');
		registerMapping(KEY_DELETE, KEY_ALT, '1');
		registerMapping(KEY_F1, KEY_ALT, '1');
		registerMapping(KEY_F2, KEY_ALT, '2');
		registerMapping(KEY_F3, KEY_ALT, '3');
		registerMapping(KEY_F4, KEY_ALT, '4');
		registerMapping(KEY_F5, KEY_ALT, '5');
		registerMapping(KEY_F6, KEY_ALT, '6');
		registerMapping(KEY_F7, KEY_ALT, '7');
		registerMapping(KEY_F8, KEY_ALT, '8');
		registerMapping(KEY_F9, KEY_ALT, '9');
		
		registerMapping(KEY_ARROW_DOWN, KEY_ALT, 'X');
		registerMapping(KEY_ARROW_UP, KEY_ALT, 'E');
		registerMapping(KEY_ARROW_LEFT, KEY_ALT, 'S');
		registerMapping(KEY_ARROW_RIGHT, KEY_ALT, 'D');

		registerMapping(KEY_PAGE_UP, KEY_ALT, '6');	// CLEAR  // (as per E/A and TI Writer)
		registerMapping(KEY_PAGE_DOWN, KEY_ALT, '4');	// PROC'D  // (as per E/A and TI Writer)
		registerMapping(KEY_HOME, KEY_ALT, '5');	// BEGIN
		registerMapping(KEY_END, KEY_ALT, '0');	
		registerMapping(KEY_INSERT, KEY_ALT, '2');	// INS	
		registerMapping(KEY_DELETE, KEY_ALT, '1');	// DEL
		
		registerMapping(KEY_KP_ARROW_DOWN, KEY_ALT, 'X');
		registerMapping(KEY_KP_ARROW_UP, KEY_ALT, 'E');
		registerMapping(KEY_KP_ARROW_LEFT, KEY_ALT, 'S');
		registerMapping(KEY_KP_ARROW_RIGHT, KEY_ALT, 'D');

		registerMapping(KEY_KP_PAGE_UP, KEY_ALT, '6');	// CLEAR  // (as per E/A and TI Writer)
		registerMapping(KEY_KP_PAGE_DOWN, KEY_ALT, '4');	// PROC'D  // (as per E/A and TI Writer)
		registerMapping(KEY_KP_HOME, KEY_ALT, '5');	// BEGIN
		registerMapping(KEY_KP_END, KEY_ALT, '0');	
		registerMapping(KEY_KP_INSERT, KEY_ALT, '2');	// INS	
		registerMapping(KEY_KP_DELETE, KEY_ALT, '1');	// DEL
		
		
		registerMapping(KEY_KP_SLASH, '/');
		registerMapping(KEY_KP_ASTERISK, KEY_SHIFT, '8');
		registerMapping(KEY_KP_MINUS,  KEY_SHIFT, '/');
		registerMapping(KEY_KP_PLUS, KEY_SHIFT, '=');
		registerMapping(KEY_KP_ENTER, '\r');
		registerMapping(KEY_KP_0, '0');
		registerMapping(KEY_KP_1, '1');
		registerMapping(KEY_KP_2, '2');
		registerMapping(KEY_KP_3, '3');
		registerMapping(KEY_KP_4, '4');
		registerMapping(KEY_KP_5, '5');
		registerMapping(KEY_KP_6, '6');
		registerMapping(KEY_KP_7, '7');
		registerMapping(KEY_KP_8, '8');
		registerMapping(KEY_KP_9, '9');
		
		registerMapping(KEY_KP_SHIFT_5);
    		
	}

}
