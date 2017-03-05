/*
  SpeechEventFormatter.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format;

import java.io.IOException;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoInputEventBuffer;
import v9t9.common.demos.IDemoOutputEventBuffer;
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
		int len = ((DemoInputBuffer) buffer).readVar();
		byte[] bytes = buffer.readData(len);
		LPCParameters params = new LPCParameters();
		try {
			params.fromBytes(bytes);
		} catch (IllegalArgumentException e) {
			throw buffer.newBufferException("corrupt speech equation: " + e.getMessage());
		}
		return new SpeechEvent(params);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#writeEvent(v9t9.common.demo.IDemoOutputEventBuffer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void writeEvent(IDemoOutputEventBuffer buffer, IDemoEvent event)
			throws IOException {
		SpeechEvent ev = (SpeechEvent) event;

		byte[] bytes = ev.getParams().toBytes();
		((DemoOutputBuffer) buffer).pushVar(bytes.length);
		buffer.pushData(bytes);
	}

}
