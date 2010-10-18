/**
 * Oct 15 2010
 */
package v9t9.engine.cpu;

public class InstPatternF99 {

	public final static int NONE = 0;
	public final static int IMM = 1;
	
	public int opcode = -1;
	
	public final int op1;
	
	InstPatternF99(int op1) {
		this.op1 = op1;
	}
	InstPatternF99() {
		this(NONE);
	}
	/**
	 * @param mopIdx
	 * @return
	 */
	public int op(int mopIdx) {
		switch (mopIdx) {
		case 1: return op1;
		}
		return NONE;
	}
}