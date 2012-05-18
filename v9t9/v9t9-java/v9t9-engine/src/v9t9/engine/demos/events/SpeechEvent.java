/**
 * 
 */
package v9t9.engine.demos.events;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.speech.ILPCParameters;

/**
 * New-style speech event.
 * @author ejs
 *
 */
public class SpeechEvent  implements IDemoEvent {

	public static final String ID = "SpeechEvent";
	
	private final ILPCParameters params;
	
	public SpeechEvent(ILPCParameters equ) {
		this.params = equ;
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
		result = prime * result + ((params == null) ? 0 : params.hashCode());
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
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		return true;
	}
	
	public ILPCParameters getParams() {
		return params;
	}

}
