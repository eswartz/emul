/*
  ChangeBlock9900.java

  (c) 2014-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.cpu;

import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.CycleCounts;
import v9t9.common.cpu.IChangeElement;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.MachineOperandState;
import v9t9.machine.ti99.cpu.Changes.AdvancePC;
import v9t9.machine.ti99.cpu.Changes.BaseOperandChangeElement;
import v9t9.machine.ti99.cpu.Changes.CalculateCruOffset;
import v9t9.machine.ti99.cpu.Changes.CalculateShift;
import v9t9.machine.ti99.cpu.Changes.JumpPC;
import v9t9.machine.ti99.cpu.Changes.ReadIncrementRegister;
import v9t9.machine.ti99.cpu.Changes.ReadIndirectRegister;
import v9t9.machine.ti99.cpu.Changes.ReadRegister;
import v9t9.machine.ti99.cpu.Changes.ReadRegisterOffset;
import v9t9.machine.ti99.interpreter.Interpreter9900;

/**
 * 9900-specific change block
 * @author ejs
 *
 */
public class ChangeBlock9900 extends ChangeBlock {
	/** if not <code>null</code>, this is what the X instruction decoded */
	public Instruction9900 xInst;

    public final ICpu cpu;
    public final CpuState9900 cpuState;		// points to CPU
    public final CycleCounts counts;		// points to CPU

	private MachineOperandState mopState1;
	private MachineOperandState mopState2;
	private MachineOperandState mopState3;

//	public int cyclesAtStart;

	// ChangeBlocks are cached, so remember this in case we #generate only once
	public int instrFetchCycles;

	public ChangeBlock9900(ICpu cpu) {
		this(cpu, cpu.getState().getPC());
	}
	public ChangeBlock9900(ICpu cpu, int pc) {
		
		this.cpu = cpu;
		this.cpuState = (CpuState9900) cpu.getState();
		this.counts = cpu.getCycleCounts();
		
		counts.saveState();
		counts.getAndResetTotal();
		inst = new Instruction9900(InstTable9900.decodeInstruction(
				cpu.getConsole().readWord(pc), 
				pc, cpu.getConsole()), cpu.getConsole());
		instrFetchCycles = counts.getFetch() + counts.getLoad();
		assert counts.getExecute() == 0;
		assert counts.getStore() == 0;
		assert counts.getOverhead() == 0;
		counts.restoreState();
		
		appendInstructionAdvance();
	}
	public ChangeBlock9900(ICpuState cpuState, int pc) {
		
		this.cpu = null;
		this.cpuState = (CpuState9900) cpuState;
		this.counts = cpuState.getCycleCounts();
		
		counts.saveState();
		counts.getAndResetTotal();
		inst = new Instruction9900(InstTable9900.decodeInstruction(
				cpuState.getConsole().readWord(pc), 
				pc, cpuState.getConsole()), cpuState.getConsole());
		instrFetchCycles = counts.getFetch() + counts.getLoad();
		assert counts.getExecute() == 0;
		assert counts.getStore() == 0;
		assert counts.getOverhead() == 0;
		counts.restoreState();
		
		appendInstructionAdvance();
	}
	
	/** 
	 * This variant is for X only -- it assumes 'op' is already read and does not
	 * add its cycles to the overhead 
	 */
	public ChangeBlock9900(ICpu cpu, int pc, short op) {
		this.cpu = cpu;
		this.cpuState = (CpuState9900) cpu.getState();
		this.counts = cpuState.getCycleCounts();
		
		counts.saveState();
		counts.getAndResetTotal();
		inst = new Instruction9900(InstTable9900.decodeInstruction(
				op, 
				pc, cpuState.getConsole()), cpuState.getConsole());
		instrFetchCycles = counts.getFetch() + counts.getLoad();
		assert counts.getExecute() == 0;
		assert counts.getStore() == 0;
		assert counts.getOverhead() == 0;
		counts.restoreState();
	}
	
//	/* (non-Javadoc)
//	 * @see v9t9.common.cpu.ChangeBlock#clone()
//	 */
//	@Override
//	public ChangeBlock clone() {
//		ChangeBlock9900 c = (ChangeBlock9900) super.clone();
//		c.mopState1 = mopState1 != null ? c.mopState1.clone() : null;
//		c.mopState2 = mopState2 != null ? c.mopState2.clone() : null;
//		c.mopState3 = mopState3 != null ? c.mopState3.clone() : null;
//		return c;
//	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ChangeBlock#getPC()
	 */
	@Override
	public int getPC() {
		return inst.pc & 0xffff;
	}
	
	public void generate() {
		appendOperandFetch();
		appendInstructionExecute();
		appendFlush();
	}
	/**
	 * 
	 */
	public void appendInstructionAdvance() {
		push(new AdvancePC(this, inst.getSize()));
	}

	/** Fetch the current instruction operands */
	public void appendOperandFetch() {
        MachineOperand9900 mop1 = (MachineOperand9900) this.inst.getOp1();
		MachineOperand9900 mop2 = (MachineOperand9900) this.inst.getOp2();
		if (mop1 != null && mop1.type != IMachineOperand.OP_NONE) {
			mopState1 = nextMachineOperandState(mop1);
			fetch(mop1, mopState1);
			if (mop2 != null && mop2.type != IMachineOperand.OP_NONE) {
				mopState2 = nextMachineOperandState(mop2);
				fetch(mop2, mopState2);
				if (inst.getInst() == Inst9900.Impy) {
					MachineOperand9900 mop3 = new MachineOperand9900(MachineOperand9900.OP_REG);
					mop3.val = mop2.val + 1;
					mop3.dest = MachineOperand9900.OP_DEST_KILLED;
					mopState3 = nextMachineOperandState(mop3);
					fetch(mop3, mopState3);
				}
				else if (inst.getInst() == Inst9900.Idiv) {
					MachineOperand9900 mop3 = new MachineOperand9900(MachineOperand9900.OP_REG);
					mop3.val = mop2.val + 1;
					mop3.dest = MachineOperand9900.OP_DEST_TRUE;
					mopState3 = nextMachineOperandState(mop3);
					fetch(mop3, mopState3);
				}
			} else {
				if (inst.getInst() == InstTableCommon.Iticks) {
					mop2 = new MachineOperand9900(MachineOperand9900.OP_REG);
					mop2.val = mop1.val + 1;
					mop2.dest = MachineOperand9900.OP_DEST_KILLED;
					mopState2 = nextMachineOperandState(mop2);
					fetch(mop2, mopState2);
				}
			}
		}
	}


	private MachineOperandState nextMachineOperandState(MachineOperand9900 mop) {
		return new MachineOperandState(mop);
	}

	/**
	 * Fetch the operand, pushing any change elements
	 * @param mop
	 * @param state 
	 */
	private void fetch(MachineOperand9900 mop, MachineOperandState state) {
    	switch (mop.type) {
    	case IMachineOperand.OP_NONE:
    		return;
    		
    	case MachineOperand9900.OP_REG:	// Rx
    		push(new ReadRegister(this, state));
    		break;
    		
    	case MachineOperand9900.OP_IND: {	// *Rx
    		push(new ReadIndirectRegister(this, state));
    		break;
    	}
    	case MachineOperand9900.OP_INC:	{ // *Rx+
    		push(new ReadIncrementRegister(this, state));
    		break;
    	}
    	case MachineOperand9900.OP_ADDR: {	// @>xxxx or @>xxxx(Rx)
    		push(new ReadRegisterOffset(this, state));
            break;
    	}
    	case MachineOperand9900.OP_IMMED:	// immediate
    		//counts.addFetch(0);
    		state.value = mop.immed;
    		break;
    	case MachineOperand9900.OP_CNT:	// shift count
    		//counts.addFetch(0);
    		state.value = (short) mop.val;
    		break;
    	case MachineOperand9900.OP_OFFS_R12:	// offset from R12
    		//counts.addFetch(0);
    		push(new CalculateCruOffset(this, state));
    		break;
    	case MachineOperand9900.OP_REG0_SHIFT_COUNT: // shift count from R0
    	    push(new CalculateShift(this, state));
		    break;
    	
    	case MachineOperand9900.OP_JUMP:	// jump target
    		state.value = (short) mop.val;
    		break;
    	case MachineOperand9900.OP_STATUS:	// status word
    		break;
    	case MachineOperand9900.OP_INST:
    		break;		
    	}
	}

	/** Add the change element for the interpretation */
	public void appendInstructionExecute() {
		Interpreter9900.appendInterpret((Cpu9900) cpu, this, 
				(Instruction9900) inst, mopState1, mopState2, mopState3);
	}
	
	/** Add the change element for flushing operands */
	public void appendFlush() {
        if (mopState1 != null && ((MachineOperand9900) mopState1.mop).dest != IOperand.OP_DEST_FALSE) {
        	push(new Changes.WriteResult(this, mopState1));
        }
        if (mopState2 != null && ((MachineOperand9900) mopState2.mop).dest != IOperand.OP_DEST_FALSE) {
        	push(new Changes.WriteResult(this, mopState2));
        }
        if (mopState3 != null && ((MachineOperand9900) mopState3.mop).dest != IOperand.OP_DEST_FALSE) {
        	push(new Changes.WriteResult(this, mopState3));
        }

    	push(new Changes.Flush(this));
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.ChangeBlock#apply(v9t9.common.cpu.ICpu)
	 */
	@Override
	public void apply(ICpu cpu) {
		counts.addFetch(instrFetchCycles);
		
		super.apply(cpu);
	}
	/**
	 * @param mop
	 * @return
	 */
	public short getEA(IMachineOperand mop) {
		for (int i = 0; i < getCount(); i++) {
			IChangeElement el = getElement(i);
			if (el instanceof Changes.AdvancePC) {
				AdvancePC oel = (AdvancePC) el;
				return (short) (oel.value + inst.pc - inst.getSize());
			}
			if (el instanceof Changes.JumpPC) {
				JumpPC oel = (JumpPC) el;
				return (short) (oel.value + inst.pc - inst.getSize());
			}
			if (el instanceof BaseOperandChangeElement) {
				BaseOperandChangeElement oel = (BaseOperandChangeElement) el;
				if (oel.state.mop == mop) {
					return oel.state.ea;
				}
			}
		}
		return 0;
	}
}
