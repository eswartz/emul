/*
  SoundFactory.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import ejs.base.internal.sound.SoundOutput;


/**
 * @author ejs
 *
 */
public class SoundFactory {
	public static ISoundOutput createSoundOutput(AudioFormat format, int tickRate) {
		return new SoundOutput(format, tickRate);
	}
	
	public static ISoundEmitter createAudioListener() {
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
