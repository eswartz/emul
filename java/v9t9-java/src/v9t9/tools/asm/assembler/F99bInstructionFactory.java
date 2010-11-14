/**
 * 
 */
package v9t9.tools.asm.assembler;

import static v9t9.engine.cpu.InstF99b.*;
import v9t9.engine.cpu.InstructionF99b;
import v9t9.engine.cpu.MachineOperandF99b;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author ejs
 *
 */
public class F99bInstructionFactory implements IInstructionFactory {

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#createRawInstruction(v9t9.tools.asm.assembler.LLInstruction)
	 */
	@Override
	public RawInstruction createRawInstruction(LLInstruction inst)
			throws ResolveException {
		// TODO Auto-generated method stub
		return null;
	}

	static class WorkBlock {
		public short pc;
		
		public MemoryDomain domain;
		
		
		public WorkBlock(short pc, MemoryDomain domain) {
			super();
			this.pc = pc;
			this.domain = domain;
		}
		/**
		 * @return
		 */
		public int nextByte() {
			return domain.readByte(pc++) & 0xff;
		}
		/**
		 * @return
		 */
		public int nextWord() {
			return (domain.readByte(pc++) << 8) | domain.readByte(pc++) & 0xff;
		}
	}
		
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#decodeInstruction(int, v9t9.engine.memory.MemoryDomain)
	 */
	@Override
	public RawInstruction decodeInstruction(int pc, MemoryDomain memory) {
		short thisPc = (short) pc;
		
		WorkBlock iblock = new WorkBlock((short) pc, memory);
		
		short opword = (short) memory.readByte(thisPc);
		if (opword < 0) {
			opword = (short) ((opword << 8) | (memory.readByte(thisPc + 1) & 0xff));
			iblock.pc += 2;
			// call
			return getCallInstruction(pc, opword);
		}
		
		iblock.pc = (short) (pc + 1);
		
		if (opword == Iext || opword == Idouble) {
			opword = (short) (((opword << 8) | (memory.readByte(thisPc + 1) & 0xff)) & 0xffff);
			++iblock.pc;
		}
		
		InstructionF99b inst;
		
		inst = getInstruction(thisPc, opword, iblock);
		
		return inst;
	}

	private InstructionF99b getInstruction(short origPC, short opword, WorkBlock iblock) {
		int opcode = opword & 0xffff;
		
		InstructionF99b inst = new InstructionF99b();
		inst.pc = origPC;
		
		inst.opcode = opcode;
		inst.setInst(opcode);

		if (opcode >= IbranchX && opcode < I0branchX + 16) {
			inst.setInst(opcode & 0xf0);
			int val = (byte)(opcode<<4) >> 4;
			if (val < 0)
				val --;
			inst.setOp1(MachineOperandF99b.createImmediateOperand(
					val, 
					MachineOperandF99b.OP_ENC_IMM4));
		}
		else if (opcode >= IlitX && opcode < IlitX + 16) {
			inst.setInst(IlitX);
			inst.setOp1(MachineOperandF99b.createImmediateOperand(
					(byte)(opcode<<4) >> 4,
					MachineOperandF99b.OP_ENC_IMM4));
		}
		else if (opcode >= IlitX_d && opcode < IlitX_d + 16) {
			inst.setInst(IlitX_d);
			inst.setOp1(MachineOperandF99b.createImmediateOperand(
					(byte)(opcode<<4) >> 4,
							MachineOperandF99b.OP_ENC_IMM4));
		}
		else
			switch (opcode) {
			case IbranchB:
			case I0branchB: {
				int val = (byte) iblock.nextByte();
				if (val < 0)
					val -= 2;
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						val, MachineOperandF99b.OP_ENC_IMM8));
				break;
			}
			case IlitB:
			case IlitB_d:
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						(byte) iblock.nextByte(), MachineOperandF99b.OP_ENC_IMM8));
				break;
			case Isyscall:
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						(byte) iblock.nextByte(), MachineOperandF99b.OP_ENC_SYSCALL));
				break;
			case ItoContext:
			case IcontextFrom:
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						iblock.nextByte() & 0xff, MachineOperandF99b.OP_ENC_CTX));
				break;
			case Irpidx:
			case Ispidx:
			case Iupidx:
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						iblock.nextByte() & 0xff, MachineOperandF99b.OP_ENC_IMM8));
				break;
			case IlitW:
			case I0branchW:
			case IbranchW:  {
				int val = (short) iblock.nextWord();
				if (val < 0)
					val -= inst.getSize();
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						val, MachineOperandF99b.OP_ENC_IMM16));
				break;
			}
			case IlitD_d: {
				int lo = iblock.nextWord() & 0xffff;
				int hi = iblock.nextWord() & 0xffff;
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						lo | (hi << 16),
						MachineOperandF99b.OP_ENC_IMM32));
				break;
			}
			default:
				// no immediate	
			}
		
		int sz = iblock.pc - inst.pc;
		if (sz < 0)
			sz += 65536;
		inst.setSize(sz);
		
		return inst;
	}

	private InstructionF99b getCallInstruction(int pc, short op) {
		InstructionF99b inst = new InstructionF99b();
		inst.pc = pc;
		inst.opcode = op;
		inst.setOp1(MachineOperandF99b.createImmediateOperand((short) (op << 1), MachineOperandF99b.OP_ENC_IMM15S1));
		inst.setInst(Icall);
		inst.setSize(2);
		return inst;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#encodeInstruction(v9t9.engine.cpu.RawInstruction)
	 */
	@Override
	public byte[] encodeInstruction(RawInstruction instruction) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#getInstName(int)
	 */
	@Override
	public String getInstName(int inst) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#getInstSize(v9t9.tools.asm.assembler.LLInstruction)
	 */
	@Override
	public int getInstSize(LLInstruction ins) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#isByteInst(int)
	 */
	@Override
	public boolean isByteInst(int inst) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#isJumpInst(int)
	 */
	@Override
	public boolean isJumpInst(int inst) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#supportsOp(int, int, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public boolean supportsOp(int inst, int num, AssemblerOperand op) {
		// TODO Auto-generated method stub
		return false;
	}

}
