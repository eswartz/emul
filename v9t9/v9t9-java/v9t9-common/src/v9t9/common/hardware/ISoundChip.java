/**
 * 
 */
package v9t9.common.hardware;


import ejs.base.properties.IPersistable;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.sound.ICassetteVoice;

/**
 * This interface is used to hook up a sound chip to a SoundHandler.
 * @author ejs
 *
 */
public interface ISoundChip extends IPersistable, IRegisterAccess {
	/** Get all the existing sound voices. */
	//ISoundVoice[] getSoundVoices();

	/** Write a byte to the sound chip(s) */
	void writeSound(int addr, byte val);

	//ISoundHandler getSoundHandler();
	//void setSoundHandler(ISoundHandler soundHandler);

	void setAudioGate(int addr, boolean b);
//	void setCassette(int addr, boolean b);
	ICassetteVoice getCassetteVoice();

	/**
	 * 
	 */
	void reset();

	//void tick();
}
