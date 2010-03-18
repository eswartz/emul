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
public class ConstPoolRefOperand extends ImmediateOperand implements AssemblerOperand {

	public ConstPoolRefOperand(AssemblerOperand op) {
		super(op);
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
}
