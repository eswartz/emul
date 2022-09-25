/*
  IRegisterAccess.java

  (c) 2011-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
		/** size in bytes */
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
	
	/** Read register without any cycle side effects */
	int getRegister(int reg);
	/** Write register without any cycle side effects, return old value */
	int setRegister(int reg, int newValue);
	
	String getRegisterTooltip(int reg);
	
	int getRegisterNumber(String id);
	
	interface IRegisterWriteListener {
		void registerChanged(int reg, int value);
	}
	
	void addWriteListener(IRegisterWriteListener listener);
	void removeWriteListener(IRegisterWriteListener listener);

}
