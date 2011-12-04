/**
 * 
 */
package v9t9.machine.f99b.cpu;


import v9t9.base.utils.HexUtils;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IStatus;
import v9t9.common.memory.IMemoryDomain;
import v9t9.machine.f99b.asm.StatusF99b;

/**
 * @author Ed
 *
 */
public class CpuStateF99b implements ICpuState {

	/**
	 * 
	 */
	private static final int ALIGNED_REGMASK = ((1 << CpuF99b.SP) | (1 << CpuF99b.SP0) 
							| (1 << CpuF99b.RP) | (1 << CpuF99b.RP0)
							| (1 << CpuF99b.UP) | (1 << CpuF99b.UP0)
							| (1 << CpuF99b.LP));
	
	private final IMemoryDomain console;
	private IStatus status;

	private short regs[] = new short[16];
	
	public CpuStateF99b(IMemoryDomain console) {
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
	

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return CpuF99b.REG_COUNT;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegisterName(int)
	 */
	@Override
	public String getRegisterName(int reg) {
		switch (reg) {
		case CpuF99b.PC: return "PC";
		case CpuF99b.SP: return "SP";
		case CpuF99b.SP0: return "SP0";
		case CpuF99b.RP: return "RP";
		case CpuF99b.RP0: return "RP0";
		case CpuF99b.UP: return "UP";
		case CpuF99b.UP0: return "UP0";
		case CpuF99b.SR: return "SR";
		case CpuF99b.LP: return "LP";
		default: return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getRegisterTooltip(int)
	 */
	@Override
	public String getRegisterTooltip(int reg) {
		switch (reg) {
		case CpuF99b.PC:
			return "Program Counter";
		case CpuF99b.SP:
			return "Stack pointer";
		case CpuF99b.SP0:
			return "Upper Stack Limit";
		case CpuF99b.RP:
			return "R-Stack Pointer";
		case CpuF99b.RP0:
			return "Upper R-Stack Limit";
		case CpuF99b.UP0:
			return "Default User Base";
		case CpuF99b.UP:
			return "User Base";
		case CpuF99b.SR:
			return "Status Register: " + getStatus().toString();
		case CpuF99b.LP:
			return "Locals Pointer";
		}
		return null;
	}

	@Override
	public IStatus createStatus() {
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
	public final IMemoryDomain getConsole() {
		return console;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#getStatus()
	 */
	@Override
	public final IStatus getStatus() {
		return status;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.cpu.CpuState#setStatus(v9t9.engine.cpu.Status)
	 */
	@Override
	public final void setStatus(IStatus status) {
		this.status = status;
	}

	public final short getST() {
	    return (short) getRegister(CpuF99b.SR);
	}

	public final void setST(short st) {
		setRegister(CpuF99b.SR, st);
	}

	public final short getBaseSP() {
		return (short) getRegister(CpuF99b.SP0);
	}
	
	public final void setBaseSP(short v) {
		setRegister(CpuF99b.SP0, v);
	}

	public final short getBaseRP() {
		return (short) getRegister(CpuF99b.RP0);
	}
	
	public final void setBaseRP(short v) {
		setRegister(CpuF99b.RP0, v);
		
	}

	public final short getBaseUP() {
		return (short) getRegister(CpuF99b.UP0);
	}
	
	public final void setBaseUP(short v) {
		setRegister(CpuF99b.UP0, v);
	}

	final void push(short val) {
		regs[CpuF99b.SP] -= 2;
		short sp = regs[CpuF99b.SP];
		console.writeWord(sp, val);
		
		/*if (sp < regs[CpuF99b.SP0] - CpuF99b.MAX_STACK) {
			System.err.println("Stack overflow!");
			cpu.fault();
		}*/			
	}
	

	final short pop(CpuF99b cpu) {
		short val = console.readWord(regs[CpuF99b.SP]);
		regs[CpuF99b.SP] += 2;
		if (regs[CpuF99b.SP] > regs[CpuF99b.SP0]) {
			System.err.println("Stack underflow!");
			cpu.fault();
		}
		return val;
	}


	public final short rpeek() {
		return console.readWord(regs[CpuF99b.RP]);
	}

	final void rpush(short val) {
		regs[CpuF99b.RP] -= 2;
		short rp = regs[CpuF99b.RP];
		console.writeWord(rp, val);
		/*if (rp < regs[CpuF99b.RP0] - CpuF99b.MAX_STACK) {
			System.err.println("R-Stack overflow!");
			cpu.fault();
		}*/	
				
	}
	final short rpop(CpuF99b cpu) {
		short val = console.readWord(regs[CpuF99b.RP]);
		regs[CpuF99b.RP] += 2;
		if (regs[CpuF99b.RP] > regs[CpuF99b.RP0]) {
			System.err.println("R-Stack underflow!");
			cpu.fault();
		}
		return val;
	}


}