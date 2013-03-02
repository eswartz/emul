/*
  OldDemoFormat.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos.format.old;



/**
 * The demo file format is very rudimentary, consisting of
 *	a series of typed buffers.
 * <p/>
 *	Header:		'V910' bytes ('TI60' in TI Emulator v6.0)
 * <p/>
 *  The primary organization is by time.  Every 1/60 second,
 *  at most, all buffers are flushed.  These are separated
 *  by single bytes ({@link TICK}).
 *  <p/>
 *  After each tick may appear one to three buffers.
 *  Each buffer has a type (one byte) and is
 *  a buffer length (little-endian, 16 bits).  The bytes
 *  following this buffer are wholly devoted to the
 *  given type.
 *<p/>
 *	For video, the buffer type is {@link VIDEO}.
 *  The contents consists of a series of either register settings
 *  (16-bit little endian address with 0x8000 mask) or VDP memory
 *  writes (16-bit little-endian addresses followed an 8-bit length and 
 *  then constituent data bytes.  <b>NOTE:</b> the data bytes may be in the
 *  next buffer!
 *<p/>
 *	For sound, the buffer type is {@link SOUND}.  Its data
 *  consists entirely of data bytes, each written to the primary sound port.  
 *<p/>
 *	For speech, the buffer type is {@link SPEECH}.  Speech has a series 
 *  of bytes of type {@link ISpeechEvent#SPEECH_...}, where the {@link ISpeechEvent#SPEECH_ADDING_BYTE} event is 
 *  followed by a byte of data.
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
