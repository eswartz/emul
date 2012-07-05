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

	public ArraySoundView(float[] data, int offset, int length, AudioFormat format) {
		super(offset, length, format);
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundView#at(int)
	 */
	@Override
	public float at(int absOffs) {
		return absOffs + offset < 0 || absOffs + offset >= data.length ? 0 : data[absOffs + offset];
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
	public ISoundView getSoundView(int fromSample, int count) {
		return new ArraySoundView(data, fromSample + offset, count, format);
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

}
