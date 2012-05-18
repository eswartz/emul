/**
 * 
 */
package v9t9.engine.sound;


import java.util.HashMap;
import java.util.Map;

import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.IMachine;
import v9t9.common.sound.IMultiSoundChip;
import v9t9.engine.demos.actors.SoundRegisterDemoActor;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public abstract class BaseMultiSound implements ISoundChip, IMultiSoundChip {
	protected ISoundChip[] chips;
	protected ListenerList<IRegisterWriteListener> listeners;
	protected final IMachine machine;
	protected int regCount;
	
	protected Map<Integer, ISoundChip> regIdToChip = new HashMap<Integer, ISoundChip>();
	protected Map<String, ISoundChip> regNameToChip = new HashMap<String, ISoundChip>();
	
	public BaseMultiSound(IMachine machine) {
		// 5 chips: the original TMS9919 on the console and 4 extra on the card
		
		this.machine = machine;
		listeners = new ListenerList<IRegisterWriteListener>();
		
		doGenerateChips();
		
		reset();
		
		// note: this case is special; MMIO-based actor is registered in memory model
		machine.getDemoManager().registerActor(new SoundRegisterDemoActor());
	}

	/**
	 * Create the multiple chips and update regIdToChip, regNameToChip, chips[] and regCount
	 */
	abstract protected void doGenerateChips();

	/**
	 * @param iSoundChip
	 * @param regBase
	 * @return
	 */
	protected int registerChip(final ISoundChip chip, int regBase) {
		int cnt = chip.getRegisterCount();
		for (int j = 0; j < cnt; j++) {
			regIdToChip.put(regBase + j, chip);
			regNameToChip.put(chip.getRegisterInfo(regBase + j).id, chip);
		}
		regBase += cnt;
		regCount += cnt;
		return regBase;
	}
	
	public void loadState(ISettingSection section) {
		if (section == null)
			return;
		int idx = 0;
		for (ISoundChip chip : chips) {
			chip.loadState(section.getSection("" + idx));
			idx++;
		}
	}
	
	public void saveState(ISettingSection section) {
		int idx = 0;
		for (ISoundChip chip : chips) {
			chip.saveState(section.addSection("" + idx));
			idx++;
		}
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
		return regCount;
	}


	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterNumber(java.lang.String)
	 */
	@Override
	public int getRegisterNumber(String id) {
		ISoundChip chip = regNameToChip.get(id);
		if (chip != null)
			return chip.getRegisterNumber(id);
		return Integer.MIN_VALUE;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterInfo(int)
	 */
	@Override
	public RegisterInfo getRegisterInfo(int reg) {
		ISoundChip chip = regIdToChip.get(reg);
		if (chip != null)
			return chip.getRegisterInfo(reg);
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		ISoundChip chip = regIdToChip.get(reg);
		if (chip != null)
			return chip.getRegister(reg);
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#setRegister(int, int)
	 */
	@Override
	public int setRegister(int reg, int newValue) {
		ISoundChip chip = regIdToChip.get(reg);
		if (chip != null)
			return chip.setRegister(reg, newValue);
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterTooltip(int)
	 */
	@Override
	public String getRegisterTooltip(int reg) {
		ISoundChip chip = regIdToChip.get(reg);
		if (chip != null)
			return chip.getRegisterTooltip(reg);
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#addWriteListener(v9t9.common.machine.IRegisterAccess.IRegisterWriteListener)
	 */
	@Override
	public void addWriteListener(IRegisterWriteListener listener) {
		for (ISoundChip chip : chips) {
			chip.addWriteListener(listener);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#removeWriteListener(v9t9.common.machine.IRegisterAccess.IRegisterWriteListener)
	 */
	@Override
	public void removeWriteListener(IRegisterWriteListener listener) {
		for (ISoundChip chip : chips) {
			chip.removeWriteListener(listener);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.IMultiSoundChip#getChipCount()
	 */
	@Override
	public int getChipCount() {
		return chips.length;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.IMultiSoundChip#getChip(int)
	 */
	@Override
	public ISoundChip getChip(int num) {
		return chips[num];
	}
	
}
