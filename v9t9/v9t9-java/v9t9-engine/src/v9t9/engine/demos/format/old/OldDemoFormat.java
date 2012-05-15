/**
 * 
 */
package v9t9.engine.demos.format.old;


/**
 * @author ejs
 *
 */
public class OldDemoFormat {
	public static final byte[] DEMO_MAGIC_HEADER_V910 = { 'V','9','1','0' };
	
	// these constants were hardcoded in v9t9 6.0 and
	// should be kept this way

	public static final int VIDEO_BUFFER_SIZE = 8192;
	public static final int SOUND_BUFFER_SIZE = 1024;
	public static final int SPEECH_BUFFER_SIZE = 512;

	/** wait for emulator tick */
	public static final int TICK = 0;

	/** video addresses and data */
	public static final int VIDEO = 1;

	/** sound bytes (all written to >8400) */
	public static final int SOUND = 2;

	/** speech commands (see ISpeechEvent) */
	public static final int SPEECH = 3;

}
