/**
 * 
 */
package ejs.base.sound;

import javax.sound.sampled.AudioFormat;

/**
 * @author ejs
 *
 */
public abstract class BaseSoundView  implements ISoundView {

	protected final AudioFormat format;

	protected final int numChannels;
	protected final int sampleCount;
	protected final int frameCount;
	protected final float time;
	protected AudioChunk audioChunk;
	protected float sampleToTime;

	protected int start;

	/**
	 * 
	 */
	public BaseSoundView(int start, int length, AudioFormat format) {
		this.start = start;
		this.sampleCount = length;
		this.format = format;
		this.numChannels = format.getChannels();
		this.frameCount = sampleCount / numChannels;
		this.time = frameCount / format.getFrameRate();
		
		this.sampleToTime = sampleCount / time;
	}

	@Override
	public String toString() {
		return "frames: " + frameCount + " (" + time +" sec), start = " + start + " for " + sampleCount + " in " + format;
	}

	
	@Override
	public AudioFormat getFormat() {
		return format;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getFrameOffset()
	 */
	@Override
	public int getSampleOffset() {
		return 0;
	}
	
	@Override
	public float atAvg(int frameOffs) {
		int absOffs = frameOffs * numChannels;
		float sum = 0f;
		for (int i = 0; i < numChannels; i++)
			sum += at(absOffs + i);
		return sum / numChannels;
	}

	@Override
	public float at(int sampleOffs, int channel) {
		return at(sampleOffs * numChannels + channel);
	}

	@Override
	public int getSampleCount() {
		return sampleCount;
	}

	@Override
	public int getFrameCount() {
		return frameCount;
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getSampleStart()
	 */
	@Override
	public int getSampleStart() {
		return start;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#setSampleStart(int)
	 */
	@Override
	public void setSampleStart(int start) {
		this.start = start;
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getStartTime()
	 */
	@Override
	public float getStartTime() {
		return start / sampleToTime;
	}
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getEndTime()
	 */
	@Override
	public float getEndTime() {
		return getStartTime() + getElapsedTime();
	}
	
	@Override
	public float getElapsedTime() {
		return time;
	}

	@Override
	public int getChannelCount() {
		return numChannels;
	}

	@Override
	public ISoundView getSoundViewFrames(int fromFrame, int count) {
		return getSoundView(fromFrame * numChannels, count * numChannels);
	}

	@Override
	public ISoundView getSoundViewTime(float fromTime, float length) {
		return getSoundView((int)(fromTime * sampleToTime), Math.round(length * sampleToTime));
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#copy()
	 */
	@Override
	public IEditableSoundView copy() {
		float[] newData = new float[getSampleCount()];
		for (int i = 0; i < newData.length; i++)
			newData[i] = at(i);
		return new ArraySoundView(start, newData, 0, getSampleCount(), format);
	}

}