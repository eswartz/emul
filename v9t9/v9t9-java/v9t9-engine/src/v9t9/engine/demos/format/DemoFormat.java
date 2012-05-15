/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import v9t9.engine.demos.events.SpeechEvent;



/**
 *	The demo file format is very rudimentary, consisting of
 *	a series of typed buffers.
 * <p/>
 *	Header:		'V910' bytes ('TI60' in TI Emulator v6.0)
 * <p/>
 *  The primary organization is by time.  Every 1/60 second,
 *  at most, all buffers are flushed.  These are separated
 *  by single bytes ({@link BufferType#TICK}).
 *  <p/>
 *  After each tick may appear one to three buffers.
 *  Each buffer has a type (one byte ({@link BufferType})) and is
 *  a buffer length (little-endian, 16 bits).  The bytes
 *  following this buffer are wholly devoted to the
 *  given type.
 *<p/>
 *	For video, the buffer type is {@link BufferType#VIDEO}.
 *  The contents consists of a series of either register settings
 *  (16-bit little endian address with 0x8000 mask) or VDP memory
 *  writes (16-bit little-endian addresses followed an 8-bit length and 
 *  then constituent data bytes.  <b>NOTE:</b> the data bytes may be in the
 *  next buffer!
 *<p/>
 *	For sound, the buffer type is {@link BufferType#SOUND}.  Its data
 *  consists entirely of data bytes, each written to the primary sound port.  
 *<p/>
 *	For speech, the buffer type is {@link BufferType#SPEECH}.  Speech has a series 
 *  of {@link SpeechEvent} bytes, where the {@link SpeechEvent#ADDING_BYTE} event is 
 *  followed by a byte of data.
 */
public class DemoFormat {

	public static final byte[] DEMO_MAGIC_HEADER_V9t9 = { 'V','9','7','0' };
	
	public static class DemoHeader {
		// private byte[4] magic;
		
		// ASCIIZ string
		private String machineModel;
		// ASCIIZ string
		private String description;
		private long timestamp = System.currentTimeMillis();
		private int timerRate = 100;
		
		// as ID byte followed by ASCIIZ string;
		// ID of 0 terminates list
		private Map<Integer, String> bufferIdentifiers = new HashMap<Integer, String>();
		
		public String getMachineModel() {
			return machineModel;
		}
		public void setMachineModel(String machineModel) {
			this.machineModel = machineModel;
		}
		
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public long getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
		public int getTimerRate() {
			return timerRate;
		}
		public void setTimerRate(int timerRate) {
			this.timerRate = timerRate;
		}
		
		
		public Map<Integer, String> getBufferIdentifierMap() {
			return bufferIdentifiers;
		}
		public void read(InputStream is) throws IOException {
			// expect machine identifier
			if (is.read() != 0x7f)
				throw new IOException("unexpected format: wanted 0x7f");
			
			setMachineModel(readString(is));

			// description
			setDescription(readString(is));
			
			// timestamp
			long time = 0;
			for (int i = 0; i < 8; i++) {
				int byt = is.read();
				if (byt < 0)
					throw new EOFException();
				time |= byt << (64 - i * 8 - 8);
			}
			setTimestamp(time);
			
			// timer rate (ticks per sec)
			int rate = is.read();
			if (rate < 0)
				throw new EOFException();
			
			setTimerRate(rate);
			
			// read TOC
			int id;
			while ((id = is.read()) != 0) {
				if (id < 0)
					throw new EOFException();
				String idString = readString(is);
				if (bufferIdentifiers.put(id, idString) != null) {
					throw new IOException("ID " + id + " is registered more than once");
				}
			}
		}
		
		
		private String readString(InputStream is) throws IOException {
			StringBuilder sb = new StringBuilder();
			int ch;
			while ((ch = is.read()) > 0) {
				sb.append((char) ch);
			}
			if (ch != 0)
				throw new EOFException();
			return sb.toString();
		}
		
		
		public void write(OutputStream os) throws IOException {
			// machine ID token
			os.write(0x7f);

			// machine identifier
			writeString(os, machineModel);
			
			// description
			writeString(os, description);
			
			// timestamp
			long time = getTimestamp();
			for (int i = 0; i < 8; i++) {
				os.write((int) (time >>> (64 - i * 8 - 8)));
			}
			
			// timer rate (ticks per sec)
			os.write(getTimerRate());
			
			// read TOC
			for (Map.Entry<Integer, String> entry : bufferIdentifiers.entrySet()) {
				if ((entry.getKey() & 0xff) != entry.getKey())
					throw new IOException("invalid buffer identifier: " + entry.getKey());
				os.write(entry.getKey());
				writeString(os, entry.getValue());
			}
			os.write(0);
			
		}
		

		private void writeString(OutputStream os, String str) throws IOException {
			if (str != null)
				os.write(str.getBytes());
			os.write(0);
		}
		
		/**
		 * Find code for identifier, or allocate one.
		 * @param identifier string
		 * @return code
		 * @throws IOException 
		 */
		public int findOrAllocateIdentifier(String id) throws IOException {
			int max = 1;
			for (Map.Entry<Integer, String> ent : bufferIdentifiers.entrySet()) {
				if (ent.getValue().equals(id)) {
					return ent.getKey();
				}
				max = Math.max(max, ent.getKey() + 1);
			}
			if (max >= 256)
				throw new IOException("no identifier space left for " + id);
			bufferIdentifiers.put(max, id);
			return max;
		}
		
	}

	
	/**
	 * Write a variable-length signed integer (UTF-8)
	 */
	public static void writeVariableLengthNumber(OutputStream os, int i) throws IOException {
		if (i < 0) {
			os.write((byte) -1);
			i = -i;
		}

		if (i >= 0 && i < 0x80) {
			// 7 bits
			os.write((byte) i);
		} else if (i >= 0x80 && i < 0x800) {
			// 11 bits
			os.write((byte) (0xc0 | (i >>> 6)));
			os.write((byte) (0x80 | (i & 0x3f)));
		} else if (i >= 0x800 && i < 0x10000) {
			// 16 bits
			os.write((byte) (0xe0 | (i >>> 12)));
			os.write((byte) (0x80 | ((i >>> 6) & 0x3f)));
			os.write((byte) (0x80 | (i & 0x3f)));
		} else if (i >= 0x10000 && i < 0x200000) {
			// 21 bits
			os.write((byte) (0xf0 | (i >>> 18)));
			os.write((byte) (0x80 | ((i >>> 12)) & 0x3f));
			os.write((byte) (0x80 | ((i >>> 6)) & 0x3f));
			os.write((byte) (0x80 | (i & 0x3f)));
		} else if (i >= 0x200000 && i < 0x4000000) {
			// 26 bits
			os.write((byte) (0xf8 | (i >>> 24)));
			os.write((byte) (0x80 | ((i >>> 18)) & 0x3f));
			os.write((byte) (0x80 | ((i >>> 12)) & 0x3f));
			os.write((byte) (0x80 | ((i >>> 6)) & 0x3f));
			os.write((byte) (0x80 | (i & 0x3f)));
		} else {
			// 31 bits
			os.write((byte) (0xfc | (i >>> 30)));
			os.write((byte) (0x80 | ((i >>> 24)) & 0x3f));
			os.write((byte) (0x80 | ((i >>> 18)) & 0x3f));
			os.write((byte) (0x80 | ((i >>> 12)) & 0x3f));
			os.write((byte) (0x80 | ((i >>> 6)) & 0x3f));
			os.write((byte) (0x80 | (i & 0x3f)));
		}
	}
	

	/**
	 * Read a variable-length signed integer (UTF-8)
	 *
	 * @return value
	 * @throws IOException 
	 */
	public static int readVariableLengthNumber(InputStream is) throws IOException {
		boolean neg = false;
		
		int byt = is.read();
		if (byt < 0)
			throw new EOFException();
		if (byt == 0xff) {
			neg = true;
			byt = is.read();
			if (byt < 0)
				throw new EOFException();
		}
		
		int val;
		if (byt < 0x80) {
			// 7 bits
			val = byt;
		}
		else if ((byt & 0xe0) == 0xc0) {
			// 11 bits
			val = ((byt & 0x1f) << 6) 
					| (ensureUtf8(is.read()) << 0);
		}
		else if ((byt & 0xf0) == 0xe0) {
			// 16 bits
			val = ((byt & 0xf) << 12) 
					| (ensureUtf8(is.read()) << 6)
					| (ensureUtf8(is.read()) << 0);
		}
		else if ((byt & 0xf8) == 0xf0) {
			// 21 bits
			val = ((byt & 0x7) << 18) 
					| (ensureUtf8(is.read()) << 12)
					| (ensureUtf8(is.read()) << 6)
					| (ensureUtf8(is.read()) << 0);
		}
		else if ((byt & 0xfc) == 0xf8) {
			// 26 bits
			val = ((byt & 0x3) << 24) 
					| (ensureUtf8(is.read()) << 18)
					| (ensureUtf8(is.read()) << 12)
					| (ensureUtf8(is.read()) << 6)
					| (ensureUtf8(is.read()) << 0);
		}
		else if ((byt & 0xfe) == 0xfc) {
			// 31 bits
			val = ((byt & 0x1) << 30) 
					| (ensureUtf8(is.read()) << 24)
					| (ensureUtf8(is.read()) << 18)
					| (ensureUtf8(is.read()) << 12)
					| (ensureUtf8(is.read()) << 6)
					| (ensureUtf8(is.read()) << 0);
		}
		else {
			throw new IOException("bad numeric encoding (bad leader) 0x" + Integer.toHexString(byt));
		}
		
		return neg ? -val : val;
	}


	private static int ensureUtf8(int read) throws IOException {
		if (read < 0)
			throw new EOFException();
		if ((read & 0xc0) != 0x80)
			throw new IOException("bad numeric encoding (bad trailer) 0x" + Integer.toHexString(read));
		return read & 0x3f;
	}
	
	/** tick byte, followed by byte count, splits frames */
	public static final int TICK = 0;
	
	public static final String VIDEO_REGS = "videoRegs";
	public static final String VIDEO_DATA = "videoData";
	public static final String SOUND_REGS = "soundRegs";
	public static final String SPEECH_PHRASES = "speechPhrases";
	/** should not be used */
	public static final String SOUND_DATA = "soundData";

}
