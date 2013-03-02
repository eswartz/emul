/*
  BasicKeyJoystickEvent.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.events;

import java.io.EOFException;
import java.io.IOException;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.engine.demos.format.DemoInputBuffer;

/**
 * @author ejs
 *
 */
public class BasicKeyJoystickEvent implements IDemoEvent {

	public static final String ID = "KeyboardEvent";
	
	public abstract static class Entry {
	}
	
	/** Event representing keypress or release of ASCII key */
	public static class AsciiKeyEntry extends Entry {
		/** ASCII representation */
		private char ch;		
		/** true: key pressed, false: key released */
		private boolean down;
		
		public AsciiKeyEntry(char ch, boolean down) {
			if (ch >= 0x7f || ch <= 0)
				throw new IllegalArgumentException();
			this.ch = ch;
			this.down = down;
		}

		public boolean isDown() {
			return down;
		}
		public char getCh() {
			return ch;
		}
		
	}
	
	/** Event representing change in state of shift keys */
	public static class ShiftKeyEntry extends Entry {
		/** mask (see {@link IKeyboardState}) */
		private byte mask;		
		
		public ShiftKeyEntry(byte mask) {
			this.mask = mask;
		}
		public byte getMask() {
			return mask;
		}
	}
	
	/** Event representing change in state of a joystick's 
	 * stick or button
	 *
	 */
	public static class JoystickEntry extends Entry {
		private byte number;
		private byte moveButtonMask;
		
		public JoystickEntry(int number, int moveButtonMask) {
			if (number <= 0 || number >= 2 || (moveButtonMask & ~0xfc) != 0)
				throw new IllegalArgumentException();
			this.number = (byte) number;
			this.moveButtonMask = (byte) moveButtonMask;
		}
		
		public static JoystickEntry create(DemoInputBuffer in) throws IOException {
			int byt = in.read();
			if (byt < 0)
				throw new EOFException();
			return new JoystickEntry((byt & 0x3) + 1, byt & 0xfc);
		}

		public byte getNumber() {
			return number;
		}
		
		public byte getMoveButtonMask() {
			return moveButtonMask;
		}
	}
	
	
	private Entry[] entries;

	public BasicKeyJoystickEvent(Entry[] keys) {
		this.entries = keys;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoEvent#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}

	/**
	 * @return
	 */
	public Entry[] getEntries() {
		return entries;
	}
}
