/**
 * 
 */
package v9t9.machine.f99b.asm;

import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.IChangeElement;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.machine.f99b.cpu.CpuF99b;
import v9t9.machine.f99b.cpu.CpuStateF99b;
import v9t9.machine.f99b.cpu.F99bInstructionFactory;

/**
 * @author ejs
 *
 */
public class ChangeBlockF99b extends ChangeBlock {
	public CpuStateF99b cpu;
	
	public short[] inStack;
	public short[] inReturnStack;
	public int fetchCycles;
	public PreExecute preExecute;
	
	public class PreExecute implements IChangeElement {

		public short pc;
		public int cycles;
		public short st;
		public int sp;
		public int rp;
		public int up;
		public int lp;

		/* (non-Javadoc)
		 * @see v9t9.common.cpu.IChangeElement#apply(v9t9.common.cpu.ICpuState)
		 */
		@Override
		public void apply(ICpuState cpuState) {
			pc = cpu.getPC();
			
			cycles = cpu.getCycleCounts().getTotal();
			st = cpu.getST();
			
			sp = cpuState.getRegister(CpuF99b.SP);
			rp = cpuState.getRegister(CpuF99b.RP);
			up = cpuState.getRegister(CpuF99b.UP);
			lp = cpuState.getRegister(CpuF99b.LP);
		}

		/* (non-Javadoc)
		 * @see v9t9.common.cpu.IChangeElement#revert(v9t9.common.cpu.ICpuState)
		 */
		@Override
		public void revert(ICpuState cpuState) {
			cpuState.setRegister(CpuF99b.SP, sp);
			cpuState.setRegister(CpuF99b.RP, rp);
			cpuState.setRegister(CpuF99b.UP, up);
			cpuState.setRegister(CpuF99b.LP, lp);
			
		}
		
	}
	public ChangeBlockF99b(ICpu cpu) {
		this(cpu, cpu.getState().getPC());
	}
	public ChangeBlockF99b(ICpu cpu, int pc) {
		this.cpu = (CpuStateF99b) cpu.getState();
		
		cpu.getCycleCounts().saveState();
		int total = cpu.getCycleCounts().getTotal();
		inst = (InstructionF99b) F99bInstructionFactory.INSTANCE.decodeInstruction(pc, cpu.getConsole());
		fetchCycles = cpu.getCycleCounts().getTotal() - total;
		cpu.getCycleCounts().restoreState();
		
		preExecute = new PreExecute();
		push(preExecute);
		
	}
	
	@Override
	public int getPC() {
		return inst.pc;
	}
	/**
	 * @return
	 */
	public int getSize() {
		return inst.getSize();
	}

}
