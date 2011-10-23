/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.engine.cpu.MachineOperandMFP201;
import v9t9.engine.cpu.Status;
import v9t9.engine.cpu.StatusMFP201;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PC=" + HexUtils.toHex4(getPC()) + "; SP=" + HexUtils.toHex4(getSP()) + "; SR=" + getStatus();
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
		if (reg == MachineOperandMFP201.SR)
			val &= ~1;
		regs[reg] = (short) val;
		if (reg == MachineOperandMFP201.SR) {
			getStatus().expand(regs[reg]);
		}
		
	}
	

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return 16;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegisterName(int)
	 */
	@Override
	public String getRegisterName(int reg) {
		return "R" + reg;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegisterTooltip(int)
	 */
	@Override
	public String getRegisterTooltip(int reg) {
		return null;
	}

	@Override
	public Status createStatus() {
		return new StatusMFP201();
	}

	public short getSP() {
		return (short) getRegister(MachineOperandMFP201.SP);
	}
	
	public void setSP(short sp) {
		setRegister(MachineOperandMFP201.SP, sp);
		
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

	public short getST() {
	    return (short) getRegister(MachineOperandMFP201.SR);
	}

	public void setST(short st) {
		getStatus().expand(st);
	}

}