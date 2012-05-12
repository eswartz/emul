/**
 * 
 */
package v9t9.server.demo.events;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.machine.IMachine;
import v9t9.server.demo.DemoFormat;

/**
 * @author ejs
 *
 */
public class SpeechWriteEvent implements IDemoEvent {

	private final DemoFormat.SpeechEvent event;
	private final int byt;

	/**
	 * @param event
	 * @param byt
	 */
	public SpeechWriteEvent(DemoFormat.SpeechEvent event,
			int byt) {
		this.event = event;
		this.byt = byt;
	}
	
	/**
	 * @return the byt
	 */
	public int getAddedByte() {
		return byt;
	}
	/**
	 * @return the event
	 */
	public DemoFormat.SpeechEvent getEvent() {
		return event;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEvent#execute(v9t9.common.machine.IMachine)
	 */
	@Override
	public void execute(IMachine machine) {
		switch (event) {
		case INTERRUPT:
			break;
		case STARTING:
			break;
		case STOPPING:
			break;
		case TERMINATING:
			break;
		case ADDING_BYTE:
			break;
		
		}
	}

}
