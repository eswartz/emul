/**
 * 
 */
package v9t9.engine.sound;

import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.sound.TMS9919Consts;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public class EnhancedToneVoice extends BaseEnhancedClockedVoice {

	/**
	 * @param id
	 * @param name
	 * @param listeners
	 * @param numEffects
	 */
	public EnhancedToneVoice(String id, String name,
			ListenerList<IRegisterWriteListener> listeners, int numEffects) {
		super(id, name, listeners, numEffects);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.ToneVoice#doInitRegisters()
	 */
	@Override
	public int doInitRegisters() {
		register(baseReg + TMS9919Consts.REG_OFFS_PERIOD,
				getId() + ":P",
				getName());
		register(baseReg + TMS9919Consts.REG_OFFS_ATTENUATION,
				getId() + ":A",
				getName());

		int effectRegCount = doInitEffectRegisters(baseReg + TMS9919Consts.REG_COUNT_TONE);
		
		return TMS9919Consts.REG_COUNT_TONE + effectRegCount;
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

		if (reg >= baseEffectReg && reg < baseEffectReg + numEffects) {
			return getEffectValue(reg - baseEffectReg);
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
		else if (reg >= baseEffectReg && reg < baseEffectReg + numEffects) {
			setEffect(reg - baseEffectReg, (byte) newValue);
		}

	}
}
