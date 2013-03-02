/*
  ISoundChip.java

  (c) 2009-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
