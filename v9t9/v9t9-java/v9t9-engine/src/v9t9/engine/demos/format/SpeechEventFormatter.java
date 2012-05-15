/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.IOException;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoInputEventBuffer;
import v9t9.common.demo.IDemoOutputEventBuffer;
import v9t9.common.demo.ISpeechEvent;
import v9t9.engine.demos.events.SpeechEvent;

/**
 * @author ejs
 *
 */
public class SpeechEventFormatter extends BaseEventFormatter  {

	public SpeechEventFormatter(String bufferId) {
		super(bufferId, SpeechEvent.ID);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#readEvent(v9t9.common.demo.IDemoInputEventBuffer)
	 */
	@Override
	public IDemoEvent readEvent(IDemoInputEventBuffer buffer)
			throws IOException {
		int code = buffer.read();
		if (code == ISpeechEvent.SPEECH_ADDING_BYTE) {
			int byt = buffer.read() & 0xff;
			return new SpeechEvent(code, byt);
		} else {
			ISpeechEvent ev = new SpeechEvent(code);
			if (ev == null) {
				throw buffer.newBufferException("corrupt speech byte " + Integer.toHexString(code));
			}
			return ev;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#writeEvent(v9t9.common.demo.IDemoOutputEventBuffer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void writeEvent(IDemoOutputEventBuffer buffer, IDemoEvent event)
			throws IOException {
		ISpeechEvent ev = (ISpeechEvent) event;
		buffer.push((byte) ev.getCode());
		if (ev.getCode() == ISpeechEvent.SPEECH_ADDING_BYTE)
			buffer.push(ev.getAddedByte());
	}

}
