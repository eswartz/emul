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
	protected final int offset;
	protected final int sampleCount;
	protected final int frameCount;
	protected final float time;
	protected AudioChunk audioChunk;
	protected float sampleToTime;

	/**
	 * 
	 */
	public BaseSoundView(int offset, int length, AudioFormat format) {
		this.offset = offset;
		this.sampleCount = length;
		this.format = format;
		this.numChannels = format.getChannels();
		this.frameCount = sampleCount / numChannels;
		this.time = frameCount / format.getFrameRate();
		
		this.sampleToTime = sampleCount / time;
	}

	@Override
	public String toString() {
		return "frames: " + frameCount + " (" + time +" sec) at [" + offset + "+" + sampleCount + "] in " + format;
	}

	@Override
	public AudioFormat getFormat() {
		return format;
	}

	@Override
	public float atAvg(int frameOffs) {
		int absOffs = frameOffs * numChannels;
		float sum = 0f;
		for (int i = 0; i < numChannels; i++)
			sum += at(offset + absOffs + i);
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

	@Override
	public float getTime() {
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
		return new ArraySoundView(newData, 0, getSampleCount(), format);
	}

}