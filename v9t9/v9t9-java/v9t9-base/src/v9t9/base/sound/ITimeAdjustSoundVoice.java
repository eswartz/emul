/**
 * 
 */
package v9t9.base.sound;


/**
 * @author ejs
 *
 */
public interface ITimeAdjustSoundVoice extends ISoundVoice {

	/**
	 */
	void flushAudio(float[] soundGeneratorWorkBuffer, int startPos, int lastUpdatedPos, int scale);

}
