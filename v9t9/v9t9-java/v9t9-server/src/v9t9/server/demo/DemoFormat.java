/**
 * 
 */
package v9t9.server.demo;


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

	public static final byte[] DEMO_MAGIC_HEADER_TI60 = { 'T','I','6','0' };
	public static final byte[] DEMO_MAGIC_HEADER_V910 = { 'V','9','1','0' };
	public static final byte[] DEMO_MAGIC_HEADER_V9t9 = { 'V','9','7','0' };
	
	public static final int DEMO_MAGIC_HEADER_LENGTH = 4;

	public enum BufferType {
		/** wait for emulator tick */
		TICK(0),
		/** video addresses and data */
		VIDEO(1),
		/** sound bytes (old format) */
		SOUND(2),
		/** speech commands */
		SPEECH(3),
		/** CRU access [live client only] */
		CRU_WRITE(4),
		/** CRU access [live client only] */
		CRU_READ(5),
		/** sound bytes (new format) */
		SOUND_REGS(6);

		
		private int code;

		private BufferType(int code) {
			this.code = code;
		}
		
		public int getCode() {
			return code;
		}
	}

	/** Sub-category of event codes for speech */
	public enum SpeechEvent {
		/** new phrase */
		STARTING(0),	
		/** an LPC encoded byte (following) */
		ADDING_BYTE(1), 
		/** terminating speech phrase */
		TERMINATING(2),
		/** finished naturally */
		STOPPING(3),
		/** interrupt to perform work */
		INTERRUPT(4);

		private int code;

		private SpeechEvent(int code) {
			this.code = code;
		}
		
		public int getCode() {
			return code;
		}

		public static SpeechEvent fromCode(int byt) {
			switch (byt) {
			case 0:
				return STARTING;
			case 1:
				return ADDING_BYTE;
			case 2:
				return TERMINATING;
			case 3:
				return STOPPING;
			case 4:
				return INTERRUPT;
			default:
				throw new IllegalArgumentException(""+byt);
			}
		}
	}
	
	// these constants were hardcoded in v9t9 6.0 and
	// should probably be kept this way

	public static final int VIDEO_BUFFER_SIZE = 8192;
	public static final int SOUND_BUFFER_SIZE = 1024;
	public static final int SPEECH_BUFFER_SIZE = 512;
	
	public static final int SOUND_REGS_BUFFER_SIZE = 4096;
}
