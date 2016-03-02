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

import org.apache.log4j.Logger;

import ejs.base.internal.sound.SoundOutput;


/**
 * @author ejs
 *
 */
public class SoundFactory {
	private static final Logger logger = Logger.getLogger(SoundFactory.class);
	
	public static ISoundOutput createSoundOutput(SoundFormat format, int tickRate) {
		return new SoundOutput(format, tickRate);
	}
	
	public static ISoundEmitter createAudioListener() {
		if (!"true".equals(System.getProperty("v9t9.sound.java"))) {
			if (System.getProperty("os.name").equals("Linux")) {
				if (isPulseRunning()) {
					logger.info("Using Pulse for sound");
					return new PulseSoundListener(100);
				} else {
					logger.info("Using ALSA for sound");
					return new AlsaSoundListener(null);
				}
			} else if (File.separatorChar == '\\') {
				logger.info("Using Win32 multimedia for sound");
				return new Win32SoundListener();
			}
		}
		logger.info("Using JRE for sound");
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
