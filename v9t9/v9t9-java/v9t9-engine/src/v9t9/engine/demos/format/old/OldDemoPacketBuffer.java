/*
  OldDemoPacketBuffer.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format.old;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demos.IDemoOutputEventBuffer;

public abstract class OldDemoPacketBuffer extends OldDemoOutputBuffer implements IDemoOutputEventBuffer {
	private final int type;

	public OldDemoPacketBuffer(OutputStream stream, int type, int size) {
		super(stream, size);
		this.type = type;
	}
	protected void writeHeader() throws IOException {
		stream.write(type);
		super.writeHeader();
	}
}
