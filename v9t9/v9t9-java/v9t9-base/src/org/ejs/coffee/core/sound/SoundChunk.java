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

	/**
	 * @param soundToWrite  data (should not be modified later!)
	 * @param format
	 */
	public SoundChunk(float[] soundToWrite, AudioFormat format) {
		if (soundToWrite == null)
			throw new NullPointerException();
		this.soundData = soundToWrite;
		this.soundDataLength = soundToWrite.length;
		this.format = format;
	}
	public SoundChunk(float[] soundToWrite, int length, AudioFormat format) {
		if (soundToWrite == null)
			throw new NullPointerException();
		this.soundData = soundToWrite;
		this.soundDataLength = length;
		this.format = format;
	}
	public SoundChunk(int silentSamples, AudioFormat format) {
		this.soundData = null;
		this.soundDataLength = silentSamples;
		this.format = format;
	}

	public float[] soundData;
	public int soundDataLength;
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