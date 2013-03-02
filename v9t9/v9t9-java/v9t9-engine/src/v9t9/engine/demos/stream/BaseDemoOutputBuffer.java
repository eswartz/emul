/*
  BaseDemoOutputBuffer.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.stream;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demos.IDemoOutputBuffer;


/**
 * @author ejs
 * 
 */
public abstract class BaseDemoOutputBuffer implements IDemoOutputBuffer {

	protected final OutputStream stream;

	public abstract boolean isAvailable(int i);

	/**
	 * @param stream
	 * 
	 */
	public BaseDemoOutputBuffer(OutputStream stream) {
		this.stream = stream;
	}

	public void pushData(byte[] data) throws IOException {
		pushData(data, 0, data.length);
	}

}