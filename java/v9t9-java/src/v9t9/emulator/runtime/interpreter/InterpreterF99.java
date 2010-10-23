package v9t9.emulator.runtime.interpreter;

import org.ejs.coffee.core.utils.Pair;

import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.emulator.runtime.cpu.CpuF99;
import v9t9.emulator.runtime.cpu.CpuStateF99;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.cpu.InstF99;
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
	private final static int fieldIndices[] = { 10, 5, 0 };
	private final static int fieldMasks[] = { 0x1f, 0x1f, 0x1f };
	
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
		
	}

	/**
	 * @return
	 */
	private InstructionF99[] getInstructions() {
		
		short pc = cpu.getPC();
		boolean skipFirst = (pc & 1) != 0;
		short thisPc = (short) (pc & ~1);
		
		short opword = memory.readWord(thisPc);
		
		iblock.op = opword;
		iblock.pc = (short) (thisPc + 2);
		
		if (opword < 0) {
			// call
			if (skipFirst) {
				cpu.triggerInterrupt(CpuF99.INT_BKPT);
				return null;
			}
			
			instBuffer[0] = getCallInstruction(opword);
		} else {
			if (!skipFirst)
				instBuffer[0] = getInstruction(thisPc, 0, opword);
			else
				instBuffer[0] = null;
			if (instBuffer[0] == null || ((instBuffer[0].getOp1() == null
					|| ((MachineOperandF99) instBuffer[0].getOp1()).encoding == MachineOperandF99.OP_ENC_IMM16)
			&& instBuffer[0].getInst() < InstF99._Iext))
				instBuffer[1] = getInstruction(thisPc, 1, opword);
			else
				instBuffer[1] = null;
			if (instBuffer[1] == null || ((instBuffer[1].getOp1() == null
					|| ((MachineOperandF99) instBuffer[1].getOp1()).encoding == MachineOperandF99.OP_ENC_IMM16)
					&& instBuffer[1].getInst() < InstF99._Iext))
				instBuffer[2] = getInstruction(thisPc, 2, opword);
			else
				instBuffer[2] = null;
		}
		return instBuffer;
	}

	private MachineOperandF99 readSignedField(int index, int opword) {
		int mask = fieldMasks[index];
		int opcode = (opword >> fieldIndices[index]) & mask;
		if ((opcode & ~(mask >> 1)) != 0) {
			opcode |= ~mask;
		}
		return MachineOperandF99.createImmediateOperand(opcode, MachineOperandF99.OP_ENC_IMM5);

	}

	private int readUnsignedField(int index, int opword) {
		int mask = fieldMasks[index];
		int opcode = (opword >> fieldIndices[index]) & mask;
		return opcode;

	}
	
	private InstructionF99 getInstruction(short pc, int index, int opword) {
		int opcode = (opword >> fieldIndices[index]) & fieldMasks[index];
		if (opcode == 0)
			return null;
		
		InstructionF99 inst = new InstructionF99();
		inst.pc = pc + (index != 0 ? 1 : 0);
		
		if (opcode == InstF99.Iext) {
			if (index == 2) {
				short next = memory.readWord(iblock.pc);
				iblock.pc++;			
				opcode = readUnsignedField(0, next);
				index = 0;
			} else {
				opcode = readUnsignedField(index + 1, iblock.op);
				index++;
			}
			opcode += InstF99._Iext;
		}
		
		inst.opcode = opcode;
		inst.setInst(opcode);

		int nextPC = (short) ((iblock.pc + 1) & ~1);
		switch (opcode) {
		case InstF99.IfieldLit:
		case InstF99.IfieldLit_d:
		case InstF99.Ispidx:
		case InstF99.Irpidx:
		case InstF99.Irsh:
		case InstF99.Ilsh:
		case InstF99.Iash:
		//case InstF99.I0fieldBranch:
		//case InstF99.IfieldBranch:
			if (index == 2) {
				short next = memory.readWord(iblock.pc);
				iblock.pc++;			
				inst.setOp1(readSignedField(0, next));
			} else {
				inst.setOp1(readSignedField(index + 1, iblock.op));
			}
			break;
		case InstF99.Ilit:
		case InstF99.I0branch:
		case InstF99.Ibranch:
		case InstF99.Iloop:
			inst.setOp1(MachineOperandF99.createImmediateOperand(memory.readWord(nextPC), MachineOperandF99.OP_ENC_IMM16));
			iblock.pc = (short) (nextPC + 2);
			break;
		case InstF99.Ilit_d: {
			inst.setOp1(MachineOperandF99.createImmediateOperand(
					(memory.readWord(nextPC) & 0xffff) | (memory.readWord(nextPC + 2) << 16),
					MachineOperandF99.OP_ENC_IMM32));
			iblock.pc = (short) ((iblock.pc & ~1) + 4);
			break;
		}
		default:
			// no immediate	
		}
		
		return inst;
	}

	private InstructionF99 getCallInstruction(short op) {
		InstructionF99 inst = new InstructionF99();
		inst.pc = cpu.getPC() - 2;
		inst.opcode = op;
		inst.setOp1(MachineOperandF99.createImmediateOperand((short) (op << 1), MachineOperandF99.OP_ENC_IMM15S1));
		inst.setInst(InstF99.Icall);
		return inst;
	}

	/**
     * Execute an instruction
     * @param ins
     */
    private void interpret(InstructionF99 ins) {
    	MachineOperandF99 mop1 = (MachineOperandF99)ins.getOp1();
		cpu.addCycles(ins.getInfo().cycles + (mop1 != null ? mop1.cycles : 0));
		
        switch (ins.getInst()) {
        case InstF99.Iload:
        	cpu.push(memory.readWord(cpu.pop()));
        	break;
        case InstF99.Istore:
        	memory.writeWord(cpu.pop(), cpu.pop());
        	break;
        case InstF99.IfieldLit:
        case InstF99.Ilit:
        	cpu.push(mop1.immed);
        	break;
        case InstF99.IfieldLit_d:
        case InstF99.Ilit_d:
        	cpu.push((short) (mop1.val & 0xffff));
        	cpu.push((short) (mop1.val >> 16));
        	break;
        	
        case InstF99.Iexit:
        	cpu.setPC(cpu.rpop());
        	break;
        case InstF99.Idup:
        	cpu.push(cpu.peek());
        	break;
        case InstF99.I0branch: {
        	short targ = (short) ((iblock.pc & ~1) + mop1.immed);
        	if (cpu.pop() == 0)
        		cpu.setPC(targ);
        	break;
        }
        case InstF99.Ibranch: {
        	short targ = (short) ((iblock.pc & ~1) + mop1.immed);
        	cpu.setPC(targ);
        	break;
        }
        case InstF99.I0lt:
        	cpu.push((short) (cpu.pop() < 0 ? -1 : 0));
        	break;
        	/*
        case InstF99.Ilt: {
        	int right = cpu.pop();
        	cpu.push((short) (cpu.pop() < right ? -1 : 0));
        	break;
        }
        */
        case InstF99.Iult: {
        	int right = cpu.pop() & 0xffff;
        	cpu.push((short) ((cpu.pop() & 0xffff) < right ? -1 : 0));
        	break;
        }
        case InstF99.I0equ:
        	cpu.push((short) (cpu.pop() == 0 ? -1 : 0));
        	break;
        	/*
        case InstF99.Iequ:
        	cpu.push((short) (cpu.pop() == cpu.pop() ? -1 : 0));
        	break;
        	*/
        case InstF99.Idrop:
        	cpu.pop();
        	break;
        case InstF99.Iswap: {
        	short x = cpu.pop();
        	short y = cpu.pop();
        	cpu.push(x);
        	cpu.push(y);
        	break;
        }
        case InstF99.Idup_d: {
        	short x = iblock.getStackEntry(0);
        	short y = iblock.getStackEntry(1);
        	cpu.push(y);
        	cpu.push(x);
        	break;
        }
        case InstF99.Iadd:
        	cpu.push((short) (cpu.pop() + cpu.pop()));
        	break;
        case InstF99.Iadd_d: {
        	int hi = cpu.pop();
        	int val = (hi << 16) | (cpu.pop() & 0xffff);
        	hi = cpu.pop();
        	int val2 = (hi << 16) | (cpu.pop() & 0xffff);
        	val += val2;
        	cpu.push((short) (val & 0xffff));
        	cpu.push((short) (val >> 16));
        	break;
        }
        case InstF99.Isub: {
        	int sub = cpu.pop();
        	cpu.push((short) (cpu.pop() - sub));
        	break;
        }
        case InstF99.Iumul: {
        	int mul = (cpu.pop() & 0xffff) * (cpu.pop() & 0xffff);
        	cpu.push((short) (mul & 0xffff));
        	cpu.push((short) (mul >> 16));
        	break;
        }
        case InstF99.Iudivmod: {
        	int div = cpu.pop() & 0xffff;
        	int num = ((cpu.pop() & 0xffff) << 16);
        	num |= (cpu.pop() & 0xffff);
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
        case InstF99.Iash: {
        	int by = mop1.immed & 0x1f;
        	int val = cpu.pop() >> by;
			cpu.push((short) val);
        	break;
        }
        case InstF99.Irsh: {
        	int by = mop1.immed & 0x1f;
        	int val = (cpu.pop() & 0xffff) >>> by;
        	cpu.push((short) val);
        	break;
        }
        case InstF99.I1plus:
	        cpu.push((short) (cpu.pop() + 1));
	    	break;
        case InstF99.I2plus:
	        cpu.push((short) (cpu.pop() + 2));
	    	break;
        case InstF99.Ineg:
        	cpu.push((short) -cpu.pop());
        	break;
        case InstF99.Ineg_d: {
        	int hi = cpu.pop();
        	int val = (hi << 16) | (cpu.pop() & 0xffff);
        	val = -val;
        	cpu.push((short) (val & 0xffff));
        	cpu.push((short) (val >> 16));
        	break;
        }
        case InstF99.Inot:
        	cpu.push((short) ~cpu.pop());
        	break;
        case InstF99.Ior:
        	cpu.push((short) (cpu.pop() | cpu.pop()));
        	break;
    	case InstF99.Iand:
    		cpu.push((short) (cpu.pop() & cpu.pop()));
    		break;
		case InstF99.Ixor:
			cpu.push((short) (cpu.pop() ^ cpu.pop()));
			break;
        	
        case InstF99.Icall:
        	cpu.rpush(iblock.pc);
        	cpu.setPC(mop1.immed);
        	break;
        	
        case InstF99.ItoR:
        	cpu.rpush(cpu.pop());
        	break;
        case InstF99.ItoR_d:
        	cpu.rpush(iblock.getStackEntry(1));
        	cpu.rpush(iblock.getStackEntry(0));
        	cpu.pop();
        	cpu.pop();
        	break;
        case InstF99.IRfrom:
        	cpu.push(cpu.rpop());
        	break;
        case InstF99.IRfrom_d:
        	cpu.push(iblock.getReturnStackEntry(1));
        	cpu.push(iblock.getReturnStackEntry(0));
        	break;
        case InstF99.Irdrop:
        	cpu.rpop();
        	break;
        case InstF99.Ii:
        	cpu.push(cpu.rpeek());
        	break;
        case InstF99.Ispidx:
        	cpu.push(iblock.getStackEntry(mop1.immed & 0x1f));
        	break;
        case InstF99.Irpidx:
        	cpu.push(iblock.getReturnStackEntry(mop1.immed & 0x1f));
        	break;
        	
        case InstF99.Iloop: {
        	short next = (short) (iblock.getReturnStackEntry(0) + 1);
        	short lim = iblock.getReturnStackEntry(1);
    		cpu.rpop();
    		cpu.rpush(next);
    		if (next != lim) {
        		short targ = (short) ((iblock.pc & ~1) + mop1.immed);
            	cpu.setPC(targ);
        	} else {
        		cpu.rpop();
        		cpu.rpop();
        	}
        	break;
        }
        case InstF99.IcontextFrom:
        	switch (cpu.pop()) {
        	case 0:
        		cpu.push(((CpuStateF99)cpu.getState()).getSP());
        		break;
        	case 1:
        		cpu.push(((CpuStateF99)cpu.getState()).getBaseSP());
        		break;
        	case 2:
        		cpu.push(((CpuStateF99)cpu.getState()).getRP());
        		break;
        	case 3:
        		cpu.push(((CpuStateF99)cpu.getState()).getBaseRP());
        		break;
        	case 4:
        		cpu.push(((CpuStateF99)cpu.getState()).getUP());
        		break;
        	case 5:
        		cpu.push(((CpuStateF99)cpu.getState()).getBaseUP());
        		break;
        	case 6:
        		cpu.push(cpu.getPC());
        		break;
    		default:
    			cpu.push((short) -1);
    			break;
        	}
        	break;
        	
        case InstF99.ItoContext:
        	switch (cpu.pop()) {
        	case 0:
        		((CpuStateF99)cpu.getState()).setSP(cpu.pop());
        		break;
        	case 1:
        		((CpuStateF99)cpu.getState()).setBaseSP(cpu.pop());
        		break;
        	case 2:
        		((CpuStateF99)cpu.getState()).setRP(cpu.pop());
        		break;
        	case 3:
        		((CpuStateF99)cpu.getState()).setBaseRP(cpu.pop());
        		break;
        	case 4:
        		((CpuStateF99)cpu.getState()).setUP(cpu.pop());
        		break;
        	case 5:
        		((CpuStateF99)cpu.getState()).setBaseUP(cpu.pop());
        		break;
        	case 6:
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
}