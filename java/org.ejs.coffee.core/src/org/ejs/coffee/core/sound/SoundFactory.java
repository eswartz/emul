/**
 * 
 */
package org.ejs.coffee.core.sound;

import java.io.File;

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
		if (System.getProperty("os.name").equals("Linux")) 
			return new AlsaSoundListener(null);
		else if (File.separatorChar == '\\')
			return new Win32SoundListener();
		return new JavaSoundListener(100);
	}
}
