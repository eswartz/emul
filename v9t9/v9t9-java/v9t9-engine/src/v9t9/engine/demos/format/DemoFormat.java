/**
 * 
 */
package v9t9.engine.demos.format;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The demo format is a magic word plus a header, followed by frames.
 * <p/>
 * The header consists of identifying information for the version of V9t9 and
 * the machine demoed, a description and timestamp, a base timing rate, and a
 * table of contents mapping registered buffer identifiers to bytes. Only the
 * tick code (0) is reserved.
 * <p/>
 * The tick code is used to schedule the demo in time. It is followed by a count
 * for the number of ticks represented (which may be zero, but it is expected
 * to be 1 or more). 
 * <p/>
 * Between ticks, frames are represented by zero or more buffers.  Each buffer starts with
 * its code (from the TOC) and a length, and then an array of bytes of that 
 * length.  The interpretation of the contents is up to the registered buffer
 * handler.
 */
public class DemoFormat {

	public static final byte[] DEMO_MAGIC_HEADER_V9t9 = { 'V','9','7','0' };
	
	/** tick byte, followed by byte count, splits frames */
	public static final int TICK = 0;
	
	public static final String VIDEO_REGS = "videoRegs";
	public static final String VIDEO_DATA = "videoData";
	public static final String SOUND_REGS = "soundRegs";
	public static final String SPEECH_PHRASES = "speechPhrases";
	public static final String SOUND_DATA = "soundData";

	private static final String V9938_ACCEL = "v9938Accel";
	
	public static IDemoFormatterRegistry FORMATTER_REGISTRY = new DemoFormatterRegistry();
	
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
	

	static {
		FORMATTER_REGISTRY.registerDemoEventFormatter(new VideoDataEventFormatter(VIDEO_DATA));
		FORMATTER_REGISTRY.registerDemoEventFormatter(new VideoRegisterEventFormatter(VIDEO_REGS));
		FORMATTER_REGISTRY.registerDemoEventFormatter(new SoundDataEventFormatter(SOUND_DATA));
		FORMATTER_REGISTRY.registerDemoEventFormatter(new SoundRegisterEventFormatter(SOUND_REGS));
		FORMATTER_REGISTRY.registerDemoEventFormatter(new SpeechEventFormatter(SPEECH_PHRASES));
		FORMATTER_REGISTRY.registerDemoEventFormatter(new VdpV9938AccelCommandEventFormatter(V9938_ACCEL));
	}

}
