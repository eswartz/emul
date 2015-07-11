/*
  SoundFileListener.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;

import ejs.base.properties.IProperty;

/**
 * Mixing and output for sound
 * 
 * @author ejs
 * 
 */
public class SoundFileListener implements ISoundEmitter {

	private static final Logger logger = Logger.getLogger(SoundFileListener.class);
	
	private FileOutputStream soundFos;
	private String soundFile;
	private AudioFormat soundFormat;

	private IProperty pauseProperty;

	private byte[] silence;

	private boolean includeSilence;

	public SoundFileListener() {
		silence = new byte[8192];
	}
	
	/**
	 * @param includeSilence the includeSilence to set
	 */
	public void setIncludeSilence(boolean includeSilence) {
		this.includeSilence = includeSilence;
	}

	public void setPauseProperty(IProperty pauseProperty) {
		this.pauseProperty = pauseProperty;
	}
	
	public void setFileName(String filename) {
		stopped();
		if (filename != null) {
			try {
				soundFos = new FileOutputStream(filename);
				soundFile = filename;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (soundFormat != null)
			started(soundFormat);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.base.core.sound.ISoundListener#setVolume(double)
	 */
	public void setVolume(double loudness) {
		// IGNORED
	}
	/**
	 * @return the soundFile
	 */
	public String getFileName() {
		return soundFile;
	}
	
	/* (non-Javadoc)
	 * 
	 */
	public void stopped() {
		if (soundFos != null && soundFormat != null) {
			closeSoundDumpFile(soundFos, soundFormat, soundFile);
			soundFos = null;
			soundFile = null;
		}

	}

	/* (non-Javadoc)
	 * 
	 */
	public void started(AudioFormat format) {
		this.soundFormat = format;
		

		Arrays.fill(silence, (byte) (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED ? 0x0 : 0x80));
	}
	

	/* (non-Javadoc)
	 * 
	 */
	public void played(ISoundView schunk) {
		if (soundFos != null && (pauseProperty == null || !pauseProperty.getBoolean())) {
			try {
				AudioChunk chunk = new AudioChunk(schunk);
				if (chunk.soundData != null) {
					soundFos.write(chunk.soundData, 0,
							chunk.soundData.length);
				} else if (includeSilence) {
					for (int len = schunk.getSampleCount(); len > 0; ) {
						int toWrite = Math.min(len, silence.length);
						soundFos.write(silence, 0, toWrite);
						len -= toWrite;
					}
				}
			} catch (IOException e) {
				try {
					if (soundFos != null)
						soundFos.close();
					soundFos = null;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}				
	}

	/* (non-Javadoc)
	 * @see org.ejs.emul.core.sound.SoundListener#waitUntilSilent()
	 */
	public void waitUntilSilent() {
		if (soundFos != null)
			try {
				soundFos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	private void closeSoundDumpFile(FileOutputStream fos, AudioFormat format, String filename) {
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (filename != null) {
			// convert to the file type
			AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
			
			int idx = filename.lastIndexOf('.');
			if (idx >= 0) {
				String ext = filename.substring(idx + 1);
				Type[] types = AudioSystem.getAudioFileTypes();
				for (Type type : types) {
					if (type.getExtension().equalsIgnoreCase(ext)) {
						fileType = type;
						break;
					}
				}
			}
			
			File soundFile = new File(filename);
			File soundFileRaw = new File(filename + ".tmp");
			
			boolean converted = false;
			try {
				if (!soundFile.renameTo(soundFileRaw))
					return;

				AudioInputStream is = null;
				
				try {
					is = new AudioInputStream(
							new FileInputStream(soundFileRaw),
							format,
							soundFileRaw.length() / (format.getSampleSizeInBits() / 8));
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				
				try {
					AudioSystem.write(is, fileType, soundFile);
					is.close();
					soundFileRaw.delete();
					converted = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			} finally {
				if (!converted) {
					logger.error("Could not convert raw sound file (" + format + ") to " + fileType);
				}
			}
		}
	}

	/**
	 * Get the supported file types
	 * @return
	 */
	public static String[] getSoundFileExtensions() {

		Type[] types = AudioSystem.getAudioFileTypes();
		String[] extensions = new String[types.length];
		for (int idx = 0; idx < extensions.length; idx++) {
			extensions[idx] = types[idx].getExtension() + "|" + types[idx].toString();
		}
		return extensions;
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundEmitter#setBlockMode(boolean)
	 */
	@Override
	public void setBlockMode(boolean block) {
		// ignored
	}

	/**
	 * @return
	 */
	public boolean isStarted() {
		return soundFos != null;
	}

}
