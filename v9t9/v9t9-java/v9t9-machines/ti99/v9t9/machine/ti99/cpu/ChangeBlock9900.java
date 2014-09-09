/**
 * 
 */
package v9t9.machine.ti99.cpu;

import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.MachineOperandState;
import v9t9.machine.ti99.cpu.Changes.AdvancePC;
import v9t9.machine.ti99.cpu.Changes.CalculateCruOffset;
import v9t9.machine.ti99.cpu.Changes.CalculateShift;
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
	/** decoded instruction */
	public final Instruction9900 inst;
	
	/** if not <code>null</code>, this is what the X instruction decoded */
	public Instruction9900 xInst;

    public final ICpu cpu;
    public final CpuState9900 cpuState;

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
		
		inst = new Instruction9900(InstTable9900.decodeInstruction(
				op, 
				pc, cpu.getConsole(), false), cpu.getConsole());
	}
	public void generate() {
		appendOperandFetch();
		appendInstructionExecute();
		appendOperandFlush();
	}
	/**
	 * 
	 */
	public void appendInstructionAdvance() {
		push(new AdvancePC(inst.getSize()));
	}

	/** Fetch the current instruction operands */
	public void appendOperandFetch() {
		
//		if (inst.getInst() == Inst9900.Ix) {
//    		ins.pc = this.inst.pc + this.inst.getSize() - 2;
//    	}
        
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
    		state.cycles += 0;
    		push(new ReadRegister(state));
    		break;
    		
    	case MachineOperand9900.OP_IND: {	// *Rx
    		state.cycles += 4;
    		
    		push(new ReadIndirectRegister(state));
    		break;
    	}
    	case MachineOperand9900.OP_INC:	{ // *Rx+
    		state.cycles += 4;
    		state.cycles += mop.byteop ? 2 : 4;
    		push(new ReadIncrementRegister(state));
    		break;
    	}
    	case MachineOperand9900.OP_ADDR: {	// @>xxxx or @>xxxx(Rx)
    		state.cycles += 8;
    		push(new ReadRegisterOffset(state));
            break;
    	}
    	case MachineOperand9900.OP_IMMED:	// immediate
    		state.cycles += 0;
    		state.value = mop.immed;
    		break;
    	case MachineOperand9900.OP_CNT:	// shift count
    		state.cycles += 0;
    		state.value = (short) mop.val;
    		break;
    	case MachineOperand9900.OP_OFFS_R12:	// offset from R12
    		state.cycles += 0;
    		push(new CalculateCruOffset(state));
    		break;
    	case MachineOperand9900.OP_REG0_SHIFT_COUNT: // shift count from R0
    	    state.cycles += 8;
    	    push(new CalculateShift(state));
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
		Interpreter9900.appendInterpret((Cpu9900) cpu, this, inst, mopState1, mopState2, mopState3);
	}
	
	/** Add the change element for flushing operands */
	public void appendOperandFlush() {
        if (mopState1 != null && ((MachineOperand9900) mopState1.mop).dest != IOperand.OP_DEST_FALSE) {
        	push(new Changes.WriteResult(mopState1));
        }
        if (mopState2 != null && ((MachineOperand9900) mopState2.mop).dest != IOperand.OP_DEST_FALSE) {
        	push(new Changes.WriteResult(mopState2));
        }
        if (mopState3 != null && ((MachineOperand9900) mopState3.mop).dest != IOperand.OP_DEST_FALSE) {
        	push(new Changes.WriteResult(mopState3));
        }

//        if ((inst.getInfo().writes & InstInfo.INST_RSRC_ST) != 0) {
//			//cpu.getState().setStatus(status);
//        	cpu.getState().setStatus(status);
//		}

        /* do this after flushing status */
//        if ((inst.getInfo().writes & InstInfo.INST_RSRC_CTX) != 0) {
//            /* update PC first */
//            cpu.setPC((short) (iblock.inst.pc + iblock.inst.getSize()));
//            cpu.contextSwitch(iblock.wp, iblock.pc);
//        } else {
//            /* flush register changes */
//            cpu.setPC(iblock.pc);
//            if ((ins.getInfo().writes & InstInfo.INST_RSRC_WP) != 0) {
//				((Cpu9900) cpu).setWP(iblock.wp);
//			}
//        }
	}

}
