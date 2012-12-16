/**
 * 
 */
package v9t9.engine.sound;

import v9t9.common.cpu.ICpu;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.sound.TMS9919Consts;

import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public class CassetteVoice extends BaseVoice {

	private boolean state;
	private IMachine machine;

	public CassetteVoice(String id, String name,
			ListenerList<IRegisterWriteListener> listeners, IMachine machine) {
		super(id, name, listeners);
		this.machine = machine;
	}

	public void setState(boolean state) {
		this.state = state;
		ICpu cpu = machine.getCpu();
		if (cpu != null) {
			int cycles = cpu.getCurrentCycleCount();
			fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_CASSETTE, state ? cycles : -cycles-1);
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#loadState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		setState(settings.getBoolean("State"));
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#saveState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection settings) {
		settings.put("State", state);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#initRegisters(java.util.Map, java.util.Map, java.util.Map, int)
	 */
	@Override
	public int doInitRegisters() {
		register(baseReg + TMS9919Consts.REG_OFFS_CASSETTE,
				getId() + ":C",
				getName());
		return TMS9919Consts.REG_COUNT_CASSETTE;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE) {
			return state ? 1 : 0;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#setRegister(int, int)
	 */
	@Override
	public void setRegister(int reg, int newValue) {
		if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE) {
			setState(newValue != 0);
		}
	}
}
