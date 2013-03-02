/*
  BaseRegisterProvider.java

  (c) 2011 Edward Swartz

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
package v9t9.gui.client.swt.shells.debugger;

import java.util.ArrayList;

import ejs.base.utils.HexUtils;


import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.machine.IRegisterAccess.RegisterInfo;

/**
 * @author ejs
 *
 */
public abstract class BaseRegisterProvider implements IRegisterProvider {

	private static final IRegister[] NO_REGS = new IRegister[0];
	protected final IMachine machine;
	private ArrayList<IRegister> regList;
	protected final IRegisterAccess access;

	class Register implements IRegister {
		private final int reg;
		private RegisterInfo info;

		public Register(int reg) {
			this.reg = reg;
			this.info = access.getRegisterInfo(reg);
		}

		@Override
		public String toString() {
			return HexUtils.toHex4(getValue());
		}

		/* (non-Javadoc)
		 * @see v9t9.gui.client.swt.debugger.IRegister#getInfo()
		 */
		@Override
		public RegisterInfo getInfo() {
			return info;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegister#getValue()
		 */
		@Override
		public int getValue() {
			return access.getRegister(reg);
		}

		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegister#setValue(int)
		 */
		@Override
		public void setValue(int value) {
			access.setRegister(reg, value);
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegister#getTooltip()
		 */
		@Override
		public String getTooltip() {
			return access.getRegisterTooltip(reg);
		}
	}
	
	/**
	 * @param machine
	 */
	public BaseRegisterProvider(IMachine machine, IRegisterAccess access) {
		this.machine = machine;
		this.access = access;
		int cnt = access.getRegisterCount();
		regList = new ArrayList<IRegister>();
		for (int i = access.getFirstRegister(); cnt-- > 0; i++) {
			regList.add(new Register(i));
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegisterProvider#getLabel()
	 */
	@Override
	public String getLabel() {
		return access.getGroupName();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegisterProvider#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return regList.size();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegisterProvider#getRegisters()
	 */
	@Override
	public IRegister[] getRegisters(int start, int count) {
		return regList.subList(start, start + count).toArray(NO_REGS);
	}

}
