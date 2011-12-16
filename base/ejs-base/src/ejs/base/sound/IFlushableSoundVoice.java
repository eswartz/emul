/**
 * 
 */
package ejs.base.sound;


/**
 * This interface announces that a voice should be
 * synchronized every time a packet of audio is flushed.
 * It may, for example, rely on the specific amount of work
 * done during a packet rather than being generated in
 * real time.
 * @author ejs
 *
 */
public interface IFlushableSoundVoice extends ISoundVoice {

	/**
	 * Update the audio buffer.
	 * @param soundGeneratorWorkBuffer
	 * @param startPos
	 * @param lastUpdatedPos
	 * @param totalCount same parameter passed to {@link ISoundOutput#flushAudio(ISoundVoice[], int)}
	 */
	void flushAudio(float[] soundGeneratorWorkBuffer, int startPos, int lastUpdatedPos, int totalCount);

}
