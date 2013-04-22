/*
  SoundTMS9919Generator.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.sound;





import java.util.HashMap;
import java.util.Map;

import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.machine.IRegisterAccess.RegisterInfo;
import v9t9.common.sound.TMS9919Consts;
import ejs.base.sound.ISoundVoice;

/**
 * Generator for the TMS9919 sound chip
 * @author ejs
 *
 */
public class SoundTMS9919Generator extends BaseSoundChipSoundGenerator {

	final public static int 
		VOICE_TONE_0 = 0, 
		VOICE_TONE_1 = 1, 
		VOICE_TONE_2 = 2, 
		VOICE_NOISE = 3,
		VOICE_AUDIO = 4,
		VOICE_CASSETTE = 5;

	protected final Map<Integer, SoundVoice> regIdToVoices = new HashMap<Integer, SoundVoice>();

	protected final Map<Integer, IRegisterAccess.IRegisterWriteListener> regIdToListener = new HashMap<Integer, IRegisterAccess.IRegisterWriteListener>();

	public SoundTMS9919Generator(IMachine machine, String name, int regBase) {
		super(machine);
		init(name, regBase);
	}
	
	@Override
	public synchronized void registerChanged(int reg, int value) {
		
		SoundVoice v = regIdToVoices.get(reg);
		if (v == null)
			return;
		IRegisterAccess.IRegisterWriteListener listener = regIdToListener.get(reg);
		if (listener == null)
			throw new IllegalStateException();
		
		listener.registerChanged(reg, value);
	}
	protected int init(String name, int regBase) {
		regBase = doInitVoices(name, regBase);
		
		for (ISoundVoice voice : getSoundVoices()) {
			if (voice instanceof ClockedSoundVoice) {
				((ClockedSoundVoice) voice).setReferenceClock(TMS9919Consts.CHIP_CLOCK);
			}
		}
		return regBase;
	}

	
	/**
	 * @param name
	 * @param regBase
	 * @return
	 */
	protected int doInitVoices(String name, int regBase) {
		for (int i = VOICE_TONE_0; i <= VOICE_TONE_2; i++) {
			ToneGeneratorVoice v = new ToneGeneratorVoice(name, i);
			soundVoicesList.add(v);
			regBase += setupToneVoice(regBase, i, v);
		}
		
		NoiseGeneratorVoice nv = new NoiseGeneratorVoice(
				name);
		soundVoicesList.add(nv);
		regBase += setupNoiseVoice(regBase, nv);
		
		AudioGateSoundVoice av = new AudioGateSoundVoice(name);
		soundVoicesList.add(av);
		regBase += setupAudioGateVoice(regBase, av);
		
//		CassetteSoundVoice cv = new CassetteSoundVoice(name);
//		soundVoicesList.add(cv);
//		regBase += setupCassetteVoice(regBase, cv);
		
		return regBase;
	}

	/**
	 * @param regBase
	 */
	protected int setupAudioGateVoice(int regBase, final AudioGateSoundVoice voice) {
		RegisterInfo info;
		info = soundChip.getRegisterInfo(regBase);
		assert info != null && info.id.endsWith("A:G");
		
		regIdToVoices.put(regBase + TMS9919Consts.REG_OFFS_AUDIO_GATE, voice);

		regIdToListener.put(regBase + TMS9919Consts.REG_OFFS_AUDIO_GATE,
			new IRegisterAccess.IRegisterWriteListener() {
				
				@Override
				public void registerChanged(int reg, int value) {
					voice.setState(value);
				}
			});
				
		return TMS9919Consts.REG_COUNT_AUDIO_GATE;
	}


	/**
	 * @param regBase
	 * @param voice 
	 * @return
	 */
	protected int setupNoiseVoice(int regBase, final NoiseGeneratorVoice voice) {
		RegisterInfo info;
		info = soundChip.getRegisterInfo(regBase);
		assert info != null && info.id.endsWith("N:Ctl");

		regIdToVoices.put(regBase + TMS9919Consts.REG_OFFS_ATTENUATION, voice);
		regIdToVoices.put(regBase + TMS9919Consts.REG_OFFS_NOISE_CONTROL, voice);

		regIdToListener.put(regBase + TMS9919Consts.REG_OFFS_NOISE_CONTROL,
				new IRegisterAccess.IRegisterWriteListener() {
					
					@Override
					public void registerChanged(int reg, int value) {
						voice.setNoiseControl(value);
						if (isNoiseTrackingTone2()) {
							updateNoisePeriod();
						}
					}
				}
		);
		
		regIdToListener.put(regBase + TMS9919Consts.REG_OFFS_ATTENUATION,
				new IRegisterAccess.IRegisterWriteListener() {
			
				@Override
				public void registerChanged(int reg, int value) {
					voice.setVolume(getVolume(value));
				}
			}
		);
			


		return TMS9919Consts.REG_COUNT_NOISE;
	}

	/**
	 * 
	 */
	protected void updateNoisePeriod() {
		ISoundVoice[] voices = getSoundVoices();
		((ClockedSoundVoice) voices[VOICE_NOISE]).setPeriod(
				((ClockedSoundVoice) voices[VOICE_TONE_2]).getPeriod());		
	}

	/**
	 * @param value
	 * @return
	 */
	protected int getVolume(int value) {
		return 0xff - ((value & 0xf) * 0xff / 0xf);
	}

	/**
	 * @param regBase
	 * @param i
	 * @return
	 */
	protected int setupToneVoice(final int regBase, int num, final ClockedSoundVoice voice) {
		RegisterInfo info;
		info = soundChip.getRegisterInfo(regBase);
		assert info != null && info.id.contains(num + ":Per");
		regIdToVoices.put(regBase + TMS9919Consts.REG_OFFS_FREQUENCY_PERIOD, 
				voice);
		regIdToVoices.put(regBase + TMS9919Consts.REG_OFFS_ATTENUATION, 
				voice);
		
		regIdToListener.put(regBase + TMS9919Consts.REG_OFFS_FREQUENCY_PERIOD,
			new IRegisterAccess.IRegisterWriteListener() {
				
				@Override
				public void registerChanged(int reg, int value) {
					voice.setPeriod(value);
					if (reg == regBase + 2 && isNoiseTrackingTone2()) {
						updateNoisePeriod();
					}
				}
			}
		);

		regIdToListener.put(regBase + TMS9919Consts.REG_OFFS_ATTENUATION,
			new IRegisterAccess.IRegisterWriteListener() {
				
				@Override
				public void registerChanged(int reg, int value) {
					voice.setVolume(getVolume(value));
				}
			}
		);

		return TMS9919Consts.REG_COUNT_TONE;
	}

	/**
	 * @return
	 */
	protected boolean isNoiseTrackingTone2() {
		ISoundVoice[] voices = getSoundVoices();
		return (((NoiseGeneratorVoice) voices[VOICE_NOISE]).getNoiseControl() & TMS9919Consts.NOISE_PERIOD_MASK)
			== TMS9919Consts.NOISE_PERIOD_VARIABLE;
	}
	
//	public void tick() {
//		if (soundHandler != null) {
//			int pos = machine.getCpu().getCurrentCycleCount();
//			int total = machine.getCpu().getCurrentTargetCycleCount();
//			long baseCount = machine.getCpu().getTotalCurrentCycleCount();
//
//			soundHandler.flushAudio(pos, total, baseCount);
//		}
//	}
}
