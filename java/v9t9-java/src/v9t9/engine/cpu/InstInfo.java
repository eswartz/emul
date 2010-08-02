/**
 * 
 */
package v9t9.engine.cpu;

/**
 * @author  Ed
 */
public class InstInfo {
	/**
	 * 
	 */
	public short cycles;
	/**
	 * 
	 */
	public int stsetBefore;
	/**
	 * 
	 */
	public int stsetAfter;
	/**
	 * 
	 */
	public int stReads;
	/** operand is a jump (INST_JUMP_COND = conditional) */
	public int jump;
	/**
	 * 
	 */
	public int reads;
	/**
	 * 
	 */
	public int writes;
	/** Instruction does not jump */
	public static final int INST_JUMP_FALSE = 0;
	/** Instruction always jumps */
	public static final int INST_JUMP_TRUE = 1;
	/** Instruction jumps conditionally */
	public static final int INST_JUMP_COND = 2;
	public static final int INST_RSRC_PC = 1;	// program counter
	public static final int INST_RSRC_WP = 2;	// workspace pointer
	public static final int INST_RSRC_ST = 4;	// status
	public static final int INST_RSRC_IO = 8;	// I/O
	public static final int INST_RSRC_EMU = 16; // emulator itself (builtin)
	public static final int INST_RSRC_CTX = 32;	// context switch (writer only)

	/**
	 * 
	 */
	public InstInfo() {
	}
}