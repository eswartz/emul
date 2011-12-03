/**
 * 
 */
package v9t9.engine.hardware;


import v9t9.base.properties.IPersistable;
import v9t9.engine.client.ISoundHandler;
import v9t9.engine.sound.SoundVoice;

/**
 * This interface is used to hook up a sound chip to a SoundHandler.
 * @author ejs
 *
 */
public interface SoundChip extends IPersistable {
	/** Get all the existing sound voices. */
	SoundVoice[] getSoundVoices();

	/** Write a byte to the sound chip(s) */
	void writeSound(int addr, byte val);

	ISoundHandler getSoundHandler();
	void setSoundHandler(ISoundHandler soundHandler);

	void setAudioGate(int addr, boolean b);

	void tick();
}
