/*
  AudioGateVoice.java

  (c) 2009-2012 Edward Swartz

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
public class AudioGateVoice extends BaseVoice {

	private boolean gate;
	private IMachine machine;

	public AudioGateVoice(String id, String name,
			ListenerList<IRegisterWriteListener> listeners, IMachine machine) {
		super(id, name, listeners);
		this.machine = machine;
	}

	public void setGate(boolean gate) {
		this.gate = gate;
		ICpu cpu = machine.getCpu();
		if (cpu != null) {
			int cycles = cpu.getCurrentCycleCount();
			fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_AUDIO_GATE, gate ? cycles : -cycles-1);
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#loadState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		setGate(settings.getBoolean("Gate"));
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
		if (reg == baseReg + TMS9919Consts.REG_OFFS_AUDIO_GATE) {
			return gate ? 1 : 0;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#setRegister(int, int)
	 */
	@Override
	public void setRegister(int reg, int newValue) {
		if (reg == baseReg + TMS9919Consts.REG_OFFS_AUDIO_GATE) {
			setGate(newValue != 0);
		}
	}
}
