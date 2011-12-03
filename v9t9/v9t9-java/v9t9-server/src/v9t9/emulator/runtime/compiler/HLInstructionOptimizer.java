/**
 * 
 */
package v9t9.emulator.runtime.compiler;

import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.cpu.Status;

/**
 * Optimizer for instruction arrays
 * @author ejs
 *
 */
public class HLInstructionOptimizer {

	/**
	 * Reset status setting flags for instructions whose results aren't
	 * read (and are destroyed) by the following instruction.
	 * This algorithm ignores flow control, but that's okay, because
	 * jumps do not destroy status.
     */
    public static void peephole_status(RawInstruction[] insts, int numinsts) {
    	RawInstruction prev = null;
        int i;
        i = 0;
        while (i < numinsts) {
        	RawInstruction ins = insts[i];
            if (ins == null) {
                i++;
                continue;
            }
            if (prev != null) {
                int prevBits = prev.getInfo().stWrites;
                int bits = ins.getInfo().stWrites;
                // we're not looking at flow of control here,
                // so just turn off status writes for instructions
                // where the next instruction writes the same bits
                if (prevBits != 0 && (prevBits & ~bits) == 0) {
                	//System.out.println("Clear status bits for " + prev);
                    prev.getInfo().stsetBefore = Status.stset_NONE;
                    prev.getInfo().stsetAfter = Status.stset_NONE;
                    prev.getInfo().stWrites = 0;

                }
            }
            prev = ins;
            if (ins.getSize() <= 0) {
				throw new AssertionError("WTF? " + ins);
			}
            i += ins.getSize() / 2;
        }

    }
}
