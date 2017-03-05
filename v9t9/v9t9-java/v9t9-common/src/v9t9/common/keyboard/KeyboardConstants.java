/*
  KeyboardConstants.java

  (c) 2012-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.keyboard;

/**
 * These constants are used in {@link IKeyboardState} to define key constants,
 * shift masks, and lock masks.
 * <p/>
 * They represent every distinct symbol and state on a typical U.S. keyboard,
 * but do not represent actual physical keys on any keyboard.
 * @author ejs
 *
 */
public final class KeyboardConstants {
	public static final int KEY_UNKNOWN = -1;
	
	public static final int KEY_SHIFT = 0;
	public static final int KEY_CONTROL = 1;
	public static final int KEY_ALT = 2;
	
	public static final int KEY_CAPS_LOCK = 3;
	public static final int KEY_NUM_LOCK = 4;
	public static final int KEY_SCROLL_LOCK = 5;
	public static final int KEY_LOGO = 6;
	public static final int KEY_CONTEXT = 7;

	public static final byte MASK_SHIFT = 1 << KEY_SHIFT;
	public static final byte MASK_CONTROL = 1 << KEY_CONTROL;
	public static final byte MASK_ALT = 1 << KEY_ALT;
	public static final byte MASK_LOGO = 1 << KEY_LOGO;
	public static final byte MASK_CONTEXT = (byte) (1 << KEY_CONTEXT);

	public static final byte MASK_CAPS_LOCK = 1 << KEY_CAPS_LOCK;
	public static final byte MASK_NUM_LOCK = 1 << KEY_NUM_LOCK;
	public static final byte MASK_SCROLL_LOCK = 1 << KEY_SCROLL_LOCK;

	public static final int KEY_A = 'A';
	public static final int KEY_Z = 'Z';
	public static final int KEY_0 = '0';
	public static final int KEY_9 = '9';
	
	public static final int KEY_MINUS = '-';
	public static final int KEY_UNDERSCORE = '_';
	public static final int KEY_SLASH = '/';
	public static final int KEY_BAR = '|';
	public static final int KEY_BACK_SLASH = '\\';
	public static final int KEY_BACK_QUOTE = '`';
	public static final int KEY_TILDE = '~';
	public static final int KEY_EXCLAMATION = '!';
	public static final int KEY_AT = '@';
	public static final int KEY_POUND = '#';
	public static final int KEY_DOLLAR = '$';
	public static final int KEY_PERCENT = '%';
	public static final int KEY_CIRCUMFLEX = '^';
	public static final int KEY_AMPERSAND = '&';
	public static final int KEY_ASTERISK = '*';
	public static final int KEY_OPEN_PARENTHESIS = '(';
	public static final int KEY_CLOSE_PARENTHESIS = ')';
	public static final int KEY_PLUS = '+';
	public static final int KEY_EQUALS = '=';
	public static final int KEY_OPEN_BRACKET = '[';
	public static final int KEY_CLOSE_BRACKET = ']';
	public static final int KEY_OPEN_BRACE = '{';
	public static final int KEY_CLOSE_BRACE = '}';
	public static final int KEY_LESS = '<';
	public static final int KEY_GREATER = '>';
	public static final int KEY_COMMA = ',';
	public static final int KEY_PERIOD = '.';
	public static final int KEY_COLON = ':';
	public static final int KEY_SEMICOLON = ';';
	public static final int KEY_QUOTE = '"';
	public static final int KEY_SINGLE_QUOTE = '\'';
	public static final int KEY_QUESTION = '?';
	
	public static final int KEY_BACKSPACE = '\b';
	public static final int KEY_ENTER = '\r';
	public static final int KEY_TAB = '\t';
	public static final int KEY_ESCAPE = 27;
	public static final int KEY_SPACE = ' ';
	
	public static final int KEY_INSERT = 128;
	public static final int KEY_DELETE = 129;
	public static final int KEY_HOME = 130;
	public static final int KEY_END = 131;
	public static final int KEY_PAGE_UP = 132;
	public static final int KEY_PAGE_DOWN = 133;
	public static final int KEY_ARROW_UP = 134;
	public static final int KEY_ARROW_DOWN = 135;
	public static final int KEY_ARROW_LEFT = 136;
	public static final int KEY_ARROW_RIGHT = 137;

	public static final int KEY_PRINT_SCREEN = 138;
	public static final int KEY_SYSRQ = 139;
	public static final int KEY_PAUSE = 140;
	public static final int KEY_BREAK = 141;
	
	public static final int KEY_QUIT = 142;

	public static final int KEY_KP_SLASH = 143;
	public static final int KEY_KP_ASTERISK = 144;
	public static final int KEY_KP_MINUS = 145;
	public static final int KEY_KP_PLUS = 146;
	public static final int KEY_KP_DELETE = 147;
	public static final int KEY_KP_INSERT = 148;
	public static final int KEY_KP_ENTER = 149;
	public static final int KEY_KP_0 = 150;
	public static final int KEY_KP_1 = 151;
	public static final int KEY_KP_2 = 152;
	public static final int KEY_KP_3 = 153;
	public static final int KEY_KP_4 = 154;
	public static final int KEY_KP_5 = 155;
	public static final int KEY_KP_6 = 156;
	public static final int KEY_KP_7 = 157;
	public static final int KEY_KP_8 = 158;
	public static final int KEY_KP_9 = 159;
	public static final int KEY_KP_POINT = 160;
	public static final int KEY_KP_HOME = 161;
	public static final int KEY_KP_END = 162;
	public static final int KEY_KP_ARROW_UP = 163;
	public static final int KEY_KP_ARROW_DOWN = 164;
	public static final int KEY_KP_ARROW_LEFT = 165;
	public static final int KEY_KP_ARROW_RIGHT = 166;
	public static final int KEY_KP_PAGE_UP = 167;
	public static final int KEY_KP_PAGE_DOWN = 168;
	public static final int KEY_KP_SHIFT_5 = 169;
	
	public static final int KEY_F1 = 170;
	public static final int KEY_F2 = 171;
	public static final int KEY_F3 = 172;
	public static final int KEY_F4 = 173;
	public static final int KEY_F5 = 174;
	public static final int KEY_F6 = 175;
	public static final int KEY_F7 = 176;
	public static final int KEY_F8 = 177;
	public static final int KEY_F9 = 178;
	public static final int KEY_F10 = 179;
	public static final int KEY_F11 = 180;
	public static final int KEY_F12 = 181;
	
	// odd are joy #2
	public static final int KEY_JOYST_UP = 182;
	public static final int KEY_JOYST_DOWN = 184;
	public static final int KEY_JOYST_LEFT = 186;
	public static final int KEY_JOYST_RIGHT = 188;
	public static final int KEY_JOYST_UP_LEFT = 190;
	public static final int KEY_JOYST_UP_RIGHT = 192;
	public static final int KEY_JOYST_DOWN_LEFT = 194;
	public static final int KEY_JOYST_DOWN_RIGHT = 196;
	public static final int KEY_JOYST_FIRE = 198;
	public static final int KEY_JOYST_FIRE_UP = 200;
	public static final int KEY_JOYST_IDLE = 202;

}
