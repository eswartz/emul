/**
 * 
 */
package v9t9.machine.ti99.cpu;

import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IStatus;
import v9t9.common.memory.MemoryDomain;

/**
 * @author Ed
 *
 */
public class CpuState9900 implements ICpuState {

	/** program counter */
	protected short PC;
	/** workspace pointer */
	protected short WP;
	private MemoryDomain console;
	private IStatus status;

	public CpuState9900(MemoryDomain console) {
		this.console = console;
		this.status = createStatus();
	}

	public short getPC() {
	    return PC;
	}

	public void setPC(short pc) {
	    PC = pc;
	}

	public short getWP() {
	    return WP;
	}

	public void setWP(short i) {
	    // TODO: verify
	    WP = i;
	}

	public int getRegister(int reg) {
		if (reg < 16)
			return console.readWord(WP + reg*2);
		
		if (reg == Cpu9900.REG_PC)
			return PC;
		else if (reg == Cpu9900.REG_WP)
			return WP;
		else if (reg == Cpu9900.REG_ST)
			return status.flatten();
		
		return 0;
	}

	@Override
	public void setRegister(int reg, int val) {
		if (reg < 16) {
			console.writeWord(WP + reg*2, (short) val);
			return;
		}
		if (reg == Cpu9900.REG_PC)
			PC = (short) val;
		else if (reg == Cpu9900.REG_WP)
			WP = (short) val;
		else if (reg == Cpu9900.REG_ST)
			status.expand((short) val);
		
	}

	@Override
	public IStatus createStatus() {
		return new Status9900();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getConsole()
	 */
	@Override
	public MemoryDomain getConsole() {
		return console;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getStatus()
	 */
	@Override
	public IStatus getStatus() {
		return status;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#setStatus(v9t9.engine.cpu.Status)
	 */
	@Override
	public void setStatus(IStatus status) {
		this.status = status;
	}

	public short getST() {
	    return getStatus().flatten();
	}

	public void setST(short st) {
		getStatus().expand(st);
	}


	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.Cpu#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return 16 + 3;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.Cpu#getRegisterName(int)
	 */
	@Override
	public String getRegisterName(int reg) {
		return reg < 16 ? "R" + reg : (reg == Cpu9900.REG_PC ? "PC" : reg == Cpu9900.REG_ST ? "ST" : 
			reg == Cpu9900.REG_WP ? "WP" : null);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegisterTooltip(int)
	 */
	@Override
	public String getRegisterTooltip(int reg) {
		boolean isGplWs = (getRegister(Cpu9900.REG_WP) & 0xffff) == 0x83e0;
		switch (reg) {
		case Cpu9900.REG_ST:
			return "Status register: " + getStatus().toString();
		case Cpu9900.REG_WP:
			return "Workspace pointer";
		case Cpu9900.REG_PC:
			return "Program counter";
		case 11:
			return "BL return address";
		case 13:
			return isGplWs ? "GROM Read Data Address" : "BLWP saved WP";
		case 14:
			return isGplWs ? "System Flags" : "BLWP saved PC";
		case 15:
			return isGplWs ? "VDP Address Write Address" : "BLWP saved ST";
		}
		return null;
	}
}