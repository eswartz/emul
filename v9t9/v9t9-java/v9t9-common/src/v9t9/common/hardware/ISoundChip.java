/*
  ISoundChip.java

  (c) 2009-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
	//ICassetteVoice getCassetteVoice();

	/**
	 * 
	 */
	void reset();

	//void tick();
}
