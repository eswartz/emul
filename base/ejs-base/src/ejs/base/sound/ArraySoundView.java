/**
 * 
 */
package ejs.base.sound;

import javax.sound.sampled.AudioFormat;

/**
 * @author ejs
 *
 */
public class ArraySoundView extends BaseSoundView implements IEditableSoundView {

	private float[] data;
	private int offset;

	public ArraySoundView(int start, float[] data, int offset, int length, AudioFormat format) {
		super(start, length, format);
		this.data = data;
		this.offset = offset;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.BaseSoundView#toString()
	 */
	@Override
	public String toString() {
		return "frames: " + frameCount + " (" + time +" sec), start = " + startFrame*numChannels + " [ " + offset + " + " + sampleCount + "] in " + format;
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#at(int)
	 */
	@Override
	public float at(int absOffs) {
		return absOffs + offset < 0 || absOffs >= sampleCount || absOffs + offset >= data.length ? 0 
				: data[absOffs + offset];
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#isSilent()
	 */
	@Override
	public boolean isSilent() {
		return false;
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#getSoundView(int, int)
	 */
	@Override
	public IEditableSoundView getSoundView(int fromSample, int count) {
		return new ArraySoundView(fromSample, data, fromSample + offset, count, format);
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.BaseSoundView#getSoundViewFrames(int, int)
	 */
	@Override
	public IEditableSoundView getSoundViewFrames(int fromFrame, int count) {
		return (IEditableSoundView) super.getSoundViewFrames(fromFrame, count);
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.BaseSoundView#getSoundViewTime(float, float)
	 */
	@Override
	public IEditableSoundView getSoundViewTime(float fromTime, float length) {
		return (IEditableSoundView) super.getSoundViewTime(fromTime, length);
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.IEditableSoundView#set(int, float)
	 */
	@Override
	public void set(int sampleOffs, float value) {
		data[sampleOffs + offset] = value;
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.IEditableSoundView#setFrame(int, float)
	 */
	@Override
	public void setFrame(int frameOffs, float value) {
		for (int i = 0; i < numChannels; i++)
			set(frameOffs * numChannels + i, value);
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.IArrayAccess#size()
	 */
	@Override
	public int size() {
		return data.length;
	}
}
