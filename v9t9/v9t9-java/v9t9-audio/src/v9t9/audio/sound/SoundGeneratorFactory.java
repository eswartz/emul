/*
  SoundGeneratorFactory.java

  (c) 2011 Edward Swartz

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
package v9t9.audio.sound;

import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;
import v9t9.common.sound.ISoundGenerator;
import v9t9.common.sound.MultiSoundTMS9919BConsts;
import v9t9.common.sound.MultiSoundTMS9919Consts;
import v9t9.common.sound.TMS9919BConsts;
import v9t9.common.sound.TMS9919Consts;

/**
 * @author ejs
 *
 */
public class SoundGeneratorFactory {

	/**
	 * @param sound
	 * @return
	 */
	public static ISoundGenerator createSoundGenerator(IMachine machine) {
        ISoundChip sound = machine.getSound();
		String group = sound.getGroupName();
		if (group.equals(TMS9919Consts.GROUP_NAME)) {
        	return new SoundTMS9919Generator(machine, group, 0);
        }
        if (group.equals(TMS9919BConsts.GROUP_NAME)) {
        	return new SoundTMS9919BGenerator(machine, group, 0);
        }
        if (group.equals(MultiSoundTMS9919Consts.GROUP_NAME)) {
        	return new MultiTMS9919Generator(machine);
        }
        if (group.equals(MultiSoundTMS9919BConsts.GROUP_NAME)) {
        	return new MultiTMS9919BGenerator(machine);
        }
		throw new UnsupportedOperationException();
	}

}
