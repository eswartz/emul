/**
 * 
 */
package v9t9.server.demo;


/**
 *	The demo file format is very rudimentary.
 *
 *	Header:		'V910' bytes
 *
 *	Followed by a list of sections for various kinds of demo data.
 *	Each section starts with one byte ({@link Event}) and is
 *	followed by nothing (for the timer) or by a buffer length
 *	(little-endian, 16 bits) which is passed to the event handler.
 *
 *	Video has 16-bit little-endian addresses followed (if the
 *	address does not have the 0x8000 bit set, which is a register
 *	write) by a 16-bit little-endian length and data bytes.
 *
 *	Sound has a series of data bytes.
 *
 *	Speech has a series of {@link SpeechEvent} bytes, and the
 *	{@link SpeechEvent#ADDING_BYTE} event is followed by that byte.
 */
public class DemoFormat {

	public static final byte[] DEMO_MAGIC_HEADER_TI60 = { 'T','I','6','0' };
	public static final byte[] DEMO_MAGIC_HEADER_V910 = { 'V','9','1','0' };
	public static final int DEMO_MAGIC_HEADER_LENGTH = 4;

	public enum Event {
		/** wait for emulator tick */
		TICK(0),
		/** video addresses and data */
		VIDEO(1),
		/** sound bytes */
		SOUND(2),
		/** speech commands */
		SPEECH(3),
		/** CRU access [live client only] */
		CRU_WRITE(4),
		/** CRU access [live client only] */
		CRU_READ(5);
		
		private int code;

		private Event(int code) {
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
}
