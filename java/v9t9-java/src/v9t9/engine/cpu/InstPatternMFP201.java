/**
 * 
 */
package v9t9.engine.cpu;

public class InstPatternMFP201 {

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
	public final byte[] enc;
	
	InstPatternMFP201(int op1, int op2, int op3, byte[] enc) {
		this.op1 = op1;
		this.op2 = op2;
		this.op3 = op3;
		this.enc = enc;
	}
	InstPatternMFP201(int op1, int op2, byte[] enc) {
		this(op1, op2, NONE, enc);
	}
	InstPatternMFP201(int op1, byte[] enc) {
		this(op1, NONE, NONE, enc);
	}
}