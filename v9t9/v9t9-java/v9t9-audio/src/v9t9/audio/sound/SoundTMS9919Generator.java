/**
 * 
 */
package v9t9.audio.sound;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import v9t9.common.client.ISoundHandler;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.machine.IRegisterAccess.RegisterInfo;
import v9t9.common.sound.ISoundGenerator;
import v9t9.common.sound.TMS9919Consts;
import ejs.base.sound.ISoundVoice;

/**
 * Generator for the TMS9919 sound chip
 * @author ejs
 *
 */
public class SoundTMS9919Generator implements ISoundGenerator, IRegisterAccess.IRegisterWriteListener {
	protected final Map<Integer, SoundVoice> regIdToVoices = 
		new HashMap<Integer, SoundVoice>();
	protected final Map<Integer, ClockedSoundVoice> regIdToVoicePeriod = 
		new HashMap<Integer, ClockedSoundVoice>();
	protected final Map<Integer, ClockedSoundVoice> regIdToVoiceAtten = 
		new HashMap<Integer, ClockedSoundVoice>();
	protected final Map<Integer, NoiseGeneratorVoice> regIdToVoiceControl = 
		new HashMap<Integer, NoiseGeneratorVoice>();
	protected final Map<Integer, AudioGateSoundVoice> regIdToVoiceAudio = 
		new HashMap<Integer, AudioGateSoundVoice>();

	final public static int 
		VOICE_TONE_0 = 0, 
		VOICE_TONE_1 = 1, 
		VOICE_TONE_2 = 2, 
		VOICE_NOISE = 3,
		VOICE_AUDIO = 4;

	private SoundVoice[] soundVoices;

	protected ISoundHandler soundHandler;

	protected int active;

	protected final IMachine machine;

	protected final ISoundChip soundChip;

	protected final List<SoundVoice> soundVoicesList = new ArrayList<SoundVoice>();
	
	public SoundTMS9919Generator(IMachine machine, String name, int regBase) {
		this.machine = machine;
		this.soundChip = machine.getSound();
		soundChip.addWriteListener(this);
		init(name, regBase);
		
	}
	
	protected int init(String name, int regBase) {
		regBase = doInitVoices(name, regBase);
		
		regIdToVoices.putAll(regIdToVoicePeriod);
		regIdToVoices.putAll(regIdToVoiceAtten);
		regIdToVoices.putAll(regIdToVoiceControl);
		regIdToVoices.putAll(regIdToVoiceAudio);
		
		soundVoices = soundVoicesList.toArray(new SoundVoice[soundVoicesList.size()]);
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
		
		NoiseGeneratorVoice nv = new NoiseGeneratorVoice(name, 
				(ClockedSoundVoice) soundVoicesList.get(soundVoicesList.size() - 1));
		soundVoicesList.add(nv);
		regBase += setupNoiseVoice(regBase, nv);
		
		AudioGateSoundVoice av = new AudioGateSoundVoice(name);
		soundVoicesList.add(av);
		regBase += setupAudioGateVoice(regBase, av);
		
		return regBase;
	}

	/**
	 * @param regBase
	 */
	protected int setupAudioGateVoice(int regBase, AudioGateSoundVoice voice) {
		RegisterInfo info;
		info = soundChip.getRegisterInfo(regBase);
		assert info != null && info.id.endsWith("A:G");
		regIdToVoiceAudio.put(regBase + TMS9919Consts.REG_COUNT_AUDIO_GATE, voice);
		return TMS9919Consts.REG_COUNT_AUDIO_GATE;
	}

	/**
	 * @param regBase
	 * @param voice 
	 * @return
	 */
	protected int setupNoiseVoice(int regBase, NoiseGeneratorVoice voice) {
		RegisterInfo info;
		info = soundChip.getRegisterInfo(regBase);
		assert info != null && info.id.endsWith("N:P");
		regIdToVoicePeriod.put(regBase + TMS9919Consts.REG_OFFS_PERIOD, 
				voice);
		regIdToVoiceAtten.put(regBase + TMS9919Consts.REG_OFFS_ATTENUATION, 
				voice);
		regIdToVoiceControl.put(regBase + TMS9919Consts.REG_OFFS_NOISE_CONTROL, 
				voice);
		return TMS9919Consts.REG_COUNT_NOISE;
	}

	/**
	 * @param regBase
	 * @param i
	 * @return
	 */
	protected int setupToneVoice(int regBase, int num, ClockedSoundVoice voice) {
		RegisterInfo info;
		info = soundChip.getRegisterInfo(regBase);
		assert info != null && info.id.contains(num + ":P");
		regIdToVoicePeriod.put(regBase + TMS9919Consts.REG_OFFS_PERIOD, 
				voice);
		regIdToVoiceAtten.put(regBase + TMS9919Consts.REG_OFFS_ATTENUATION, 
				voice);
		return TMS9919Consts.REG_COUNT_TONE;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess.IRegisterWriteListener#registerChanged(int, int)
	 */
	@Override
	public void registerChanged(int reg, int value) {
		
		SoundVoice v = regIdToVoices.get(reg);
		if (v == null)
			throw new IllegalStateException();
		
		ClockedSoundVoice cv = regIdToVoicePeriod.get(reg);
		if (cv != null) {
			cv.setOperationPeriod(value);
		}
		else {
			cv = regIdToVoiceAtten.get(reg);
			if (cv != null) {
				cv.setOperationAttenuation(value);
			}
			else {
				NoiseGeneratorVoice nv = regIdToVoiceControl.get(reg);
				if (nv != null) {
					nv.setOperationNoiseControl(value);
				}
				else {
					AudioGateSoundVoice av = regIdToVoiceAudio.get(reg);
					if (av != null) {
						av.setState(machine, value != 0);
					} 
					else
						throw new IllegalStateException();
				}
			}
		}
		
		v.setupVoice();
		
		if (soundHandler != null)
			soundHandler.generateSound();
	}
	
	public ISoundHandler getSoundHandler() {
		return soundHandler;
	}


	public void setSoundHandler(ISoundHandler soundHandler) {
		this.soundHandler = soundHandler;
	}


	public ISoundVoice[] getSoundVoices() {
		return soundVoices;
	}
	
	public void tick() {
		if (soundHandler != null) {
			soundHandler.flushAudio();
		}
	}
}
