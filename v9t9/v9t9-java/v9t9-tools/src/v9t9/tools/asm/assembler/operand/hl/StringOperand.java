/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author ejs
 *
 */
public class StringOperand extends BaseOperand {

	private String string;

	public StringOperand(String string) {
		this.string = string;
	}
	
	@Override
	public String toString() {
		return '"' + string + '"';
	}
	
	public String getString() {
		return string;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return false;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.Operand#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.RawInstruction)
	 */
	public LLOperand resolve(IAssembler assembler, IInstruction inst)
			throws ResolveException {
		throw new ResolveException(this, "Cannot resolve a string outside DB, BYTE, TEXT, etc.");
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#getChildren()
	 */
	@Override
	public AssemblerOperand[] getChildren() {
		return new AssemblerOperand[0];
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#addOffset(int)
	 */
	@Override
	public AssemblerOperand addOffset(int i) {
		return null;
	}
}
