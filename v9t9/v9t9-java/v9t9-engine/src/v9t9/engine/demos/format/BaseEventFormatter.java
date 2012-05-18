/**
 * 
 */
package v9t9.engine.demos.format;

import v9t9.common.demos.IDemoEventFormatter;

/**
 * @author ejs
 *
 */
public abstract class BaseEventFormatter implements IDemoEventFormatter {

	private final String bufferId;
	private final String eventId;
	
	/**
	 * @param bufferId TODO
	 * @param eventId 
	 * 
	 */
	public BaseEventFormatter(String bufferId, String eventId) {
		this.bufferId = bufferId;
		this.eventId = eventId;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#getBufferIdentifer()
	 */
	@Override
	public String getBufferIdentifer() {
		return bufferId;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return eventId;
	}
}
