/**
 * 
 */
package v9t9.machine.f99b.cpu;

import java.io.PrintWriter;
import java.util.LinkedList;

import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.machine.f99b.asm.InstF99b;
import v9t9.machine.f99b.asm.InstructionWorkBlockF99b;

/**
 * @author ejs
 *
 */
public class DebugConditionListenerF99b implements IInstructionListener {

	private LinkedList<InstructionWorkBlockF99b> blocks = new LinkedList<InstructionWorkBlockF99b>();
	private ICpu cpu;
	
	public DebugConditionListenerF99b(ICpu cpu)  {
		this.cpu = cpu;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionWorkBlock, v9t9.engine.cpu.InstructionWorkBlock)
	 */
	@Override
	public void executed(InstructionWorkBlock before_, InstructionWorkBlock after_) {
		InstructionWorkBlockF99b before = (InstructionWorkBlockF99b) before_;
		InstructionWorkBlockF99b after = (InstructionWorkBlockF99b) after_;
		if (blocks.size() > 1024)
			blocks.remove(0);
		RawInstruction prev = blocks.size() > 0 ? blocks.get(blocks.size() - 1).inst : null;
		blocks.add(before);
		
		boolean suspicious = prev != null 
			&& prev.getSize() == 1 && prev.getInst() == InstF99b.IbranchX
			&& before.inst.getSize() == 1 && before.inst.getInst() == InstF99b.IbranchX;
		
		boolean intfault = (after.cpu.getRegister(CpuF99b.SP) & 0xffff) == 0xFFDC
						&& (after.cpu.getRegister(CpuF99b.RP) & 0xffff) == 0xFFCC;
		
		if (suspicious || intfault) {
			PrintWriter pw = new PrintWriter(System.err);
			DumpFullReporterF99b dfp = new DumpFullReporterF99b((CpuF99b) cpu, pw);
			for (int i = 0; i < blocks.size() - 1; i++) {
				dfp.executed(blocks.get(i), blocks.get(i+1));
			}
			System.err.println("FAULT at " + before.inst);
		}
	}

}
