package v9t9.tools.asm.assembler.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

public interface ILLOperandFactory {

	 LLOperand resolve(Assembler assembler, IInstruction inst, AssemblerOperand src)
		throws ResolveException;
}
