/*
  BasicKeyJoystickEventFormatter.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoInputEventBuffer;
import v9t9.common.demos.IDemoOutputEventBuffer;
import v9t9.engine.demos.events.BasicKeyJoystickEvent;
import v9t9.engine.demos.events.BasicKeyJoystickEvent.AsciiKeyEntry;
import v9t9.engine.demos.events.BasicKeyJoystickEvent.Entry;
import v9t9.engine.demos.events.BasicKeyJoystickEvent.JoystickEntry;
import v9t9.engine.demos.events.BasicKeyJoystickEvent.ShiftKeyEntry;

/**
 * @author ejs
 *
 */
public class BasicKeyJoystickEventFormatter extends BaseEventFormatter {

	public static final int KEY_CODE = 0;
	public static final int SHIFT_CODE = 1;
	public static final int JOYST_CODE = 2;
	
	public BasicKeyJoystickEventFormatter(String bufferId) {
		super(bufferId, BasicKeyJoystickEvent.ID);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoEventFormatter#readEvent(v9t9.common.demos.IDemoInputEventBuffer)
	 */
	@Override
	public IDemoEvent readEvent(IDemoInputEventBuffer buffer)
			throws IOException {
		List<Entry> entries = new ArrayList<BasicKeyJoystickEvent.Entry>();
		while (buffer.isAvailable()) {
			int code = buffer.read();
			int byt;
			switch (code) {
			case KEY_CODE:
				byt = buffer.read();
				if (byt < 0)
					throw new EOFException();
				entries.add(new AsciiKeyEntry((char) (byt & 0x7f), (byt & 0x80) == 0));
				break;
			case SHIFT_CODE:
				byt = buffer.read();
				if (byt < 0)
					throw new EOFException();
				entries.add(new ShiftKeyEntry((byte) byt));
				break;
			case JOYST_CODE:
				byt = buffer.read();
				if (byt < 0)
					throw new EOFException();
				entries.add(new JoystickEntry(byt & 0x3,
						(byt & 0xfc)));
				break;
			}
		}
		return new BasicKeyJoystickEvent(entries.toArray(new Entry[entries.size()]));
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoEventFormatter#writeEvent(v9t9.common.demos.IDemoOutputEventBuffer, v9t9.common.demos.IDemoEvent)
	 */
	@Override
	public void writeEvent(IDemoOutputEventBuffer buffer, IDemoEvent event)
			throws IOException {
		BasicKeyJoystickEvent ev = (BasicKeyJoystickEvent) event;
		for (BasicKeyJoystickEvent.Entry entry : ev.getEntries()) {
			if (entry instanceof AsciiKeyEntry) {
				AsciiKeyEntry asciiKeyEntry = (AsciiKeyEntry) entry;
				buffer.push((byte) KEY_CODE);
				buffer.push((byte) ((asciiKeyEntry.isDown() ? 0 : 0x80)  
						| asciiKeyEntry.getCh()));
			}
			else if (entry instanceof ShiftKeyEntry) {
				ShiftKeyEntry shiftKeyEntry = (ShiftKeyEntry) entry;
				buffer.push((byte) SHIFT_CODE);
				buffer.push((byte) shiftKeyEntry.getMask());
			}
			else if (entry instanceof JoystickEntry) {
				JoystickEntry joystEntry = (JoystickEntry) entry;
				buffer.push((byte) JOYST_CODE);
				buffer.push((byte) ((joystEntry.getNumber() - 1)  | 
						joystEntry.getMoveButtonMask()));
			}
			else {
				throw new IOException(new UnsupportedOperationException(entry.toString()));
			}
		}
	}

}
