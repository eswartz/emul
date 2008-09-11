/**
 * 
 */
package v9t9.emulator.runtime;

import v9t9.engine.cpu.Instruction;

/**
 * Optimizer for instruction arrays
 * @author ejs
 *
 */
public class LLInstructionOptimizer {

	/**
	 * Reset status setting flags for instructions whose results aren't
	 * read (and are destroyed) by the following instruction.
	 * This algorithm ignores flow control, but that's okay, because
	 * jumps do not destroy status.
     */
    public static void peephole_status(Instruction[] insts, int numinsts) {
        Instruction prev = null;
        int i;
        i = 0;
        while (i < numinsts) {
            Instruction ins = insts[i];
            if (ins == null) {
                i++;
                continue;
            }
            if (prev != null) {
                int prevBits = Instruction.getStatusBits(prev.stsetBefore)
                    | Instruction.getStatusBits(prev.stsetAfter);
                int bits  = Instruction.getStatusBits(ins.stsetBefore)
                    | Instruction.getStatusBits(ins.stsetAfter);
                // we're not looking at flow of control here,
                // so just turn off status writes for instructions
                // where the next instruction writes the same bits
                if (prevBits != 0 && (prevBits & ~bits) == 0) {
                	//System.out.println("Clear status bits for " + prev);
                    prev.stsetBefore = Instruction.st_NONE;
                    prev.stsetAfter = Instruction.st_NONE;
                }
            }
            prev = ins;
            if (ins.size <= 0) {
				throw new AssertionError("WTF? " + ins);
			}
            i += ins.size / 2;
        }

    }
}
