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

/**
 * Multiple packed TMS9919 chips.  This provides a TMS9919 at any number
 * of addresses.  It is based on the description of the FORTI sound chips at
 * <http://nouspikel.group.shef.ac.uk//ti99/forti.htm>
 * @author ejs
 *
 */
public class MultiSoundTMS9919 implements ISoundChip {
	
	private static final int REG_COUNT = 4 + 1;
	
	private final static Map<Integer, String> regNames = new HashMap<Integer, String>();
	private final static Map<Integer, String> regDescs = new HashMap<Integer, String>();
	private final static Map<String, Integer> regIds = new HashMap<String, Integer>();
	
	protected static void register(int reg, String id, String desc) {
		regNames.put(reg, id);
		regDescs.put(reg, desc);
		regIds.put(id, reg);
	}
	
	static void registerRegisters() {
		for (int chip = 0; chip < 4; chip++) {
			int base = chip;
			
			register(base, "Ctrl" + chip, "Sound Control #" + chip);
		}


		register(4, 
					"AudioGate",
					"Audio Gate");
	}
	
	static {
		registerRegisters();
	}

	private ListenerList<IRegisterWriteListener> listeners;
	private SoundTMS9919[] chips;
	private ISoundVoice[] voices;
	private ISoundHandler soundHandler;
	private AudioGateVoice audioGateVoice;
	private final IMachine machine;
	
	private byte[] registers;
	
	public MultiSoundTMS9919(IMachine machine) {
		// 5 chips: the original TMS9919 on the console and 4 extra on the card
		
		this.machine = machine;
		registers = new byte[REG_COUNT];
		
		listeners = new ListenerList<IRegisterWriteListener>();
		
		// Prolly in the card, the console chip is ignored, because it
		// borks the intended use of stereo on the other chips.
		// So we ignore it.
		this.chips = new SoundTMS9919[5];
		int regBase = 0;
		for (int i = 0; i < 5; i++) {
			
			chips[i] = new SoundTMS9919(machine, "Chip #" + i, 2, regBase);
			regBase += 2;
			
			/*byte balance = (byte) (i == 0 ? 0 : ((i & 1) != 0 ? -128 : 128)); 
			for (SoundVoice voice : chips[i].getSoundVoices()) {
				voice.setBalance(balance);
			}*/
		}
		voices = null;
	}

	public synchronized ISoundVoice[] getSoundVoices() {
		if (voices == null) {
			voices = new SoundVoice[4 * 4 + 1];
			int idx = 0;
			for (int i = 0; i < 4; i++) {
				SoundTMS9919 chip = chips[i + 1];
				ISoundVoice[] chipVoices = chip.getSoundVoices();
				System.arraycopy(chipVoices, 0, voices, idx, 4);
				idx += 4;
			}
			voices[idx++] = audioGateVoice = new AudioGateVoice(null);
		}
		return voices;
	}
	
	public void writeSound(int addr, byte val) {
		if ((addr & 0xff) < 0xC0) {
			int mask = addr & 0x1e;
			
			// Ignore main chip.  It only provides audio gate -- if that.
			//chips[0].writeSound(addr, val);
			
			int chip = 1;
			// the low 4 bits of the word address tell (NAND-wise)
			// which chip to target
			for (int bit = 2; bit <= 0x10; bit += bit) {
				if ((mask & bit) == 0 && chip < chips.length) {
					chips[chip].writeSound(addr, val);
				}
				chip++;
			}
			soundHandler.generateSound();
		}
	}
	
	public void setAudioGate(int addr, boolean b) {
		audioGateVoice.setState(machine, b);
		audioGateVoice.setupVoice();
		if (soundHandler != null)
			soundHandler.generateSound();
	}

	public ISoundHandler getSoundHandler() {
		return soundHandler;
	}
	
	public void setSoundHandler(ISoundHandler soundHandler) {
		this.soundHandler = soundHandler;
		for (SoundTMS9919 chip : chips) {
			chip.setSoundHandler(null);
		}
	}

	public void loadState(ISettingSection section) {
		if (section == null)
			return;
		int idx = 0;
		for (SoundTMS9919 chip : chips) {
			chip.loadState(section.getSection("" + idx));
			idx++;
		}
	}
	
	public void saveState(ISettingSection section) {
		int idx = 0;
		for (SoundTMS9919 chip : chips) {
			chip.saveState(section.addSection("" + idx));
			idx++;
		}
	}
	
	public boolean isStereo() {
		return true;
	}
	
	public void tick() {
		for (SoundTMS9919 chip : chips) {
			chip.tick();
		}		
		if (soundHandler != null) {
			soundHandler.flushAudio();
		}
	}
	

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getGroupName()
	 */
	@Override
	public String getGroupName() {
		return "Multi TMS 9919";
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
		int oldValue = registers[reg];
		if (registers[reg] != newValue) {
			registers[reg] = (byte) newValue;
			fireRegisterChanged(reg, newValue);
		}
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
