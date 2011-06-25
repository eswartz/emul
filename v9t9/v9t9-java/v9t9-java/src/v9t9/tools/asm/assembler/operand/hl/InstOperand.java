/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

import java.util.Collections;
import java.util.List;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.HLInstruction;
import v9t9.tools.asm.assembler.LLInstruction;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLInstOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author ejs
 *
 */
public class InstOperand extends BaseOperand {

	private final HLInstruction inst;

	public InstOperand(HLInstruction inst) {
		this.inst = inst;
	}

	@Override
	public String toString() {
		return inst.toString();
	}
	
	@Override
	public boolean isMemory() {
		return false;
	}
	@Override
	public boolean isRegister() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return false;
	}
	

	public LLOperand resolve(Assembler assembler, IInstruction inst_)
			throws ResolveException {
		List<IInstruction> insts = assembler.resolve(
				Collections.<IInstruction>singletonList(inst));
		if (insts.size() != 1)
			throw new ResolveException(this, "No single instruction supplied in " + inst);
		
		return new LLInstOperand(this, (LLInstruction) insts.get(0));
	}

	/**
	 * @return the inst
	 */
	public HLInstruction getInst() {
		return inst;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.BaseOperand#replaceOperand(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		
		AssemblerOperand op1 = inst.getOp1() != null ? 
				inst.getOp1().replaceOperand(src, dst) : null;
		AssemblerOperand op2 = inst.getOp2() != null ?
				inst.getOp2().replaceOperand(src, dst) : null;
		AssemblerOperand op3 = inst.getOp3() != null ?
				inst.getOp3().replaceOperand(src, dst) : null;
		if (op1 != inst.getOp1() || op2 != inst.getOp2() || op3 != inst.getOp3()) {
			HLInstruction newInst = new HLInstruction(inst.getInstructionFactory());
			newInst.setInst(inst.getInst());
			newInst.setOp1(op1);
			newInst.setOp2(op2);
			newInst.setOp3(op3);
			return new InstOperand(newInst);
		}
		return this;
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
		throw new IllegalArgumentException();
	}
}
