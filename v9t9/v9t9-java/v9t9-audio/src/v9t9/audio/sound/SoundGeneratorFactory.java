/*
  SoundGeneratorFactory.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
	public static ISoundGenerator createCassetteGenerator(IMachine machine) {
		return new CassetteSoundGenerator(machine);
	}

}
