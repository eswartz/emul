/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;


import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.InstructionWorkBlock;

/**
 * @author ejs
 *
 */
public class InstRow {

	private static int gCounter;
	private final int count = gCounter++;
	private final InstructionWorkBlock before;
	private final InstructionWorkBlock after;
	private boolean isGeneric;
	
	public InstRow(InstructionWorkBlock before) {
		this.before = before;
		this.after = before;
		this.isGeneric = true;
	}
	public InstRow(InstructionWorkBlock before, InstructionWorkBlock after) {
		this.before = before;
		this.after = after;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		InstRow other = (InstRow) obj;
		if (count != other.count) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return count;
	}

	/**
	 * @return
	 */
	public boolean isGeneric() {
		return isGeneric;
	}
	/**
	 * @return
	 */
	public RawInstruction getBeforeInst() {
		return before.inst;
	}
	public RawInstruction getAfterInst() {
		return after.inst;
	}
	/**
	 * @return the after
	 */
	public InstructionWorkBlock getAfter() {
		return after;
	}
	/**
	 * @return the before
	 */
	public InstructionWorkBlock getBefore() {
		return before;
	}
}
