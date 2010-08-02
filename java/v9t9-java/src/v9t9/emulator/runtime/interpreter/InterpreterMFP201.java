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
import v9t9.emulator.hardware.TI99Machine;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.emulator.runtime.cpu.Cpu9900;
import v9t9.emulator.runtime.cpu.CpuMFP201;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.cpu.BaseMachineOperand;
import v9t9.engine.cpu.Inst9900;
import v9t9.engine.cpu.InstInfo;
import v9t9.engine.cpu.InstTableCommon;
import v9t9.engine.cpu.Instruction9900;
import v9t9.engine.cpu.InstTable9900;
import v9t9.engine.cpu.InstructionWorkBlock;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.MachineOperand9900;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.cpu.Status9900;
import v9t9.engine.cpu.StatusMFP201;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;

/**
 * This class interprets 9900 instructions one by one.
 * 
 * @author ejs
 */
public class InterpreterMFP201 implements Interpreter {
	Machine machine;

    MemoryDomain memory;

    // per-PC prebuilt instructions
    Map<MemoryArea, Instruction9900[]> parsedInstructions; 
    //Instruction[] instructions; 
    
    InstructionWorkBlock iblock;

	private CpuMFP201 cpu;

    public InterpreterMFP201(Machine machine) {
        this.machine = machine;
        this.cpu = (CpuMFP201) machine.getCpu();
        this.memory = machine.getCpu().getConsole();
        //instructions = new Instruction[65536/2];// HashMap<Integer, Instruction>();
        parsedInstructions = new HashMap<MemoryArea, Instruction9900[]>();
        iblock = new InstructionWorkBlock(cpu.createStatus());
        iblock.domain = memory;
     }

    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.interpreter.Interpreter#execute(java.lang.Short)
	 */
    public void execute(Short op_x) {
    	InstructionListener[] instructionListeners = machine.getExecutor().getInstructionListeners();
    	if (instructionListeners != null) {
    		executeAndListen(op_x, instructionListeners);
    	} else {
    		executeFast(op_x);
    	}
    	
        /* dump instruction */
        //PrintWriter dumpfull = machine.getExecutor().getDumpfull(); 
        //PrintWriter dump = machine.getExecutor().getDump();
        
        //if (dumpfull != null || dump != null || Machine.settingDebugTracing.getBoolean()) {
        //} else {
        //}

    }

    /* (non-Javadoc)
	 * @see v9t9.emulator.runtime.interpreter.Interpreter#executeFast(java.lang.Short)
	 */
    public void executeFast(Short op_x) {
        Instruction9900 ins = getInstruction(cpu, op_x);

        BaseMachineOperand mop1 = (BaseMachineOperand) ins.getOp1();
        BaseMachineOperand mop2 = (BaseMachineOperand) ins.getOp2();

        /* get current operand values and instruction timings */
        fetchOperands(cpu, ins, cpu.getWP(), cpu.getStatus());

        /* do pre-instruction status word updates */
        if (ins.info.stsetBefore != Instruction9900.st_NONE) {
            updateStatus(ins.info.stsetBefore);
        }

        /* execute */
        interpret(ins);
        
        /* do post-instruction status word updates */
        if (ins.info.stsetAfter != Instruction9900.st_NONE) {
            updateStatus(ins.info.stsetAfter);
        }

        /* save any operands */
        flushOperands(cpu, ins);
        
        cpu.addCycles(ins.info.cycles + mop1.cycles + mop2.cycles);
	}

	private void executeAndListen(Short op_x, InstructionListener[] instructionListeners) { 
		//PrintWriter dump = machine.getExecutor().getDump();
		//PrintWriter dumpfull = machine.getExecutor().getDumpfull();
		
        Instruction9900 ins = getInstruction(cpu, op_x);
        
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
        fetchOperands(cpu, ins, cpu.getWP(), cpu.getStatus());

        InstructionWorkBlock block = new InstructionWorkBlock(cpu.createStatus());
        this.iblock.copyTo(block);
        
        /* dump values before execution */
        /*
        if (dumpfull != null) {
            dumpFullMid(mop1, mop2, dumpfull);
        }*/

        /* do pre-instruction status word updates */
        if (ins.info.stsetBefore != Instruction9900.st_NONE) {
            updateStatus(ins.info.stsetBefore);
        }

        /* execute */
        interpret(ins);
        
        /* do post-instruction status word updates */
        if (ins.info.stsetAfter != Instruction9900.st_NONE) {
            updateStatus(ins.info.stsetAfter);
        }

        /* save any operands */
        flushOperands(cpu, ins);
        
        cpu.addCycles(ins.info.cycles + mop1.cycles + mop2.cycles);

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

	private RawInstruction getInstruction(Short op_x) {
		Instruction9900 ins;
	    int pc = cpu.getPC() & 0xfffe;
	    
	    short op;
	    MemoryDomain console = cpu.getConsole();
		if (op_x != null) {
	    	op = op_x;
	    	ins = new Instruction9900(InstTable9900.decodeInstruction(op, pc, console));
	    } else {
	    	MemoryEntry entry = console.getEntryAt(pc);
	    	op = entry.readWord(pc);
	    	MemoryArea area = entry.getArea();
	    	Instruction9900[] instructions = parsedInstructions.get(area);
	    	if (instructions == null) {
	    		instructions = new Instruction9900[65536/2];
	    		parsedInstructions.put(area, instructions);
	    	}
	    	if ((ins = instructions[pc/2]) != null) {
	    		// expensive (10%)
	    		ins = ins.update(op, pc, console);
	    	} else {
	    		ins = new Instruction9900(InstTable9900.decodeInstruction(op, pc, console));
	    	}
	    	instructions[pc/2] = ins;
	    }
		return ins;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.interpreter.Interpreter#getInstruction(v9t9.emulator.runtime.Cpu9900)
	 */
	public RawInstruction getInstruction() {
		return getInstruction(null);
	}

    /** Fetch operands for instruction (runtime)
     * @param ins
     * @param memory2
     */
    private void fetchOperands(Instruction9900 ins, short wp, Status9900 st) {
        iblock.inst = ins;
        iblock.pc = (short) (iblock.inst.pc + iblock.inst.size);
        iblock.wp = cpu.getWP();
        iblock.status = st;
        
        MachineOperand9900 mop1 = (MachineOperand9900) iblock.inst.getOp1();
        MachineOperand9900 mop2 = (MachineOperand9900) iblock.inst.getOp2();

        if (mop1.type != MachineOperand.OP_NONE) {
        	mop1.cycles = 0;
			iblock.ea1 = mop1.getEA(memory, iblock.inst.pc, wp);
		}
        if (mop2.type != MachineOperand.OP_NONE) {
        	mop2.cycles = 0;
			iblock.ea2 = mop2.getEA(memory, iblock.inst.pc, wp);
		}
        if (mop1.type != MachineOperand.OP_NONE) {
        	//if (ins.inst != InstructionTable.Ili)		// even LI will read in the real hardware
        	iblock.val1 = mop1.getValue(memory, iblock.ea1);
		}
        if (mop2.type != MachineOperand.OP_NONE) {
			iblock.val2 = mop2.getValue(memory, iblock.ea2);
		}
        if (iblock.inst.inst == Inst9900.Idiv) {
            iblock.val3 = memory.readWord(iblock.ea2 + 2);
        }
    }

    /**
     * 
     */
    private void flushOperands(Instruction9900 ins) {
        BaseMachineOperand mop1 = (BaseMachineOperand) ins.getOp1();
        BaseMachineOperand mop2 = (BaseMachineOperand) ins.getOp2();
        if (mop1.dest != Operand.OP_DEST_FALSE) {
            if (mop1.byteop) {
				memory.writeByte(iblock.ea1, (byte) iblock.val1);
			} else {
				memory.writeWord(iblock.ea1, iblock.val1);
				if (ins.inst == InstTableCommon.Iticks) {
					memory.writeWord(iblock.ea1 + 2, iblock.val2);
				}
			}
				
        }
        if (mop2.dest != Operand.OP_DEST_FALSE) {
        	if (ins.inst == Inst9900.Icb)
        		mop2.dest = 1;
            if (mop2.byteop) {
				memory.writeByte(iblock.ea2, (byte) iblock.val2);
			} else {
                memory.writeWord(iblock.ea2, iblock.val2);
                if (ins.inst == Inst9900.Impy 
                		|| ins.inst == Inst9900.Idiv) {
                    memory.writeWord(iblock.ea2 + 2, iblock.val3);
                }
            }
        }

        if ((ins.info.writes & InstInfo.INST_RSRC_ST) != 0) {
			cpu.setStatus(iblock.status);
		}

        /* do this after flushing status */
        if ((ins.info.writes & InstInfo.INST_RSRC_CTX) != 0) {
            /* update PC first */
            cpu.setPC((short) (iblock.inst.pc + iblock.inst.size));
            cpu.contextSwitch(iblock.wp, iblock.pc);
        } else {
            /* flush register changes */
            cpu.setPC(iblock.pc);
            if ((ins.info.writes & InstInfo.INST_RSRC_WP) != 0) {
				cpu.setWP(iblock.wp);
			}
        }
    }

    /**
     */
    private void updateStatus(int handler) {
    	StatusMFP201 status = (StatusMFP201) iblock.status;
        switch (handler) {
       
        default:
            throw new AssertionError("unhandled status handler " + handler);
        }

    }

    /**
     * Execute an instruction
     * @param ins
     */
    private void interpret(Instruction9900 ins) {
    	StatusMFP201 status = (StatusMFP201) iblock.status;
        switch (ins.inst) {
       
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