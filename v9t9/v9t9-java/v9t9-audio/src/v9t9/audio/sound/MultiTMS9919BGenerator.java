/*
  MultiTMS9919BGenerator.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.sound;

import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;
import v9t9.common.sound.ISoundGenerator;
import v9t9.common.sound.TMS9919Consts;

/**
 * @author ejs
 *
 */
public class MultiTMS9919BGenerator extends BaseMultiSoundGenerator {

	public MultiTMS9919BGenerator(IMachine machine) {
		super(machine);
	}

	protected ISoundGenerator createSoundGenerator(IMachine machine,
			ISoundChip sound, int regBase) {
		if (sound.getGroupName().equals(TMS9919Consts.GROUP_NAME))
			return new SoundTMS9919Generator(machine, sound.getGroupName(), regBase);
		else
			return new SoundTMS9919BGenerator(machine, sound.getGroupName(), regBase);
	}

}
