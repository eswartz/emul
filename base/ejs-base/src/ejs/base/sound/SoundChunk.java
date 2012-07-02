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
public class SoundChunk implements ISoundView {
	private final AudioFormat format;
	private final int numChannels;
	private final int offset;
	private final float[] soundData;
	private final int sampleCount;
	private final int frameCount;
	private final float time;
	
	private AudioChunk audioChunk;
	private float sampleToTime;
	
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
//		if (soundData != null && (offset < 0 || offset + length > soundData.length))
//			throw new IllegalArgumentException();
		this.soundData = soundData;
		this.offset = offset;
		this.sampleCount = length;
		this.format = format;
		this.numChannels = format.getChannels();
		this.frameCount = sampleCount / numChannels;
		this.time = frameCount / format.getFrameRate();
		
		this.sampleToTime = sampleCount / time;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "frames: " + frameCount + " (" + time +" sec) at [" + offset + "+" + sampleCount + "] in " + format;
	}
	public synchronized AudioChunk asAudioChunk() {
		if (audioChunk == null) {
			audioChunk = new AudioChunk(this);
		}
		return audioChunk;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getFormat()
	 */
	@Override
	public AudioFormat getFormat() {
		return format;
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
	 * @see ejs.base.sound.ISoundView#atAvg(int)
	 */
	@Override
	public float atAvg(int frameOffs) {
		int absOffs = frameOffs * numChannels;
		if (soundData == null || absOffs < offset || absOffs + offset >= sampleCount)
			return 0f;
		float sum = 0f;
		for (int i = 0; i < numChannels; i++)
			sum += soundData[offset + absOffs + i];
		return sum / numChannels;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#at(int, int)
	 */
	@Override
	public float at(int sampleOffs, int channel) {
		return at(sampleOffs * numChannels + channel);
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#isSilent()
	 */
	@Override
	public boolean isSilent() {
		return soundData == null;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getSampleCount()
	 */
	@Override
	public int getSampleCount() {
		return sampleCount;
	}
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getFrameCount()
	 */
	@Override
	public int getFrameCount() {
		return frameCount;
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

	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getTime()
	 */
	@Override
	public float getTime() {
		return time;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getChannelCount()
	 */
	@Override
	public int getChannelCount() {
		return numChannels;
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
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getSoundViewFrames(int, int)
	 */
	@Override
	public ISoundView getSoundViewFrames(int fromFrame, int count) {
		return getSoundView(fromFrame * numChannels, count * numChannels);
	}
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getSoundViewTime(float, float)
	 */
	@Override
	public ISoundView getSoundViewTime(float fromTime, float length) {
		return getSoundView((int)(fromTime * sampleToTime), Math.round(length * sampleToTime));
	}

}