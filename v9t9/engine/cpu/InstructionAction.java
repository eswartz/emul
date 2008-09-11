/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 31, 2005
 *
 */
package v9t9.engine.cpu;

import v9t9.engine.memory.MemoryDomain;

public interface InstructionAction {
    public class Block {
        /* our CPU memory */
        public MemoryDomain domain;
        /* the instruction (in) */
        public Instruction inst;	
        /* EAs for operands 1 and 2 */
        public short ea1, ea2;
        /* values for operands 1 and 2 (in: EAs or values, out: value)
        for MPY/DIV, val3 holds lo reg */
        public short val1, val2, val3;	
        /* values (in: original, out: changed, if needed) */
        public short pc, wp;
        /* status word (in/out) */
        public Status status;

    }
    void act(InstructionAction.Block block);
}