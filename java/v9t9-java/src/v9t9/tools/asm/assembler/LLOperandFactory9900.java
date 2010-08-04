package v9t9.tools.asm.assembler;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.ILLOperandFactory;
import v9t9.tools.asm.assembler.operand.hl.RegisterOperand;
import v9t9.tools.asm.assembler.operand.ll.LLAddrOperand;
import v9t9.tools.asm.assembler.operand.ll.LLForwardOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLPCRelativeOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;

public class LLOperandFactory9900 implements ILLOperandFactory {

	@Override
	public LLOperand resolve(Assembler assembler, IInstruction inst,
			AssemblerOperand src) throws ResolveException {
		if (src.getClass().equals(RegisterOperand.class)) {
			LLOperand op = src.resolve(assembler, inst);
			if (op instanceof LLImmedOperand) {
				return new LLRegisterOperand(op.getImmediate());
			}
			throw new ResolveException(op);
		}
		
		if (src.getClass().equals(AddrOperand.class)) {
			AddrOperand addr = (AddrOperand) src;
			LLOperand lop = addr.getAddr().resolve(assembler, inst);
			if (lop instanceof LLForwardOperand)
				return new LLForwardOperand(addr, 2);
			
			if (lop instanceof LLPCRelativeOperand) {
				lop = new LLAddrOperand(addr, inst.getPc() + ((LLPCRelativeOperand)lop).getOffset());
			} else if (lop instanceof LLImmedOperand) {
				lop = new LLAddrOperand(addr, lop.getImmediate());
			} else
				throw new ResolveException(lop, "Expected an immediate");
			return lop;
		}
		return null;
	}

}
