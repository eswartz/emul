/*
  SpeechEventFormatter.java

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
