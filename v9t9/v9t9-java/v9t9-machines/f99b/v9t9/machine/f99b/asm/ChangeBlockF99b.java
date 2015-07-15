/**
 * 
 */
package v9t9.machine.f99b.asm;

import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.ICpu;
import v9t9.machine.f99b.cpu.CpuStateF99b;
import v9t9.machine.f99b.cpu.F99bInstructionFactory;

/**
 * @author ejs
 *
 */
public class ChangeBlockF99b extends ChangeBlock {
	private CpuStateF99b state;
	
	public ChangeBlockF99b(ICpu cpu) {
		this(cpu, cpu.getState().getPC());
	}
	public ChangeBlockF99b(ICpu cpu, int pc) {
		this.state = (CpuStateF99b) cpu.getState();
		RawInstruction rawInst = F99bInstructionFactory.INSTANCE.decodeInstruction(pc, state.getConsole());
		inst = new InstructionF99b(rawInst);
	}
	
	@Override
	public int getPC() {
		return inst.pc;
	}

}
