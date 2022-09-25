package v9t9.machine.ti99.machine;

import java.util.Arrays;

import v9t9.common.machine.IMachine;
import v9t9.engine.keyboard.KeyboardState;

import static v9t9.common.keyboard.KeyboardConstants.*;

public class KeyboardState994 extends KeyboardState {
	/** entries as 0x[row:4][column:4] */
	static final char x9901ToLatin[] = {
		// >175C
//		0x01, 0x02, 0x03, 0x04, 'Q', 'R', 'S', 'T',
//		0x09, 0x0a, 0x0b, 0x0c, 'I', 'J', 'K', 'L',
//		0x0d, 0x0e, 0x0f, 0x10, 'E', 'F', 'G', 'H',
//		0x11, 0x12, 0x13, 0x14, 'A', 'B', 'C', 'D',
//		0xff, 0xff, 
		
		// >1786
		'1',  'Q',  ' ',  0x10, // row 0
		'2',  'W',  'A',  'Z', 	
		'3',  'E',  'S',  'X',  // row 1
		'4',  'R',  'D',  'C', 	
		'5',  'T',  'F',  'V',  // row 2 
		0xff, 0xff, 0xff, 0xff, 
		0xff, 0xff, 0xff, 0xff, // row 3
		0xff, 0xff, 0xff, 0xff,
//		'!',  0x05, ' ',  0x10, // row 0 shifted 
//		'@',  0x0e, 0x01, 0x0f,
//		'#',  0x0b, 0x08, 0x0a, // row 1 shifted
//		'$',  0x06, 0x09, 0x02,
//		'%',  0x07, 0x03, 0x0c, // row 2 shifted 
//		0xff, 0xff, 0xff, 0xff,
//		0xff, 0xff, 0xff, 0xff, // row 3 shifted
//		0xff, 0xff, 0xff, 0xff,
		'6', 'Y',   'G',  'B',  // row 4
		'7', 'U',   'H',  'N', 
		'8', 'I',   'J',  'M',  // row 5
		'9', 'O',   'K',  '.',
		'0', 'P',   'L',  0x0d, // row 6
		0xff, 0xff, 0xff, 0xff,
		0xff, 0xff, 0xff, 0xff, // row 7
		0xff, 0xff, 0xff, 0xff,
//		'\'', '>',  '.',  '?',  // row 4 shifted
//		'&',  '_',  '<',  ':',
//		'*',  '-',  '^',  ';',  // row 5 shifted 
//		'(',  '+',  '/',  ',',  
//		')',  '"',  '=',  0x0d, // row 6 shifted 
//		0xff, 0xff, 0xff, 0xff,
//		0xff, 0xff, 0xff, 0xff, // row 7 shifted
//		0xff, 0xff, 0xff, 0xff,
		
    };
    
	public KeyboardState994(IMachine machine) {
		super(machine);
		 /* CRU rows and columns */
	    SHIFT_R = 2;
	    SHIFT_C = 0;
	    CTRL_R = 1;
	    CTRL_C = 0;
	    
	    FCTN_R = -1;
	    FCTN_C = -1;

	    JOY_FIRE_R = 7;
		JOY_LEFT_R = 6;
		JOY_RIGHT_R = 5;
		JOY_DOWN_R = 4;
		JOY_UP_R = 3;
		
		JOY1_C = -1;
		JOY2_C = -1;
		
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
	}

}
