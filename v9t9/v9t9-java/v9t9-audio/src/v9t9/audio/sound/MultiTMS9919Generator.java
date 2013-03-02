/*
  MultiTMS9919Generator.java

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

/**
 * @author ejs
 *
 */
public class MultiTMS9919Generator extends BaseMultiSoundGenerator {

	/**
	 * @param machine
	 */
	public MultiTMS9919Generator(IMachine machine) {
		super(machine);
	}

	protected ISoundGenerator createSoundGenerator(IMachine machine,
			ISoundChip sound, int regBase) {
		 return new SoundTMS9919Generator(machine, sound.getGroupName(), regBase);
	}

}
