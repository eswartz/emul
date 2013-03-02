/*
  OldSpeechEvent.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.demos.events;

import v9t9.common.demos.IDemoEvent;


/**
 * @author ejs
 *
 */
public class OldSpeechEvent implements IDemoEvent {

	/**
	 * 
	 */
	public static final String ID = "OldSpeechEvent";

	/** new phrase */
	public static final int SPEECH_STARTING = 0;

	/** an LPC encoded byte (Byte) (legacy format) */
	public static final int SPEECH_ADDING_BYTE = 1;
	
	/** terminating speech phrase */
	public static final int SPEECH_TERMINATING = 2;

	/** finished naturally */
	public static final int SPEECH_STOPPING = 3;
	
	private final int code;
	private final byte addedByte;
	
	public OldSpeechEvent(int code) {
		if (code == SPEECH_ADDING_BYTE)
			throw new IllegalArgumentException("SPEECH_ADDING_BYTE must have a byte");
		this.code = code;
		this.addedByte = -1;
	}
	public OldSpeechEvent(byte byt) {
		this.code = SPEECH_ADDING_BYTE;
		this.addedByte = byt;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEvent#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		result = prime * result + addedByte;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OldSpeechEvent other = (OldSpeechEvent) obj;
		if (code != other.code)
			return false;
		if (addedByte != other.addedByte) 
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.demos.events.ISpeechEvent#getCode()
	 */
	public int getCode() {
		return code;
	}
	
	public byte getAddedByte() {
		return addedByte;
	}

}
