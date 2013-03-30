/**
 * 
 */
package v9t9.tools.cycler;

import java.io.PrintStream;

import v9t9.common.asm.IInstruction;
import v9t9.common.cpu.AbortedException;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IExecutor;
import v9t9.common.machine.IMachine;
import v9t9.machine.ti99.cpu.Cpu9900;
import v9t9.machine.ti99.cpu.CpuState9900;

/**
 * @author ejs
 *
 */
public class Cycler {
	private IMachine machine;
	private PrintStream out;
	private IExecutor executor;
	private int stopAddr;
	private ICpuState state;
	private ICpu cpu;

	public Cycler(IMachine machine, int startAddr, int stopAddr,
			PrintStream out) {
		this.machine = machine;
		this.stopAddr = stopAddr;
		this.out = out;
		
		executor = machine.getExecutor();
		state = machine.getCpu().getState();
		cpu = machine.getCpu();

		if (machine.getCpu() instanceof Cpu9900) {
			((CpuState9900) state).setWP((short) 0x83e0);
			cpu.getConsole().writeWord(0x83e0 + 11 * 2, (short) 0);
		} 
		state.setPC((short) startAddr);
	}

	public void run() {
		while (true) {
			if (stopAddr != 0 && (state.getPC() & 0xffff) == stopAddr)
				break;
			try {
				long beforeCycles = cpu.getCurrentCycleCount();
				IInstruction instr = cpu.getInstructionFactory().decodeInstruction(
						state.getPC(), machine.getConsole());
				out.print(state + "; " + instr);
				executor.interpretOneInstruction();
				long afterCycles = cpu.getCurrentCycleCount();
				out.println("; " + (afterCycles - beforeCycles));
				
				if (state.getPC() == 0)
					break;
			} catch (AbortedException e) {
				break;
			}
		}
	}
	
}
