/*
  CassetteVoice.java

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
import v9t9.common.sound.ICassetteVoice;
import v9t9.common.sound.TMS9919Consts;

import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public class CassetteVoice extends BaseVoice implements ICassetteVoice {

	private int state;
	private int motor1;
	private int motor2;
	private IMachine machine;

	public CassetteVoice(String id, String name,
			ListenerList<IRegisterWriteListener> listeners, IMachine machine) {
		super(id, name, listeners);
		this.machine = machine;
	}

	public void setState(boolean state) {
		ICpu cpu = machine.getCpu();
		if (cpu != null) {
			int cycles = cpu.getCurrentCycleCount();
			this.state = state ? cycles : -cycles-1;
			fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_STATE, this.state);
		}
	}
	
	public void setMotor1(boolean motor) {
		ICpu cpu = machine.getCpu();
		if (cpu != null) {
			int cycles = cpu.getCurrentCycleCount();
			this.motor1 = motor ? cycles : -cycles-1;
			fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_1, this.motor1);
		}
	}
	public void setMotor2(boolean motor) {
		ICpu cpu = machine.getCpu();
		if (cpu != null) {
			int cycles = cpu.getCurrentCycleCount();
			this.motor2 = motor ? cycles : -cycles-1;
			fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_2, this.motor2);
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#loadState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		setState(settings.getBoolean("State"));
		setMotor1(settings.getBoolean("Motor1"));
		setMotor2(settings.getBoolean("Motor2"));
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#saveState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection settings) {
		settings.put("State", state >= 0);
		settings.put("Motor1", motor1 >= 0);
		settings.put("Motor2", motor2 >= 0);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseVoice#initRegisters(java.util.Map, java.util.Map, java.util.Map, int)
	 */
	@Override
	public int doInitRegisters() {
		register(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_STATE,
				getId() + ":C",
				getName());
		register(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_1,
				getId() + ":1",
				getName());
		register(baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_2,
				getId() + ":2",
				getName());
		return TMS9919Consts.REG_COUNT_CASSETTE;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE_STATE) {
			return state;
		}
		else if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_1) {
			return motor1;
		}
		else if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_2) {
			return motor2;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#setRegister(int, int)
	 */
	@Override
	public void setRegister(int reg, int newValue) {
		if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE_STATE) {
			state = newValue;
			fireRegisterChanged(reg, this.state);
		}
		else if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_1) {
			motor1 = newValue;
			fireRegisterChanged(reg, this.motor1);
		}
		else if (reg == baseReg + TMS9919Consts.REG_OFFS_CASSETTE_MOTOR_2) {
			motor2 = newValue;
			fireRegisterChanged(reg, this.motor2);
		}
	}
}
