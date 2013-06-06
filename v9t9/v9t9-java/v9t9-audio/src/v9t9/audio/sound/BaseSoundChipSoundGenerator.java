/*
  BaseSoundChipSoundGenerator.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.sound;

import javax.sound.sampled.AudioFormat;

import v9t9.common.client.ISoundHandler;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;
import v9t9.common.sound.TI99SoundSmoother;
import ejs.base.sound.ISoundOutput;

/**
 * @author ejs
 *
 */
public abstract class BaseSoundChipSoundGenerator extends BaseSoundGenerator {

	private static final AudioFormat format = new AudioFormat(48000, 16, 2, true, false);
	
	protected int active;
	protected final ISoundChip soundChip;

	/**
	 * @param machine 
	 * @param name 
	 * 
	 */
	public BaseSoundChipSoundGenerator(IMachine machine) {
		super(machine);
		this.soundChip = machine.getSound();
		soundChip.addWriteListener(this);
	}

	@Override
	public String getName() {
		return "sound";
	}

	@Override
	public SettingSchema getRecordingSettingSchema() {
		return ISoundHandler.settingRecordSoundOutputFile;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return format;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#configureSoundOutput(ejs.base.sound.ISoundOutput)
	 */
	@Override
	public void configureSoundOutput(ISoundOutput output) {
		output.addMutator(new TI99SoundSmoother());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#isSilenceRecorded()
	 */
	@Override
	public boolean isSilenceRecorded() {
		return true;
	}
}