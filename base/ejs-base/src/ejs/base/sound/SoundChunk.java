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
public class SoundChunk extends BaseSoundView implements IEditableSoundView {
	private final float[] soundData;
	private int offset;
	/**
	 * @param soundData  data (should not be modified later!)
	 * @param format
	 */
	public SoundChunk(float[] soundData, AudioFormat format) {
		this(0, soundData, 0, soundData.length, format);
	}
	public SoundChunk(int silentSamples, AudioFormat format) {
		this(0, null, 0, silentSamples, format);
	}
	public SoundChunk(int frameOffset, int silentSamples, AudioFormat format) {
		this(frameOffset, null, 0, silentSamples, format);
	}
	public SoundChunk(int start, float[] soundData, int offset, int length, AudioFormat format) {
		super(start, length, format);
		this.soundData = soundData;
		this.offset = offset;
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
	 * @param i
	 * @return
	 */
	public SoundChunk getFromSample(int sampOffs) {
		//if (sampOffs < 0 || sampOffs > sampleCount)
		//	throw new IllegalArgumentException();
		if (sampOffs == 0)
			return this;
		return new SoundChunk(startFrame, soundData, offset + sampOffs, sampleCount - sampOffs, format);
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
		return new SoundChunk(startFrame, soundData, offset, sampCount, format);
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
		return new SoundChunk(startFrame + fromSample/numChannels, soundData, offset + fromSample, count, format);
	}

	/**
	 * Set a sample 
	 * @param sampleOffs
	 * @param v
	 */
	@Override
	public void set(int sampleOffs, float v) {
		soundData[offset + sampleOffs] = v;
	}
	
	/**
	 * Set a sample for all channels
	 * @param frameOffs
	 * @param v
	 */
	@Override
	public void setFrame(int frameOffs, float v) {
		int absOffs = frameOffs * numChannels;
		for (int i = 0; i < numChannels; i++)
			soundData[offset + absOffs + i] = v;
	}
	/**
	 * @return
	 */
	public float[] getData() {
		return soundData;
	}
	
}