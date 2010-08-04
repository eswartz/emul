/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import v9t9.engine.cpu.Status;
import v9t9.engine.cpu.Status9900;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author Ed
 *
 */
public class CpuState9900 implements CpuState {

	/** program counter */
	protected short PC;
	/** workspace pointer */
	protected short WP;
	private MemoryDomain console;
	private Status status;

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
	    return console.readWord(WP + reg*2);
	}

	@Override
	public void setRegister(int reg, int val) {
		console.writeWord(WP + reg*2, (short) val);
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