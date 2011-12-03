/**
 * 
 */
package v9t9.engine.cpu;

public class InstPattern9900 {
	public final int op1;
	public final int op2;
	final int off1;
	public final static int NONE = 0;
	public final static int REG = 1;
	public final static int CNT = 2;
	public final static int IMM = 3;
	public final static int GEN = 4;
	public final static int OFF = 5;

	InstPattern9900(int op1, int op2, int off1) {
		this.op1 = op1;
		this.op2 = op2;
		this.off1 = off1;
	}
	InstPattern9900(int op1, int op2) {
		this(op1, op2, 0);
	}
}