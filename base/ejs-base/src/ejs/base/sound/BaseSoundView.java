/*
  BaseSoundView.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;


/**
 * @author ejs
 *
 */
public abstract class BaseSoundView  implements ISoundView {

	protected final SoundFormat format;

	protected final int numChannels;
	protected final int sampleCount;
	protected final int frameCount;
	protected final float time;
	protected AudioChunk audioChunk;
	protected float sampleToTime;

	protected int startFrame;

	/**
	 * 
	 */
	public BaseSoundView(int startFrame, int length, SoundFormat format) {
		this.startFrame = startFrame;
		this.sampleCount = length;
		this.format = format;
		this.numChannels = format.getChannels();
		this.frameCount = sampleCount / numChannels;
		this.time = frameCount / format.getFrameRate();
		
		this.sampleToTime = time > 0 ? sampleCount / time : 1;
	}

	@Override
	public String toString() {
		return "frames: " + frameCount + " (" + time +" sec), start = " + startFrame*numChannels + " for " + sampleCount + " in " + format;
	}

	
	@Override
	public SoundFormat getFormat() {
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
	public float atFrame(int frameOffs) {
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
	public int getFrameStart() {
		return startFrame;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#setSampleStart(int)
	 */
	@Override
	public void setFrameStart(int start) {
		this.startFrame = start;
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getStartTime()
	 */
	@Override
	public float getStartTime() {
		return startFrame * numChannels / sampleToTime;
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
		return new ArraySoundView(startFrame, newData, 0, newData.length, format);
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.IArrayAccess#size()
	 */
	@Override
	public int size() {
		return getSampleCount();
	}
	
}