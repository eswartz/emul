/**
 * 
 */
package ejs.base.sound;

import javax.sound.sampled.AudioFormat;

/**
 * Float data for sound.
 * @author ejs
 *
 */
public class SoundChunk {
	private final AudioFormat format;
	private final int offset;
	private final float[] soundData;
	private final int soundDataLength;
	
	private AudioChunk audioChunk;
	/**
	 * @param soundToWrite  data (should not be modified later!)
	 * @param format
	 */
	public SoundChunk(float[] soundToWrite, AudioFormat format) {
		if (soundToWrite == null)
			throw new NullPointerException();
		this.soundData = soundToWrite;
		this.offset = 0;
		this.soundDataLength = soundToWrite.length;
		this.format = format;
	}
	public SoundChunk(float[] soundToWrite, int offset, int length, AudioFormat format) {
		if (soundToWrite == null)
			throw new NullPointerException();
		this.soundData = soundToWrite;
		this.offset = offset;
		this.soundDataLength = length;
		this.format = format;
	}
	public SoundChunk(int silentSamples, AudioFormat format) {
		this.soundData = null;
		this.offset = 0;
		this.soundDataLength = silentSamples;
		this.format = format;
	}

	
	public synchronized AudioChunk asAudioChunk() {
		if (audioChunk == null) {
			audioChunk = new AudioChunk(this);
		}
		return audioChunk;
	}
	
	public AudioFormat getFormat() {
		return format;
	}
	
	public float at(int offs) {
		if (soundData == null || offs < offset || offs + offset >= soundDataLength)
			return 0f;
		return soundData[offset + offs];
	}
	/**
	 * @return
	 */
	public boolean isSilent() {
		return soundData == null;
	}
	/**
	 * @return
	 */
	public int getLength() {
		return soundDataLength;
	}
	/**
	 * @param i
	 * @param v
	 */
	public void set(int i, float v) {
		soundData[offset + i] = v;
	}

}