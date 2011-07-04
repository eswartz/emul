/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;

import java.util.ArrayList;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.cpu.Cpu9900;

/**
 * @author ejs
 *
 */
public class CpuRegisterProvider implements IRegisterProvider {

	private static final IRegister[] NO_REGS = new IRegister[0];
	private final Machine machine;
	private ArrayList<IRegister> regList;

	class CpuRegister implements IRegister {
		private final int reg;

		public CpuRegister(int reg) {
			this.reg = reg;
		}

		@Override
		public String toString() {
			return HexUtils.toHex4(getValue());
		}

		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegister#getName()
		 */
		@Override
		public String getName() {
			return machine.getCpu().getRegisterName(reg);
		}

		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegister#getValue()
		 */
		@Override
		public int getValue() {
			return machine.getCpu().getRegister(reg);
		}

		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegister#setValue(int)
		 */
		@Override
		public void setValue(int value) {
			machine.getCpu().setRegister(reg, value);
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegister#getTooltip()
		 */
		@Override
		public String getTooltip() {
			switch (reg) {
			case Cpu9900.REG_ST:
				return "Status register: " + machine.getCpu().getStatus().toString();
			case Cpu9900.REG_WP:
				return "Workspace pointer";
			case Cpu9900.REG_PC:
				return "Program counter";
			case 11:
				return "BL return address";
			case 13:
				return "BLWP saved WP";
			case 14:
				return "BLWP saved PC";
			case 15:
				return "BLWP saved ST";
			}
			return null;
		}
	}
	
	/**
	 * @param machine
	 */
	public CpuRegisterProvider(Machine machine) {
		this.machine = machine;
		int cnt = machine.getCpu().getRegisterCount();
		regList = new ArrayList<IRegister>();
		for (int  i = 0; i < cnt; i++) {
			regList.add(new CpuRegister(i));
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegisterProvider#getLabel()
	 */
	@Override
	public String getLabel() {
		return "CPU Registers";
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
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegisterProvider#getNumDigits()
	 */
	@Override
	public int getNumDigits() {
		return 4;
	}

}
