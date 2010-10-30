/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.engine.cpu.*;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author Ed
 *
 */
public class CpuStateF99b implements CpuState {

	private MemoryDomain console;
	private Status status;

	private short regs[] = new short[6];
	private short baseUP;
	private short baseRP;
	private short baseSP;
	
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
		if (reg == CpuF99b.SP || reg == CpuF99b.RP || reg == CpuF99b.UP)
			val &= ~1;
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

	/**
	 * @param int1
	 */
	public void setBaseSP(short v) {
		this.baseSP = v;
	}

	/**
	 * @param int1
	 */
	public void setBaseRP(short v) {
		this.baseRP = v;
		
	}

	/**
	 * @param int1
	 */
	public void setBaseUP(short v) {
		this.baseUP = v;
	}

	/**
	 * @return
	 */
	public short getBaseSP() {
		return baseSP;
	}

	/**
	 * @return
	 */
	public short getBaseRP() {
		return baseRP;
	}

	/**
	 * @return
	 */
	public short getBaseUP() {
		return baseUP;
	}

}