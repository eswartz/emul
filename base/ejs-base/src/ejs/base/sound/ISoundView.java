/**
 * 
 */
package ejs.base.sound;

import javax.sound.sampled.AudioFormat;

/**
 * @author ejs
 *
 */
public interface ISoundView extends IArrayAccess {

	AudioFormat getFormat();

	/**
	 * Get the value of the frame (averaging channels)
	 * @param frameOffs
	 * @return sample, or 0 if out of range
	 */
	float atFrame(int frameOffs);

	/**
	 * Get the sample (interleaving channels)
	 * @param sampleOffs
	 * @return sample, or 0 if out of range
	 */
	float at(int sampleOffs, int channel);

	boolean isSilent();

	/**
	 * Get the channel count.
	 */
	int getChannelCount();
	/**
	 * Get the number of raw entries (one per channel)
	 * @return
	 */
	int getSampleCount();
	/** 
	 * Get offset in parent (if any) 
	 * @return
	 */
	int getSampleOffset();

	/**
	 * Get the number of frames (samples divided by channels)
	 * @return
	 */
	int getFrameCount();

	
	/** 
	 * Get start frame number (e.g. time) 
	 * @return
	 */
	int getFrameStart();
	
	void setFrameStart(int start);
	
	/**
	 * Get the time in seconds for the view
	 * @return
	 */
	float getElapsedTime();

	/**
	 * @return
	 */
	float getStartTime();

	/**
	 * @return
	 */
	float getEndTime();

	ISoundView getSoundView(int fromSample, int count);
	ISoundView getSoundViewFrames(int fromFrame, int count);
	ISoundView getSoundViewTime(float fromTime, float length);

	/**
	 * Make a deep copy of the view
	 * @return
	 */
	IEditableSoundView copy();



}