/*
  NoiseVoice.java

  (c) 2011-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.sound;

import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.sound.TMS9919Consts;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
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
		register(baseReg + TMS9919Consts.REG_OFFS_NOISE_CONTROL,
				getId() + ":Ctl",
				getName());
		register(baseReg + TMS9919Consts.REG_OFFS_ATTENUATION,
				getId() + ":Att",
				getName());
		
		return TMS9919Consts.REG_COUNT_NOISE;
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
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#setRegister(int, int)
	 */
	@Override
	public void setRegister(int reg, int newValue) {
		if (reg == baseReg + TMS9919Consts.REG_OFFS_ATTENUATION) {
			setAttenuation(newValue);
		}
		else if (reg == baseReg + TMS9919Consts.REG_OFFS_NOISE_CONTROL) {
			setControl(newValue);
		}
		else {
			System.err.println("unknown register " + reg);
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
		setControl(settings.getInt("Control"));
	}


	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#saveState(ejs.base.settings.ISettingSection)
	 */
	public void saveState(ISettingSection settings) {
		super.saveState(settings);
		settings.put("Control", control);
	}

}
