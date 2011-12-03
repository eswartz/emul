/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

/**
 * @author ejs
 * 
 */
public abstract class BaseOperand implements AssemblerOperand {

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#replaceOperand(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		return this;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#accept(v9t9.tools.asm.assembler.operand.hl.IOperandVisitor)
	 */
	@Override
	public void accept(IOperandVisitor visitor) {
		if (visitor.enterOperand(this)) {
			for (AssemblerOperand kid : getChildren())
				kid.accept(visitor);
			visitor.exitOperand(this);
		}
	}
}
