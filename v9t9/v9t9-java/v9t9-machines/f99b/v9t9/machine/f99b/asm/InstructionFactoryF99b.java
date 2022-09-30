/*
  InstructionFactoryF99b.java

  (c) 2022 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.asm;

import static v9t9.common.asm.IHighLevelInstruction.*;
import static v9t9.machine.f99b.asm.InstF99b.*;

import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.IInstructionFactory;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.InstInfo;
import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ICpuState;
import v9t9.common.memory.IMemoryDomain;
import v9t9.machine.f99b.cpu.WorkBlock;
import v9t9.machine.ti99.asm.HighLevelCodeInfo9900;
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.machine.ti99.cpu.InstTable9900;
import v9t9.machine.ti99.cpu.Instruction9900;

/**
 * @author ejs
 *
 */
public class InstructionFactoryF99b implements IInstructionFactory {

	public static final IInstructionFactory INSTANCE = new InstructionFactoryF99b();

	/**
	 * 
	 */
	public InstructionFactoryF99b() {
		super();
	}

	@Override
	public byte[] encodeInstruction(RawInstruction instruction) {
		short[] words = InstTable9900.encode(instruction);
		byte[] bytes = new byte[words.length * 2];
		for (int idx = 0; idx < words.length; idx++) {
			bytes[idx*2] = (byte) (words[idx] >> 8);
			bytes[idx*2+1] = (byte) (words[idx] & 0xff);
		}
		return bytes;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#decodeInstruction(int, v9t9.engine.memory.MemoryDomain)
	 */
	@Override
	public RawInstruction decodeInstruction(int pc, IMemoryDomain memory) {
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
					val -= 2 - 1;
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
			case Ilpidx:
			case Ilocal:
			case Ilalloc:
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						iblock.nextByte() & 0xff, MachineOperandF99b.OP_ENC_IMM8));
				break;
			case I0branchW:
			case IbranchW:  {
				int val = (short) iblock.nextWord();
				if (val < 0)
					val -= 3 - 1;
				inst.setOp1(MachineOperandF99b.createImmediateOperand(
						val, MachineOperandF99b.OP_ENC_IMM16));
				break;
			}
			case IlitW: {
				int val = (short) iblock.nextWord();
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

	public boolean isByteInst(int inst) {
		return true;
	}

	@Override
	public boolean isJumpInst(int inst) {
		return inst == InstF99b.I0branchB ||
				inst == InstF99b.I0branchW ||
				inst == InstF99b.I0branchX ||
				inst == InstF99b.IbranchB ||
				inst == InstF99b.IbranchW ||
				inst == InstF99b.IbranchX ||
				inst == InstF99b.IloopUp ||
				inst == InstF99b.IloopUp_d ||
				inst == InstF99b.IplusLoopUp ||
				inst == InstF99b.IplusLoopUp_d;
	}

	@Override
	public String getInstName(int inst) {
		return InstF99b.getInstName(inst);
	}

	@Override
	public int getInstructionFlags(RawInstruction inst) {
		int flags = 0;
		int i = inst.getInst();
        if (inst.getInfo().jump != 0) {
        	flags |=  fEndsBlock;
            if (i == InstF99b.Icall) {
				flags |= fIsCall+fIsBranch;
			} else if (i == InstF99b.Iexit || i == InstF99b.Iexiti) {
				flags |= fIsReturn+fIsBranch+fNotFallThrough;
			} else if (isJumpInst(i)) {
				if (i == InstF99b.IbranchB ||
						i == InstF99b.IbranchW ||
						i == InstF99b.IbranchX) {
					flags |= fIsBranch;
				} else {
					flags |= fIsCondBranch+fIsBranch;
				}
			} else {
				flags |= fIsBranch+fNotFallThrough;
			}
        }
        return flags;
	}

	@Override
	public IDecompileInfo createDecompileInfo(ICpuState cpuState) {
		return new HighLevelCodeInfo9900(cpuState, this);
	}

}