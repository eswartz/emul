/**
 * 
 */
package org.ejs.coffee.core.sound;

import javax.sound.sampled.AudioFormat;

/**
 * Float data for sound.
 * @author ejs
 *
 */
public class SoundChunk {
	private final AudioFormat format;

	public SoundChunk(float[] soundToWrite, AudioFormat format) {
		this.soundData = soundToWrite;
		this.format = format;
	}

	public float[] soundData;
	private AudioChunk audioChunk;
	
	public synchronized AudioChunk asAudioChunk() {
		if (audioChunk == null) {
			audioChunk = new AudioChunk(this);
		}
		return audioChunk;
	}
	
	/**
	 * @return the format
	 */
	public AudioFormat getFormat() {
		return format;
	}

}