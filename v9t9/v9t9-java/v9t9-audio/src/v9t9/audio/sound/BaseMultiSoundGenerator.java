/*
  BaseMultiSoundGenerator.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.sound;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.sound.IMultiSoundChip;
import v9t9.common.sound.ISoundGenerator;
import ejs.base.sound.ISoundVoice;

/**
 * @author ejs
 *
 */
public abstract class BaseMultiSoundGenerator extends BaseSoundChipSoundGenerator {

	private TreeMap<Integer, IRegisterAccess.IRegisterWriteListener> regIdToGenerator = new TreeMap<Integer, IRegisterAccess.IRegisterWriteListener>();
	private ISoundGenerator[] generators;

	/**
	 * 
	 */
	public BaseMultiSoundGenerator(IMachine machine) {
		super(machine);
		
		IMultiSoundChip sound = (IMultiSoundChip) machine.getSound();
		
		// Prolly in the card, the console chip is ignored, because it
		// borks the intended use of stereo on the other chips.
		// So we ignore it.
		this.generators = new ISoundGenerator[sound.getChipCount()];
		int regBase = 0;
		for (int i = 0; i < sound.getChipCount(); i++) {
			ISoundChip soundChip = sound.getChip(i);
			//System.out.println(soundChip.getGroupName() + ": " + soundChip.getFirstRegister());
			generators[i] = createSoundGenerator(machine, soundChip, regBase);
			soundChip.addWriteListener(this);
			assert generators[i] instanceof IRegisterAccess.IRegisterWriteListener;
			regIdToGenerator.put(soundChip.getFirstRegister(), (IRegisterAccess.IRegisterWriteListener) generators[i]);
			regBase += soundChip.getRegisterCount();
		}
		
		for (int i = 0; i < sound.getChipCount(); i++) {
			ISoundVoice[] svs = generators[i].getSoundVoices();
			soundVoicesList.addAll(Arrays.asList(svs));
		}
	}

	abstract protected ISoundGenerator createSoundGenerator(IMachine machine,
			ISoundChip sound, int regBase);

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess.IRegisterWriteListener#registerChanged(int, int)
	 */
	@Override
	public synchronized void registerChanged(int reg, int value) {
		Map.Entry<Integer, IRegisterAccess.IRegisterWriteListener> generatorMap = regIdToGenerator.floorEntry(reg);
		if (generatorMap == null)
			throw new IllegalArgumentException();
		IRegisterAccess.IRegisterWriteListener generator = generatorMap.getValue();
		generator.registerChanged(reg, value);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#tick()
	 */
//	@Override
//	public void tick() {
//		for (ISoundGenerator generator : generators)
//			generator.tick();
//	}

}
