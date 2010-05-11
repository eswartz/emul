/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;


/**
 * @author Ed
 *
 */
public class HLInstruction extends AssemblerInstruction {

	public HLInstruction() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.AssemblerInstruction#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.IInstruction, boolean)
	 */
	

	@Override
	public byte[] getBytes() throws ResolveException {
		throw new ResolveException(this, null, "Cannot resolve high-level instruction");
	}
	
	public static HLInstruction create(int inst) {
		HLInstruction instr = new HLInstruction();
		instr.setInst(inst);
		return instr;
	}
	public static HLInstruction create(int inst, AssemblerOperand op1) {
		HLInstruction instr = new HLInstruction();
		instr.setInst(inst);
		instr.setOp1(op1);
		return instr;
	}
	public static HLInstruction create(int inst, AssemblerOperand op1, AssemblerOperand op2) {
		HLInstruction instr = new HLInstruction();
		instr.setInst(inst);
		instr.setOp1(op1);
		instr.setOp2(op2);
		return instr;
	}

	public static HLInstruction create(int inst, AssemblerOperand op1,
			AssemblerOperand op2, AssemblerOperand op3) {
		HLInstruction instr = new HLInstruction();
		instr.setInst(inst);
		instr.setOp1(op1);
		instr.setOp2(op2);
		instr.setOp3(op3);
		return instr;
	}

}
