/*
  DemoOutputBuffer.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ejs.base.utils.RleSegmenter;

import v9t9.engine.demos.stream.BaseDemoOutputBuffer;

/**
 * This buffer can be any size, unlike the old format, thus
 * a lot of confusion about flushing in the middle of a packet
 * is gone.
 * <p/>
 * Also, as opposed to the old format, numbers may be encoded in a variable
 * length format (which is just UTF-8 with a flag marking negative values).
 * <p/>
 * Sound and video register writes are written in a generic form
 * (with variable-length encoding) so any register set may be represented
 * in the future.
 * <p/>
 * Finally, memory writes may be encoded with run-length encoding, to account
 * for the common case where the  
 * variable-length encoding to ensure they can be any length, but without making
 * every register write large.
 * @author ejs
 *
 */
public class DemoOutputBuffer extends BaseDemoOutputBuffer {

	private ByteArrayOutputStream buffer;
	private final int code;
	private final String id;

	public DemoOutputBuffer(OutputStream stream, String id, int code) {
		super(stream);
		this.id = id;
		if (code <= 0 || code >= 256)
			throw new IllegalArgumentException();
		this.code = code;
		this.buffer = new ByteArrayOutputStream();
	}
	
	/**
	 * @return the id
	 */
	public String getIdentifier() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.demos.format.old.BaseDemoOutputBuffer#isAvailable(int)
	 */
	@Override
	public boolean isAvailable(int i) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.demos.stream.IDemoOutputBuffer#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return buffer.size() == 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.stream.IDemoOutputBuffer#flush()
	 */
	@Override
	public void flush() throws IOException {
		if (buffer.size() > 0) {
			stream.write(code);
			DemoFormat.writeVariableLengthNumber(stream, buffer.size());
			stream.write(buffer.toByteArray());
			buffer.reset();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.stream.IDemoOutputBuffer#push(byte)
	 */
	@Override
	public void push(byte val) throws IOException {
		buffer.write(val);
	}

	/**
	 * Push a 16-bit big-endian word
	 * @param val
	 * @throws IOException
	 */
	public void pushWord(int val) throws IOException {
		buffer.write(val >> 8);
		buffer.write(val & 0xff);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.stream.IDemoOutputBuffer#pushData(byte[], int, int)
	 */
	@Override
	public void pushData(byte[] chunk, int offs, int len) throws IOException {
		buffer.write(chunk, offs, len);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.stream.IDemoOutputBuffer#pushData(byte[])
	 */
	@Override
	public void pushData(byte[] data) throws IOException {
		buffer.write(data);		
	}

	/**
	 * Push a variable-length signed integer (UTF-8)
	 * 
	 * @param i
	 * @throws IOException
	 */
	public void pushVar(int i) throws IOException {
		DemoFormat.writeVariableLengthNumber(buffer, i);
	}

	/**
	 * Emit a memory write using RLE encoding
	 * @param granularity minimum # of repeats before emitting RLE encoding
	 * @param address
	 * @param data
	 * @param offset
	 * @param length
	 * @throws IOException 
	 */
	public void pushRleMemoryWriteData(int granularity, int address, byte[] data, int offset,
			int length) throws IOException {
		RleSegmenter segmenter = new RleSegmenter(granularity, data, offset, length);
		for (RleSegmenter.Segment segment : segmenter) {
			int addr = segment.getOffset() - offset + address;
			if (segment.isRepeat()) {
				pushVar(addr);
				pushVar(- segment.getLength());
				push(data[segment.getOffset()]);
			}
			else {
				pushVar(addr);
				pushVar(segment.getLength());
				pushData(data, segment.getOffset(), segment.getLength());
			}
		}
		
	}
	
}
