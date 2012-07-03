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
public class SoundChunk extends BaseSoundView{
	final float[] soundData;
	/**
	 * @param soundData  data (should not be modified later!)
	 * @param format
	 */
	public SoundChunk(float[] soundData, AudioFormat format) {
		this(soundData, 0, soundData.length, format);
	}
	public SoundChunk(int silentSamples, AudioFormat format) {
		this(null, 0, silentSamples, format);
	}
	public SoundChunk(float[] soundData, int offset, int length, AudioFormat format) {
		super(offset, length, format);
		this.soundData = soundData;
	}
	
	public synchronized AudioChunk asAudioChunk() {
		if (audioChunk == null) {
			audioChunk = new AudioChunk(this);
		}
		return audioChunk;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#at(int)
	 */
	@Override
	public float at(int absOffs) {
		if (soundData == null || absOffs + offset < 0 || absOffs + offset >= soundData.length)
			return 0f;
		return soundData[offset + absOffs];
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#isSilent()
	 */
	@Override
	public boolean isSilent() {
		return soundData == null;
	}
	
	/**
	 * Set a sample (interleaving channels)
	 * @param absOffs
	 * @param v
	 */
	public void set(int absOffs, float v) {
		soundData[offset + absOffs] = v;
	}
	/**
	 * @param i
	 * @return
	 */
	public SoundChunk getFromSample(int sampOffs) {
		//if (sampOffs < 0 || sampOffs > sampleCount)
		//	throw new IllegalArgumentException();
		if (sampOffs == 0)
			return this;
		return new SoundChunk(soundData, offset + sampOffs, sampleCount - sampOffs, format);
	}
	/**
	 * @param f
	 * @return
	 */
	public SoundChunk getToSample(int sampCount) {
		//if (sampCount < 0 || sampCount > sampleCount)
		//	throw new IllegalArgumentException();
		if (sampCount == sampleCount)
			return this;
		return new SoundChunk(soundData, offset, sampCount, format);
	}
	/**
	 * @param f
	 * @return
	 */
	public ISoundView getFromTime(float f) {
		return getFromSample((int) (f * sampleToTime));
	}
	/**
	 * @param f
	 * @return
	 */
	public ISoundView getToTime(float f) {
		return getToSample(Math.round(f * sampleToTime));
	}
	/**
	 * @param i
	 * @return
	 */
	public SoundChunk getFromFrame(int frameOffs) {
		return getFromSample(frameOffs * numChannels);
	}
	/**
	 * @param i
	 * @return
	 */
	public SoundChunk getToFrame(int frameCount) {
		return getToSample(frameCount * numChannels);
	}
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getSoundView(int, int)
	 */
	@Override
	public ISoundView getSoundView(int fromSample, int count) {
		if (fromSample == 0 && count == sampleCount)
			return this;
		return new SoundChunk(soundData, offset + fromSample, count, format);
	}

	/**
	 * Set a sample for the given channel
	 * @param sampleOffs
	 * @param v
	 */
	public void set(int sampleOffs, int channel, float v) {
		set(sampleOffs * numChannels + channel, v);
	}
	/**
	 * Set a sample for all channels
	 * @param sampleOffs
	 * @param v
	 */
	public void setAll(int sampleOffs, float v) {
		int absOffs = sampleOffs * numChannels;
		for (int i = 0; i < numChannels; i++)
			soundData[offset + absOffs + i] = v;
	}
	
}