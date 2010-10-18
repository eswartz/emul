/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;
import java.util.LinkedList;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.runtime.InstructionListener;
import v9t9.engine.cpu.BaseInstructionWorkBlock;
import v9t9.engine.cpu.InstructionWorkBlock;
import v9t9.engine.cpu.MachineOperand9900;

/**
 * @author ejs
 *
 */
public class DebugConditionListener implements InstructionListener {

	private LinkedList<InstructionWorkBlock> blocks = new LinkedList<InstructionWorkBlock>();
	private Cpu cpu;
	
	public DebugConditionListener(Cpu cpu)  {
		this.cpu = cpu;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionWorkBlock, v9t9.engine.cpu.InstructionWorkBlock)
	 */
	@Override
	public void executed(BaseInstructionWorkBlock before_, BaseInstructionWorkBlock after_) {
		InstructionWorkBlock before = (InstructionWorkBlock) before_;
		InstructionWorkBlock after = (InstructionWorkBlock) after_;
		if (blocks.size() > 1024)
			blocks.remove(0);
		blocks.add(before);
		
		int sp = before.cpu.getConsole().readWord(before.wp + 10 * 2) & 0xffff;
		if (before.ea1 == before.wp + 10 * 2 &&
				sp < 0xf740 - 0x40) {
			DumpFullReporter9900 dfp = new DumpFullReporter9900((Cpu9900) cpu);
			PrintWriter pw = new PrintWriter(System.err);
			for (InstructionWorkBlock block : blocks) {
				dfp.dumpFullStart(block, block.inst, pw);
				pw.println();
			}
			dfp.dumpFullStart(before, before.inst, pw);
			dfp.dumpFullEnd(after, 0, (MachineOperand9900)before.inst.getOp1(), (MachineOperand9900)before.inst.getOp2(), pw);
			pw.println();
			System.err.println("stack underflow at " + before.inst + ": " + HexUtils.toHex4(sp));
		}
	}

}
