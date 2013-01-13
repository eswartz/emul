/**
 * 
 */
package v9t9.engine.sound;



import java.util.HashMap;
import java.util.Map;

import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;
import v9t9.common.client.ISoundHandler;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.settings.Settings;
import v9t9.common.sound.ICassetteVoice;
import v9t9.common.sound.IVoice;
import static v9t9.common.sound.TMS9919Consts.*;

/**
 * Controller for the TMS9919 sound chip
 * <p>
 * Receives commands:
 * <pre>
 * [1vv0yyyy] 	to set low frequency period for voice vv (0-2)
 * [11100xyy] 	to set type and period of noise voice; 
 * 				x=0 for periodic, 1 for white noise; 
 * 				y=0-2 for fixed period (16, 32, 64)
 * [00yyyyyy] 	to set high frequency period of voice (last set via 1vv0yyyy command)
 * [1vv1yyyy] 	to set attenuation for voice vv (0-3)
 * </pre>
 * <p>
 * Frequency in Hz is sound clock / (32 * period).
 * <p>
 * 3579545 Hz divided by 32 = 111860.78125 / 2 = 55930 Hz maximum frequency
 * @author ejs
 *
 */
public class SoundTMS9919 implements ISoundChip {


	protected final Map<Integer, String> regNames;
	protected final Map<Integer, String> regDescs;
	protected final Map<String, Integer> regIds;
	
	protected final Map<Integer, IVoice> regIdToVoice;
	
	protected static int getOperationVoice(int op) {
		return ( ((op) & 0x60) >> 5);
	}

	/** current voice (for freq hi command) */
	protected int cvoice;

	protected final IMachine machine;

	protected final ListenerList<IRegisterWriteListener> listeners;

	protected int[] periodLatches = new int[4];
	
	protected int regBase;
	protected IClockedVoice[] voices = new IClockedVoice[4];
	protected AudioGateVoice audioGateVoice;
	protected CassetteVoice cassetteVoice;

	public SoundTMS9919(IMachine machine, String id, String name, int regBase) {
		this.machine = machine;
		listeners = new ListenerList<IRegisterWriteListener>();
		
		this.regBase = regBase;
		
		regNames = new HashMap<Integer, String>();
		regDescs = new HashMap<Integer, String>();
		regIds = new HashMap<String, Integer>();
		
		regIdToVoice = new HashMap<Integer, IVoice>();
		
		initRegisters(id, name, regBase);
		
		reset();
	}
	
	public SoundTMS9919(IMachine machine) {
		this(machine, "", "Sound", 0);
	}
	
	/** Initialize registers and return new regBase */
	public int initRegisters(String id, String name, int regBase) {
		int count;
		for (int i = 0; i < 3; i++) {
			voices[i] = new ToneVoice(id + "V" + i, name + " Voice " + i, listeners);
			count = ((BaseVoice) voices[i]).initRegisters(regNames, regDescs, regIds, regBase);
			mapRegisters(regBase, count, voices[i]);
			regBase += count;
		}
		
		voices[3] = new NoiseVoice(id + "N", name + " Noise", listeners);
		count = ((BaseVoice) voices[3]).initRegisters(regNames, regDescs, regIds, regBase);
		mapRegisters(regBase, count, voices[3]);
		regBase += count;
		
		audioGateVoice = new AudioGateVoice(id + "A", "Audio Gate", listeners, machine);
		count = ((BaseVoice) audioGateVoice).initRegisters(regNames, regDescs, regIds, regBase);
		mapRegisters(regBase, count, audioGateVoice);
		regBase += count;
		
		cassetteVoice = new CassetteVoice(id + "C", "Cassette", listeners, machine);
		count = ((BaseVoice) cassetteVoice).initRegisters(regNames, regDescs, regIds, regBase);
		mapRegisters(regBase, count, cassetteVoice);
		regBase += count;
		
		return regBase;
	}

	/**
	 * @param regBase2
	 * @param count
	 * @param audioGateVoice2
	 */
	protected void mapRegisters(int regBase, int count, IVoice voice) {
		while (count-- > 0)
			regIdToVoice.put(regBase++, voice);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.SoundHandler#writeSound(byte)
	 */
	public void writeSound(int addr, byte val) {
		IClockedVoice v;
		/*  handle command byte */
		//System.out.println("sound byte: " + Utils.toHex2(val));
		int vn;
		if ((val & 0x80) != 0) {
			vn = getOperationVoice(val);
			cvoice = vn;
			v = (IClockedVoice) voices[vn];
			switch ((val & 0x70) >> 4) 
			{
			case 0:				/* T1 FRQ */
			case 2:				/* T2 FRQ */
			case 4:				/* T3 FRQ */
				periodLatches[cvoice] = (val & 0xf);
				return;		// nothing changes til second byte
			case 1:				/* T1 ATT */
			case 3:				/* T2 ATT */
			case 5:				/* T3 ATT */
			case 7:				/* noise vol */
				v.setAttenuation(val);
				break;
			case 6:				/* noise ctl */
				((INoiseVoice) voices[3]).setControl(val & 0xf);
				break;
			default:
				return;
			}
		}
		/*  second frequency byte */
		else {
			v = voices[cvoice];
			v.setPeriod(getFullPeriod(val));
		}
	}


	/**
	 * Get the register affected by the given MMIO data byte
	 * @param address
	 * @param byt
	 * @return register number
	 */
	public int convertMmioToRegister(int address,
			byte val) {
		int vn;
		if ((val & 0x80) != 0) {
			vn = getOperationVoice(val);
			switch ((val & 0x70) >> 4) 
			{
			case 0:				/* T1 FRQ */
			case 2:				/* T2 FRQ */
			case 4:				/* T3 FRQ */
				return REG_OFFS_FREQUENCY_PERIOD + vn * REG_COUNT_TONE;
			case 1:				/* T1 ATT */
			case 3:				/* T2 ATT */
			case 5:				/* T3 ATT */
			case 7:				/* noise vol */
				return REG_OFFS_ATTENUATION + vn * REG_COUNT_TONE;
			case 6:				/* noise ctl */
				return REG_OFFS_NOISE_CONTROL + vn * REG_COUNT_TONE;
			}
			return -1;
		}
		/*  second frequency byte */
		else {
			return -1;
		}
	}

	protected int getFullPeriod(byte val) {
		int period = periodLatches[cvoice] | ((val & 0x3f) << 4);
		return period;
	}

	public void saveState(ISettingSection settings) {
		Settings.get(machine, ISoundHandler.settingPlaySound).saveState(settings);
		for (int vn = 0; vn < voices.length; vn++) {
			IVoice v = voices[vn];
			v.saveState(settings.addSection(v.getName()));
		}
		if (audioGateVoice != null)
			audioGateVoice.saveState(settings.addSection(audioGateVoice.getName()));
		cassetteVoice.saveState(settings.addSection(cassetteVoice.getName()));

	}
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		Settings.get(machine, ISoundHandler.settingPlaySound).loadState(settings);
		for (int vn = 0; vn < voices.length; vn++) {
			IVoice v = voices[vn];
			String name = v.getName();
			v.loadState(settings.getSection(name));
		}
		if (audioGateVoice != null)
			audioGateVoice.loadState(settings.getSection(audioGateVoice.getName()));
		cassetteVoice.loadState(settings.getSection(cassetteVoice.getName()));
	}
	
	public void setAudioGate(int addr, boolean b) {
		audioGateVoice.setGate(b);
	}


	public void setCassette(int addr, boolean b) {
		cassetteVoice.setState(b);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.ISoundChip#getCassetteVoice()
	 */
	@Override
	public ICassetteVoice getCassetteVoice() {
		return cassetteVoice;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getGroupName()
	 */
	@Override
	public String getGroupName() {
		return GROUP_NAME;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getFirstRegister()
	 */
	@Override
	public int getFirstRegister() {
		return regBase;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return regIds.size();
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
		IVoice voice = regIdToVoice.get(reg);
		RegisterInfo info = new RegisterInfo(regNames.get(reg), 
				IRegisterAccess.FLAG_ROLE_GENERAL,
				voice instanceof CassetteVoice ? 4 : 1,
				regDescs.get(reg));
		return info;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		IVoice voice = regIdToVoice.get(reg);
		if (voice == null)
			return 0;
		return voice.getRegister(reg);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#setRegister(int, int)
	 */
	@Override
	public int setRegister(int reg, int newValue) {
		IVoice voice = regIdToVoice.get(reg);
		if (voice == null)
			return 0;
		int old = voice.getRegister(reg);
		voice.setRegister(reg, newValue);
		return old;
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
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.ISoundChip#reset()
	 */
	@Override
	public void reset() {
		for (IVoice v : voices) {
			v.setRegister(v.getBaseRegister() + REG_OFFS_ATTENUATION, 0xf);
		}		
//		audioGateVoice.setGate(false);
		cassetteVoice.setState(false);
		cassetteVoice.setMotor1(false);
		cassetteVoice.setMotor2(false);
	}
}
