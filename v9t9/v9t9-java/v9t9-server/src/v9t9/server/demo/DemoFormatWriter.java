/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demo.IDemoOutputStream;
import v9t9.server.demo.DemoFormat.Event;

public class DemoFormatWriter implements IDemoOutputStream {

	private OutputStream os;

	private DemoBuffer videoBuffer;
	private DemoBuffer soundBuffer;
	private VideoSubBuffer videoSubBuffer;

	static class DemoBuffer {
		private byte[] buffer;
		private int length;
		private int index;
		protected final OutputStream stream;

		public DemoBuffer(OutputStream stream, int size) {
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
			writeHeader();
			stream.write(buffer, 0, index);
			index = 0;
		}
		protected void writeHeader() throws IOException {
			stream.write((byte) (index & 0xff));
			stream.write((byte) (index >> 8));
		}
	}
	
	static class VideoSubBuffer extends DemoBuffer {

		private int nextAddr;

		public VideoSubBuffer(OutputStream stream, int size) {
			super(stream, size);
			nextAddr = -1;
		}

		public boolean isNextAddr(short addr) {
			return addr == nextAddr;
		}

		public void init(short addr) throws IOException {
			push((byte) (addr & 0xff));
			push((byte) (addr >> 8));
			nextAddr = addr;
		}
		
		@Override
		public void push(byte arg0) throws IOException {
			super.push(arg0);
			nextAddr++;
		}
		
	}
	static class DemoPacketBuffer extends DemoBuffer {
		private final Event type;

		public DemoPacketBuffer(OutputStream stream, DemoFormat.Event type, int size) {
			super(stream, size);
			this.type = type;
		}
		protected void writeHeader() throws IOException {
			stream.write(type.getCode());
			super.writeHeader();
		}
	}
	
	public DemoFormatWriter(OutputStream os) throws IOException {
		this.os = os;
		
		os.write(DemoFormat.DEMO_MAGIC_HEADER);
		
		this.videoBuffer = new DemoPacketBuffer(os, DemoFormat.Event.VIDEO, 1024);
		this.videoSubBuffer = new VideoSubBuffer(os, 1024);
		this.soundBuffer = new DemoPacketBuffer(os, DemoFormat.Event.SOUND, 1024);
	}
	
	public void close() throws IOException {
		if (videoBuffer != null)
			videoBuffer.flush();
		if (soundBuffer != null)
			soundBuffer.flush();
		
		if (os != null) {
			os.close();
			os = null;
		}
		soundBuffer = null;
		videoBuffer = null;
	}
	
	
	public void tick() throws IOException {
		if (os == null)
			return;
			
		os.write(DemoFormat.Event.TICK.getCode());
		videoBuffer.flush();
		soundBuffer.flush();
	}
	
	public void pushSoundByte(byte sound) throws IOException {
		if (soundBuffer != null) {
			soundBuffer.push(sound);
		}
	}
	
	public void pushSetVideoMemory(short addr, byte val) throws IOException {
		if (videoBuffer != null) {
			if (!videoSubBuffer.isNextAddr(addr)) {
				videoSubBuffer.flush();
				videoSubBuffer.init(addr);
			}
			videoSubBuffer.push(val);
		}
	}
}
