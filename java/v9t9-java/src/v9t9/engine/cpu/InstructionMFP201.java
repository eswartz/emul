/**
 * 
 */
package v9t9.engine.cpu;

/**
 * @author Ed
 *
 */
public class InstructionMFP201 extends RawInstruction {

	/** SZ bit in mem/size prefix */
	public static final byte MEM_SIZE_SZ = 0x10;
	/** low bit of As bits in mem/size prefix */
	public static final byte MEM_SIZE_AS_POS = 2;
	/** low bit of Ad bits in mem/size prefix */
	public static final byte MEM_SIZE_AD_POS = 0;
	public static final byte MEM_SIZE_OPCODE = 0x40;
	
	//public static final int VARIANT_NONE = 0;
	//public static final int VARIANT_BYTE = 1;
	//public static final int VARIANT_NO_WRITE = 2;
	//public static final int VARIANT_BYTE_NO_WRITE = 3;
	
	/** the variant of an instruction which affords
	 * byte or no-write versions
	 */
	//public int variant;
	
	public InstructionMFP201() {
		super();
	}

	/**
	 * @param other
	 */
	public InstructionMFP201(RawInstruction other) {
		super(other);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.RawInstruction#setInst(int)
	 */
	@Override
	public void setInst(int inst) {
		super.setInst(inst);
		setSize(0);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.RawInstruction#setOp1(v9t9.engine.cpu.Operand)
	 */
	@Override
	public void setOp1(Operand op1) {
		super.setOp1(op1);
		setSize(0);
	}
	
	@Override
	public void setOp2(Operand op2) {
		super.setOp2(op2);
		setSize(0);
	}
	
	@Override
	public void setOp3(Operand op3) {
		super.setOp3(op3);
		setSize(0);
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.RawInstruction#getSize()
	 */
	@Override
	public int getSize() {
		int sz = super.getSize();
		if (sz == 0) {
			try {
				byte[] bytes = InstTableMFP201.encode(this);
				sz = bytes.length;
			} catch (IllegalArgumentException e) {
				sz = 1;
			}
			setSize(sz);
		}
		return sz;
	}
}
