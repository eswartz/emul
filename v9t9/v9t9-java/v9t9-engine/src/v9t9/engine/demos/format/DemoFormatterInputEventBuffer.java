/*
  DemoFormatterInputEventBuffer.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoEventFormatter;

/**
 * @author ejs
 *
 */
public final class DemoFormatterInputEventBuffer extends
		DemoInputEventBuffer {
	/**
	 * 
	 */
	private final IDemoEventFormatter formatter;

	/**
	 * @param is
	 * @param code
	 * @param identifier
	 * @param formatter
	 */
	public DemoFormatterInputEventBuffer(InputStream is, int code,
			String identifier, IDemoEventFormatter formatter) {
		super(is, code, identifier);
		this.formatter = formatter;
	}

	@Override
	public void decodeEvents(Queue<IDemoEvent> queuedEvents) throws IOException {
		while (isAvailable())
			queuedEvents.add(formatter.readEvent(this));
	}
}