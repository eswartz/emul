/**
 * 
 */
package ejs.base.sound;

/**
 * @author ejs
 *
 */
public interface IEditableSoundView extends ISoundView, IWriteArrayAccess {

	
	/**
	 * Set the frame to have all channels the same value
	 * @param frameOffs
	 * @param value
	 */
	void setFrame(int frameOffs, float value);
	
	IEditableSoundView getSoundView(int fromSample, int count);
	IEditableSoundView getSoundViewFrames(int fromFrame, int count);
	IEditableSoundView getSoundViewTime(float fromTime, float length);

}
