/**
 * 
 */
package v9t9.engine.sound;



import java.util.HashMap;
import java.util.Map;

import ejs.base.settings.ISettingSection;
import ejs.base.sound.ISoundVoice;
import ejs.base.utils.ListenerList;
import v9t9.common.client.ISoundHandler;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.settings.Settings;

import static v9t9.common.sound.TMS9919Consts.*;

/**
 * Controller for the TMS9919 sound chip
 * <p>
 * 3579545 Hz divided by 32 = 111860.78125 / 2 = 55930 Hz maximum frequency
 * @author ejs
 *
 */
public class SoundTMS9919 implements ISoundChip {

	/** Control + Audio Gate */
	private final static int REG_COUNT = 2;
	
	protected final static Map<Integer, String> regNames = new HashMap<Integer, String>();
	protected final static Map<Integer, String> regDescs = new HashMap<Integer, String>();
	protected final static Map<String, Integer> regIds = new HashMap<String, Integer>();
	
	protected static void register(Map<Integer, String> regNames, Map<Integer, String> regDescs, Map<String, Integer> regIds,
			int reg, String id, String desc) {
		regNames.put(reg, id);
		regDescs.put(reg, desc);
		regIds.put(id, reg);
	}
	
	static int registerRegisters(Map<Integer, String> regNames, Map<Integer, String> regDescs, Map<String, Integer> regIds,
			int base, boolean audioGate) {
		register(regNames, regDescs, regIds, base, "Ctrl", "Sound Control");
		
		if (audioGate) {
			register(regNames, regDescs, regIds, 
					base + 1, 
					"AudioGate",
					"Audio Gate");
			
			return 2;
		}
		
		return 1;
	}
	
	static {
		registerRegisters(regNames, regDescs, regIds, 0, true);
	}

	private byte[] registers;
	
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

	protected int	cvoice;

	protected ISoundHandler soundHandler;


	protected int active;

	protected final IMachine machine;

	private ListenerList<IRegisterWriteListener> listeners;

	protected final int regBase;

	protected SoundTMS9919(IMachine machine, String name, int regCount, int regBase) {
		this.machine = machine;
		this.regBase = regBase;
		init(name);
		listeners = new ListenerList<IRegisterWriteListener>();
		registers = new byte[regCount];
	}
	
	public SoundTMS9919(IMachine machine, String name) {
		this(machine, name, 2, 0);
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
		setRegister(regBase, val);
		
		
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
				v.operation[OPERATION_NOISE_CONTROL] = val;
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

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getGroupName()
	 */
	@Override
	public String getGroupName() {
		return "TMS 9919";
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getFirstRegister()
	 */
	@Override
	public int getFirstRegister() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return REG_COUNT;
	}


	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterNumber(java.lang.String)
	 */
	@Override
	public int getRegisterNumber(String id) {
		Integer val = regIds.get(id);
		return val != null ? val : Integer.MIN_VALUE;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterInfo(int)
	 */
	@Override
	public RegisterInfo getRegisterInfo(int reg) {
		RegisterInfo info = new RegisterInfo(regNames.get(reg), 
				IRegisterAccess.FLAG_ROLE_GENERAL,
				1,
				regDescs.get(reg));
				
		return info;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		return registers[reg];
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#setRegister(int, int)
	 */
	@Override
	public int setRegister(int reg, int newValue) {
		int oldValue = registers[reg - regBase];
		registers[reg - regBase] = (byte) newValue;
		fireRegisterChanged(reg, newValue);
		return oldValue;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterTooltip(int)
	 */
	@Override
	public String getRegisterTooltip(int reg) {
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#addWriteListener(v9t9.common.machine.IRegisterAccess.IRegisterWriteListener)
	 */
	@Override
	public void addWriteListener(IRegisterWriteListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#removeWriteListener(v9t9.common.machine.IRegisterAccess.IRegisterWriteListener)
	 */
	@Override
	public void removeWriteListener(IRegisterWriteListener listener) {
		listeners.remove(listener);
		
	}

	protected void fireRegisterChanged(int reg, int newValue) {
		if (!listeners.isEmpty()) {
			for (Object listenerObj : listeners.toArray()) {
				((IRegisterWriteListener) listenerObj).registerChanged(reg, newValue);
			}
		}
	}

	
}
