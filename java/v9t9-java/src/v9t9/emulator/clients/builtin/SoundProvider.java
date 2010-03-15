/**
 * 
 */
package v9t9.emulator.clients.builtin;

import org.ejs.coffee.core.properties.IPersistable;

import v9t9.emulator.hardware.sound.SoundVoice;
import v9t9.engine.SoundHandler;

/**
 * This interface is used to hook up a sound chip to a SoundHandler.
 * @author ejs
 *
 */
public interface SoundProvider extends IPersistable {
	/** Get all the existing sound voices. */
	SoundVoice[] getSoundVoices();

	/** Write a byte to the sound chip(s) */
	void writeSound(int addr, byte val);

	SoundHandler getSoundHandler();
	void setSoundHandler(SoundHandler soundHandler);

	void setAudioGate(int addr, boolean b);

	void tick();
}
