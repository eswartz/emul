/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;
import java.io.OutputStream;

class DemoOutBuffer {
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
}