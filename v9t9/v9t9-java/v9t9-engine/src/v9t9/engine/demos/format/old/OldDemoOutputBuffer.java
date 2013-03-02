/*
  OldDemoOutputBuffer.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format.old;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demos.IDemoOutputBuffer;
import v9t9.engine.demos.stream.BaseDemoOutputBuffer;

public abstract class OldDemoOutputBuffer extends BaseDemoOutputBuffer implements IDemoOutputBuffer {
	byte[] buffer;
	int length;
	int index;
	public OldDemoOutputBuffer(OutputStream stream, int size) {
		super(stream);
		this.buffer = new byte[size];
		this.index = 0;
		this.length = size;
	}
	@Override
	public void push(byte val) throws IOException {
		if (index >= length) {
			flush();
		}
		buffer[index++] = val;
	}

	/** Push a little-endian 16-bit word */
	public void pushWord(int val) throws IOException {
		push((byte) (val & 0xff));
		push((byte) ((val >> 8) & 0xff));
	}

	
	public void flush() throws IOException {
		if (index > 0) {
			writeHeader();
			stream.write(buffer, 0, index);
			index = 0;
		}
	}
	protected void writeHeader() throws IOException {
		stream.write((byte) (index & 0xff));
		stream.write((byte) (index >> 8));
	}

	public boolean isEmpty() {
		return index == 0;
	}

	/**
	 * @param i
	 * @return
	 */
	@Override
	public boolean isAvailable(int i) {
		return index + i <= length;
	}
	
	public void pushData(byte[] chunk, int offs, int len) throws IOException {
		if (index + len >= length) {
			flush();
		}
		System.arraycopy(chunk, offs, buffer, index, len);
		index += len;
	}

}