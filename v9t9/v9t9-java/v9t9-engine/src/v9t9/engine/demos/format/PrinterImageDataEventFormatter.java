
/*
  SoundDataEventFormatter.java

  (c) 2012-2014 Edward Swartz

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
import v9t9.engine.demos.events.PrinterImageEvent;

/**
 * @author ejs
 *
 */
public class PrinterImageDataEventFormatter extends BaseEventFormatter {

	public PrinterImageDataEventFormatter(String bufferId) {
		super(bufferId, PrinterImageEvent.ID);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#readEvent(v9t9.common.demo.IDemoInputEventBuffer)
	 */
	@Override
	public IDemoEvent readEvent(IDemoInputEventBuffer buffer)
			throws IOException {
		int type = buffer.read();
		if (type == PrinterImageEvent.DATA)
			return PrinterImageEvent.writeData(buffer.readRest());
		else if (type == PrinterImageEvent.NEW_PAGE)
			return PrinterImageEvent.newPage();
		else
			throw new IOException("unhandled printer event type: " + type);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEventFormatter#writeEvent(v9t9.common.demo.IDemoOutputEventBuffer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void writeEvent(IDemoOutputEventBuffer buffer, IDemoEvent event)
			throws IOException {
		PrinterImageEvent ev = (PrinterImageEvent) event;
		buffer.push(ev.getType());
		if (ev.getType() == PrinterImageEvent.DATA) {
			buffer.pushData(ev.getData());
		} else if (ev.getType() == PrinterImageEvent.NEW_PAGE) {
			;
		} else {
			throw new IOException("unhandled printer event type: " + ev.getType());
		}
			
	}

}
