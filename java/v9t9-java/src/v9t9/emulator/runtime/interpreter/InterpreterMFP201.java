/*
 * 8 Aug 2010 
 */
package v9t9.emulator.runtime.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.emulator.runtime.cpu.CpuMFP201;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.cpu.*;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;

import static v9t9.engine.cpu.InstMFP201.*;
import static v9t9.engine.cpu.MachineOperandMFP201.*;

/**
 * This class interprets MFP201 instructions one by one.
 * 
 * @author ejs
 */
public class InterpreterMFP201 implements Interpreter {
	Machine machine;

    MemoryDomain memory;

    // per-PC prebuilt instructions
    Map<MemoryArea, InstructionMFP201[]> parsedInstructions; 
    
    InstructionWorkBlock iblock;

	private CpuMFP201 cpu;

	private StatusMFP201 status;

    public InterpreterMFP201(Machine machine) {
        this.machine = machine;
        this.cpu = (CpuMFP201) machine.getCpu();
        this.memory = machine.getCpu().getConsole();
        //instructions = new Instruction[65536/2];// HashMap<Integer, Instruction>();
        parsedInstructions = new HashMap<MemoryArea, InstructionMFP201[]>();
        iblock = new InstructionWorkBlock(cpu);
        iblock.domain = memory;
        this.status = (StatusMFP201) cpu.getStatus();
     }

    /* (non-Javadoc)
     * @see v9t9.emulator.runtime.interpreter.Interpreter#executeChunk(int, v9t9.emulator.runtime.cpu.Executor)
     */
    @Override
    public void executeChunk(int numinsts, Executor executor) {
    	InstructionListener[] instructionListeners = machine.getExecutor().getInstructionListeners();
    	if (instructionListeners != null) {
    		while (numinsts-- > 0) {
    			executeAndListen(instructionListeners);
    			executor.nInstructions++;
    			if (executor.interruptExecution)
    				break;
    		}
    	} else {
    		while (numinsts-- > 0) {
    			executeFast();
    			executor.nInstructions++;
    			if (executor.interruptExecution)
    				break;
    		}
    	}
    }

    public void executeFast() {
        InstructionMFP201 ins = getInstruction();
        InstInfo info = ins.getInfo();

        /* get current operand values and instruction timings */
        fetchOperands(ins);

        /* execute */
        interpret(ins);

        /* save any operands */
        flushOperands(ins, iblock);
        
        cpu.addCycles(info.cycles);
	}

	private void executeAndListen(InstructionListener[] instructionListeners) { 
        InstructionMFP201 ins = getInstruction();
        
        iblock.cycles = cpu.getCurrentCycleCount();
        
        /* get current operand values and instruction timings */
        fetchOperands(ins);

        InstructionWorkBlock block = new InstructionWorkBlock(cpu);
        this.iblock.copyTo(block);

        /* execute */
        interpret(ins);

        /* save any operands */
        flushOperands(ins, iblock);
        
        cpu.addCycles(ins.getInfo().cycles);

        block.cycles = cpu.getCurrentCycleCount();
        
        /* notify listeners */
        if (instructionListeners != null) {
        	for (InstructionListener listener : instructionListeners) {
        		listener.executed(block, iblock);
        	}
        }
	}

	private InstructionMFP201 getInstruction() {
		InstructionMFP201 ins;
	    int pc = cpu.getPC() & 0xffff;
	    
	    MemoryDomain console = cpu.getConsole();
    	MemoryEntry entry = console.getEntryAt(pc);
    	MemoryArea area = entry.getArea();
    	
    	InstructionMFP201[] instructions = parsedInstructions.get(area);
    	if (instructions == null) {
    		instructions = new InstructionMFP201[entry.size];
    		parsedInstructions.put(area, instructions);
    	}
    	if ((ins = instructions[pc - entry.addr]) != null) {
    		if (area.hasWriteAccess()) {
    			// see if memory changed
    			long opcode = 0;
    			int sz = ins.getSize();
    			for (int i = 0; i < sz; i++) {
    				opcode = (opcode << 8) | (console.flatReadByte(pc + i) & 0xff);
    			}
    			if (opcode != ins.opcode) {
    				ins = null;
    			}
    		}
    	}
    	if (ins == null) {
    		ins = new InstructionMFP201(InstTableMFP201.decodeInstruction(pc, console));
    	}
    	instructions[pc - entry.addr] = ins;
		return ins;
	}

    /** Fetch operands for instruction (runtime)
     * @param ins
     */
    private void fetchOperands(InstructionMFP201 ins) {
        iblock.inst = ins;
        iblock.pc = (short) (iblock.inst.pc + iblock.inst.getSize());
        iblock.st = cpu.getST();
        
        MachineOperandMFP201 mop1 = (MachineOperandMFP201) iblock.inst.getOp1();
        MachineOperandMFP201 mop2 = (MachineOperandMFP201) iblock.inst.getOp2();
        MachineOperandMFP201 mop3 = (MachineOperandMFP201) iblock.inst.getOp3();

        if (mop1 != null && mop1.type != MachineOperand.OP_NONE) {
			iblock.ea1 = mop1.getEA(iblock);
		}
        if (mop2 != null && mop2.type != MachineOperand.OP_NONE) {
			iblock.ea2 = mop2.getEA(iblock);
		}
        if (mop3 != null && mop3.type != MachineOperand.OP_NONE) {
        	iblock.ea3 = mop3.getEA(iblock);
        }
        if (mop1 != null && mop1.type != MachineOperand.OP_NONE) {
        	iblock.val1 = mop1.getValue(iblock, iblock.ea1);
		}
        if (mop2 != null && mop2.type != MachineOperand.OP_NONE) {
			iblock.val2 = mop2.getValue(iblock, iblock.ea2);
		}
        if (mop3 != null && mop3.type != MachineOperand.OP_NONE) {
        	iblock.val3 = mop3.getValue(iblock, iblock.ea3);
        }
        if (iblock.inst.getInst() == Idiv) {
            iblock.val3 = memory.readWord(iblock.ea2 + 2);
        }
    }

    /**
     * @param iblock TODO
     * 
     */
    private void flushOperands(InstructionMFP201 ins, InstructionWorkBlock iblock) {
    	MachineOperandMFP201 mop1 = (MachineOperandMFP201) ins.getOp1();
        MachineOperandMFP201 mop2 = (MachineOperandMFP201) ins.getOp2();
        MachineOperandMFP201 mop3 = (MachineOperandMFP201) ins.getOp3();
        
        if (mop1 != null && mop1.dest != Operand.OP_DEST_FALSE) {
        	mop1.putValue(iblock, iblock.ea1, iblock.val1);
        }
        if (mop2 != null && mop2.dest != Operand.OP_DEST_FALSE) {
        	mop2.putValue(iblock, iblock.ea2, iblock.val2);
        }
        if (mop3 != null && mop3.dest != Operand.OP_DEST_FALSE) {
        	mop3.putValue(iblock, iblock.ea3, iblock.val3);
        }
        if ((ins.getInfo().writes & InstInfo.INST_RSRC_ST) != 0) {
			cpu.setST(iblock.st);
		}

        /* do this after flushing status */
        cpu.setPC(iblock.pc);
    }

    /**
     * Execute an instruction
     * @param ins
     */
    private void interpret(InstructionMFP201 ins) {
    	MachineOperandMFP201 mop1 = (MachineOperandMFP201) ins.getOp1();
        MachineOperandMFP201 mop2 = (MachineOperandMFP201) ins.getOp2();
        MachineOperandMFP201 mop3 = (MachineOperandMFP201) ins.getOp3();
        
        InstInfo info = ins.getInfo();
        
    	System.out.println(HexUtils.toHex4(ins.pc) + "(" + ins.getSize() + "): " + ins);
        switch (ins.getInst()) {
       
        case Ibkpt:
        	cpu.push(iblock.pc);
        	iblock.pc = cpu.readIntVec(CpuMFP201.INT_BKPT);
        	break;
        	
        case Icall:
        case Icalla:
        	cpu.push(iblock.pc);
        	
        case Ibr:
        case Ibra:
        	iblock.pc = iblock.val1;
        	break;
        	
        case Ildc:
        	iblock.val2 = iblock.val1;
        	break;
        	
        case Ior:
        case Iorb:
        case Iorq:
        case Iorbq:
        	iblock.val2 |= iblock.val1;
        	break;
        	
        case Iand:
        case Iandb:
        case Itst:
        case Itstb:
        	iblock.val2 &= iblock.val1;
        	break;
        	
        case Inand:
        case Inandb:
        case Itstn:
        case Itstnb:
        	iblock.val2 &= ~iblock.val1;
        	break;
        	
        case Ixor:
        case Ixorb:
        case Ixorq:
        case Ixorbq:
        	iblock.val2 ^= iblock.val1;
        	break;
        	
        case Ipush:
        case Ipushb:
        	cpu.push(iblock.val1);
        	break;
        
        case Ipop:
        case Ipopb:
        	iblock.val1 = cpu.pop();
        	break;
        	
        case Ipushn:
        case Ipushnb:
    		while (iblock.val1-- > 0) {
    			short val;
    			if (mop2.type == OP_REG) {
    				val = (short) cpu.getRegister(iblock.val2++);
    				if (iblock.inst.byteop)
    					val &= 0xff;
    			}
    			else {
    				val = iblock.domain.readWord(iblock.val2);
    				iblock.val2 += iblock.inst.byteop ? 1 : 2;
    			}
    			cpu.push(val);
    		}
        	break;
        
        case Ipopn:
        case Ipopnb:
    		while (iblock.val1-- > 0) {
    			short val = cpu.pop();
    			if (iblock.inst.byteop)
    				val &= 0xff;
    			if (mop2.type == OP_REG) {
    				cpu.setRegister(iblock.val2+iblock.val1, val);
    			}
    			else {
    				iblock.domain.writeWord(iblock.val2 + (iblock.inst.byteop ? 1 : 2) * iblock.val1, val);
    			}
    		}
        	break;
        	
        case Ijmp:
        	iblock.pc = iblock.val1;
        	info.cycles++;
        	break;
        case Ijc:
        	if (status.isC()) {
	        	iblock.pc = iblock.val1;
	        	info.cycles++;
        	}
        	break;
        case Ijnc:
        	if (!status.isC()) {
	        	iblock.pc = iblock.val1;
	        	info.cycles++;
        	}
        	break;
        case Ijeq:
        	if (status.isEQ()) {
	        	iblock.pc = iblock.val1;
	        	info.cycles++;
        	}
        	break;
        case Ijne:
        	if (!status.isEQ()) {
	        	iblock.pc = iblock.val1;
	        	info.cycles++;
        	}
        	break;
        case Ijn:
        	if (status.isN()) {
	        	iblock.pc = iblock.val1;
	        	info.cycles++;
        	}
        	break;
        case Ijge:
        	if (status.isGE()) {
	        	iblock.pc = iblock.val1;
	        	info.cycles++;
        	}
        	break;
        case Ijl:
        	if (status.isLT()) {
        		iblock.pc = iblock.val1;
        		info.cycles++;
        	}
        	break;

        case Imov:
        case Imovb:
        	iblock.val2 = iblock.val1;
        	break;
        case Imovc:
        case Imovcb:
        	iblock.val2 = status.isC() ? iblock.val1 : iblock.val2;
        	break;
        case Imovnc:
        case Imovncb:
        	iblock.val2 = !status.isC() ? iblock.val1 : iblock.val2;
        	break;
        case Imoveq:
        case Imoveqb:
        	iblock.val2 = status.isEQ() ? iblock.val1 : iblock.val2;
        	break;
        case Imovne:
        case Imovneb:
        	iblock.val2 = status.isNE() ? iblock.val1 : iblock.val2;
        	break;
        case Imovn:
        case Imovnb:
        	iblock.val2 = status.isN() ? iblock.val1 : iblock.val2;
        	break;
        case Imovge:
        case Imovgeb:
        	iblock.val2 = status.isGE() ? iblock.val1 : iblock.val2;
        	break;
        case Imovl:
        case Imovlb:
        	iblock.val2 = status.isLT() ? iblock.val1 : iblock.val2;
        	break;

        case Iadd:
        case Iaddb:
        	if (ins.getOp3() != null)
        		iblock.val3 = (short) (iblock.val1 + iblock.val2);
        	else
        		iblock.val2 += iblock.val1;
        	break;
        case Iadc:
        case Iadcb:
        	if (ins.getOp3() != null)
        		iblock.val3 = (short) (iblock.val1 + iblock.val2 + (status.isC() ? 1 : 0));
        	else
        		iblock.val2 += iblock.val1 + (status.isC() ? 1 : 0);
        	break;
        case Isub:
        case Isubb:
        	if (ins.getOp3() != null)
        		iblock.val3 = (short) (iblock.val1 - iblock.val2);
        	else
        		iblock.val2 -= iblock.val1;
        	break;
        case Isbb:
        case Isbbb:
        	if (ins.getOp3() != null)
        		iblock.val3 = (short) (iblock.val1 - iblock.val2 - (status.isC() ? 1 : 0));
        	else
        		iblock.val2 -= iblock.val1 + (status.isC() ? 1 : 0);
        	break;

        case Iloop: 
        case Iloopc: 
        case Iloopnc: 
        case Iloopeq: 
        case Iloopne: 
        case Iloopn: 
        case Iloopge: 
        case Iloopl: 
        {
        	InstructionWorkBlock lblock = new InstructionWorkBlock(cpu);
        	iblock.copyTo(lblock);
        	
        	InstructionMFP201 subinst = ((MachineOperandMFP201Inst) ins.getOp2()).inst;
        	int loopCycles = subinst.getInfo().cycles;
        	
        	while (lblock.val1 > 0) {
                fetchOperands(subinst);
                interpret(subinst);
                flushOperands(subinst, iblock);		// note: changes to PC ignored
                
                cpu.addCycles(loopCycles);
                
                boolean term = false;
                switch (ins.getInst()) {
                case Iloopc: 
                	term = status.isC(); break;
                case Iloopnc: 
                	term = !status.isC(); break;
                case Iloopeq: 
                	term = status.isEQ(); break;
                case Iloopne: 
                	term = status.isNE(); break;
                case Iloopn: 
                	term = status.isN(); break;
                case Iloopge: 
                	term = status.isGE(); break;
                case Iloopl: 
                	term = status.isLT(); break;
                }
                
                if (term)
                	break;
                
        		lblock.val1--;
        		flushOperands(subinst, lblock);	// record change to loop
        	}
        	
        	lblock.copyTo(iblock);
        	break;
        }
        	
        case InstTableCommon.Idbg:
        	int oldCount = machine.getExecutor().debugCount; 
        	if (iblock.val1 == 0)
        		machine.getExecutor().debugCount++;
        	else
        		machine.getExecutor().debugCount--;
        	if ((oldCount == 0) != (machine.getExecutor().debugCount == 0))
        		Executor.settingDumpFullInstructions.setBoolean(iblock.val1 == 0);
        	break;
        	
        default:
        	System.err.println("unhandled:" + ins);
        	break;
        }
    }
}