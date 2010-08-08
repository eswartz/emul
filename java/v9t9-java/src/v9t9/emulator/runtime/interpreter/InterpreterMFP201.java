/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.runtime.interpreter;

import java.util.HashMap;
import java.util.Map;

import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.emulator.runtime.cpu.CpuMFP201;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.cpu.*;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;

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
    //Instruction[] instructions; 
    
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

        BaseMachineOperand mop1 = (BaseMachineOperand) ins.getOp1();
        BaseMachineOperand mop2 = (BaseMachineOperand) ins.getOp2();

        /* get current operand values and instruction timings */
        fetchOperands(cpu, ins, cpu.getWP(), cpu.getStatus());

        /* do pre-instruction status word updates */
        if (ins.getInfo().stsetBefore != InstructionMFP201.st_NONE) {
            updateStatus(ins.getInfo().stsetBefore);
        }

        /* execute */
        interpret(ins);
        
        /* do post-instruction status word updates */
        if (ins.getInfo().stsetAfter != InstructionMFP201.st_NONE) {
            updateStatus(ins.getInfo().stsetAfter);
        }

        /* save any operands */
        flushOperands(cpu, ins);
        
        cpu.addCycles(ins.getInfo().cycles + mop1.cycles + mop2.cycles);
	}

	private void executeAndListen(InstructionListener[] instructionListeners) { 
		//PrintWriter dump = machine.getExecutor().getDump();
		//PrintWriter dumpfull = machine.getExecutor().getDumpfull();
		
        InstructionMFP201 ins = getInstruction(cpu, op_x);
        
        BaseMachineOperand mop1 = (BaseMachineOperand) ins.getOp1();
        BaseMachineOperand mop2 = (BaseMachineOperand) ins.getOp2();

        /*
        if (dumpfull != null) {
            dumpFullStart(ins, dumpfull);
        }
        if (dump != null) {
            dumpStart(cpu, ins, dump);
        }*/

        iblock.cycles = cpu.getCurrentCycleCount();
        
        /* get current operand values and instruction timings */
        fetchOperands(cpu, ins, cpu.getStatus());

        InstructionWorkBlock block = new InstructionWorkBlock(cpu);
        this.iblock.copyTo(block);
        
        /* dump values before execution */
        /*
        if (dumpfull != null) {
            dumpFullMid(mop1, mop2, dumpfull);
        }*/

        /* do pre-instruction status word updates */
        if (ins.getInfo().stsetBefore != InstructionMFP201.st_NONE) {
            updateStatus(ins.getInfo().stsetBefore);
        }

        /* execute */
        interpret(ins);
        
        /* do post-instruction status word updates */
        if (ins.getInfo().stsetAfter != InstructionMFP201.st_NONE) {
            updateStatus(ins.getInfo().stsetAfter);
        }

        /* save any operands */
        flushOperands(cpu, ins);
        
        cpu.addCycles(ins.getInfo().cycles + mop1.cycles + mop2.cycles);

        block.cycles = cpu.getCurrentCycleCount();
        
        /* dump values after execution */
        /*
        if (dumpfull != null) {
            dumpFullEnd(cpu, origCycleCount, mop1, mop2, dumpfull);
        }*/
		
        /* notify listeners */
        if (instructionListeners != null) {
        	for (InstructionListener listener : instructionListeners) {
        		listener.executed(block, iblock);
        	}
        }
	}

	private InstructionMFP201 getInstruction() {
		InstructionMFP201 ins;
	    int pc = cpu.getPC();
	    
	    short op;
	    MemoryDomain console = cpu.getConsole();
    	MemoryEntry entry = console.getEntryAt(pc);
    	op = entry.readWord(pc);
    	MemoryArea area = entry.getArea();
    	InstructionMFP201[] instructions = parsedInstructions.get(area);
    	if (instructions == null) {
    		instructions = new InstructionMFP201[65536];
    		parsedInstructions.put(area, instructions);
    	}
    	if ((ins = instructions[pc]) != null) {
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
    	instructions[pc] = ins;
		return ins;
	}

    /** Fetch operands for instruction (runtime)
     * @param ins
     * @param memory2
     */
    private void fetchOperands(InstructionMFP201 ins) {
        iblock.inst = ins;
        iblock.pc = (short) (iblock.inst.pc + iblock.inst.getSize());
        iblock.st = cpu.getST();
        
        MachineOperandMFP201 mop1 = (MachineOperandMFP201) iblock.inst.getOp1();
        MachineOperandMFP201 mop2 = (MachineOperandMFP201) iblock.inst.getOp2();
        MachineOperandMFP201 mop3 = (MachineOperandMFP201) iblock.inst.getOp3();

        if (mop1.type != MachineOperand.OP_NONE) {
        	mop1.cycles = 0;
			iblock.ea1 = mop1.getEA(iblock);
		}
        if (mop2.type != MachineOperand.OP_NONE) {
        	mop2.cycles = 0;
			iblock.ea2 = mop2.getEA(iblock);
		}
        if (mop3.type != MachineOperand.OP_NONE) {
        	mop3.cycles = 0;
        	iblock.ea3 = mop3.getEA(iblock);
        }
        if (mop1.type != MachineOperand.OP_NONE) {
        	iblock.val1 = mop1.getValue(iblock, iblock.ea1);
		}
        if (mop2.type != MachineOperand.OP_NONE) {
			iblock.val2 = mop2.getValue(iblock, iblock.ea2);
		}
        if (mop3.type != MachineOperand.OP_NONE) {
        	iblock.val3 = mop3.getValue(iblock, iblock.ea3);
        }
        if (iblock.inst.getInst() == InstMFP201.Idiv) {
            iblock.val3 = memory.readWord(iblock.ea2 + 2);
        }
    }

    /**
     * 
     */
    private void flushOperands(InstructionMFP201 ins) {
        BaseMachineOperand mop1 = (BaseMachineOperand) ins.getOp1();
        BaseMachineOperand mop2 = (BaseMachineOperand) ins.getOp2();
        BaseMachineOperand mop3 = (BaseMachineOperand) ins.getOp3();
        
        if (mop1.dest != Operand.OP_DEST_FALSE) {
            if (mop1.byteop) {
				memory.writeByte(iblock.ea1, (byte) iblock.val1);
			} else {
				memory.writeWord(iblock.ea1, iblock.val1);
			}
				
        }
        if (mop2.dest != Operand.OP_DEST_FALSE) {
            if (mop2.byteop) {
				memory.writeByte(iblock.ea2, (byte) iblock.val2);
			} else {
                memory.writeWord(iblock.ea2, iblock.val2);
                if (ins.getInst() == InstMFP201.Imuld 
                		|| ins.getInst() == InstMFP201.Idivd) {
                    memory.writeWord(iblock.ea2 + 2, iblock.val3);
                }
            }
        }

        if ((ins.getInfo().writes & InstInfo.INST_RSRC_ST) != 0) {
			cpu.setST(iblock.st);
		}

        /* do this after flushing status */
        cpu.setPC(iblock.pc);
    }

    /**
     */
    private void updateStatus(int handler) {
        switch (handler) {
       
        default:
            throw new AssertionError("unhandled status handler " + handler);
        }

    }

    /**
     * Execute an instruction
     * @param ins
     */
    private void interpret(InstructionMFP201 ins) {
    	StatusMFP201 status = (StatusMFP201) iblock.status;
        switch (ins.getInst()) {
       
        case InstTableCommon.Idbg:
        	int oldCount = machine.getExecutor().debugCount; 
        	if (iblock.val1 == 0)
        		machine.getExecutor().debugCount++;
        	else
        		machine.getExecutor().debugCount--;
        	if ((oldCount == 0) != (machine.getExecutor().debugCount == 0))
        		Executor.settingDumpFullInstructions.setBoolean(iblock.val1 == 0);
        	break;
        	
        	
        }
    }
}