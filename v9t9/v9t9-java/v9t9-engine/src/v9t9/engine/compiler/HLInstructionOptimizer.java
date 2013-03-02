/*
  HLInstructionOptimizer.java

  (c) 2008-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
