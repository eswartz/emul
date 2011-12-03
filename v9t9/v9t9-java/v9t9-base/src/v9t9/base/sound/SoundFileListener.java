package v9t9.base.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Mixing and output for sound
 * 
 * @author ejs
 * 
 */
public class SoundFileListener implements ISoundListener {

	private FileOutputStream soundFos;
	private String soundFile;
	private AudioFormat soundFormat;

	public SoundFileListener() {
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
	 * @see org.ejs.chiprocksynth.SoundListener#stopped()
	 */
	public void stopped() {
		if (soundFos != null && soundFormat != null) {
			closeSoundDumpFile(soundFos, soundFormat, soundFile);
			soundFos = null;
			soundFile = null;
		}

	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.SoundListener#started(javax.sound.sampled.AudioFormat)
	 */
	public void started(AudioFormat format) {
		this.soundFormat = format;
		

	}
	

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.SoundListener#played(org.ejs.chiprocksynth.AudioChunk)
	 */
	public void played(SoundChunk schunk) {
		if (soundFos != null) {
			try {
				AudioChunk chunk = schunk.asAudioChunk();
				soundFos.write(chunk.soundData, 0,
						chunk.soundData.length);
			} catch (IOException e) {
				try {
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
			soundFileRaw.delete();
			
			boolean converted = false;
			try {
				if (!soundFile.renameTo(soundFileRaw))
					return;

				AudioInputStream is = null;
				
				try {
					is = new AudioInputStream(
							new FileInputStream(soundFileRaw),
							format,
							soundFileRaw.length());
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				
				try {
					AudioSystem.write(is, fileType, soundFile);
					
					soundFileRaw.delete();
					converted = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			} finally {
				if (!converted) {
					System.err.println("Could not convert raw sound file (" + format + ") to " + fileType);
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


}
