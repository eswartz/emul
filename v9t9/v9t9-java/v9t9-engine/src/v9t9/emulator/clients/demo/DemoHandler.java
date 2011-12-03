/**
 * 
 */
package v9t9.emulator.clients.demo;

import java.io.IOException;
import java.io.OutputStream;

/**
 *	The demo file format is very rudimentary.
 *
 *	Header:		'V910' bytes
 *
 *	Followed by a list of sections for various demo_types.
 *	Each section starts with one byte (demo_type) and is
 *	followed by nothing (for the timer) or by a buffer length
 *	(little-endian, 16 bits) which is passed to the event handler.
 *
 *	Video has 16-bit little-endian addresses followed (if the
 *	address does not have the 0x8000 bit set, which is a register
 *	write) by a 16-bit little-endian length and data bytes.
 *
 *	Sound has a series of data bytes.
 *
 *	Speech has a series of demo_speech_event bytes, and the
 *	demo_speech_adding_byte event is followed by that byte.
 * @author Ed
 */
public class DemoHandler {
	char[] DEMO_MAGIC_HEADER = { 'V', '9', '1', '0' };
	private OutputStream out;
	private DemoBuffer videoBuffer;
	private DemoBuffer soundBuffer;
	private VideoSubBuffer videoSubBuffer;
	final static int DEMO_MAGIC_HEADER_LENGTH = 4;

	final static byte
		demo_type_tick = 0,	/* wait for emulator tick */
		demo_type_video = 1,	/* video addresses and data */
		demo_type_sound = 2,	/* sound bytes */
		demo_type_speech = 3;	/* speech commands */

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
		private final byte type;

		public DemoPacketBuffer(OutputStream stream, byte type, int size) {
			super(stream, size);
			this.type = type;
		}
		protected void writeHeader() throws IOException {
			stream.write(type);
			super.writeHeader();
		}
	}
	public DemoHandler() {
		
	}
	
	public void startRecording(OutputStream out) {
		this.out = out;
		this.videoBuffer = new DemoPacketBuffer(out, demo_type_video, 1024);
		this.videoSubBuffer = new VideoSubBuffer(out, 1024);
		this.soundBuffer = new DemoPacketBuffer(out, demo_type_sound, 1024);
	}
	
	public void stopRecording() {
		if (out == null)
			return;
		
		try {
			out.close();
		} catch (IOException e) {
		}
		out = null;
		soundBuffer = null;
		videoBuffer = null;
	}
	
	public void tick() {
		if (out == null)
			return;
			
		try {
			out.write(demo_type_tick);
			videoBuffer.flush();
			soundBuffer.flush();
		} catch (IOException e) {
			fail(e);
		}
	}
	
	private void fail(IOException e) {
		e.printStackTrace();
		stopRecording();
	}

	public void pushSoundByte(byte sound) {
		if (soundBuffer != null) {
			try {
				soundBuffer.push(sound);
			} catch (IOException e) {
				fail(e);
			}
		}
	}
	
	public void pushSetVideoMemory(short addr, byte val) {
		if (videoBuffer != null) {
			try {
				if (!videoSubBuffer.isNextAddr(addr)) {
					videoSubBuffer.flush();
					videoSubBuffer.init(addr);
				}
				videoSubBuffer.push(val);
			}catch (IOException e) {
				fail(e);
			}
		}
	}
}
