/**
 * 
 */
package org.ejs.coffee.core.sound;

import java.io.File;
import java.io.IOException;

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
			// TODO: fix crashes using pulse....
			if (isPulseRunning())
				return new PulseSoundListener(100);
			else
				return new AlsaSoundListener(null);
		else if (File.separatorChar == '\\')
			return new Win32SoundListener();
		return new JavaSoundListener(100);
	}

	/**
	 * @return
	 */
	private static boolean isPulseRunning() {
		ProcessBuilder pb = new ProcessBuilder("pulseaudio", "--check");
		Process process;
		try {
			process = pb.start();
			int ret = process.waitFor();
			return ret == 0;
		} catch (IOException e) {
			return false;
		} catch (InterruptedException e) {
			return false;
		}
	}
}
