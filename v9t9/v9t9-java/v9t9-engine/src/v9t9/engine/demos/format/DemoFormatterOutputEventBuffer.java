/*
  DemoFormatterOutputEventBuffer.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoEventFormatter;

/**
 * @author ejs
 *
 */
public final class DemoFormatterOutputEventBuffer extends
		DemoOutputEventBuffer {
	private final IDemoEventFormatter formatter;

	/**
	 * @param stream
	 * @param code
	 * @param id
	 */
	public DemoFormatterOutputEventBuffer(OutputStream stream, int code,
			String id, IDemoEventFormatter formatter) {
		super(stream, id, code);
		this.formatter = formatter;
	}

	@Override
	public void encodeEvent(IDemoEvent event) throws IOException {
		formatter.writeEvent(this, event);
	}
}