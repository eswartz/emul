/*
  DemoOutputEventBuffer.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format;

import java.io.OutputStream;

import v9t9.common.demos.IDemoOutputEventBuffer;

/**
 * @author ejs
 *
 */
public abstract class DemoOutputEventBuffer extends DemoOutputBuffer implements
		IDemoOutputEventBuffer {

	/**
	 * @param stream
	 * @param id
	 * @param code
	 */
	public DemoOutputEventBuffer(OutputStream stream, String id, int code) {
		super(stream, id, code);
	}

}
