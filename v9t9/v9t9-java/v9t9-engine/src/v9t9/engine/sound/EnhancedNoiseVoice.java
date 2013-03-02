/*
  EnhancedNoiseVoice.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.sound;

import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.sound.TMS9919Consts;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * 
 * This is the periodic/white "noise" voice.
 * 
 * The register set for noise only adds the periodic/white noise selector
 * while maintaining a seemingly independent frequency register.  This 
 * reduces complexity on the client side.
 * 
 * Internally, we still track the full noise control nybble (type + freq)
 * so we can send the frequency register change events when the control
 * register changes or the voice 2 frequency changes.
 * @author ejs
 *
 */
public class EnhancedNoiseVoice extends BaseEnhancedClockedVoice implements IEnhancedVoice, INoiseVoice {

	private int control;


	/**
	 * @param id
	 * @param name
	 * @param listeners
	 */
	public EnhancedNoiseVoice(String id, String name,
			ListenerList<IRegisterWriteListener> listeners,
			int numEffects) {
		super(id, name, listeners, numEffects);
	}


	/* (non-Javadoc)
	 * @see v9t9.engine.sound.ToneVoice#doInitRegisters()
	 */
	@Override
	public int doInitRegisters() {
		register(baseReg + TMS9919Consts.REG_OFFS_FREQUENCY_PERIOD,
				getId() + ":Ctl",
				getName());
		register(baseReg + TMS9919Consts.REG_OFFS_ATTENUATION,
				getId() + ":Att",
				getName());

		int effectRegCount = doInitEffectRegisters(baseReg + TMS9919Consts.REG_COUNT_NOISE);
		
		return TMS9919Consts.REG_COUNT_NOISE + effectRegCount;
	}


	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		if (reg == baseReg + TMS9919Consts.REG_OFFS_ATTENUATION) {
			return getAttenuation();
		}
		if (reg == baseReg + TMS9919Consts.REG_OFFS_NOISE_CONTROL) {
			return getControl();
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
		if (reg == baseReg + TMS9919Consts.REG_OFFS_NOISE_CONTROL) {
			setControl(newValue);
		}		
		else if (reg == baseReg + TMS9919Consts.REG_OFFS_ATTENUATION) {
			setAttenuation(newValue);
		}
		else if (reg >= baseEffectReg && reg < baseEffectReg + numEffects) {
			setEffect(reg - baseEffectReg, (byte) newValue);
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
	 * @see v9t9.engine.sound.BaseEnhancedClockedVoice#loadState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null)
			return;
		super.loadState(settings);
		setControl(settings.getInt("Control"));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseEnhancedClockedVoice#saveState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection settings) {
		super.saveState(settings);
		settings.put("Control", control);
	}
	

}
