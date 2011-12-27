/**
 * 
 */
package v9t9.engine.sound;

import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.sound.TMS9919Consts;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * This is the periodic/white "noise" voice.
 * @author ejs
 *
 */
public class NoiseVoice extends BaseClockedVoice implements INoiseVoice {

	private int control;

	public NoiseVoice(String id, String name, ListenerList<IRegisterWriteListener> listeners) {
		super(id, name, listeners);
	}


	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#initRegisters(java.util.Map, java.util.Map, java.util.Map, ejs.base.utils.ListenerList, int)
	 */
	@Override
	public int doInitRegisters() {
		register(baseReg + TMS9919Consts.REG_OFFS_PERIOD,
				getId() + ":P",
				getName());
		register(baseReg + TMS9919Consts.REG_OFFS_ATTENUATION,
				getId() + ":A",
				getName());
		register(baseReg + TMS9919Consts.REG_OFFS_NOISE_CONTROL,
				getId() + ":C",
				getName());
		
		return TMS9919Consts.REG_COUNT_NOISE;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		if (reg == baseReg + TMS9919Consts.REG_OFFS_PERIOD) {
			return getPeriod();
		}
		if (reg == baseReg + TMS9919Consts.REG_OFFS_ATTENUATION) {
			return getAttenuation();
		}
		if (reg == baseReg + TMS9919Consts.REG_OFFS_NOISE_CONTROL) {
			return getControl();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#setRegister(int, int)
	 */
	@Override
	public void setRegister(int reg, int newValue) {
		if (reg == baseReg + TMS9919Consts.REG_OFFS_PERIOD) {
			setPeriod(newValue);
		}
		else if (reg == baseReg + TMS9919Consts.REG_OFFS_ATTENUATION) {
			setAttenuation(newValue);
		}
		else if (reg == baseReg + TMS9919Consts.REG_OFFS_NOISE_CONTROL) {
			setControl(newValue);
		}
	}

	


	public int getControl() {
		return control;
	}


	public void setControl(int control) {
		this.control = control;
		fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_NOISE_CONTROL, control);
	}


	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#loadState(ejs.base.settings.ISettingSection)
	 */
	public void loadState(ISettingSection settings) {
		if (settings == null)
			return;
		super.loadState(settings);
		control = settings.getInt("Control");
	}


	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#saveState(ejs.base.settings.ISettingSection)
	 */
	public void saveState(ISettingSection settings) {
		super.saveState(settings);
		settings.put("Control", control);
	}

}