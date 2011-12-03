/**
 * 
 */
package v9t9.engine.cpu;

public class Effects {
	/** method status is set after operands parsed, before execution (Status.stset_xxx) */
    public int stsetBefore; // 
    /** method status is set after execution (Status.stset_xxx) */
    public int stsetAfter; // 
    /** bits read by instruction (Status.ST_xxx mask) */
    public int stReads;     // 
    /** operand is a jump (INST_JUMP_COND = conditional) */
    public int jump; 

    /** what resources (INST_RSRC_xxx) are read and written? */
    public int reads, writes;	
    public int mop1_dest, mop2_dest, mop3_dest;
    
    public boolean byteop;
}