/*
  CassetteSoundGenerator.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.sound;

import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;

import v9t9.common.hardware.CassetteConsts;
import v9t9.common.hardware.ICassetteChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.machine.IRegisterAccess.RegisterInfo;
import v9t9.common.settings.SettingSchema;
import v9t9.common.sound.ICassetteVoice;
import v9t9.common.sound.ISoundGenerator;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.ISoundVoice;

/**
 * @author ejs
 *
 */
public class CassetteSoundGenerator extends BaseSoundGenerator implements ISoundGenerator, IRegisterAccess.IRegisterWriteListener {
	
	private static final AudioFormat format = new AudioFormat(22000, 8, 1, false, false);
		// pulse freaks out with 22050 :(
	
	protected final Map<Integer, SoundVoice> regIdToVoices = 
			new HashMap<Integer, SoundVoice>();
	protected final Map<Integer, IRegisterAccess.IRegisterWriteListener> regIdToListener = 
			new HashMap<Integer, IRegisterAccess.IRegisterWriteListener>();

	private CassetteSoundVoice cassette1Voice;
	private ICassetteChip cassetteChip;

	/**
	 * @param baseReg 
	 * 
	 */
	public CassetteSoundGenerator(IMachine machine) {
		super(machine);
		this.cassetteChip = machine.getCassette();
		cassetteChip.addWriteListener(this);
		cassette1Voice = new CassetteSoundVoice("cs1");
		setupCassetteVoice(0, cassette1Voice);
		soundVoicesList.add(cassette1Voice);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#getName()
	 */
	@Override
	public String getName() {
		return "cassette";
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#getAudioFormat()
	 */
	@Override
	public AudioFormat getAudioFormat() {
		return format;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#getRecordingSettingSchema()
	 */
	@Override
	public SettingSchema getRecordingSettingSchema() {
		return ICassetteVoice.settingCassette1OutputFile;
	}
	/**
	 * @param regBase
	 */
	protected int setupCassetteVoice(int regBase, final CassetteSoundVoice voice) {
		RegisterInfo info;
		info = cassetteChip.getRegisterInfo(regBase);
		assert info != null && info.id.endsWith("C:C");
		
		regIdToVoices.put(regBase + CassetteConsts.REG_OFFS_CASSETTE_OUTPUT, voice);
		regIdToVoices.put(regBase + CassetteConsts.REG_OFFS_CASSETTE_MOTOR_1, voice);
		regIdToVoices.put(regBase + CassetteConsts.REG_OFFS_CASSETTE_MOTOR_2, voice);
		
		regIdToListener.put(regBase + CassetteConsts.REG_OFFS_CASSETTE_OUTPUT,
				new IRegisterAccess.IRegisterWriteListener() {
			
			@Override
			public void registerChanged(int reg, int value) {
				voice.setState(value);
			}
		});
		
		regIdToListener.put(regBase + CassetteConsts.REG_OFFS_CASSETTE_MOTOR_1,
				new IRegisterAccess.IRegisterWriteListener() {
			
			@Override
			public void registerChanged(int reg, int value) {
				voice.setMotor1(value, value >= 0);
			}
		});
		regIdToListener.put(regBase + CassetteConsts.REG_OFFS_CASSETTE_MOTOR_2,
				new IRegisterAccess.IRegisterWriteListener() {
			
			@Override
			public void registerChanged(int reg, int value) {
				voice.setMotor2(value, value >= 0);
			}
		});
		
		return CassetteConsts.REG_COUNT_CASSETTE;
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess.IRegisterWriteListener#registerChanged(int, int)
	 */
	@Override
	public void registerChanged(int reg, int value) {

		SoundVoice v = regIdToVoices.get(reg);
		if (v == null)
			return;
		IRegisterAccess.IRegisterWriteListener listener = regIdToListener.get(reg);
		if (listener == null)
			throw new IllegalStateException();
		
		listener.registerChanged(reg, value);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#getSoundVoices()
	 */
	@Override
	public ISoundVoice[] getSoundVoices() {
		return new ISoundVoice[] { cassette1Voice };
	}

	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#configureSoundOutput(ejs.base.sound.ISoundOutput)
	 */
	@Override
	public void configureSoundOutput(ISoundOutput output) {
		
	}
	/* (non-Javadoc)
	 * @see v9t9.audio.sound.BaseSoundGenerator#isSilenceRecorded()
	 */
	@Override
	public boolean isSilenceRecorded() {
		return false;
	}
}
