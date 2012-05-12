/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.common.events.NotifyException;
import v9t9.server.demo.DemoFormat.BufferType;
import v9t9.server.demo.DemoFormat.SpeechEvent;
import v9t9.server.demo.events.SoundWriteDataEvent;
import v9t9.server.demo.events.SoundWriteRegisterEvent;
import v9t9.server.demo.events.SpeechWriteEvent;
import v9t9.server.demo.events.TimerTick;
import v9t9.server.demo.events.VideoWriteDataEvent;
import v9t9.server.demo.events.VideoWriteRegisterEvent;

public class DemoFormatWriter implements IDemoOutputStream {

	private OutputStream os;

	private DemoBuffer videoBuffer;
	
	private DemoBuffer soundDataBuffer;
	private DemoBuffer soundRegsBuffer;
	private DemoBuffer speechBuffer;

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
	
	static class DemoPacketBuffer extends DemoBuffer {
		private final BufferType type;

		public DemoPacketBuffer(OutputStream stream, DemoFormat.BufferType type, int size) {
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
		
		os.write(DemoFormat.DEMO_MAGIC_HEADER_V970);
		
		this.videoBuffer = new DemoPacketBuffer(os, DemoFormat.BufferType.VIDEO, DemoFormat.VIDEO_BUFFER_SIZE);
		this.soundDataBuffer = new DemoPacketBuffer(os, DemoFormat.BufferType.SOUND, DemoFormat.SOUND_BUFFER_SIZE);
		this.soundRegsBuffer = new DemoPacketBuffer(os, DemoFormat.BufferType.SOUND_REGS, DemoFormat.SOUND_REGS_BUFFER_SIZE);
		this.speechBuffer = new DemoPacketBuffer(os, DemoFormat.BufferType.SPEECH, DemoFormat.SPEECH_BUFFER_SIZE);
	}
	
	public synchronized void close() throws IOException {
		flushAll();
		
		if (os != null) {
			os.close();
			os = null;
		}
		soundRegsBuffer = null;
		soundDataBuffer = null;
		videoBuffer = null;
		speechBuffer = null;
	}

	/**
	 * @throws IOException
	 */
	protected void flushAll() throws IOException {
		if (videoBuffer != null)
			videoBuffer.flush();
		if (soundRegsBuffer != null)
			soundRegsBuffer.flush();
		if (soundDataBuffer != null)
			soundDataBuffer.flush();
		if (speechBuffer != null)
			speechBuffer.flush();
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoOutputStream#writeEvent(v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public synchronized void writeEvent(IDemoEvent event) throws NotifyException {
		try {
			if (event instanceof TimerTick) {
				flushAll();
				os.write(BufferType.TICK.getCode());
			}
			else if (event instanceof VideoWriteRegisterEvent) {
				if (!videoBuffer.isAvailable(2)) {
					videoBuffer.flush();
				}
				videoBuffer.pushWord(((VideoWriteRegisterEvent) event).getAddr());
			}
			else if (event instanceof VideoWriteDataEvent) {
				VideoWriteDataEvent we = (VideoWriteDataEvent) event;
				int len = we.getLength();
				int offs = 0;
				while (len > 0) {
					int toUse = Math.min(255, len);
					if (!videoBuffer.isAvailable(toUse + 3)) {
						videoBuffer.flush();
					}
					videoBuffer.pushWord(we.getAddress() + offs);
					videoBuffer.push((byte) toUse);
					videoBuffer.pushData(we.getData(), offs + we.getOffset(), toUse);
					
					len -= toUse;
					offs += toUse;
				}
				
			}
			else if (event instanceof SoundWriteDataEvent) {
				SoundWriteDataEvent ev = (SoundWriteDataEvent) event;
				byte[] data = ev.getData();
			 	soundDataBuffer.pushData(data, 0, ev.getLength());
			}
			else if (event instanceof SoundWriteRegisterEvent) {
				if (!soundRegsBuffer.isAvailable(3)) {
					soundRegsBuffer.flush();
				}
				SoundWriteRegisterEvent ev = (SoundWriteRegisterEvent) event;
				soundRegsBuffer.pushWord(ev.getReg());
				soundRegsBuffer.pushWord(ev.getVal());
			}
			else if (event instanceof SpeechWriteEvent) {
				if (!speechBuffer.isAvailable(2)) {
					speechBuffer.flush();
				}

				SpeechWriteEvent ev = (SpeechWriteEvent) event;
				speechBuffer.push((byte) ev.getEvent().getCode());
				if (ev.getEvent() == SpeechEvent.ADDING_BYTE) {
					speechBuffer.push((byte) ev.getAddedByte());
				}
			}
			else {
				throw new NotifyException(null, "unknown event type: " + event);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new NotifyException(null, "error writing demo: " + e.getMessage());
		}
	}
}
