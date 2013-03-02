/*
  TMS9919Consts.java

  (c) 2011-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.common.sound;

/**
 * @author ejs
 *
 */
public class TMS9919Consts {
	public static final String GROUP_NAME = "TMS 9919";

	/** 
	 * This is the offset in a tone or noise register bank for
	 * the frequency period of the voice (inversely related
	 * to the actual frequency by 3.579545 MHz / 32 / period).
	 */
	final public static int REG_OFFS_FREQUENCY_PERIOD = 0;
	/** 
	 * This is the offset in a tone or noise register bank for
	 * the attenuation of the voice, which affects volume in 
	 * that 15 = silence and 0 = loudest.
	 * 
	 */
	final public static int REG_OFFS_ATTENUATION = 1;
	/** 
	 * This is the offset in a noise register bank that
	 * controls its type (NOISE_FEEDBACK_MASK) and its frequency
	 * (NOISE_PERIOD_MASK).
	 * 
	 */
	final public static int REG_OFFS_NOISE_CONTROL = 0;
	
	/** 
	 * This is the offset in the audio gate register bank for
	 * the state of the audio gate (0=off, 1=on) 
	 */
	final public static int REG_OFFS_AUDIO_GATE = 0;
	
	/** 
	 * This is the offset in the cassette register bank for
	 * the current queued value of the cassette toggle (0=off, 1=on) 
	 */
	final public static int REG_OFFS_CASSETTE_STATE = 0;
	/** 
	 * This is the offset in the cassette register bank for
	 * the state of the cassette #1 motor (0=off, 1=on) 
	 */
	final public static int REG_OFFS_CASSETTE_MOTOR_1 = 1;
	/** 
	 * This is the offset in the cassette register bank for
	 * the state of the cassette #2 motor (0=off, 1=on) 
	 */
	final public static int REG_OFFS_CASSETTE_MOTOR_2 = 2;
	
	final public static int REG_COUNT_TONE = 2;
	final public static int REG_COUNT_NOISE = 2;
	final public static int REG_COUNT_AUDIO_GATE = 1;
	final public static int REG_COUNT_CASSETTE = 3;

	/**
	 *	Masks for the noise control register
	 */
	public final static int NOISE_FEEDBACK_PERIODIC = 0,
		NOISE_FEEDBACK_WHITE = 0x4,
		NOISE_FEEDBACK_MASK = 0x4
	;

	/**
	 *	Masks for the noise control register
	 **/
	public final static int NOISE_PERIOD_FIXED_0 = 0,
		NOISE_PERIOD_FIXED_1 = 1,
		NOISE_PERIOD_FIXED_2 = 2,
		NOISE_PERIOD_VARIABLE = 3,
		NOISE_PERIOD_MASK = 0x3;
	;
	

	public static final int NOISE_DIVISORS[] = 
	{
		16,
		32, 
		64, 
		0 						/* determined by VOICE_TONE_2 */
	};

	public static final int CHIP_CLOCK = 3579545;
}
