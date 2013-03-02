/*
  BaseSoundTest.java

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
package v9t9.audio.sound.test;

import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import v9t9.audio.sound.ClockedSoundVoice;
import v9t9.common.sound.TMS9919Consts;

import ejs.base.internal.sound.SoundOutput;
import ejs.base.sound.ISoundEmitter;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.ISoundVoice;
import ejs.base.sound.SoundFactory;

/**
 * @author ejs
 *
 */
public class BaseSoundTest {

	private static ISoundEmitter soundListener;
	private static ISoundOutput soundOutput;
	protected static AudioFormat format = new AudioFormat(55930, 16, 2, true, false);

	@BeforeClass
	public static void setup() {
		soundListener = SoundFactory.createAudioListener();
		soundListener.started(format);
		
		soundOutput = new SoundOutput(format, 100);
		soundOutput.addEmitter(soundListener);
	}

	@AfterClass
	public static void tearDown() {
		soundOutput.dispose();
		soundListener.waitUntilSilent();
		soundListener.stopped();
	}

	/**
	 * 
	 */
	public BaseSoundTest() {
		super();
	}

	/**
	 * @param samples
	 * @param toneVoice
	 */
	protected void generate(int samples, ISoundVoice... voices) {
		soundOutput.generate(voices, samples);
		soundOutput.flushAudio(voices, 0);
	}

	protected int toSamples(TimeUnit unit, int i) {
		long ms = TimeUnit.MILLISECONDS.convert(i, unit);
		return (int) (ms * format.getFrameRate() / 1000); 
	}


	/**
	 * @param toneVoice
	 */
	protected void setupVoice(ClockedSoundVoice toneVoice) {
		toneVoice.setFormat(format);
		toneVoice.setReferenceClock(TMS9919Consts.CHIP_CLOCK);
		toneVoice.setVolume(192);
	}
}