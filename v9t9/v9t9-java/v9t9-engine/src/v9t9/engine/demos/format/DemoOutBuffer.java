/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.IOException;
import java.io.OutputStream;

public class DemoOutBuffer {
	private byte[] buffer;
	private int length;
	private int index;
	protected final OutputStream stream;

	public DemoOutBuffer(OutputStream stream, int size) {
		this.stream = stream;
		this.buffer = new byte[size];
		this.index = 0;
		this.length = size;
	}
	public void push(byte val) throws IOException {
		if (index >= length) {
			flush();
		}
		buffer[index++] = val;
	}
	public boolean flush() throws IOException {
		if (index > 0) {
			writeHeader();
			stream.write(buffer, 0, index);
			index = 0;
			return true;
		}
		return false;
	}
	protected void writeHeader() throws IOException {
		stream.write((byte) (index & 0xff));
		stream.write((byte) (index >> 8));
	}

	public boolean isEmpty() {
		return index == 0;
	}

	public void pushData(byte[] chunk, int offs, int len) throws IOException {
		if (index + len >= length) {
			flush();
		}
		System.arraycopy(chunk, offs, buffer, index, len);
		index += len;
	}
	
	public void pushWord(int val) throws IOException {
		push((byte) (val & 0xff));
		push((byte) ((val >> 8) & 0xff));
	}
	/**
	 * @param i
	 * @return
	 */
	public boolean isAvailable(int i) {
		return index + i <= length;
	}
	/**
	 * @param data
	 * @throws IOException 
	 */
	public void pushData(byte[] data) throws IOException {
		pushData(data, 0, data.length);
	}
	
	/**
	 * Push a variable-length signed integer (UTF-8)
	 * 
	 * @param i
	 * @throws IOException 
	 */
	public void pushVar(int i) throws IOException {
		if (i < 0) {
			push((byte) -1);
			i = -i;
		}
		
		if (i >= 0 && i < 0x80) {
			// 7 bits
			push((byte) i);
		} else if (i >= 0x80 && i < 0x800) {
			// 11 bits
			ensureSpace(2);
			push((byte) (0xc0 | (i >>> 6)));
			push((byte) (0x80 | (i & 0x3f)));
		} else if (i >= 0x800 && i < 0x10000) {
			// 16 bits
			ensureSpace(3);
			push((byte) (0xe0 | (i >>> 12)));
			push((byte) (0x80 | ((i >>> 6) & 0x3f)));
			push((byte) (0x80 | (i & 0x3f)));
		} else if (i >= 0x10000 && i < 0x200000) {
			// 21 bits
			ensureSpace(4);
			push((byte) (0xf0 | (i >>> 18)));
			push((byte) (0x80 | ((i >>> 12)) & 0x3f));
			push((byte) (0x80 | ((i >>> 6)) & 0x3f));
			push((byte) (0x80 | (i & 0x3f)));
		} else if (i >= 0x200000 && i < 0x4000000) {
			// 26 bits
			ensureSpace(5);
			push((byte) (0xf8 | (i >>> 24)));
			push((byte) (0x80 | ((i >>> 18)) & 0x3f));
			push((byte) (0x80 | ((i >>> 12)) & 0x3f));
			push((byte) (0x80 | ((i >>> 6)) & 0x3f));
			push((byte) (0x80 | (i & 0x3f)));
		} else  {
			// 31 bits
			ensureSpace(6);
			push((byte) (0xfc | (i >>> 30)));
			push((byte) (0x80 | ((i >>> 24)) & 0x3f));
			push((byte) (0x80 | ((i >>> 18)) & 0x3f));
			push((byte) (0x80 | ((i >>> 12)) & 0x3f));
			push((byte) (0x80 | ((i >>> 6)) & 0x3f));
			push((byte) (0x80 | (i & 0x3f)));
		}
	}
	/**
	 * @param i
	 * @throws IOException 
	 */
	public void ensureSpace(int i) throws IOException {
		if (index + i > length) {
			flush();
		}
	}
}