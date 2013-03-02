/*
  IRegisterAccess.java

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
package v9t9.common.machine;

import v9t9.common.memory.IMemoryDomain;

/**
 * This interface encapsulates any aspect of the emulator
 * which uses registers.
 * @author ejs
 *
 */
public interface IRegisterAccess {
	String ID_CPU = "CPU";
	String ID_VIDEO = "VIDEO";
	String ID_SOUND = "SOUND";
	
	
	int FLAG_ROLE_GENERAL = 0;
	int FLAG_ROLE_PC = 1;
	int FLAG_ROLE_ST = 2;
	int FLAG_ROLE_SP = 3;
	int FLAG_ROLE_FP = 4;
	int FLAG_ROLE_RET = 5;
	int FLAG_ROLE_MASK = 0x7;
	int FLAG_VOLATILE = 1 << 3;
	int FLAG_SIDE_EFFECTS = 1 << 4;


	
	class RegisterInfo {
		public final String id;
		public final int flags;
		public final int size;
		public final String description;
		public IMemoryDomain domain;
		public int addr;
		
		public RegisterInfo(String id, int flags, int size,
				String description) {
			this.id = id;
			this.flags = flags;
			this.size = size;
			this.description = description;
		}
		
		
	}

	String getGroupName();
	int getFirstRegister();
	int getRegisterCount();
	RegisterInfo getRegisterInfo(int reg);
	int getRegister(int reg);
	int setRegister(int reg, int newValue);
	String getRegisterTooltip(int reg);
	
	int getRegisterNumber(String id);
	
	interface IRegisterWriteListener {
		void registerChanged(int reg, int value);
	}
	
	void addWriteListener(IRegisterWriteListener listener);
	void removeWriteListener(IRegisterWriteListener listener);

}
