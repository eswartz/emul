/**
 * 
 */
package ejs.base.sound;

/**
 * @author ejs
 *
 */
public interface IEditableSoundView extends ISoundView {

	/**
	 * Set the sample value 
	 * @param sampleOffs
	 * @param value
	 */
	void set(int sampleOffs, float value);
	/**
	 * Set the frame to have all channels the same value
	 * @param frameOffs
	 * @param value
	 */
	void setFrame(int frameOffs, float value);
}
