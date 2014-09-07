/**
 * 
 */
package v9t9.machine.ti99.cpu;

import v9t9.common.asm.IMachineOperand;
import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.ICpu;
import v9t9.machine.ti99.cpu.OperandChanges9900.*;

/**
 * 9900-specific change block
 * @author ejs
 *
 */
public class ChangeBlock9900 extends ChangeBlock {
	/** decoded instruction */
	public Instruction9900 inst;
	
	/** if not <code>null</code>, this is what the X instruction decoded */
	public Instruction9900 xInst;

    /** original register values */
//    public short pc, wp, st;

    public final ICpu cpu;
    public final CpuState9900 cpuState;

	private int pc;

//	private Status9900 status;

	public ChangeBlock9900(ICpu cpu) {
		this(cpu, cpu.getState().getPC());
	}
	public ChangeBlock9900(ICpu cpu, int pc) {
		this.cpu = cpu;
		this.pc = pc;
		cpuState = (CpuState9900) cpu.getState();
//    	status = cpuState.createStatus();
	}


	/** Fetch the current instruction and operands */
	public void createOperandFetch() {
//		pc = (short) cpuState.getPC();
//		wp = (short) cpuState.getWP();
//		st = cpuState.getST();
//		status.expand(this.st);
		
		inst = new Instruction9900(InstTable9900.decodeInstruction(
				cpu.getConsole().flatReadWord(pc), 
				pc, cpu.getConsole(), false), cpu.getConsole());
		
		
		push(new WriteRegister(cpuState, Cpu9900.REG_PC, pc + inst.getSize()));
		
//		if (inst.getInst() == Inst9900.Ix) {
//    		ins.pc = this.inst.pc + this.inst.getSize() - 2;
//    	}
        
        MachineOperand9900 mop1 = (MachineOperand9900) this.inst.getOp1();
		MachineOperand9900 mop2 = (MachineOperand9900) this.inst.getOp2();
		if (mop1.type != IMachineOperand.OP_NONE) {
			MachineOperandState mopState1 = nextMachineOperandState(mop1);
			fetch(mop1, mopState1);
			if (mop2.type != IMachineOperand.OP_NONE) {
				MachineOperandState mopState2 = nextMachineOperandState(mop2);
				fetch(mop2, mopState2);
			}
		}
	}


	private MachineOperandState nextMachineOperandState(MachineOperand9900 mop) {
		return new MachineOperandState(mop);
	}
	
//	@Override
//	public IFetchStateTracker getParent() {
//		return cpu;
//	}
//	@Override
//	public void setParent(IFetchStateTracker parent) {
//		throw new IllegalStateException();
//	}
//	@Override
//	public byte fetchByte(int addr) {
//		return cpu.fetchByte(addr);
//	}
//	@Override
//	public int fetchRegister(int reg) {
//		return cpu.fetchRegister(reg);
//	}
//	@Override
//	public short fetchWord(int addr) {
//		return cpu.fetchWord(addr);
//	}

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
//    		state.ea = (short) ((mop.val<<1) + wp);
//    		state.cycles += 0;
//
//    		pushFetchMemory(state);
    		push(new ReadRegister(cpuState, state));
    		break;
    		
    	case MachineOperand9900.OP_IND: {	// *Rx
//    		short ad = (short)((mop.val<<1) + wp);
//    		state.ea = ad;
    		state.cycles += 4;
    		
    		push(new ReadIndirectRegister(cpuState, state));
    		break;
    	}
    	case MachineOperand9900.OP_INC:	{ // *Rx+
//    		short ad = (short)((mop.val<<1) + wp);
//    		state.ea = ad;
    		state.cycles += 4;
    		state.cycles += mop.byteop ? 2 : 4;
    		push(new ReadIncrementRegister(cpuState, state));
    		break;
    	}
    	case MachineOperand9900.OP_ADDR: {	// @>xxxx or @>xxxx(Rx)
//    	    state.ea = mop.immed; 
    		state.cycles += 8;
    		if (state.mop.val == 0)
    			pushFetchMemory(state);
    		else
    			push(new ReadRegisterOffset(cpuState, state));
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
//    	case MachineOperand9900.OP_OFFS_R12:	// offset from R12
//    		state.cycles += 0;
//    		state.ea = (short) ((12<<1) + wp);
//    		push(new ReadWord(cpuState, state));
//    		push(new CalculateOffset(cpuState, state));
//    		break;
//    	case MachineOperand9900.OP_REG0_SHIFT_COUNT: // shift count from R0
//    		state.ea = wp;
//    	    state.cycles += 8;
//    	    push(new ReadWord(cpuState, state));
//    	    push(new CalculateShift(cpuState, state));
//		    break;
    	
    	case MachineOperand9900.OP_JUMP:	// jump target
    		state.ea = (short)(mop.val + pc);
    		state.value = state.ea;
    		break;
    	case MachineOperand9900.OP_STATUS:	// status word
            //TODO: NOTHING -- make sure we don't depend on this   
    		break;
    	case MachineOperand9900.OP_INST:
    		push(new ReadWord(cpuState, state));
    		break;		
    	}
	}
	
	private void pushFetchMemory(MachineOperandState state) {
		if (state.mop.bIsReference) {
			push(new CopyAddressToValue(state));
		} else {
			if (state.mop.byteop) {
				push(new ReadByte(cpuState, state));
			} else {
				push(new ReadWord(cpuState, state));
			}
		}
	}
//	private void pushFetchIndirectMemory(MachineOperandState state) {
//		if (state.mop.bIsReference) {
//			push(new CopyAddressToValue(state));
//		} else {
//			if (state.mop.byteop) {
//				push(new ReadByteIndirect(state));
//			} else {
//				push(new ReadWordIndirect(state));
//			}
//		}
//	}

	/** Add the change element for the interpretation */
	public void createInstructionExecute() {
	}

}
