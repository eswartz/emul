/*
  OldSpeechEvent.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
