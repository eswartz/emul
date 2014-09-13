/**
 * 
 */
package v9t9.machine.ti99.cpu;

import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.CycleCounts;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.MachineOperandState;
import v9t9.machine.ti99.cpu.Changes.AdvancePC;
import v9t9.machine.ti99.cpu.Changes.CalculateCruOffset;
import v9t9.machine.ti99.cpu.Changes.CalculateShift;
import v9t9.machine.ti99.cpu.Changes.ReadIncrementRegister;
import v9t9.machine.ti99.cpu.Changes.ReadIndirectRegister;
import v9t9.machine.ti99.cpu.Changes.ReadRegister;
import v9t9.machine.ti99.cpu.Changes.ReadRegisterOffset;
import v9t9.machine.ti99.interpreter.NewInterpreter9900;

/**
 * 9900-specific change block
 * @author ejs
 *
 */
public class ChangeBlock9900 extends ChangeBlock {
	/** decoded instruction */
	public final Instruction9900 inst;
	
	/** if not <code>null</code>, this is what the X instruction decoded */
	public Instruction9900 xInst;

    public final ICpu cpu;
    public final CpuState9900 cpuState;
    public final CycleCounts counts;

	private MachineOperandState mopState1;
	private MachineOperandState mopState2;
	private MachineOperandState mopState3;

	public ChangeBlock9900(ICpu cpu) {
		this(cpu, cpu.getState().getPC());
	}
	public ChangeBlock9900(ICpu cpu, int pc) {
		this(cpu, pc, cpu.getConsole().flatReadWord(pc));
		appendInstructionAdvance();
	}
	public ChangeBlock9900(ICpu cpu, int pc, short op) {
		this.cpu = cpu;
		cpuState = (CpuState9900) cpu.getState();
		this.counts = new CycleCounts();
		
		inst = new Instruction9900(InstTable9900.decodeInstruction(
				op, 
				pc, cpu.getConsole(), false), cpu.getConsole());
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
		push(new AdvancePC(this, inst.getSize(), 0));
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
				if (inst.getInst() == Inst9900.Impy || inst.getInst() == InstTableCommon.Iticks) {
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
    		counts.addFetch(0);
    		push(new ReadRegister(this, state));
    		break;
    		
    	case MachineOperand9900.OP_IND: {	// *Rx
    		counts.addFetch(4);
    		
    		push(new ReadIndirectRegister(this, state));
    		break;
    	}
    	case MachineOperand9900.OP_INC:	{ // *Rx+
    		counts.addFetch(4);
    		counts.addFetch(mop.byteop ? 2 : 4);
    		push(new ReadIncrementRegister(this, state));
    		break;
    	}
    	case MachineOperand9900.OP_ADDR: {	// @>xxxx or @>xxxx(Rx)
    		counts.addFetch(8);
    		push(new ReadRegisterOffset(this, state));
            break;
    	}
    	case MachineOperand9900.OP_IMMED:	// immediate
    		counts.addFetch(0);
    		state.value = mop.immed;
    		break;
    	case MachineOperand9900.OP_CNT:	// shift count
    		counts.addFetch(0);
    		state.value = (short) mop.val;
    		break;
    	case MachineOperand9900.OP_OFFS_R12:	// offset from R12
    		counts.addFetch(0);
    		push(new CalculateCruOffset(this, state));
    		break;
    	case MachineOperand9900.OP_REG0_SHIFT_COUNT: // shift count from R0
    		counts.addFetch(8);
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
		NewInterpreter9900.appendInterpret((Cpu9900) cpu, this, inst, mopState1, mopState2, mopState3);
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

    	push(new Changes.AddCycles(this));
	}

}
