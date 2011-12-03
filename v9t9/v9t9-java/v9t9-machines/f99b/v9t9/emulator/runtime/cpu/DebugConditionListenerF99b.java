/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;
import java.util.LinkedList;

import v9t9.emulator.runtime.InstructionListener;
import v9t9.engine.cpu.BaseInstructionWorkBlock;
import v9t9.engine.cpu.InstF99b;
import v9t9.engine.cpu.InstructionWorkBlockF99b;
import v9t9.engine.cpu.RawInstruction;

/**
 * @author ejs
 *
 */
public class DebugConditionListenerF99b implements InstructionListener {

	private LinkedList<InstructionWorkBlockF99b> blocks = new LinkedList<InstructionWorkBlockF99b>();
	private Cpu cpu;
	
	public DebugConditionListenerF99b(Cpu cpu)  {
		this.cpu = cpu;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionWorkBlock, v9t9.engine.cpu.InstructionWorkBlock)
	 */
	@Override
	public void executed(BaseInstructionWorkBlock before_, BaseInstructionWorkBlock after_) {
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
