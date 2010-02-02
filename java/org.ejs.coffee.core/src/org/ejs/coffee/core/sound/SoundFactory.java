/**
 * 
 */
package org.ejs.coffee.core.sound;

import javax.sound.sampled.AudioFormat;

import org.ejs.coffee.internal.core.sound.SoundOutput;

/**
 * @author ejs
 *
 */
public class SoundFactory {
	public static ISoundOutput createSoundOutput(AudioFormat format, int tickRate) {
		return new SoundOutput(format, tickRate);
	}
	
	public static ISoundListener createAudioListener() {
		return new AlsaSoundListener(null);
	}
}
