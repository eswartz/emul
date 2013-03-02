/*
  DemoInputBuffer.java

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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import v9t9.engine.demos.stream.BaseDemoInputBuffer;

/**
 * @author ejs
 *
 */
public class DemoInputBuffer extends BaseDemoInputBuffer {
	private byte[] buffer;
	private int length;
	private int index;
	private final int code;
	
	class Utf8InputStream extends InputStream {

		@Override
		public int read() throws IOException {
			if (index >= length)
				return -1;
			return buffer[index++] & 0xff;
		}
		
	}
	
	private Utf8InputStream utf8Is = new Utf8InputStream();
	
	public DemoInputBuffer(InputStream is, 
			int code,
			String identifier)  {
		super(is, identifier);
		this.code = code;
	}
	
	public String getIdentifier() {
		return label;
	}

	public int getCode() {
		return code;
	}
	

	@Override
	public void refill() throws IOException {
		length = DemoFormat.readVariableLengthNumber(is);
		
		if (buffer == null || length > buffer.length)
			buffer = new byte[length];
		
		int read = is.read(buffer, 0, length);
		if (read != length)
			throw newBufferException("short read of " + read + ", expected " + length);
		
		index = 0;
	}



	/* (non-Javadoc)
	 * @see v9t9.engine.demos.stream.IDemoInputBuffer#getEffectivePos()
	 */
	@Override
	public long getEffectivePos() {
		return startPos + index;
	}

	@Override
	public boolean isAvailable() {
		return index < length;
	}

	@Override
	public int read() throws IOException {
		if (index >= length)
			throw new EOFException();
		return buffer[index++] & 0xff; 
	}

	@Override
	public byte[] readRest() {
		byte[] data = new byte[length - index];
		System.arraycopy(buffer, index, data, 0, length - index);
		index = length;
		return data;
	}

	@Override
	public byte[] readData(int chunkLength) throws IOException {
		if (index + chunkLength > length) {
			throw newBufferException("short data block in " + label + "; expected " + chunkLength +
					", only have " + (length - index));
		}
		
		byte[] data = new byte[chunkLength];
		System.arraycopy(buffer, index, data, 0, chunkLength);
		
		index += chunkLength;
		return data;
	}
	
	public int readVar() throws IOException {
		return DemoFormat.readVariableLengthNumber(utf8Is);
	}
	
}
