/*
  SoundDataEventFormatter.java

  (c) 2012 Edward Swartz

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
import v9t9.engine.demos.events.SoundWriteDataEvent;

/**
 * @author ejs
 *
 */
public class SoundDataEventFormatter extends BaseEventFormatter {

	public SoundDataEventFormatter(String bufferId) {
		super(bufferId, SoundWriteDataEvent.ID);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#readEvent(v9t9.common.demo.IDemoInputEventBuffer)
	 */
	@Override
	public IDemoEvent readEvent(IDemoInputEventBuffer buffer)
			throws IOException {
		// blast all the data to the same address
		return new SoundWriteDataEvent(0x8400, buffer.readRest());
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#writeEvent(v9t9.common.demo.IDemoOutputEventBuffer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void writeEvent(IDemoOutputEventBuffer buffer, IDemoEvent event)
			throws IOException {
		SoundWriteDataEvent ev = (SoundWriteDataEvent) event;
		byte[] data = ev.getData();
		buffer.pushData(data, 0, ev.getLength());	
	}

}
