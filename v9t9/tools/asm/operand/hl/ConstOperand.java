/**
 * 
 */
package v9t9.tools.asm.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;
import v9t9.tools.asm.operand.ll.LLForwardOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOperand;

/**
 * A request for a const
 * @author Ed
 *
 */
public class ConstOperand extends ImmediateOperand implements AssemblerOperand {

	public ConstOperand(AssemblerOperand op) {
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
		int addr;
		if (inst.isByteOp()) {
			addr = assembler.getConstTable().allocateByte(value);
		} else {
			addr = assembler.getConstTable().allocateWord(value);
		}
		return assembler.getConstTable().createTableOffset(assembler, addr).resolve(assembler, inst);
	}
}
