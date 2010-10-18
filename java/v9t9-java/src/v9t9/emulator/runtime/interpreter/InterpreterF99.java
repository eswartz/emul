package v9t9.emulator.runtime.interpreter;

import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.emulator.runtime.cpu.CpuF99;
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
	private final static int fieldIndices[] = { 9, 6, 0 };
	private final static int fieldMasks[] = { 0x3f, 0x7, 0x3f };
	
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
        iblock.sp = cpu.getSP();
        iblock.rp = cpu.getRSP();
        iblock.inst = ins;
        
        InstructionWorkBlockF99 block = new InstructionWorkBlockF99(cpu);
        this.iblock.copyTo(block);
        
        
        /* execute */
        interpret(ins);
        
        block.cycles = cpu.getCurrentCycleCount();

        iblock.pc = cpu.getPC();
        iblock.st = cpu.getST();
        iblock.sp = cpu.getSP();
        iblock.rp = cpu.getRSP();
        
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
			if (instBuffer[0] == null || instBuffer[0].getOp1() == null || ((MachineOperandF99) instBuffer[0].getOp1()).encoding == MachineOperandF99.OP_ENC_IMM16)
				instBuffer[1] = getInstruction(thisPc, 1, opword);
			else
				instBuffer[1] = null;
			if (instBuffer[1] == null || instBuffer[1].getOp1() == null || ((MachineOperandF99) instBuffer[1].getOp1()).encoding == MachineOperandF99.OP_ENC_IMM16)
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
		return MachineOperandF99.createImmediateOperand(opcode, 
				mask == 0x7 ? MachineOperandF99.OP_ENC_IMM3 : MachineOperandF99.OP_ENC_IMM6);

	}
	
	private InstructionF99 getInstruction(short pc, int index, int opword) {
		int opcode = (opword >> fieldIndices[index]) & fieldMasks[index];
		if (opcode == 0)
			return null;
		
		InstructionF99 inst = new InstructionF99();
		inst.pc = pc;
		inst.opcode = opcode;
		inst.setInst(opcode);
		
		switch (opcode) {
		case InstF99.IfieldLiteral:
		case InstF99.I0fieldBranch:
		case InstF99.IfieldBranch:
			if (index == 2) {
				short next = memory.readWord(iblock.pc);
				iblock.pc++;			
				inst.setOp1(readSignedField(0, next));
			} else {
				inst.setOp1(readSignedField(index + 1, iblock.op));
			}
			break;
		case InstF99.Iliteral:
		case InstF99.I0branch:
		case InstF99.Ibranch:
			inst.setOp1(MachineOperandF99.createImmediateOperand(memory.readWord(iblock.pc & ~1), MachineOperandF99.OP_ENC_IMM16));
			iblock.pc = (short) ((iblock.pc & ~1) + 2);
			break;
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
        case InstF99.Ifetch:
        	cpu.push(memory.readWord(cpu.pop()));
        	break;
        case InstF99.Istore:
        	memory.writeWord(cpu.pop(), cpu.pop());
        	break;
        case InstF99.IfieldLiteral:
        case InstF99.Iliteral:
        	cpu.push(mop1.immed);
        	break;
        case InstF99.Iexit:
        	cpu.setPC(cpu.rpop());
        	break;
        case InstF99.Idup:
        	cpu.push(cpu.peek());
        	break;
        case InstF99.I0branch: {
        	short targ = (short) (iblock.pc + mop1.immed);
        	if (cpu.pop() == 0)
        		cpu.setPC(targ);
        	break;
        }
        case InstF99.Ibranch: {
        	short targ = (short) (iblock.pc + mop1.immed);
        	cpu.setPC(targ);
        	break;
        }
        case InstF99.InegOne:
        	cpu.push((short) -1);
        	break;
        case InstF99.Izero:
        	cpu.push((short) 0);
        	break;
        case InstF99.Ione:
        	cpu.push((short) 1);
        	break;
        case InstF99.Itwo:
        	cpu.push((short) 2);
        	break;
        case InstF99.I0lt:
        	cpu.push((short) (cpu.pop() < 0 ? -1 : 0));
        	break;
        case InstF99.Ilt: {
        	int right = cpu.pop();
        	cpu.push((short) (cpu.pop() < right ? -1 : 0));
        	break;
        }
        case InstF99.Iult: {
        	int right = cpu.pop() & 0xffff;
        	cpu.push((short) ((cpu.pop() & 0xffff) < right ? -1 : 0));
        	break;
        }
        case InstF99.I0equ:
        	cpu.push((short) (cpu.pop() == 0 ? -1 : 0));
        	break;
        case InstF99.Iequ:
        	cpu.push((short) (cpu.pop() == cpu.pop() ? -1 : 0));
        	break;
        	
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
    	default:
    		throw new UnsupportedOperationException("" + ins);
        }

    }
}