/**
 * 
 */
package v9t9.tools.asm;

import static v9t9.engine.cpu.InstructionTable.Iab;
import static v9t9.engine.cpu.InstructionTable.Icb;
import static v9t9.engine.cpu.InstructionTable.Imovb;
import static v9t9.engine.cpu.InstructionTable.Isb;
import static v9t9.engine.cpu.InstructionTable.Isocb;
import static v9t9.engine.cpu.InstructionTable.Iszcb;
import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.InstEncodePattern;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.RawInstruction;
import v9t9.tools.asm.operand.ll.LLEmptyOperand;
import v9t9.tools.asm.operand.ll.LLOperand;

/**
 * @author Ed
 *
 */
public class LLInstruction extends BaseAssemblerInstruction {
	public LLInstruction() {
		super();
	}

	public LLInstruction(LLInstruction instruction) {
		this.setInst(instruction.getInst());
		this.setOp1(instruction.getOp1());
		this.setOp2(instruction.getOp2());
	}

	public LLInstruction(int inst, LLOperand lop1, LLOperand lop2) {
		setInst(inst);
		setOp1(lop1);
		setOp2(lop2);
	}

	private int inst;
	private LLOperand op1;
	private LLOperand op2;
	private int size;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LLInst ");
		builder.append(InstructionTable.getInstName(inst));
		if (op1 != null && !(op1 instanceof LLEmptyOperand)) {
			builder.append(' ');
			builder.append(op1);
			if (op2 != null && !(op2 instanceof LLEmptyOperand)) {
				builder.append(',');
				builder.append(op2);
			}
		}
		return builder.toString();
	}

	
	public IInstruction[] resolve(Assembler assembler, IInstruction previous,
			boolean finalPass) throws ResolveException {
		setPc(assembler.getPc());
		setOp1(op1 != null ? op1.resolve(assembler, this) : null);
		setOp2(op2 != null ? op2.resolve(assembler, this) : null);
		assembler.setPc(getPc() + getSize());
		return new IInstruction[] { this };
	}
	
	private int calculateInstructionSize() {
		int size = 0;
    	if (getInst() == InstructionTable.Idata) {
    		size = 2;
    		return size;
    	} else if (getInst() == InstructionTable.Ibyte) {
    		size = 1;
    		return size;
    	}
    	size = 2;
    	InstEncodePattern pattern = InstructionTable.lookupEncodePattern(getInst());
		if (pattern == null)
			return size;
		
		if (op1 != null)
			size += coerceSize(pattern.op1, getOp1().getSize());
		if (op2 != null)
			size += coerceSize(pattern.op2, getOp2().getSize());
		return size;
	}

	private int coerceSize(int type, int size) {
		if (size > 0) {
			if (type == InstEncodePattern.CNT || type == InstEncodePattern.OFF)
				size = 0;
		}
		return size;
	}

	public int getSize() {
		if (size == 0)
			size = calculateInstructionSize();
		return size;
	}

	public byte[] getBytes() throws ResolveException {
		short[] words = InstructionTable.encode(createRawInstruction());
		byte[] bytes = new byte[words.length * 2];
		for (int idx = 0; idx < words.length; idx++) {
			bytes[idx*2] = (byte) (words[idx] >> 8);
			bytes[idx*2+1] = (byte) (words[idx] & 0xff);
		}
		return bytes;
	}

	public RawInstruction createRawInstruction() throws ResolveException {
		RawInstruction rawInst = new RawInstruction();
		rawInst.pc = pc;
		rawInst.inst = getInst();
		rawInst.op1 = getOp1() != null ? getOp1().createMachineOperand() : MachineOperand.createEmptyOperand();
		rawInst.op2 = getOp2() != null ? getOp2().createMachineOperand() : MachineOperand.createEmptyOperand();
		InstructionTable.calculateInstructionSize(rawInst);
		InstructionTable.coerceOperandTypes(rawInst);
		return rawInst;
	}

	public boolean isJumpInst() {
		return getInst() >= InstructionTable.Ijmp && getInst() <= InstructionTable.Ijop;
	}

	public void setOp1(LLOperand op1) {
		this.op1 = op1;
		size = 0;
	}

	public LLOperand getOp1() {
		return op1;
	}

	public void setOp2(LLOperand op2) {
		this.op2 = op2;
		size = 0;
	}

	public LLOperand getOp2() {
		return op2;
	}

	public void setInst(int inst) {
		this.inst = inst;
		size = 0;
	}

	public int getInst() {
		return inst;
	}
	
	public boolean isByteOp() {
		return inst == Isocb || inst == Icb || inst == Iab 
		|| inst == Isb || inst == Iszcb || inst == Imovb;
	}

}
