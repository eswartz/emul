/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;

import v9t9.common.demo.IDemoEvent;
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
			String identifier) {
		super(is, identifier);
		this.code = code;
	}
	
	public String getIdentifier() {
		return label;
	}

	public int getCode() {
		return code;
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
	public void refill() throws IOException {
		length = DemoFormat.readVariableLengthNumber(is);
		
		if (buffer == null || length > buffer.length)
			buffer = new byte[length];
		
		int read = is.read(buffer, 0, length);
		if (read != length)
			throw newBufferException("short read of " + read + ", expected " + length);
		
		index = 0;
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

	/**
	 * Decode events from the buffer
	 * @param queuedEvents
	 */
	public void decodeEvents(Queue<IDemoEvent> queuedEvents) throws IOException {
		throw new UnsupportedOperationException();
	}

	
}
