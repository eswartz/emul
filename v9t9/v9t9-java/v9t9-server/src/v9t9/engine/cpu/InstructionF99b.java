/**
 * OCt 15 2010
 */
package v9t9.engine.cpu;

/**
 * @author Ed
 *
 */
public class InstructionF99b extends RawInstruction {
	public InstructionF99b() {
		super();
	}

	public InstructionF99b(RawInstruction other) {
		super(other);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.RawInstruction#toString()
	 */
	@Override
	public String toString() {
		return super.toString();
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.RawInstruction#setInst(int)
	 */
	@Override
	public void setInst(int inst) {
		super.setInst(inst);
		setName(InstF99b.getInstName(inst));
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
		return super.getSize();
	}
}
