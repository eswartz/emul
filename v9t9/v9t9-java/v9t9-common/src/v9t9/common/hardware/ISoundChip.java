/**
 * 
 */
package v9t9.common.hardware;


import ejs.base.properties.IPersistable;
import ejs.base.sound.ISoundVoice;
import v9t9.common.client.ISoundHandler;

/**
 * This interface is used to hook up a sound chip to a {@link ISoundHandler}.
 * @author ejs
 *
 */
public interface ISoundChip extends IPersistable {
	/** Get all the existing sound voices. */
	ISoundVoice[] getSoundVoices();

	/** Write a byte to the sound chip(s) */
	void writeSound(int addr, byte val);

	ISoundHandler getSoundHandler();
	void setSoundHandler(ISoundHandler soundHandler);

	void setAudioGate(int addr, boolean b);

	void tick();
}
