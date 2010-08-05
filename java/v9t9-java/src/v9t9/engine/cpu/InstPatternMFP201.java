/**
 * 
 */
package v9t9.engine.cpu;

public class InstPatternMFP201 {

	public int opcode = -1;
	
	public final int length;
	
	public final int op1;
	public final int op2;
	public final int op3;
	
	public final static int NONE = 0;
	public final static int REG = 1;
	public final static int CNT = 2;
	/** register or memory or immediate (encoded as *PC+) */
	public final static int GEN = 3;
	/** PC-relative offset for jump */
	public final static int OFF = 4;
	/** immediate */
	public final static int IMM = 5;
	
	/** array of metainstructions -- see InstTableMFP201 */
	public final byte[] enc;
	
	InstPatternMFP201(int op1, int op2, int op3, int length, byte[] enc) {
		this.op1 = op1;
		this.op2 = op2;
		this.op3 = op3;
		this.length = length;
		this.enc = enc;
	}
	InstPatternMFP201(int op1, int op2, int op3, byte[] enc) {
		this(op1, op2, op3, 3, enc);
	}
	InstPatternMFP201(int op1, int op2, byte[] enc) {
		this(op1, op2, NONE, 2, enc);
	}
	InstPatternMFP201(int op1, byte[] enc) {
		this(op1, NONE, NONE, 1, enc);
	}
	InstPatternMFP201(byte[] enc) {
		this(NONE, NONE, NONE, 0, enc);
	}
	
	private InstPatternMFP201(InstPatternMFP201 p, int opcode) {
		this.opcode = opcode;
		this.enc = p.enc;
		this.length = p.length;
		this.op1 = p.op1;
		this.op2 = p.op2;
		this.op3 = p.op3;
	}
	public InstPatternMFP201 opcode(int opcode) {
		InstPatternMFP201 copy = new InstPatternMFP201(this, opcode);
		return copy;
	}
	/**
	 * @param mopIdx
	 * @return
	 */
	public int op(int mopIdx) {
		switch (mopIdx) {
		case 1: return op1;
		case 2: return op2;
		case 3: return op3;
		}
		return NONE;
	}
}