/**
 * 
 */
package v9t9.gui.client.swt.debugger;

import java.util.ArrayList;


import v9t9.base.utils.HexUtils;
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
