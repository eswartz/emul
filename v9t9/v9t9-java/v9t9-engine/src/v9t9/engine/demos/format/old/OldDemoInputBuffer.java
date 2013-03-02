/*
  OldDemoInputBuffer.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format.old;

import java.io.IOException;

import ejs.base.utils.CountingInputStream;

import v9t9.common.demos.IDemoInputEventBuffer;
import v9t9.common.events.NotifyException;
import v9t9.engine.demos.stream.BaseDemoInputBuffer;

public abstract class OldDemoInputBuffer extends BaseDemoInputBuffer implements IDemoInputEventBuffer {
	private final byte[] buffer;
	private int length;
	int index;

	private int myType;
	
	public OldDemoInputBuffer(CountingInputStream is, String label, int myType, int size) {
		super(is, label);
		this.buffer = new byte[size];
		this.index = 0;
		this.length = size;
		this.myType = myType;
	}

	protected void readHeader() throws IOException {
		startPos = ((CountingInputStream) is).getPosition();
		length = (is.read() & 0xff) | ((is.read() & 0xff) << 8);
//		System.err.println(Integer.toHexString(reader.isPos)+": " + label + " header, length " 
//				+ length + " (to " + Integer.toHexString(reader.isPos + length + 2) + ")");
	}
	public long getEffectivePos() {
		return startPos + index;
	}
	public boolean isAvailable() {
		return index < length;
	}
	
	public void refill() throws IOException {
		readHeader();
		if (length > buffer.length)
			throw newBufferException("length longer than max " + label + " buffer");
		int read = is.read(buffer, 0, length);
		if (read != length)
			throw newBufferException("short read of " + read + ", expected " + length); 
		length = read;
		index = 0;
	}

	public int read() throws IOException {
		if (index >= length) {
			refill();
		}
		return buffer[index++] & 0xff; 
	}

	public byte[] readRest() {
		byte[] data = new byte[length - index];
		System.arraycopy(buffer, index, data, 0, length - index);
		index = length;
		return data;
	}

	public int readWord() throws IOException {
		int word = (read() & 0xff) | ((read() & 0xff) << 8);
		return word;
	}
	
	/**
	 * @param chunkLength
	 * @return
	 * @throws IOException 
	 * @throws NotifyException 
	 */
	public byte[] readData(int chunkLength) throws IOException {
		// all chunk should be buffered, or not
		if (index >= length) {
			int typ = is.read();
			if (typ != myType) {
				throw newBufferException("expected successive " + label + " blocks"); 
			}
			refill();
		}
		byte[] data = new byte[chunkLength];
		try {
			System.arraycopy(buffer, index, data, 0, chunkLength);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw newBufferException("array overflow for " + label + " ("+index+"+"+chunkLength+" >= " + buffer.length); 
		}
		index += chunkLength;
		return data;
	}

	/**
	 * @return
	 */
	public int getCode() {
		return myType;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoInputEventBuffer#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return label;
	}
	

}