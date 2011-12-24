/**
 * 
 */
package v9t9.engine.sound;



import java.util.HashMap;
import java.util.Map;

import v9t9.common.client.ISoundHandler;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import ejs.base.settings.ISettingSection;
import ejs.base.sound.ISoundVoice;
import ejs.base.utils.ListenerList;

/**
 * Multiple packed TMS9919 chips.  This provides one traditional TMS9919
 * sound chip, followed by four TMS9919Bs, each with three ports (spaced by 2s).

 * @author ejs
 *
 */
public class MultiSoundTMS9919B implements ISoundChip {

	private static final int REG_COUNT = 1 + 3 * 4 + 1;
	
	private final static Map<Integer, String> regNames = new HashMap<Integer, String>();
	private final static Map<Integer, String> regDescs = new HashMap<Integer, String>();
	private final static Map<String, Integer> regIds = new HashMap<String, Integer>();
	
	static {
		// base TMS9919
		int offs = SoundTMS9919.registerRegisters(regNames, regDescs, regIds, 0, false);
		
		// now, four TMS9919Bs
		for (int chip = 0; chip < 4; chip++) {
			int regBase = offs + 3 * chip;
			
			SoundTMS9919.register(regNames, regDescs, regIds,
					regBase, 
					"Ctrl" + chip,
					"Sound Control #" + chip);
			
			SoundTMS9919.register(regNames, regDescs, regIds,
					regBase + 1, 
					"FX" + chip,
					"Effects #" + chip);
			
			SoundTMS9919.register(regNames, regDescs, regIds,
					regBase + 2, 
					"FXVal" + chip,
					"Effects Value #" + chip);
		}

		SoundTMS9919.register(regNames, regDescs, regIds,
				REG_COUNT - 1, 
				"AudioGate",
				"Audio Gate");
	}

	private ListenerList<IRegisterWriteListener> listeners;
	private byte[] registers = new byte[REG_COUNT];
	
	private SoundTMS9919[] chips;
	private ISoundVoice[] voices;
	private ISoundHandler soundHandler;
	private AudioGateVoice audioGateVoice;
	private final IMachine machine;
	
	public MultiSoundTMS9919B(IMachine machine) {
		// 5 chips: the original TMS9919 on the console and 4 extra on the card
		
		this.machine = machine;
		this.chips = new SoundTMS9919[5];
		chips[0] = new SoundTMS9919(machine, "Console Chip");
		
		int regBase = ((SoundTMS9919) chips[0]).getRegisterCount();
		//chips[0] = new SoundTMS9919B(machine, "Console Chip");
		for (int i = 0; i < 4; i++) {
			chips[i + 1] = new SoundTMS9919B(machine, "Chip #" + i, regBase);
			regBase += ((SoundTMS9919B) chips[i + 1]).getRegisterCount();
			
			/*byte balance = (byte) ((i & 1) == 0 ? -128 : 127); 
			for (SoundVoice voice : chips[i + 1].getSoundVoices()) {
				voice.setBalance(balance);
			}*/
		}
		voices = null;
		
		listeners = new ListenerList<IRegisterAccess.IRegisterWriteListener>();
	}

	public synchronized ISoundVoice[] getSoundVoices() {
		if (voices == null) {
			voices = new SoundVoice[chips.length * 4 + 1];
			int idx = 0;
			for (int i = 0; i < chips.length; i++) {
				SoundTMS9919 chip = chips[i];
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
			
			if (mask == 0) {
				// Main chip.
				chips[0].writeSound(addr, val);
				return;
			}
			
			int chip = 1 + ((mask - 2) / 6);
			int offs = mask + 6 - 2 - (chip * 6);
			
			if (chip < chips.length) {
				chips[chip].writeSound(offs, val);
			
				if (soundHandler != null)
					soundHandler.generateSound();
			}
		}
	}
	
	public void setAudioGate(int addr, boolean b) {
		if (audioGateVoice != null) {
			audioGateVoice.setState(machine, b);
			audioGateVoice.setupVoice();
			if (soundHandler != null)
				soundHandler.generateSound();
		}
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
		registers[reg] = (byte) newValue;
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
