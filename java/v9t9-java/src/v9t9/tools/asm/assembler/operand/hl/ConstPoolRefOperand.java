/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLForwardOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * A request for a const
 * @author Ed
 *
 */
public class ConstPoolRefOperand extends ImmediateOperand {

	public ConstPoolRefOperand(AssemblerOperand op) {
		super(op);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.ImmediateOperand#toString()
	 */
	@Override
	public String toString() {
		return "#" + super.toString();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return true;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return false;
	}
	public LLOperand resolve(Assembler assembler, IInstruction inst) throws ResolveException {
		LLOperand op = immed.resolve(assembler, inst);
		if (op instanceof LLForwardOperand)
			return new LLForwardOperand(this, 2);
		if (!(op instanceof LLImmedOperand)) {
			throw new ResolveException(op, "Expected an immediate");
		}
		
		int value = op.getImmediate();
		AssemblerOperand addr;
		if (inst.isByteOp()) {
			addr = assembler.getConstPool().allocateByte(value);
		} else {
			addr = assembler.getConstPool().allocateWord(value);
		}
		
		LLOperand resOp = addr.resolve(assembler, inst);
		resOp.setOriginal(this);
		return resOp;
	}

	/**
	 * @return
	 */
	public Integer getValue() {
		return ((NumberOperand) immed).getValue();
	}
}
