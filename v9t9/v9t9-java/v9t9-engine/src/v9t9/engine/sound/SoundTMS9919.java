/**
 * 
 */
package v9t9.engine.sound;



import ejs.base.settings.ISettingSection;
import ejs.base.sound.ISoundVoice;
import v9t9.common.client.ISoundHandler;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;

/**
 * Controller for the TMS9919 sound chip
 * <p>
 * 3579545 Hz divided by 32 = 111860.78125 / 2 = 55930 Hz maximum frequency
 * @author ejs
 *
 */
public class SoundTMS9919 implements ISoundChip {

	/* These are used as an index into the operation[] field */
	final static int OPERATION_FREQUENCY_LO = 0,		/* low 4 bits [1vv0yyyy] */
		OPERATION_CONTROL = 0,			/* for noise  [11100xyy] */
		OPERATION_FREQUENCY_HI = 1,		/* hi 6 bits  [00yyyyyy] */
		OPERATION_ATTENUATION = 2		/* low 4 bits [1vv1yyyy] */
	;

	final public static int 
		VOICE_TONE_0 = 0, 
		VOICE_TONE_1 = 1, 
		VOICE_TONE_2 = 2, 
		VOICE_NOISE = 3,
		VOICE_AUDIO = 4;

	protected SoundVoice sound_voices[] = new SoundVoice[5];

	protected static int getOperationVoice(int op) {
		return ( ((op) & 0x60) >> 5);
	}

	/*
	 *	Masks for the OPERATION_CONTROL byte for VOICE_NOISE
	 */
	protected final static int NOISE_PERIODIC = 0,
		NOISE_WHITE = 0x4
	;
	
	protected final static int NOISE_PERIOD_FIXED_0 = 0,
		NOISE_PERIOD_FIXED_1 = 1,
		NOISE_PERIOD_FIXED_2 = 2,
		NOISE_PERIOD_VARIABLE = 3;
	;

	protected static final int  noise_period[] = 
	{
		16,
		32, 
		64, 
		0 						/* determined by VOICE_TONE_2 */
	};


	protected static int periodToHertz(int p) {
		return ((p) > 1 ? (111860 / (p)) : 55930);
	}
	protected static int period16ToHertz(int p) {
		return (int) ((p) > 1 ? ((long)111860 * 55930 / (p)) : 55930 * 55930);
	}

	protected int	cvoice;

	protected ISoundHandler soundHandler;


	protected int active;

	protected final IMachine machine;

	public SoundTMS9919(IMachine machine, String name) {
		this.machine = machine;
		init(name);
	}
	
	protected void init(String name) {
		for (int i = 0; i < 3; i++) {
			sound_voices[i] = new ToneGeneratorVoice(name, i);
		}
		sound_voices[VOICE_NOISE] = new NoiseGeneratorVoice(name, (ClockedSoundVoice) sound_voices[VOICE_TONE_2]);
		sound_voices[VOICE_AUDIO] = new AudioGateVoice(name);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.SoundHandler#writeSound(byte)
	 */
	public void writeSound(int addr, byte val) {
		ClockedSoundVoice v;
		/*  handle command byte */
		//System.out.println("sound byte: " + Utils.toHex2(val));
		int vn;
		if ((val & 0x80) != 0) {
			vn = getOperationVoice(val);
			cvoice = vn;
			v = (ClockedSoundVoice) sound_voices[vn];
			switch ((val & 0x70) >> 4) 
			{
			case 0:				/* T1 FRQ */
			case 2:				/* T2 FRQ */
			case 4:				/* T3 FRQ */
				v.operation[OPERATION_FREQUENCY_LO] = val;
				return;		// nothing changes til second byte
			case 1:				/* T1 ATT */
			case 3:				/* T2 ATT */
			case 5:				/* T3 ATT */
				v.operation[OPERATION_ATTENUATION] = val;
				break;
			case 6:				/* noise ctl */
				v.operation[OPERATION_CONTROL] = val;
				break;
			case 7:				/* noise vol */
				v.operation[OPERATION_ATTENUATION] = val;
				break;
			default:
				return;
			}
		}
		/*  second frequency byte */
		else {
			vn = cvoice;
			v = (ClockedSoundVoice) sound_voices[vn];
			v.operation[OPERATION_FREQUENCY_HI] = val;
		}
		
		updateVoice(v, val);
		
		if (soundHandler != null)
			soundHandler.generateSound();
	}

	/**
	 * @param v
	 * @param val
	 */
	protected void updateVoice(ClockedSoundVoice v, byte val) {
		v.setupVoice();
		updateNoise();
	}

	void
	updateNoise()
	{
		ClockedSoundVoice v = (ClockedSoundVoice) sound_voices[VOICE_NOISE];
		
		if ((cvoice == VOICE_TONE_2 && v.getOperationNoisePeriod() == NOISE_PERIOD_VARIABLE)
			 || cvoice == VOICE_NOISE)
		{
			updateNoiseVoice(v);
		}
	}


	protected void updateNoiseVoice(ClockedSoundVoice v) {
		v.setupVoice();
	}

	public ISoundHandler getSoundHandler() {
		return soundHandler;
	}


	public void setSoundHandler(ISoundHandler soundHandler) {
		this.soundHandler = soundHandler;
	}


	public ISoundVoice[] getSoundVoices() {
		return sound_voices;
	}
	
	public void saveState(ISettingSection settings) {
		Settings.get(machine, ISoundHandler.settingPlaySound).saveState(settings);
		for (int vn = 0; vn < sound_voices.length; vn++) {
			SoundVoice v = sound_voices[vn];
			v.saveState(settings.addSection(v.getName()));
			
		}
	}
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		Settings.get(machine, ISoundHandler.settingPlaySound).loadState(settings);
		for (int vn = 0; vn < sound_voices.length; vn++) {
			SoundVoice v = sound_voices[vn];
			String name = v.getName();
			v.loadState(settings.getSection(name));
			v.setupVoice();
		}
	}

	public void setAudioGate(int addr, boolean b) {
		if (soundHandler != null) {
			AudioGateVoice v = (AudioGateVoice) sound_voices[VOICE_AUDIO];
			v.setState(machine, b);
			v.setupVoice();
			soundHandler.generateSound();
		}

	}
	
	public void tick() {
		if (soundHandler != null) {
			soundHandler.flushAudio();
		}
	}
}
