/**
 * 
 */
package v9t9.engine.sound;

import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.sound.TMS9919Consts;

import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public class AudioGateVoice extends BaseVoice {

	private boolean gate;

	public AudioGateVoice(String id, String name,
			ListenerList<IRegisterWriteListener> listeners) {
		super(id, name, listeners);
	}

	public void setGate(boolean gate) {
		if (this.gate != gate) {
			this.gate = gate;
			fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_AUDIO_GATE, 
					gate ? 1 : 0);
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#loadState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		gate = settings.getBoolean("Gate");
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#saveState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection settings) {
		settings.put("Gate", gate);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#initRegisters(java.util.Map, java.util.Map, java.util.Map, int)
	 */
	@Override
	public int doInitRegisters() {
		register(baseReg + TMS9919Consts.REG_OFFS_AUDIO_GATE,
				getId() + ":G",
				getName());
		return TMS9919Consts.REG_COUNT_AUDIO_GATE;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		if (reg == TMS9919Consts.REG_OFFS_AUDIO_GATE) {
			return gate ? 1 : 0;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#setRegister(int, int)
	 */
	@Override
	public void setRegister(int reg, int newValue) {
		if (reg == TMS9919Consts.REG_OFFS_AUDIO_GATE) {
			setGate(newValue != 0);
		}
	}
}
