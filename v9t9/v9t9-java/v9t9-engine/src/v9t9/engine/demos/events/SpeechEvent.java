/**
 * 
 */
package v9t9.engine.demos.events;

import v9t9.common.demo.ISpeechEvent;

/**
 * @author ejs
 *
 */
public class SpeechEvent implements ISpeechEvent {

	public static final String ID = "SpeechEvent";
	
	private final int code;
	private final int byt;
	
	
	public SpeechEvent(int code) {
		if (code == SPEECH_ADDING_BYTE)
			throw new IllegalArgumentException("SPEECH_ADDING_BYTE must have a byte");
		this.code = code;
		this.byt = -1;
	}
	public SpeechEvent(int code, int byt) {
		this.code = code;
		this.byt = byt;
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
		if (code == SPEECH_ADDING_BYTE) {
			result = prime * result + byt;
		}
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
		SpeechEvent other = (SpeechEvent) obj;
		if (code != other.code)
			return false;
		if (code == SPEECH_ADDING_BYTE) {
			if (byt != other.byt)
				return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.events.ISpeechEvent#getCode()
	 */
	@Override
	public int getCode() {
		return code;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.demos.events.ISpeechEvent#getAddedByte()
	 */
	@Override
	public byte getAddedByte() {
		return (byte) byt;
	}

}
