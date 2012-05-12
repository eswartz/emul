/**
 * 
 */
package v9t9.server.demo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoInputStream;
import v9t9.common.events.NotifyException;
import v9t9.server.demo.DemoFormat.BufferType;
import v9t9.server.demo.events.SoundWriteDataEvent;
import v9t9.server.demo.events.SoundWriteRegisterEvent;
import v9t9.server.demo.events.SpeechWriteEvent;
import v9t9.server.demo.events.TimerTick;
import v9t9.server.demo.events.VideoWriteDataEvent;
import v9t9.server.demo.events.VideoWriteRegisterEvent;


public class DemoFormatReader implements IDemoInputStream {

	class DemoBuffer {
		private byte[] buffer;
		private int length;
		private int index;

		private int startPos;
		private final String label;
		private int myType;
		
		public DemoBuffer(String label, BufferType myType, int size) {
			this.label = label;
			this.buffer = new byte[size];
			this.index = 0;
			this.length = size;
			this.myType = myType.getCode();
		}
		public int read() throws IOException, NotifyException {
			if (index >= length) {
				refill();
			}
			return buffer[index++] & 0xff; 
		}
		public void refill() throws IOException, NotifyException {
			readHeader();
			int read = is.read(buffer, 0, length);
			if (read != length)
				throw new NotifyException(null, "corrupt demo in " + label + " buffer at " + getEffectivePos());
			isPos += read;
			length = read;
			index = 0;
		}
		protected void readHeader() throws IOException {
			startPos = isPos;
			//System.err.println(Integer.toHexString(isPos)+": " + label + " header");
			length = (is.read() & 0xff) | ((is.read() & 0xff) << 8);
			isPos += 2;
		}
		
		public int getEffectivePos() {
			return startPos + index;
		}

		public byte[] readRest() {
			byte[] data = new byte[length - index];
			System.arraycopy(buffer, index, data, 0, length - index);
			index = length;
			return data;
		}

		public int readWord() throws IOException, NotifyException {
			int word = (read() & 0xff) | ((read() & 0xff) << 8);
			return word;
		}
		/**
		 * @return
		 */
		public boolean isAvailable() {
			return index < length;
		}
		/**
		 * @param chunkLength
		 * @return
		 * @throws IOException 
		 * @throws NotifyException 
		 */
		public byte[] readData(int chunkLength) throws IOException, NotifyException {
			// all chunk should be buffered, or not
			if (index >= length) {
				int typ = is.read();
				if (typ != myType) {
					throw new NotifyException(null, "corrupt demo buffering for " + label + " at " + Integer.toHexString(getEffectivePos()));
				}
				refill();
			}
			byte[] data = new byte[chunkLength];
			System.arraycopy(buffer, index, data, 0, chunkLength);
			index += chunkLength;
			return data;
		}
	}
	
	
	private DemoBuffer videoBuffer, soundBuffer, speechBuffer;
	
	private final InputStream is;
	private int isPos;

	
	private Queue<IDemoEvent> queuedEvents;

	private int rate;

	public DemoFormatReader(InputStream is) throws IOException {
		this.is = is instanceof BufferedInputStream ? is : new BufferedInputStream(is);
		queuedEvents = new LinkedList<IDemoEvent>();
		
		// skip header
		byte[] header = new byte[DemoFormat.DEMO_MAGIC_HEADER_LENGTH];
		is.read(header);
		if (Arrays.equals(header, DemoFormat.DEMO_MAGIC_HEADER_TI60)
				|| Arrays.equals(header, DemoFormat.DEMO_MAGIC_HEADER_V910)) {
			rate = 60;
		} else {
			rate = 100;
		}
		isPos += DemoFormat.DEMO_MAGIC_HEADER_LENGTH;
		
		videoBuffer = new DemoBuffer("video", BufferType.VIDEO, DemoFormat.VIDEO_BUFFER_SIZE);
		soundBuffer = new DemoBuffer("sound", BufferType.SOUND, Math.max(DemoFormat.SOUND_REGS_BUFFER_SIZE, DemoFormat.SOUND_BUFFER_SIZE));
		speechBuffer = new DemoBuffer("speech", BufferType.SPEECH, DemoFormat.SPEECH_BUFFER_SIZE);
	}

	public void close() throws IOException {
		if (is != null)
			is.close();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoInputStream#readNext()
	 */
	@Override
	public IDemoEvent readNext() throws NotifyException {
		try {
			ensureEvents();
		} catch (NotifyException e) {
			throw e;
		} catch (Throwable e) {
			throw new NotifyException(null, "Error reading demo at " + Integer.toHexString(isPos), e);
		}
		
		return queuedEvents.poll();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoInputStream#getTimerRate()
	 */
	@Override
	public int getTimerRate() {
		return rate;
	}
	/**
	 * @throws IOException 
	 * 
	 */
	private void ensureEvents() throws IOException, NotifyException {
		if (!queuedEvents.isEmpty())
			return;
		
		int kind = is.read();  isPos++;
		if (kind < 0)
			return;
		
		if (kind == DemoFormat.BufferType.TICK.getCode()) {
			queuedEvents.add(new TimerTick());
		}
		else if (kind == DemoFormat.BufferType.VIDEO.getCode()) {
			queueVideoEvents();
		}
		else if (kind == DemoFormat.BufferType.SOUND.getCode()) {
			queueSoundEvents();
		}
		else if (kind == DemoFormat.BufferType.SOUND_REGS.getCode()) {
			queueSoundRegEvents();
		}
		else if (kind == DemoFormat.BufferType.SPEECH.getCode()) {
			queueSpeechEvents();
		}
		else {
			// urf
			throw new NotifyException(null, "demo corrupted at " + Integer.toHexString(isPos) + "; byte " + Integer.toHexString(kind));
		}
		
	}

	/**
	 * @throws IOException
	 * @throws NotifyException 
	 */
	protected void queueSoundEvents() throws IOException, NotifyException {
		// collection of sound events
		soundBuffer.refill();
		
		// blast all the data to the same address
		queuedEvents.add(new SoundWriteDataEvent(0x8400, soundBuffer.readRest()));
	}

	/**
	 * @throws IOException
	 * @throws NotifyException 
	 */
	protected void queueVideoEvents() throws IOException, NotifyException {
		// collection of video events
		videoBuffer.refill();
		
		// parse events
		while (videoBuffer.isAvailable()) {
			int addr = videoBuffer.readWord(); 
			if ((addr & 0x8000) != 0) {
				queuedEvents.add(new VideoWriteRegisterEvent(addr));
			} else {
				int chunkLength = videoBuffer.read() & 0xff; 
				byte[] chunk = videoBuffer.readData(chunkLength);
				queuedEvents.add(new VideoWriteDataEvent(addr & 0x3fff, chunk));
			}
		}
	}


	/**
	 * @throws NotifyException 
	 */
	protected void queueSpeechEvents() throws IOException, NotifyException {
		// collection of speech events
		speechBuffer.refill();
		
		// parse events
		while (speechBuffer.isAvailable()) {
			int byt = speechBuffer.read();  
			if (byt != DemoFormat.SpeechEvent.ADDING_BYTE.getCode()) {
				if (byt == 255) {
					// bug in TI Emulator 6.0
					continue;
				}
				try {
					queuedEvents.add(new SpeechWriteEvent(DemoFormat.SpeechEvent.fromCode(byt), 0));
				} catch (IllegalArgumentException e) {
					throw new NotifyException(null, "corrupt speech byte " + Integer.toHexString(byt) + " at " 
							+ Integer.toHexString(speechBuffer.getEffectivePos()));
				}
			} else {
				byt = speechBuffer.read() & 0xff;  
				queuedEvents.add(new SpeechWriteEvent(DemoFormat.SpeechEvent.ADDING_BYTE, byt));
			}
		}
	}
	
	/**
	 * @throws NotifyException 
	 */
	protected void queueSoundRegEvents() throws IOException, NotifyException {
		soundBuffer.refill();
		
		// parse events
		while (soundBuffer.isAvailable()) {
			int reg = (short) soundBuffer.readWord();
			int val = soundBuffer.readWord() & 0xffff;  
			queuedEvents.add(new SoundWriteRegisterEvent(reg, val));
		}
	}

}
