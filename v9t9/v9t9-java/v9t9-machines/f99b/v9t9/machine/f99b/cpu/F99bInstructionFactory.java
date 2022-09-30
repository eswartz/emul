/*
  F99bInstructionFactory.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.cpu;

import static v9t9.machine.f99b.asm.InstF99b.I0branchB;
import static v9t9.machine.f99b.asm.InstF99b.I0branchW;
import static v9t9.machine.f99b.asm.InstF99b.I0branchX;
import static v9t9.machine.f99b.asm.InstF99b.IbranchB;
import static v9t9.machine.f99b.asm.InstF99b.IbranchW;
import static v9t9.machine.f99b.asm.InstF99b.IbranchX;
import static v9t9.machine.f99b.asm.InstF99b.Icall;
import static v9t9.machine.f99b.asm.InstF99b.IcontextFrom;
import static v9t9.machine.f99b.asm.InstF99b.Idouble;
import static v9t9.machine.f99b.asm.InstF99b.Iext;
import static v9t9.machine.f99b.asm.InstF99b.Ilalloc;
import static v9t9.machine.f99b.asm.InstF99b.IlitB;
import static v9t9.machine.f99b.asm.InstF99b.IlitB_d;
import static v9t9.machine.f99b.asm.InstF99b.IlitD_d;
import static v9t9.machine.f99b.asm.InstF99b.IlitW;
import static v9t9.machine.f99b.asm.InstF99b.IlitX;
import static v9t9.machine.f99b.asm.InstF99b.IlitX_d;
import static v9t9.machine.f99b.asm.InstF99b.Ilocal;
import static v9t9.machine.f99b.asm.InstF99b.Ilpidx;
import static v9t9.machine.f99b.asm.InstF99b.Irpidx;
import static v9t9.machine.f99b.asm.InstF99b.Ispidx;
import static v9t9.machine.f99b.asm.InstF99b.Isyscall;
import static v9t9.machine.f99b.asm.InstF99b.ItoContext;
import static v9t9.machine.f99b.asm.InstF99b.Iupidx;
import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.asm.RawInstruction;
import v9t9.common.memory.IMemoryDomain;
import v9t9.machine.f99b.asm.InstructionF99b;
import v9t9.machine.f99b.asm.MachineOperandF99b;

/**
 * @author ejs
 *
 */
public class F99bInstructionFactory implements IRawInstructionFactory {
	public static final F99bInstructionFactory INSTANCE = new F99bInstructionFactory();
	
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

	/* (non-Javadoc)
	 * @see v9t9.common.asm.IRawInstructionFactory#getChunkSize()
	 */
	@Override
	public int getChunkSize() {
		return 1;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.asm.IRawInstructionFactory#getMaxInstrLength()
	 */
	@Override
	public int getMaxInstrLength() {
		return 5;	// LIT.D
	}
}
