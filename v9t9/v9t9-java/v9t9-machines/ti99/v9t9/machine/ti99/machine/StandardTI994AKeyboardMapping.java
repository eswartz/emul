/*
  StandardTI994AKeyboardMapping.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.machine;

import static v9t9.common.keyboard.KeyboardConstants.*;
import static v9t9.machine.ti99.machine.TI994AKeys.*;
import v9t9.common.keyboard.IKeyboardMapping;
import v9t9.engine.keyboard.BaseKeyboardMapping;
import v9t9.engine.keyboard.BaseKeyboardMode;
import v9t9.engine.keyboard.BaseKeyboardMode.KeyInfoBuilder;

/**
 * @author ejs
 *
 */
public class StandardTI994AKeyboardMapping extends BaseKeyboardMapping
		implements IKeyboardMapping {

	private static BaseKeyboardMode defaultMode = new BaseKeyboardMode(TI994A.KEYBOARD_MODE_TI994A, "Default");
	static { 
		KeyInfoBuilder.forKey(PKEY_1)
			.normal('1', "1").shift(KEY_EXCLAMATION, "!").fctn(KEY_F1, "DELETE").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_2)
			.normal('2', "2").shift(KEY_AT, "@").fctn(KEY_F2, "INSERT").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_3)
			.normal('3', "3").shift(KEY_POUND, "#").fctn(KEY_F3, "ERASE").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_4)
			.normal('4', "4").shift(KEY_DOLLAR, "$").fctn(KEY_F4, "CLEAR").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_5)
			.normal('5', "5").shift(KEY_PERCENT, "%").fctn(KEY_F5, "BEGIN").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_6)
			.normal('6', "6").shift(KEY_CIRCUMFLEX, "^").fctn(KEY_F6, "PROC'D").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_7)
			.normal('7', "7").shift(KEY_AMPERSAND, "&").fctn(KEY_F7, "AID").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_8)
			.normal('8', "8").shift(KEY_ASTERISK, "*").fctn(KEY_F8, "REDO").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_9)
			.normal('9', "9").shift(KEY_OPEN_PARENTHESIS, "(").fctn(KEY_F9, "BACK").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_0)
			.normal('0', "0").shift(KEY_CLOSE_PARENTHESIS, ")").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_EQU)
			.normal(KEY_EQUALS, "=").shift(KEY_PLUS, "+").fctn(KEY_QUIT, "QUIT").apply(defaultMode);
		
		KeyInfoBuilder.forKey(PKEY_Q)
			.alpha('Q', "q").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_W)
			.alpha('W', "w").fctn(KEY_TILDE, "~").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_E)
			.alpha('E', "e").fctn(KEY_ARROW_UP, "UP").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_R)
			.alpha('R', "r").fctn(KEY_OPEN_BRACKET, "[").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_T)
			.alpha('T', "t").fctn(KEY_CLOSE_BRACKET, "]").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_Y)
			.alpha('Y', "y").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_U)
			.alpha('U', "u").fctn(KEY_UNDERSCORE, "_").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_I)
			.alpha('I', "i").fctn(KEY_QUESTION, "?").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_O)
			.alpha('O', "o").fctn(KEY_SINGLE_QUOTE, "'").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_P)
			.alpha('P', "p").fctn(KEY_QUOTE, "\"").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_SLASH)
			.normal(KEY_SLASH, "/").shift(KEY_MINUS, "-").apply(defaultMode);

		KeyInfoBuilder.forKey(PKEY_A)
			.alpha('A', "a").fctn(KEY_BAR, "|").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_S)
			.alpha('S', "s").fctn(KEY_ARROW_LEFT, "LEFT").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_D)
			.alpha('D', "d").fctn(KEY_ARROW_RIGHT, "RIGHT").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_F)
			.alpha('F', "f").fctn(KEY_OPEN_BRACE, "{").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_G)
			.alpha('G', "g").fctn(KEY_CLOSE_BRACE, "}").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_H).alpha('H', "h").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_J).alpha('J', "j").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_K).alpha('K', "k").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_L).alpha('L', "l").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_SEMI)
			.normal(KEY_SEMICOLON, ";").shift(KEY_COLON, ":").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_ENTER)
			.normal(KEY_ENTER, "ENTER").apply(defaultMode);

		KeyInfoBuilder.forKey(PKEY_LSHIFT)
			.normal(KEY_SHIFT, "SHIFT").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_Z)
			.alpha('Z', "z").fctn(KEY_BACK_SLASH, "\\").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_X)
			.alpha('X', "x").fctn(KEY_ARROW_DOWN, "DOWN").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_C)
			.alpha('C', "c").fctn(KEY_BACK_QUOTE, "`").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_V).alpha('V', "v").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_B).alpha('B', "b").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_N).alpha('N', "n").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_M).alpha('M', "m").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_COMMA)
			.normal(KEY_COMMA, ",").shift(KEY_LESS, "<").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_PERIOD)
			.normal(KEY_PERIOD, ".").shift(KEY_GREATER, ">").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_RSHIFT)
			.normal(KEY_SHIFT, "SHIFT").apply(defaultMode);

		KeyInfoBuilder.forKey(PKEY_ALPHA)
			.normal(KEY_CAPS_LOCK, "ALPHA\nLOCK").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_CTRL)
			.normal(KEY_CONTROL, "CTRL").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_SPACE)
			.normal(KEY_SPACE, " ").apply(defaultMode);
		KeyInfoBuilder.forKey(PKEY_FCTN)
			.normal(KEY_ALT, "FCTN").apply(defaultMode);

	}
	/**
	 * 
	 */
	public StandardTI994AKeyboardMapping() {
		add(defaultMode);
	}

}
