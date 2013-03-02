/*
  HLInstructionOptimizer.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.compiler;

import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.IStatus;

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
                    prev.getInfo().stsetBefore = IStatus.stset_NONE;
                    prev.getInfo().stsetAfter = IStatus.stset_NONE;
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
