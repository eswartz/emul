/**
 * 
 */
package v9t9.machine.f99b.cpu;

import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.CycleCounts;
import v9t9.common.cpu.IChangeElement;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.MachineOperandState;
import v9t9.machine.f99b.asm.InstF99b;
import v9t9.machine.f99b.asm.InstructionF99b;
import v9t9.machine.f99b.asm.MachineOperandF99b;
import v9t9.machine.f99b.cpu.Changes.AdvancePC;
import v9t9.machine.f99b.cpu.Changes.JumpPC;
import v9t9.machine.f99b.interpreter.InterpreterF99b;

/**
 * @author ejs
 *
 */
public class ChangeBlockF99b extends ChangeBlock {

	/** if not <code>null</code>, this is what the X instruction decoded */
	public InstructionF99b xInst;

    public final ICpu cpu;
    public final CpuStateF99b cpuState;		// points to CPU
    public final CycleCounts counts;		// points to CPU

	private MachineOperandState mopState1;
	private MachineOperandState mopState2;
	private MachineOperandState mopState3;

//	public int cyclesAtStart;

	// ChangeBlocks are cached, so remember this in case we #generate only once
	public int instrFetchCycles;

	public ChangeBlockF99b(ICpu cpu) {
		this(cpu, cpu.getState().getPC());
	}
	public ChangeBlockF99b(ICpu cpu, int pc) {
		
		this.cpu = cpu;
		this.cpuState = (CpuStateF99b) cpu.getState();
		this.counts = cpu.getCycleCounts();
		
		counts.saveState();
		counts.getAndResetTotal();
		inst = cpu.getInstructionFactory().decodeInstruction(
				pc, cpu.getConsole());
		instrFetchCycles = counts.getFetch() + counts.getLoad();
		assert counts.getExecute() == 0;
		assert counts.getStore() == 0;
		assert counts.getOverhead() == 0;
		counts.restoreState();
		
		appendInstructionAdvance();
	}
	public ChangeBlockF99b(ICpuState cpuState, int pc) {
		
		this.cpu = null;
		this.cpuState = (CpuStateF99b) cpuState;
		this.counts = cpuState.getCycleCounts();
		
		counts.saveState();
		counts.getAndResetTotal();
		inst = cpu.getInstructionFactory().decodeInstruction(
				pc, cpuState.getConsole());
		instrFetchCycles = counts.getFetch() + counts.getLoad();
		assert counts.getExecute() == 0;
		assert counts.getStore() == 0;
		assert counts.getOverhead() == 0;
		counts.restoreState();
		
		appendInstructionAdvance();
	}
	
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
        MachineOperandF99b mop1 = (MachineOperandF99b) this.inst.getOp1();
		MachineOperandF99b mop2 = (MachineOperandF99b) this.inst.getOp2();
		if (mop1 != null && mop1.type != IMachineOperand.OP_NONE) {
			mopState1 = nextMachineOperandState(mop1);
			fetch(mop1, mopState1);
			if (mop2 != null && mop2.type != IMachineOperand.OP_NONE) {
				mopState2 = nextMachineOperandState(mop2);
				fetch(mop2, mopState2);
				if (inst.getInst() == InstF99b.Impy) {
					MachineOperandF99b mop3 = new MachineOperandF99b(MachineOperandF99b.OP_REG);
					mop3.val = mop2.val + 1;
					mop3.dest = MachineOperandF99b.OP_DEST_KILLED;
					mopState3 = nextMachineOperandState(mop3);
					fetch(mop3, mopState3);
				}
				else if (inst.getInst() == InstF99b.Idiv) {
					MachineOperandF99b mop3 = new MachineOperandF99b(MachineOperandF99b.OP_REG);
					mop3.val = mop2.val + 1;
					mop3.dest = MachineOperandF99b.OP_DEST_TRUE;
					mopState3 = nextMachineOperandState(mop3);
					fetch(mop3, mopState3);
				}
				else if (inst.getInst() == InstTableCommon.Iticks) {
					mop2 = new MachineOperandF99b(MachineOperandF99b.OP_REG);
					mop2.val = mop1.val + 1;
					mop2.dest = MachineOperandF99b.OP_DEST_KILLED;
					mopState2 = nextMachineOperandState(mop2);
					fetch(mop2, mopState2);
				}

			}
		}
	}


	private MachineOperandState nextMachineOperandState(MachineOperandF99b mop) {
		return new MachineOperandState(mop);
	}

	/**
	 * Fetch the operand, pushing any change elements
	 * @param mop
	 * @param state 
	 */
	private void fetch(MachineOperandF99b mop, MachineOperandState state) {
    	switch (mop.type) {
    	case IMachineOperand.OP_NONE:
    		return;
    		
    	case MachineOperandF99b.OP_REG:	// Rx
    		push(new ReadRegister(this, state));
    		break;
    		
    	case MachineOperandF99b.OP_IND: {	// *Rx
    		push(new ReadIndirectRegister(this, state));
    		break;
    	}
    	case MachineOperandF99b.OP_INC:	{ // *Rx+
    		push(new ReadIncrementRegister(this, state));
    		break;
    	}
    	case MachineOperandF99b.OP_ADDR: {	// @>xxxx or @>xxxx(Rx)
    		push(new ReadRegisterOffset(this, state));
            break;
    	}
    	case MachineOperandF99b.OP_IMMED:	// immediate
    		//counts.addFetch(0);
    		state.value = mop.immed;
    		break;
    	case MachineOperandF99b.OP_CNT:	// shift count
    		//counts.addFetch(0);
    		state.value = (short) mop.val;
    		break;
    	case MachineOperandF99b.OP_OFFS_R12:	// offset from R12
    		//counts.addFetch(0);
    		push(new CalculateCruOffset(this, state));
    		break;
    	case MachineOperandF99b.OP_REG0_SHIFT_COUNT: // shift count from R0
    	    push(new CalculateShift(this, state));
		    break;
    	
    	case MachineOperandF99b.OP_JUMP:	// jump target
    		state.value = (short) mop.val;
    		break;
    	case MachineOperandF99b.OP_STATUS:	// status word
    		break;
    	case MachineOperandF99b.OP_INST:
    		break;		
    	}
	}

	/** Add the change element for the interpretation */
	public void appendInstructionExecute() {
		InterpreterF99b.appendInterpret((CpuF99b) cpu, this, 
				(InstructionF99b) inst, mopState1, mopState2, mopState3);
	}
	
	/** Add the change element for flushing operands */
	public void appendFlush() {
        if (mopState1 != null && ((MachineOperandF99b) mopState1.mop).dest != IOperand.OP_DEST_FALSE) {
        	push(new Changes.WriteResult(this, mopState1));
        }
        if (mopState2 != null && ((MachineOperandF99b) mopState2.mop).dest != IOperand.OP_DEST_FALSE) {
        	push(new Changes.WriteResult(this, mopState2));
        }
        if (mopState3 != null && ((MachineOperandF99b) mopState3.mop).dest != IOperand.OP_DEST_FALSE) {
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
		}
		return 0;
	}
}
