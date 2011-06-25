/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.runtime.interpreter;

import java.util.HashMap;
import java.util.Map;

import v9t9.emulator.hardware.TI99Machine;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.emulator.runtime.cpu.Cpu9900;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.cpu.Inst9900;
import v9t9.engine.cpu.InstInfo;
import v9t9.engine.cpu.InstTableCommon;
import v9t9.engine.cpu.Instruction9900;
import v9t9.engine.cpu.InstTable9900;
import v9t9.engine.cpu.InstructionWorkBlock;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.MachineOperand9900;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.Status9900;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;

/**
 * This class interprets 9900 instructions one by one.
 * 
 * @author ejs
 */
public class Interpreter9900 implements Interpreter {
	TI99Machine machine;

    MemoryDomain memory;

    // per-PC prebuilt instructions
    Map<MemoryArea, Instruction9900[]> parsedInstructions; 
    //Instruction[] instructions; 
    
    InstructionWorkBlock iblock;

	private Cpu9900 cpu;

	private Status9900 status;

    public Interpreter9900(TI99Machine machine) {
        this.machine = machine;
        this.cpu = (Cpu9900) machine.getCpu();
        this.memory = machine.getCpu().getConsole();
        //instructions = new Instruction[65536/2];// HashMap<Integer, Instruction>();
        parsedInstructions = new HashMap<MemoryArea, Instruction9900[]>();
        iblock = new InstructionWorkBlock(cpu);
        iblock.domain = memory;
        status = (Status9900) cpu.createStatus();
     }

    /* (non-Javadoc)
     * @see v9t9.emulator.runtime.interpreter.Interpreter#dispose()
     */
    @Override
    public void dispose() {
    	
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
        Instruction9900 ins = getInstruction(op_x);

        MachineOperand9900 mop1 = (MachineOperand9900) ins.getOp1();
        MachineOperand9900 mop2 = (MachineOperand9900) ins.getOp2();

        /* get current operand values and instruction timings */
        fetchOperands(ins, op_x != null);

        /* do pre-instruction status word updates */
        if (ins.getInfo().stsetBefore != Instruction9900.st_NONE) {
            updateStatus(ins.getInfo().stsetBefore);
        }

        /* execute */
        interpret(ins);
        
        /* do post-instruction status word updates */
        if (ins.getInfo().stsetAfter != Instruction9900.st_NONE) {
            updateStatus(ins.getInfo().stsetAfter);
        }

        /* save any operands */
        flushOperands(ins);
        
        int cycles = ins.getInfo().cycles + mop1.cycles + mop2.cycles;
		cpu.addCycles(cycles);
	}
    
    /* (non-Javadoc)
     * @see v9t9.emulator.runtime.interpreter.Interpreter#executeChunk(int, v9t9.emulator.runtime.cpu.Executor)
     */
    @Override
    public void executeChunk(int numinsts, Executor executor) {
    	// pretend the realtime and instructionListeners settings don't change often
		if (executor.getInstructionListeners() == null) {
			for (int i = 0; i < numinsts; i++) {
				executeFast(null);
				executor.nInstructions++;
				cpu.checkAndHandleInterrupts();
				if (executor.interruptExecution)
					break;
			}
		} else {
			for (int i = 0; i < numinsts; i++) {
				execute(null);
				executor.nInstructions++;
				cpu.checkAndHandleInterrupts();
				if (executor.interruptExecution)
					break;
			}
		}    	
    }

	private void executeAndListen(Short op_x, InstructionListener[] instructionListeners) { 
        Instruction9900 ins = getInstruction(op_x);
        
        MachineOperand9900 mop1 = (MachineOperand9900) ins.getOp1();
        MachineOperand9900 mop2 = (MachineOperand9900) ins.getOp2();

        iblock.cycles = cpu.getCurrentCycleCount();
        
        /* get current operand values and instruction timings */
        fetchOperands(ins, op_x != null);

        InstructionWorkBlock block = new InstructionWorkBlock(cpu);
        this.iblock.copyTo(block);

        /* do pre-instruction status word updates */
        if (ins.getInfo().stsetBefore != Instruction9900.st_NONE) {
            updateStatus(ins.getInfo().stsetBefore);
        }

        /* execute */
        interpret(ins);
        
        /* do post-instruction status word updates */
        if (ins.getInfo().stsetAfter != Instruction9900.st_NONE) {
            updateStatus(ins.getInfo().stsetAfter);
        }

        /* save any operands */
        flushOperands(ins);
        
        cpu.addCycles(ins.getInfo().cycles + mop1.cycles + mop2.cycles);

        block.cycles = cpu.getCurrentCycleCount();
        
        /* notify listeners */
        if (instructionListeners != null) {
        	for (InstructionListener listener : instructionListeners) {
        		listener.executed(block, iblock);
        	}
        }
	}

	private Instruction9900 getInstruction(Short op_x) {
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

    /** Fetch operands for instruction (runtime)
     * @param ins
     * @param is_X 
     */
    private void fetchOperands(Instruction9900 ins, boolean is_X) {
    	if (is_X) {
    		ins.pc = iblock.inst.pc + iblock.inst.getSize() - 2;
    	}
    	iblock.inst = ins;
    	iblock.pc = (short) (iblock.inst.pc + iblock.inst.getSize());
        iblock.wp = (short) cpu.getWP();
        iblock.st = cpu.getST();
        status.expand(iblock.st);
        
        MachineOperand9900 mop1 = (MachineOperand9900) iblock.inst.getOp1();
        MachineOperand9900 mop2 = (MachineOperand9900) iblock.inst.getOp2();

        if (mop1.type != MachineOperand.OP_NONE) {
        	mop1.cycles = 0;
			iblock.ea1 = mop1.getEA(iblock);
		}
        if (mop2.type != MachineOperand.OP_NONE) {
        	mop2.cycles = 0;
			iblock.ea2 = mop2.getEA(iblock);
		}
        if (mop1.type != MachineOperand.OP_NONE) {
        	//if (ins.inst != InstructionTable.Ili)		// even LI will read in the real hardware
        	iblock.val1 = mop1.getValue(iblock, iblock.ea1);
		}
        if (mop2.type != MachineOperand.OP_NONE) {
			iblock.val2 = mop2.getValue(iblock, iblock.ea2);
		}
        if (iblock.inst.getInst() == Inst9900.Idiv) {
            iblock.val3 = memory.readWord(iblock.ea2 + 2);
        }
    }

    /**
     * 
     */
    private void flushOperands(Instruction9900 ins) {
    	MachineOperand9900 mop1 = (MachineOperand9900) ins.getOp1();
    	MachineOperand9900 mop2 = (MachineOperand9900) ins.getOp2();
        if (mop1.dest != Operand.OP_DEST_FALSE) {
            if (mop1.byteop) {
				memory.writeByte(iblock.ea1, (byte) iblock.val1);
			} else {
				memory.writeWord(iblock.ea1, iblock.val1);
				if (ins.getInst() == InstTableCommon.Iticks) {
					memory.writeWord(iblock.ea1 + 2, iblock.val2);
				}
			}
				
        }
        if (mop2.dest != Operand.OP_DEST_FALSE) {
        	if (ins.getInst() == Inst9900.Icb)
        		mop2.dest = 1;
            if (mop2.byteop) {
				memory.writeByte(iblock.ea2, (byte) iblock.val2);
			} else {
                memory.writeWord(iblock.ea2, iblock.val2);
                if (ins.getInst() == Inst9900.Impy 
                		|| ins.getInst() == Inst9900.Idiv) {
                    memory.writeWord(iblock.ea2 + 2, iblock.val3);
                }
            }
        }

        if ((ins.getInfo().writes & InstInfo.INST_RSRC_ST) != 0) {
			cpu.setStatus(status);
		}

        /* do this after flushing status */
        if ((ins.getInfo().writes & InstInfo.INST_RSRC_CTX) != 0) {
            /* update PC first */
            cpu.setPC((short) (iblock.inst.pc + iblock.inst.getSize()));
            cpu.contextSwitch(iblock.wp, iblock.pc);
        } else {
            /* flush register changes */
            cpu.setPC(iblock.pc);
            if ((ins.getInfo().writes & InstInfo.INST_RSRC_WP) != 0) {
				((Cpu9900) cpu).setWP(iblock.wp);
			}
        }
    }

    /**
     */
    private void updateStatus(int handler) {
        switch (handler) {
        case Instruction9900.st_NONE:
            return;
        case Instruction9900.st_ALL:
            // just a note that Status should be up to date, for future work
            return;
        case Instruction9900.st_INT:
            status.setIntMask(iblock.val1);
            break;
        case Instruction9900.st_ADD_BYTE_LAECOP:
            status.set_ADD_BYTE_LAECOP((byte) iblock.val2,
                    (byte) iblock.val1);
            break;
        case Instruction9900.st_ADD_LAECO:
            status.set_ADD_LAECO(iblock.val2, iblock.val1);
            break;
        case Instruction9900.st_ADD_LAECO_REV:
            status.set_ADD_LAECO(iblock.val1, iblock.val2);
            break;
        case Instruction9900.st_ADD_LAECO_REV_1:
        	status.set_ADD_LAECO(iblock.val1, (short) 1);
        	break;
        case Instruction9900.st_ADD_LAECO_REV_2:
        	status.set_ADD_LAECO(iblock.val1, (short) 2);
        	break;
        case Instruction9900.st_ADD_LAECO_REV_N1:
        	status.set_ADD_LAECO(iblock.val1, (short) -1);
        	break;
        case Instruction9900.st_ADD_LAECO_REV_N2:
        	status.set_ADD_LAECO(iblock.val1, (short) -2);
        	break;
        case Instruction9900.st_SUB_BYTE_LAECOP:
            status.set_SUB_BYTE_LAECOP((byte) iblock.val2,
                    (byte) iblock.val1);
            break;
        case Instruction9900.st_SUB_LAECO:
            status.set_SUB_LAECO(iblock.val2, iblock.val1);
            break;

        case Instruction9900.st_BYTE_CMP:
            status.set_BYTE_CMP((byte) iblock.val1,
                    (byte) iblock.val2);
            break;

        case Instruction9900.st_CMP:
            status.set_CMP(iblock.val1, iblock.val2);
            break;
        case Instruction9900.st_DIV_O:
            status
                    .set_O((iblock.val1 & 0xffff) <= (iblock.val2 & 0xffff));
            break;
        case Instruction9900.st_E:
            status.set_E(iblock.val1 == iblock.val2);
            break;
        case Instruction9900.st_LAE:
            status.set_LAE(iblock.val2);
            break;
        case Instruction9900.st_LAE_1:
            status.set_LAE(iblock.val1);
            break;

        case Instruction9900.st_BYTE_LAEP:
            status.set_BYTE_LAEP((byte) iblock.val2);
            break;
        case Instruction9900.st_BYTE_LAEP_1:
            status.set_BYTE_LAEP((byte) iblock.val1);
            break;

        case Instruction9900.st_LAEO:
            status.set_LAEO(iblock.val1);

        case Instruction9900.st_O:
            status.set_O(iblock.val1 == (short) 0x8000);
            break;

        case Instruction9900.st_SHIFT_LEFT_CO:
            status.set_SHIFT_LEFT_CO(iblock.val1, iblock.val2);
            break;
        case Instruction9900.st_SHIFT_RIGHT_C:
            status.set_SHIFT_RIGHT_C(iblock.val1, iblock.val2);
            break;

        case Instruction9900.st_XOP:
            status.set_X();
            break;

        default:
            throw new AssertionError("unhandled status handler " + handler);
        }

    }

    /**
     * Execute an instruction
     * @param ins
     */
    private void interpret(Instruction9900 ins) {
        switch (ins.getInst()) {
        case InstTableCommon.Idata:
            break;
        case Inst9900.Ili:
        	iblock.val1 = iblock.val2;
            break;
        case Inst9900.Iai:
        	iblock.val1 += iblock.val2;
            break;
        case Inst9900.Iandi:
        	iblock.val1 &= iblock.val2;
            break;
        case Inst9900.Iori:
        	iblock.val1 |= iblock.val2;
            break;
        case Inst9900.Ici:
            break;
        case Inst9900.Istwp:
        	iblock.val1 = iblock.wp;
            break;
        case Inst9900.Istst:
        	iblock.val1 = status.flatten();
            break;
        case Inst9900.Ilwpi:
        	iblock.wp = iblock.val1;
            break;
        case Inst9900.Ilimi:
            // all done in status (Status#setIntMask() performed as post-instruction
        	// action due to ST_INT effect)
            break;
        case Inst9900.Iidle:
            //cpu.idle(); // TODO
            break;
        case Inst9900.Irset:
            //cpu.rset(); // TODO
            break;
        case Inst9900.Irtwp:
        	status.expand(memory.readWord(iblock.wp + 15 * 2));
        	iblock.pc = memory.readWord(iblock.wp + 14 * 2);
        	iblock.wp = memory.readWord(iblock.wp + 13 * 2);
            break;
        case Inst9900.Ickon:
            // TODO
            break;
        case Inst9900.Ickof:
            // TODO
            break;
        case Inst9900.Ilrex:
            // TODO
            break;
        case Inst9900.Iblwp:
        	iblock.wp = memory.readWord(iblock.val1);
        	iblock.pc = memory.readWord(iblock.val1 + 2);
            break;

        case Inst9900.Ib:
        	iblock.pc = iblock.val1;
            break;
        case Inst9900.Ix: {
        	execute(iblock.val1);
            break;
        }
        case Inst9900.Iclr:
        	iblock.val1 = 0;
            break;
        case Inst9900.Ineg:
        	iblock.val1 = (short) -iblock.val1;
            break;
        case Inst9900.Iinv:
        	iblock.val1 = (short) ~iblock.val1;
            break;
        case Inst9900.Iinc:
        	iblock.val1 ++;
            break;
        case Inst9900.Iinct:
        	iblock.val1 += 2;
            break;
        case Inst9900.Idec:
        	iblock.val1 --;
            break;
        case Inst9900.Idect:
        	iblock.val1 -= 2;
            break;
        case Inst9900.Ibl:
        	memory.writeWord(iblock.wp + 11 * 2, iblock.pc);
        	iblock.pc = iblock.val1;
            break;
        case Inst9900.Iswpb:
        	iblock.val1 = (short) (iblock.val1 >> 8 & 0xff | iblock.val1 << 8 & 0xff00);
            break;
        case Inst9900.Iseto:
        	iblock.val1 = -1;
            break;
        case Inst9900.Iabs:
        	if ((iblock.val1 & 0x8000) != 0) {
        		iblock.val1 = (short) -iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case Inst9900.Isra:
        	iblock.val1 = (short) (iblock.val1 >> iblock.val2);
        	cpu.addCycles(iblock.val2 * 2);
            break;
        case Inst9900.Isrl:
        	iblock.val1 = (short) ((iblock.val1 & 0xffff) >> iblock.val2);
        	cpu.addCycles(iblock.val2 * 2);
            break;

        case Inst9900.Isla:
        	iblock.val1 = (short) (iblock.val1 << iblock.val2);
        	cpu.addCycles(iblock.val2 * 2);
            break;

        case Inst9900.Isrc:
        	iblock.val1 = (short) ((iblock.val1 & 0xffff) >> iblock.val2 | (iblock.val1 & 0xffff) << 16 - iblock.val2);
        	cpu.addCycles(iblock.val2 * 2);
            break;

        case Inst9900.Ijmp:
        	iblock.pc = iblock.val1;
        	cpu.addCycles(2);
            break;
        case Inst9900.Ijlt:
        	if (status.isLT()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case Inst9900.Ijle:
        	if (status.isLE()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;

        case Inst9900.Ijeq:
        	if (status.isEQ()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case Inst9900.Ijhe:
        	if (status.isHE()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case Inst9900.Ijgt:
        	if (status.isGT()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case Inst9900.Ijne:
        	if (status.isNE()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case Inst9900.Ijnc:
        	if (!status.isC()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case Inst9900.Ijoc:
        	if (status.isC()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case Inst9900.Ijno:
        	if (!status.isO()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case Inst9900.Ijl:
        	if (status.isL()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case Inst9900.Ijh:
        	if (status.isH()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
            }
            break;

        case Inst9900.Ijop:
            // jump on ODD parity
            if (status.isP()) {
				iblock.pc = iblock.val1;
				cpu.addCycles(2);
            }
            break;

        case Inst9900.Isbo:
        	machine.getCruManager().writeBits(iblock.val1, 1, 1);
            break;

        case Inst9900.Isbz:
        	machine.getCruManager().writeBits(iblock.val1, 0, 1);
            break;

        case Inst9900.Itb:
        	iblock.val1 = (short) machine.getCruManager().readBits(iblock.val1, 1);
        	iblock.val2 = 0;
            break;

        case Inst9900.Icoc:
        	iblock.val2 = (short) (iblock.val1 & iblock.val2);
            break;

        case Inst9900.Iczc:
        	iblock.val2 = (short) (iblock.val1 & ~iblock.val2);
            break;

        case Inst9900.Ixor:
        	iblock.val2 ^= iblock.val1;
            break;

        case Inst9900.Ixop:
        	iblock.wp = memory.readWord(iblock.val2 * 4 + 0x40);
            iblock.pc = memory.readWord(iblock.val2 * 4 + 0x42);
            memory.writeWord(iblock.wp + 11 * 2, iblock.ea1);
            break;

        case Inst9900.Impy:
            int val = (iblock.val1 & 0xffff)
                    * (iblock.val2 & 0xffff);
            // manually write second reg
            iblock.val3 = (short) val;
            //memory.writeWord(block.op2.ea + 2, (short) val);
            iblock.val2 = (short) (val >> 16);
            break;

        case Inst9900.Idiv:
            // manually read second reg
            if (iblock.val1 > iblock.val2) {
                short low = iblock.val3;
                //short low = memory.readWord(block.op2.ea + 2);
                int dval = (iblock.val2 & 0xffff) << 16
                        | low & 0xffff;
                try {
                    iblock.val2 = (short) (dval / (iblock.val1 & 0xffff));
                    iblock.val3 = (short) (dval % (iblock.val1 & 0xffff));
                } catch (ArithmeticException e) {
                	cpu.addCycles((124 + 92) / 2 - 16);
                }
                //memory.writeWord(block.op2.ea + 2,
                //        (short) (val % (block.val1 & 0xffff)));
                //inst.op2.value = (short) val;
            } else {
            	cpu.addCycles((124 + 92) / 2 - 16);
            }
            break;

        case Inst9900.Ildcr:
        	machine.getCruManager().writeBits(
                    memory.readWord(iblock.wp + 12 * 2), iblock.val1,
                    iblock.val2);
            break;

        case Inst9900.Istcr:
        	iblock.val1 = (short) machine.getCruManager().readBits(
        			memory.readWord(iblock.wp + 12 * 2), iblock.val2);
            break;
        case Inst9900.Iszc:
        case Inst9900.Iszcb:
        	iblock.val2 &= ~iblock.val1;
            break;

        case Inst9900.Is:
        case Inst9900.Isb:
        	iblock.val2 -= iblock.val1;
            break;

        case Inst9900.Ic:
        case Inst9900.Icb:
            break;

        case Inst9900.Ia:
        case Inst9900.Iab:
        	iblock.val2 += iblock.val1;
            break;

        case Inst9900.Imov:
        case Inst9900.Imovb:
        	iblock.val2 = iblock.val1;
            break;

        case Inst9900.Isoc:
        case Inst9900.Isocb:
        	iblock.val2 |= iblock.val1;
            break;

        case InstTableCommon.Idsr:
        	machine.getDsrManager().handleDSR(iblock);
        	break;
        	
        case InstTableCommon.Iticks: {
        	int count = machine.getCpu().getTickCount();
        	iblock.val1 = (short) (count >> 16);
        	iblock.val2 = (short) (count & 0xffff);
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
        	
        	
        }
    }
}