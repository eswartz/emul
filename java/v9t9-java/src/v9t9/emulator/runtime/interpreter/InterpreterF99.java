package v9t9.emulator.runtime.interpreter;

import java.util.Arrays;

import org.ejs.coffee.core.utils.Pair;

import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.emulator.runtime.cpu.CpuF99;
import v9t9.emulator.runtime.cpu.CpuStateF99;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.cpu.InstF99;
import static v9t9.engine.cpu.InstF99.*;
import v9t9.engine.cpu.InstructionF99;
import v9t9.engine.cpu.InstructionWorkBlockF99;
import v9t9.engine.cpu.MachineOperandF99;
import v9t9.engine.memory.MemoryDomain;

/**
 * This class interprets F99 instructions one by one.
 * 
 * @author ejs
 */
public class InterpreterF99 implements Interpreter {
	public final static int fieldIndices[] = { 10, 5, 0 };
	public final static int fieldMasks[] = { 0x1f, 0x1f, 0x1f };
	
	Machine machine;

    MemoryDomain memory;
    
    InstructionWorkBlockF99 iblock;

	private CpuF99 cpu;

	private InstructionF99[] instBuffer;
	
	
    public InterpreterF99(Machine machine) {
        this.machine = machine;
        this.cpu = (CpuF99) machine.getCpu();
        this.memory = machine.getCpu().getConsole();
        iblock = new InstructionWorkBlockF99(cpu);
        iblock.domain = memory;
        iblock.showSymbol = true;
        instBuffer = new InstructionF99[3];
     }

    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.interpreter.Interpreter#execute(java.lang.Short)
	 */
    public void execute() {
    	InstructionListener[] instructionListeners = machine.getExecutor().getInstructionListeners();
    	executeAndListen(instructionListeners);
    }

    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.interpreter.Interpreter#executeChunk(int, v9t9.emulator.runtime.cpu.Executor)
	 */
	@Override
	public void executeChunk(int numinsts, Executor executor) {
		for (int i = 0; i < numinsts; i++) {
			execute();
			executor.nInstructions++;
			cpu.checkAndHandleInterrupts();
			if (executor.interruptExecution)
				break;
		}
	}

	private void executeAndListen(InstructionListener[] instructionListeners) {
		iblock.pc = cpu.getPC();
		iblock.cycles = 0;
		iblock.st = cpu.getST();
		
	    InstructionF99[] ins = getInstructions();
	    
	    short origPc = iblock.pc; 
	    cpu.setPC(origPc);
	    
	    if (ins[0] != null)
	    	executeAndListen(instructionListeners, ins[0]);
	    if (ins[1] != null && cpu.getPC() == origPc)
	    	executeAndListen(instructionListeners, ins[1]);
	    if (ins[2] != null && cpu.getPC() == origPc)
	    	executeAndListen(instructionListeners, ins[2]);
	}

	/**
	 * @param instructionListeners
	 * @param ins
	 */
	private void executeAndListen(InstructionListener[] instructionListeners,
			InstructionF99 ins) {
        iblock.cycles = cpu.getCurrentCycleCount();

        iblock.pc = cpu.getPC();
        iblock.st = cpu.getST();
        iblock.sp = ((CpuStateF99)cpu.getState()).getSP();
        iblock.rp = ((CpuStateF99)cpu.getState()).getRP();
        iblock.inst = ins;
        

        Pair<Integer, Integer> fx = InstF99.getStackEffects(ins.getInst());
		if (fx != null) {
			int spused = fx.first;
			if (spused < 0)
				spused = 4;
			for (int i = 0; i < spused; i++)
				iblock.inStack[i] = iblock.getStackEntry(spused - i - 1);
		}
        fx = InstF99.getReturnStackEffects(ins.getInst());
		if (fx != null) {
			int rpused = fx.first;
			if (rpused < 0)
				rpused = 4;
			for (int i = 0; i < rpused; i++)
				iblock.inReturnStack[i] = iblock.getReturnStackEntry(rpused - i - 1);
		}
		
        InstructionWorkBlockF99 block = new InstructionWorkBlockF99(cpu);
        this.iblock.copyTo(block);
        
        
        /* execute */
        interpret(ins);
        
        block.cycles = cpu.getCurrentCycleCount();

        iblock.pc = cpu.getPC();
        iblock.st = cpu.getST();
        iblock.sp = ((CpuStateF99)cpu.getState()).getSP();
        iblock.rp = ((CpuStateF99)cpu.getState()).getRP();
        
        /* notify listeners */
        if (instructionListeners != null) {
        	for (InstructionListener listener : instructionListeners) {
        		listener.executed(block, iblock);
        	}
        }
        
        iblock.showSymbol = (ins.getInst() == Iexit || ins.getInst() == Icall);
		
	}
	
	

	/**
	 * @return
	 */
	private InstructionF99[] getInstructions() {
		
		short pc = cpu.getPC();
		boolean skipFirst = (pc & 1) != 0;
		short thisPc = (short) (pc & ~1);
		
		short opword = memory.readWord(thisPc);
		
		iblock.instNum = 0;
		iblock.opword = opword;
		iblock.pc = (short) (thisPc + 2);
		iblock.index = skipFirst ? 1 : 0;
		
		Arrays.fill(instBuffer, null);
		
		if (opword < 0) {
			// call
			if (skipFirst) {
				cpu.triggerInterrupt(CpuF99.INT_BKPT);
				return instBuffer;
			}
			
			instBuffer[0] = getCallInstruction(opword);
		} else {
			while (iblock.index < 3 && iblock.instNum < 3)
				getInstruction(thisPc);
		}
		iblock.pc += (iblock.index == 4 ? 1 : 0);
		
		return instBuffer;
	}

	private void getInstruction(short origPC) {
		int thePC = origPC + (iblock.index != 0 ? 1 : 0);
		int opcode = iblock.nextField();
		if (opcode == 0)
			return;
		
		InstructionF99 inst = new InstructionF99();
		inst.pc = thePC;
		
		if (opcode == Iext) {
			opcode = iblock.nextField() + InstF99._Iext;
		}
		
		inst.opcode = opcode;
		inst.setInst(opcode);

		switch (opcode) {
		case Ibranch_f:
		case I0branch_f:
		case IfieldLit:
		case IfieldLit_d:
			inst.setOp1(MachineOperandF99.createImmediateOperand(iblock.nextSignedField(), MachineOperandF99.OP_ENC_IMM5));
			break;
		case Ispidx:
		case Irpidx:
		case I0cmp:
		case I0cmp_d:
		case Icmp:
		case Icmp_d:
		case Ibinop:
		case Ibinop_d:
		case ItoContext:
		case IcontextFrom:
			inst.setOp1(MachineOperandF99.createImmediateOperand(iblock.nextField(), MachineOperandF99.OP_ENC_IMM5));
			if (opcode == I0cmp || opcode == Icmp || opcode == I0cmp_d || opcode == Icmp_d)
				((MachineOperandF99)inst.getOp1()).encoding = MachineOperandF99.OP_ENC_CMP;
			else if (opcode == Ibinop || opcode == Ibinop_d)
				((MachineOperandF99)inst.getOp1()).encoding = MachineOperandF99.OP_ENC_OP;
			else if (opcode == ItoContext || opcode == IcontextFrom)
				((MachineOperandF99)inst.getOp1()).encoding = MachineOperandF99.OP_ENC_CTX;
			break;
		case Ilit:
		case I0branch:
		case Ibranch:
		case Iloop:
		case IplusLoop:
		case IuplusLoop:
			inst.setOp1(MachineOperandF99.createImmediateOperand(iblock.nextWord(), MachineOperandF99.OP_ENC_IMM16));
			break;
		case Ilit_d: {
			int lo = iblock.nextWord() & 0xffff;
			int hi = iblock.nextWord() & 0xffff;
			inst.setOp1(MachineOperandF99.createImmediateOperand(
					lo | (hi << 16),
					MachineOperandF99.OP_ENC_IMM32));
			break;
		}
		default:
			// no immediate	
		}
		
		instBuffer[iblock.instNum++] = inst;
	}

	private InstructionF99 getCallInstruction(short op) {
		InstructionF99 inst = new InstructionF99();
		inst.pc = cpu.getPC() - 2;
		inst.opcode = op;
		inst.setOp1(MachineOperandF99.createImmediateOperand((short) (op << 1), MachineOperandF99.OP_ENC_IMM15S1));
		inst.setInst(Icall);
		return inst;
	}

	/**
     * Execute an instruction
     * @param ins
     */
    private void interpret(InstructionF99 ins) {
    	MachineOperandF99 mop1 = (MachineOperandF99)ins.getOp1();
		cpu.addCycles(ins.getInfo().cycles + (mop1 != null ? mop1.cycles : 0));
		
		int alignPC = ins.pc & ~1;
        
		switch (ins.getInst()) {
        case Iload:
        	cpu.push(memory.readWord(cpu.pop()));
        	break;
        case Icload:
        	cpu.push(memory.readByte(cpu.pop()));
        	break;
        case Istore:
        	memory.writeWord(cpu.pop(), cpu.pop());
        	break;
        case Icstore:
        	memory.writeByte(cpu.pop(), (byte) cpu.pop());
        	break;
        	
        case IplusStore: {
        	short addr = cpu.pop();
        	iblock.domain.writeWord(addr, (short) (iblock.domain.readWord(addr) + cpu.pop()));
        	break;
        }
        	
        case IfieldLit:
        case Ilit:
        	cpu.push(mop1.immed);
        	break;
        case IfieldLit_d:
        case Ilit_d:
        	cpu.pushd(mop1.val);
        	break;
        	
        case Iexit:
        	cpu.setPC(cpu.rpop());
        	iblock.showSymbol = true;
        	break;
        case Idup:
        	cpu.push(cpu.peek());
        	break;
        case I0branch: {
        	short targ = (short) (alignPC + mop1.immed);
        	if (cpu.pop() == 0)
        		cpu.setPC(targ);
        	break;
        }
        case Ibranch: {
        	short targ = (short) (alignPC + mop1.immed);
        	cpu.setPC(targ);
        	break;
        }
        case I0cmp: {
        	short val = cpu.pop();
        	boolean c = false;
        	switch (mop1.immed) {
        	case InstF99.CMP_GE: c = val >= 0; break;
        	case InstF99.CMP_GT: c = val > 0; break;
        	case InstF99.CMP_LE: c = val <= 0; break;
        	case InstF99.CMP_LT: c = val < 0; break;
        	case InstF99.CMP_UGE: c = true; break;
        	case InstF99.CMP_UGT: c = val != 0; break;
        	case InstF99.CMP_ULE: c = val == 0; break;
        	case InstF99.CMP_ULT: c = false; break;
        	}
        	cpu.push((short) (c ? -1 : 0));
        	break;
        }
        case I0cmp_d: {
        	int val = cpu.pop();
        	boolean c = false;
        	switch (mop1.immed) {
        	case InstF99.CMP_GE: c = val >= 0; break;
        	case InstF99.CMP_GT: c = val > 0; break;
        	case InstF99.CMP_LE: c = val <= 0; break;
        	case InstF99.CMP_LT: c = val < 0; break;
        	case InstF99.CMP_UGE: c = true; break;
        	case InstF99.CMP_UGT: c = val != 0; break;
        	case InstF99.CMP_ULE: c = val == 0; break;
        	case InstF99.CMP_ULT: c = false; break;
        	}
        	cpu.push((short) (c ? -1 : 0));
        	break;
        }
        case Icmp: {
        	short r = cpu.pop();
        	short l = cpu.pop();
        	boolean c = false;
        	switch (mop1.immed) {
        	case InstF99.CMP_GE: c = l >= r; break;
        	case InstF99.CMP_GT: c = l > r; break;
        	case InstF99.CMP_LE: c = l <= r; break;
        	case InstF99.CMP_LT: c = l < r; break;
        	case InstF99.CMP_UGE: c = (l & 0xffff) >= (r & 0xffff); break;
        	case InstF99.CMP_UGT: c = (l & 0xffff) >  (r & 0xffff); break;
        	case InstF99.CMP_ULE: c = (l & 0xffff) <= (r & 0xffff); break;
        	case InstF99.CMP_ULT: c = (l & 0xffff) <  (r & 0xffff); break;
        	}
        	cpu.push((short) (c ? -1 : 0));
        	break;
        }
        case Icmp_d: {
        	int r = cpu.popd();
        	int l = cpu.popd();
        	boolean c = false;
        	switch (mop1.immed) {
        	case InstF99.CMP_GE: c = l >= r; break;
        	case InstF99.CMP_GT: c = l > r; break;
        	case InstF99.CMP_LE: c = l <= r; break;
        	case InstF99.CMP_LT: c = l < r; break;
        	case InstF99.CMP_UGE: c = (((long)l) & 0xffffffffL) >= (((long)r) & 0xffffffffL); break;
        	case InstF99.CMP_UGT: c = (((long)l) & 0xffffffffL) >  (((long)r) & 0xffffffffL); break;
        	case InstF99.CMP_ULE: c = (((long)l) & 0xffffffffL) <= (((long)r) & 0xffffffffL); break;
        	case InstF99.CMP_ULT: c = (((long)l) & 0xffffffffL) <  (((long)r) & 0xffffffffL); break;
        	}
        	cpu.push((short) (c ? -1 : 0));
        	break;
        }
        case I0equ:
        	cpu.push((short) (cpu.pop() == 0 ? -1 : 0));
        	break;
        case I0equ_d:
        	cpu.push((short) (cpu.popd() == 0 ? -1 : 0));
        	break;
        case Iequ:
        	cpu.push((short) (cpu.pop() == cpu.pop() ? -1 : 0));
        	break;
        case Iequ_d:
        	cpu.push((short) (cpu.popd() == cpu.popd() ? -1 : 0));
        	break;
        case Idrop:
        	cpu.pop();
        	break;
        case Iswap: {
        	short x = cpu.pop();
        	short y = cpu.pop();
        	cpu.push(x);
        	cpu.push(y);
        	break;
        }
        case Iover:
        	cpu.push(iblock.getStackEntry(1));
        	break;
        case Idup_d: {
        	int v = cpu.popd();
        	cpu.pushd(v);
        	cpu.pushd(v);
        	break;
        }
        case Iadd:
        	cpu.push((short) (cpu.pop() + cpu.pop()));
        	break;
        	
        case Ibinop: {
        	short r = cpu.pop();
        	short l = cpu.pop();
        	cpu.push((short) binOp(l, r, mop1.immed));
        	break;
        }
        case Ibinop_d: {
        	int r = cpu.popd();
        	int l = cpu.popd();
        	cpu.pushd(binOp_d(l, r, mop1.immed));
        	break;
        }
        	
        case I2times:
        	cpu.push((short) (cpu.pop() * 2));
        	break;
        case I2div:
        	cpu.push((short) (cpu.pop() / 2));
        	break;
        	
        case Iumul: {
        	int mul = (cpu.pop() & 0xffff) * (cpu.pop() & 0xffff);
        	cpu.push((short) (mul & 0xffff));
        	cpu.push((short) (mul >> 16));
        	break;
        }
        case Iudivmod: {
        	int div = cpu.pop() & 0xffff;
        	int num = cpu.popd();
        	if (div == 0) {
            	cpu.push((short) -1);
            	cpu.push((short) 0);
        	} else {
        		int quot = num / div;
        		int rem = num % div;
        		if (quot >= 65536) {
        			cpu.push((short) -1);
                	cpu.push((short) 0);
        		} else {
        			cpu.push((short) rem);
                	cpu.push((short) quot);
        		}
        	}
        	break;
        }
        case I1plus:
	        cpu.push((short) (cpu.pop() + 1));
	    	break;
        case I2plus:
	        cpu.push((short) (cpu.pop() + 2));
	    	break;
        case Ineg:
        	cpu.push((short) -cpu.pop());
        	break;
        case Ineg_d:
        	cpu.pushd(-cpu.popd());
        	break;
        case Iinvert:
        	cpu.push((short) ~cpu.pop());
        	break;
        	
        case Icall:
        	cpu.rpush(iblock.pc);
        	cpu.setPC(mop1.immed);
        	break;
        	
        case ItoR:
        	cpu.rpush(cpu.pop());
        	break;
        case ItoR_d:
        	cpu.rpush(iblock.getStackEntry(1));
        	cpu.rpush(iblock.getStackEntry(0));
        	cpu.pop();
        	cpu.pop();
        	break;
        case IRfrom:
        	cpu.push(cpu.rpop());
        	break;
        case IRfrom_d:
        	cpu.push(iblock.getReturnStackEntry(1));
        	cpu.push(iblock.getReturnStackEntry(0));
        	break;
        case Irdrop:
        	cpu.rpop();
        	break;
        case Ii:
        	cpu.push(cpu.rpeek());
        	break;
        case Ispidx:
        	cpu.push(iblock.getStackEntry(mop1.immed & 0x1f));
        	break;
        case Irpidx:
        	cpu.push(iblock.getReturnStackEntry(mop1.immed & 0x1f));
        	break;
        	
        case Iloop: {
        	short next = (short) (iblock.getReturnStackEntry(0) + 1);
        	short lim = iblock.getReturnStackEntry(1);
    		cpu.rpop();
    		cpu.rpush(next);
    		if (next != lim) {
        		short targ = (short) (alignPC + mop1.immed);
            	cpu.setPC(targ);
        	}
        	break;
        }
        case IplusLoop: {
        	short change = cpu.pop();
        	short cur = iblock.getReturnStackEntry(0);
        	short next = (short) (cur + change);
        	short lim = iblock.getReturnStackEntry(1);
    		cpu.rpop();
    		cpu.rpush(next);
    		if (lim != 0 ? next < lim : next >= change) {
        		short targ = (short) (alignPC + mop1.immed);
            	cpu.setPC(targ);
        	}
        	break;
        }
        case IuplusLoop: {
        	short change = cpu.pop();
        	short cur = iblock.getReturnStackEntry(0);
        	short next = (short) (cur + change);
        	short lim = iblock.getReturnStackEntry(1);
        	cpu.rpop();
        	cpu.rpush(next);
        	if (lim != 0 ? (next & 0xffff) < (lim & 0xffff) : (next & 0xffff) >= (change & 0xffff)) {
        		short targ = (short) (alignPC + mop1.immed);
        		cpu.setPC(targ);
        	}
        	break;
        }
        case IcontextFrom:
        	switch (mop1.immed) {
        	case CTX_SP:
        		cpu.push(((CpuStateF99)cpu.getState()).getSP());
        		break;
        	case CTX_SP0:
        		cpu.push(((CpuStateF99)cpu.getState()).getBaseSP());
        		break;
        	case CTX_RP:
        		cpu.push(((CpuStateF99)cpu.getState()).getRP());
        		break;
        	case CTX_RP0:
        		cpu.push(((CpuStateF99)cpu.getState()).getBaseRP());
        		break;
        	case CTX_UP:
        		cpu.push(((CpuStateF99)cpu.getState()).getUP());
        		break;
        	case CTX_UP0:
        		cpu.push(((CpuStateF99)cpu.getState()).getBaseUP());
        		break;
        	case CTX_PC:
        		cpu.push(cpu.getPC());
        		break;
    		default:
    			cpu.push((short) -1);
    			break;
        	}
        	break;
        	
        case ItoContext:
        	switch (mop1.immed) {
        	case CTX_SP:
        		((CpuStateF99)cpu.getState()).setSP(cpu.pop());
        		break;
        	case CTX_SP0:
        		((CpuStateF99)cpu.getState()).setBaseSP(cpu.pop());
        		break;
        	case CTX_RP:
        		((CpuStateF99)cpu.getState()).setRP(cpu.pop());
        		break;
        	case CTX_RP0:
        		((CpuStateF99)cpu.getState()).setBaseRP(cpu.pop());
        		break;
        	case CTX_UP:
        		((CpuStateF99)cpu.getState()).setUP(cpu.pop());
        		break;
        	case CTX_UP0:
        		((CpuStateF99)cpu.getState()).setBaseUP(cpu.pop());
        		break;
        	case CTX_PC:
        		((CpuStateF99)cpu.getState()).setPC(cpu.pop());
        		break;
        	default:
        		cpu.pop();
        		break;
        	}
        	break;
        	
        default:
    		throw new UnsupportedOperationException("" + ins);
        }

    }

	/**
	 * @param l
	 * @param r
	 * @param immed
	 * @return
	 */
	private int binOp(short l, short r, short immed) {
		switch (immed) {
		case OP_ADD:
			return l + r;
		case OP_SUB:
			return l - r;
		case OP_AND:
			return l & r;
		case OP_OR:
			return l | r;
		case OP_XOR:
			return l ^ r;
		case OP_LSH:
			return l << (r & 0x1f);
		case OP_RSH:
			return (l & 0xffff) >>> (r & 0x1f);
		case OP_ASH:
			return l >> (r & 0x1f);
		}
		return 0;
	}
	private int binOp_d(int l, int r, short immed) {
		switch (immed) {
		case OP_ADD:
			return l + r;
		case OP_SUB:
			return l - r;
		case OP_AND:
			return l & r;
		case OP_OR:
			return l | r;
		case OP_XOR:
			return l ^ r;
		case OP_LSH:
			return l << (r & 0x1f);
		case OP_RSH:
			return (int) ((((long)l) & 0xffffffffL) >>> (r & 0x1f));
		case OP_ASH:
			return l >> (r & 0x1f);
		}
		return 0;
	}

	/**
	 * 
	 */
	public void setShowSymbol() {
		iblock.showSymbol = true;
	}
}