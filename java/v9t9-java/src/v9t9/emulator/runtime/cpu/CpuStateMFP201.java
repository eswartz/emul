/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import v9t9.engine.cpu.MachineOperandMFP201;
import v9t9.engine.cpu.Status;
import v9t9.engine.cpu.Status9900;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author Ed
 *
 */
public class CpuStateMFP201 implements CpuState {

	private MemoryDomain console;
	private Status status;

	private short[] regs = new short[16];
	
	public CpuStateMFP201(MemoryDomain console) {
		this.console = console;
		this.status = createStatus();
	}

	public short getPC() {
	    return (short) getRegister(MachineOperandMFP201.PC);
	}

	public void setPC(short pc) {
		setRegister(MachineOperandMFP201.PC, pc);
	}

	public int getRegister(int reg) {
	    return regs[reg];
	}

	@Override
	public void setRegister(int reg, int val) {
		// always aligned
		if (reg == MachineOperandMFP201.PC || reg == MachineOperandMFP201.SR)
			val &= ~1;
		regs[reg] = (short) val;
		if (reg == MachineOperandMFP201.SR) {
			getStatus().expand(regs[reg]);
		}
		
	}

	@Override
	public Status createStatus() {
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
	public Status getStatus() {
		return status;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#setStatus(v9t9.engine.cpu.Status)
	 */
	@Override
	public void setStatus(Status status) {
		this.status = status;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#setConsole(v9t9.engine.memory.MemoryDomain)
	 */
	@Override
	public void setConsole(MemoryDomain console) {
		this.console = console;
	}

	public short getST() {
	    return getStatus().flatten();
	}

	public void setST(short st) {
		getStatus().expand(st);
	}

}