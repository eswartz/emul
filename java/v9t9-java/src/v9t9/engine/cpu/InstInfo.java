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

	/**
	 * 
	 */
	public InstInfo() {
	}
}