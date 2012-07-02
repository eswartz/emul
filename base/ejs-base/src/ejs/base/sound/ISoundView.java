/**
 * 
 */
package ejs.base.sound;

import javax.sound.sampled.AudioFormat;

/**
 * @author ejs
 *
 */
public interface ISoundView {

	AudioFormat getFormat();

	/**
	 * Get the sample (interleaving channels)
	 * @param absOffs
	 * @return sample, or 0 if out of range
	 */
	float at(int absOffs);

	/**
	 * Get the value of the frame (averaging channels)
	 * @param frameOffs
	 * @return sample, or 0 if out of range
	 */
	float atAvg(int frameOffs);

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
	 * Get the number of frames (samples divided by channels)
	 * @return
	 */
	int getFrameCount();

	/**
	 * Get the time in seconds for the view
	 * @return
	 */
	float getTime();

	ISoundView getSoundView(int fromSample, int count);
	ISoundView getSoundViewFrames(int fromFrame, int count);
	ISoundView getSoundViewTime(float fromTime, float length);
//	
//	ISoundView getFromTime(float f);
//	ISoundView getToTime(float f);
//	ISoundView getFromSample(int sampOffs);
//	ISoundView getToSample(int sampCount);
//	ISoundView getFromFrame(int frameOffs);
//	ISoundView getToFrame(int frameCount);
}