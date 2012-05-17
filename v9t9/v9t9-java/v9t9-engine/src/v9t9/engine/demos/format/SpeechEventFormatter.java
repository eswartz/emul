/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.IOException;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoInputEventBuffer;
import v9t9.common.demo.IDemoOutputEventBuffer;
import v9t9.common.demo.ISpeechEvent;
import v9t9.common.speech.ILPCParameters;
import v9t9.engine.demos.events.SpeechEvent;
import v9t9.engine.speech.LPCParameters;

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
			return new SpeechEvent((byte) buffer.read());
		} else if (code == ISpeechEvent.SPEECH_ADDING_EQUATION) {
			int len = ((DemoInputBuffer) buffer).readVar();
			byte[] bytes = buffer.readData(len);
			LPCParameters params = new LPCParameters();
			try {
				params.fromBytes(bytes);
			} catch (IllegalArgumentException e) {
				throw buffer.newBufferException("corrupt speech equation " + Integer.toHexString(code) + "; " + e.getMessage());
			}
			return new SpeechEvent(params);
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
		if (ev.getCode() == ISpeechEvent.SPEECH_ADDING_BYTE) {
			buffer.push((Byte) ev.getData());
		} else if (ev.getCode() == ISpeechEvent.SPEECH_ADDING_EQUATION) {
			byte[] bytes = ((ILPCParameters) ev.getData()).toBytes();
			((DemoOutputBuffer) buffer).pushVar(bytes.length);
			buffer.pushData(bytes);
		}
	}

}
