/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.hardware.memory.F99ConsoleMmioArea;
import v9t9.engine.cpu.*;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author Ed
 *
 */
public class CpuStateF99b implements CpuState {

	/**
	 * 
	 */
	private static final int ALIGNED_REGMASK = ((1 << CpuF99b.SP) | (1 << CpuF99b.SP0) 
							| (1 << CpuF99b.RP) | (1 << CpuF99b.RP0)
							| (1 << CpuF99b.UP) | (1 << CpuF99b.UP0)
							| (1 << CpuF99b.LP));
	
	private MemoryDomain console;
	private Status status;

	private short regs[] = new short[16];
	
	public CpuStateF99b(MemoryDomain console) {
		this.console = console;
		this.status = createStatus();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PC=" + HexUtils.toHex4(getPC()) + "; SP=" + HexUtils.toHex4(getSP()) + "; RP=" + HexUtils.toHex4(getRP());
	}
	
	
	public short getPC() {
	    return (short) getRegister(CpuF99b.PC);
	}

	public void setPC(short pc) {
		setRegister(CpuF99b.PC, pc);
	}

	public int getRegister(int reg) {
	    return regs[reg];
	}

	@Override
	public void setRegister(int reg, int val) {
		// always aligned
		if (((1 << reg) & ALIGNED_REGMASK) != 0) {	
			val &= ~1;
		}
		regs[reg] = (short) val;
		if (reg == CpuF99b.SR) {
			getStatus().expand(regs[reg]);
		}
		
	}

	@Override
	public Status createStatus() {
		return new StatusF99b();
	}

	public short getSP() {
		return (short) getRegister(CpuF99b.SP);
	}
	
	public void setSP(short sp) {
		setRegister(CpuF99b.SP, sp);
		
	}
	

	public short getRP() {
		return (short) getRegister(CpuF99b.RP);
	}
	
	public void setRP(short sp) {
		setRegister(CpuF99b.RP, sp);
	}
	

	public short getUP() {
		return (short) getRegister(CpuF99b.UP);
	}
	
	public void setUP(short up) {
		setRegister(CpuF99b.UP, up);
		
	}
	public short getLP() {
		return (short) getRegister(CpuF99b.LP);
	}
	
	public void setLP(short sp) {
		setRegister(CpuF99b.LP, sp);
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
	    return (short) getRegister(CpuF99b.SR);
	}

	public void setST(short st) {
		getStatus().expand(st);
	}

	public short getBaseSP() {
		return (short) getRegister(CpuF99b.SP0);
	}
	
	public void setBaseSP(short v) {
		setRegister(CpuF99b.SP0, v);
	}

	public short getBaseRP() {
		return (short) getRegister(CpuF99b.RP0);
	}
	
	public void setBaseRP(short v) {
		setRegister(CpuF99b.RP0, v);
		
	}

	public short getBaseUP() {
		return (short) getRegister(CpuF99b.UP0);
	}
	
	public void setBaseUP(short v) {
		setRegister(CpuF99b.UP0, v);
	}


}