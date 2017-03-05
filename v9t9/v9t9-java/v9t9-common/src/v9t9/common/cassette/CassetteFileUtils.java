/*
  CassetteFileUtils.java

  (c) 2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cassette;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author ejs
 *
 */
public class CassetteFileUtils {

	public static AudioFileFormat scanAudioFile(File audioFile) throws IOException, UnsupportedAudioFileException {
		InputStream fis = new BufferedInputStream(new FileInputStream(audioFile));
		try {
			return AudioSystem.getAudioFileFormat(fis);
		} finally {
			fis.close();
		}
	}
}
