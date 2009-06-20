/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.runtime.interpreter;

import java.util.HashMap;
import java.util.Map;

import v9t9.emulator.Machine;
import v9t9.emulator.runtime.Cpu;
import v9t9.emulator.runtime.Executor;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.InstructionWorkBlock;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.Status;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;

/**
 * This class interprets 9900 instructions one by one.
 * 
 * @author ejs
 */
public class Interpreter {
    Machine machine;

    MemoryDomain memory;

    // per-PC prebuilt instructions
    Map<MemoryArea, Instruction[]> parsedInstructions; 
    //Instruction[] instructions; 
    
    InstructionWorkBlock iblock;

    public Interpreter(Machine machine) {
        this.machine = machine;
        this.memory = machine.getCpu().getConsole();
        //instructions = new Instruction[65536/2];// HashMap<Integer, Instruction>();
        parsedInstructions = new HashMap<MemoryArea, Instruction[]>();
        iblock = new InstructionWorkBlock();
        iblock.domain = memory;
     }

    /**
     * Execute an instruction: general entry point
     * @param cpu
     * @param op_x if not-null, execute the instruction from an X instruction
     */
    public void execute(Cpu cpu, Short op_x) {
    	InstructionListener[] instructionListeners = machine.getExecutor().getInstructionListeners();
    	if (instructionListeners != null) {
    		executeAndListen(cpu, op_x, instructionListeners);
    	} else {
    		executeFast(cpu, op_x);
    	}
    	
        /* dump instruction */
        //PrintWriter dumpfull = machine.getExecutor().getDumpfull(); 
        //PrintWriter dump = machine.getExecutor().getDump();
        
        //if (dumpfull != null || dump != null || Machine.settingDebugTracing.getBoolean()) {
        //} else {
        //}

    }

    /**
     * This version is called when you know nothing needs to monitor instructions
     * @param cpu
     * @param op_x
     */
    public void executeFast(Cpu cpu, Short op_x) {
        Instruction ins = getInstruction(cpu, op_x);

        MachineOperand mop1 = (MachineOperand) ins.op1;
        MachineOperand mop2 = (MachineOperand) ins.op2;

        /* get current operand values and instruction timings */
        fetchOperands(cpu, ins, cpu.getWP(), cpu.getStatus());

        /* do pre-instruction status word updates */
        if (ins.stsetBefore != Instruction.st_NONE) {
            updateStatus(ins.stsetBefore);
        }

        /* execute */
        interpret(cpu, ins);
        
        /* do post-instruction status word updates */
        if (ins.stsetAfter != Instruction.st_NONE) {
            updateStatus(ins.stsetAfter);
        }

        /* save any operands */
        flushOperands(cpu, ins);
        
        cpu.addCycles(ins.cycles + mop1.cycles + mop2.cycles);
	}

	private void executeAndListen(Cpu cpu, Short op_x, InstructionListener[] instructionListeners) { 
		//PrintWriter dump = machine.getExecutor().getDump();
		//PrintWriter dumpfull = machine.getExecutor().getDumpfull();
		
        Instruction ins = getInstruction(cpu, op_x);
        
        MachineOperand mop1 = (MachineOperand) ins.op1;
        MachineOperand mop2 = (MachineOperand) ins.op2;

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

        InstructionWorkBlock block = new InstructionWorkBlock();
        this.iblock.copyTo(block);
        
        /* dump values before execution */
        /*
        if (dumpfull != null) {
            dumpFullMid(mop1, mop2, dumpfull);
        }*/

        /* do pre-instruction status word updates */
        if (ins.stsetBefore != Instruction.st_NONE) {
            updateStatus(ins.stsetBefore);
        }

        /* execute */
        interpret(cpu, ins);
        
        /* do post-instruction status word updates */
        if (ins.stsetAfter != Instruction.st_NONE) {
            updateStatus(ins.stsetAfter);
        }

        /* save any operands */
        flushOperands(cpu, ins);
        
        cpu.addCycles(ins.cycles + mop1.cycles + mop2.cycles);

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

	private Instruction getInstruction(Cpu cpu, Short op_x) {
		Instruction ins;
	    int pc = cpu.getPC() & 0xfffe;
	    
	    short op;
	    MemoryDomain console = cpu.getConsole();
		if (op_x != null) {
	    	op = op_x;
	    	ins = new Instruction(InstructionTable.decodeInstruction(op, pc, console));
	    } else {
	    	MemoryEntry entry = console.getEntryAt(pc);
	    	op = entry.readWord(pc);
	    	MemoryArea area = entry.getArea();
	    	Instruction[] instructions = parsedInstructions.get(area);
	    	if (instructions == null) {
	    		instructions = new Instruction[65536/2];
	    		parsedInstructions.put(area, instructions);
	    	}
	    	if ((ins = instructions[pc/2]) != null) {
	    		// expensive (10%)
	    		ins = ins.update(op, pc, console);
	    	} else {
	    		ins = new Instruction(InstructionTable.decodeInstruction(op, pc, console));
	    	}
	    	instructions[pc/2] = ins;
	    }
		return ins;
	}

	public Instruction getInstruction(Cpu cpu) {
		return getInstruction(cpu, null);
	}

    /** Fetch operands for instruction (runtime)
     * @param ins
     * @param memory2
     */
    private void fetchOperands(Cpu cpu, Instruction ins, short wp, Status st) {
        iblock.inst = ins;
        iblock.pc = (short) (iblock.inst.pc + iblock.inst.size);
        iblock.wp = cpu.getWP();
        iblock.status = st;
        
        MachineOperand mop1 = (MachineOperand) iblock.inst.op1;
        MachineOperand mop2 = (MachineOperand) iblock.inst.op2;

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
        if (iblock.inst.inst == InstructionTable.Idiv) {
            iblock.val3 = memory.readWord(iblock.ea2 + 2);
        }
    }

    /**
     * 
     */
    private void flushOperands(Cpu cpu, Instruction ins) {
        MachineOperand mop1 = (MachineOperand) ins.op1;
        MachineOperand mop2 = (MachineOperand) ins.op2;
        if (mop1.dest != Operand.OP_DEST_FALSE) {
            if (mop1.byteop) {
				memory.writeByte(iblock.ea1, (byte) iblock.val1);
			} else {
				memory.writeWord(iblock.ea1, iblock.val1);
				if (ins.inst == InstructionTable.Iticks) {
					memory.writeWord(iblock.ea1 + 2, iblock.val2);
				}
			}
				
        }
        if (mop2.dest != Operand.OP_DEST_FALSE) {
        	if (ins.inst == InstructionTable.Icb)
        		mop2.dest = 1;
            if (mop2.byteop) {
				memory.writeByte(iblock.ea2, (byte) iblock.val2);
			} else {
                memory.writeWord(iblock.ea2, iblock.val2);
                if (ins.inst == InstructionTable.Impy 
                		|| ins.inst == InstructionTable.Idiv) {
                    memory.writeWord(iblock.ea2 + 2, iblock.val3);
                }
            }
        }

        if ((ins.writes & Instruction.INST_RSRC_ST) != 0) {
			cpu.setStatus(iblock.status);
		}

        /* do this after flushing status */
        if ((ins.writes & Instruction.INST_RSRC_CTX) != 0) {
            /* update PC first */
            cpu.setPC((short) (iblock.inst.pc + iblock.inst.size));
            cpu.contextSwitch(iblock.wp, iblock.pc);
        } else {
            /* flush register changes */
            cpu.setPC(iblock.pc);
            if ((ins.writes & Instruction.INST_RSRC_WP) != 0) {
				cpu.setWP(iblock.wp);
			}
        }
    }

    /**
     */
    private void updateStatus(int handler) {
        switch (handler) {
        case Instruction.st_NONE:
            return;
        case Instruction.st_ALL:
            // just a note that Status should be up to date, for future work
            return;
        case Instruction.st_INT:
            iblock.status.setIntMask(iblock.val1);
            break;
        case Instruction.st_ADD_BYTE_LAECOP:
            iblock.status.set_ADD_BYTE_LAECOP((byte) iblock.val2,
                    (byte) iblock.val1);
            break;
        case Instruction.st_ADD_LAECO:
            iblock.status.set_ADD_LAECO(iblock.val2, iblock.val1);
            break;
        case Instruction.st_ADD_LAECO_REV:
            iblock.status.set_ADD_LAECO(iblock.val1, iblock.val2);
            break;
        case Instruction.st_SUB_BYTE_LAECOP:
            iblock.status.set_SUB_BYTE_LAECOP((byte) iblock.val2,
                    (byte) iblock.val1);
            break;
        case Instruction.st_SUB_LAECO:
            iblock.status.set_SUB_LAECO(iblock.val2, iblock.val1);
            break;

        case Instruction.st_BYTE_CMP:
            iblock.status.set_BYTE_CMP((byte) iblock.val1,
                    (byte) iblock.val2);
            break;

        case Instruction.st_CMP:
            iblock.status.set_CMP(iblock.val1, iblock.val2);
            break;
        case Instruction.st_DIV_O:
            iblock.status
                    .set_O((iblock.val1 & 0xffff) <= (iblock.val2 & 0xffff));
            break;
        case Instruction.st_E:
            iblock.status.set_E(iblock.val1 == iblock.val2);
            break;
        case Instruction.st_LAE:
            iblock.status.set_LAE(iblock.val2);
            break;
        case Instruction.st_LAE_1:
            iblock.status.set_LAE(iblock.val1);
            break;

        case Instruction.st_BYTE_LAEP:
            iblock.status.set_BYTE_LAEP((byte) iblock.val2);
            break;
        case Instruction.st_BYTE_LAEP_1:
            iblock.status.set_BYTE_LAEP((byte) iblock.val1);
            break;

        case Instruction.st_LAEO:
            iblock.status.set_LAEO(iblock.val1);

        case Instruction.st_O:
            iblock.status.set_O(iblock.val1 == (short) 0x8000);
            break;

        case Instruction.st_SHIFT_LEFT_CO:
            iblock.status.set_SHIFT_LEFT_CO(iblock.val1, iblock.val2);
            break;
        case Instruction.st_SHIFT_RIGHT_C:
            iblock.status.set_SHIFT_RIGHT_C(iblock.val1, iblock.val2);
            break;

        case Instruction.st_XOP:
            iblock.status.set_X();
            break;

        default:
            throw new AssertionError("unhandled status handler " + handler);
        }

    }

    /**
     * Execute an instruction
     * 
     * @param cpu
     * @param ins
     */
    private void interpret(final Cpu cpu, Instruction ins) {
        switch (ins.inst) {
        case InstructionTable.Idata:
            break;
        case InstructionTable.Ili:
        	iblock.val1 = iblock.val2;
            break;
        case InstructionTable.Iai:
        	iblock.val1 += iblock.val2;
            break;
        case InstructionTable.Iandi:
        	iblock.val1 &= iblock.val2;
            break;
        case InstructionTable.Iori:
        	iblock.val1 |= iblock.val2;
            break;
        case InstructionTable.Ici:
            break;
        case InstructionTable.Istwp:
        	iblock.val1 = iblock.wp;
            break;
        case InstructionTable.Istst:
        	iblock.val1 = iblock.status.flatten();
            break;
        case InstructionTable.Ilwpi:
        	iblock.wp = iblock.val1;
            break;
        case InstructionTable.Ilimi:
            // all done in status (Status#setIntMask() performed as post-instruction
        	// action due to ST_INT effect)
            break;
        case InstructionTable.Iidle:
            //cpu.idle(); // TODO
            break;
        case InstructionTable.Irset:
            //cpu.rset(); // TODO
            break;
        case InstructionTable.Irtwp:
        	iblock.status.expand(memory.readWord(iblock.wp + 15 * 2));
        	iblock.pc = memory.readWord(iblock.wp + 14 * 2);
        	iblock.wp = memory.readWord(iblock.wp + 13 * 2);
            break;
        case InstructionTable.Ickon:
            // TODO
            break;
        case InstructionTable.Ickof:
            // TODO
            break;
        case InstructionTable.Ilrex:
            // TODO
            break;
        case InstructionTable.Iblwp:
        	iblock.wp = memory.readWord(iblock.val1);
        	iblock.pc = memory.readWord(iblock.val1 + 2);
            break;

        case InstructionTable.Ib:
        	iblock.pc = iblock.val1;
            break;
        case InstructionTable.Ix: {
        	short newPc = iblock.pc;
        	execute(cpu, iblock.val1);
        	iblock.pc = newPc;
            break;
        }
        case InstructionTable.Iclr:
        	iblock.val1 = 0;
            break;
        case InstructionTable.Ineg:
        	iblock.val1 = (short) -iblock.val1;
            break;
        case InstructionTable.Iinv:
        	iblock.val1 = (short) ~iblock.val1;
            break;
        case InstructionTable.Iinc:
        case InstructionTable.Iinct:
        case InstructionTable.Idec:
        case InstructionTable.Idect:
        	iblock.val1 += iblock.val2;
            break;
        case InstructionTable.Ibl:
        	memory.writeWord(iblock.wp + 11 * 2, iblock.pc);
        	iblock.pc = iblock.val1;
            break;
        case InstructionTable.Iswpb:
        	iblock.val1 = (short) (iblock.val1 >> 8 & 0xff | iblock.val1 << 8 & 0xff00);
            break;
        case InstructionTable.Iseto:
        	iblock.val1 = -1;
            break;
        case InstructionTable.Iabs:
        	if ((iblock.val1 & 0x8000) != 0) {
        		iblock.val1 = (short) -iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Isra:
        	iblock.val1 = (short) (iblock.val1 >> iblock.val2);
        	cpu.addCycles(iblock.val2 * 2);
            break;
        case InstructionTable.Isrl:
        	iblock.val1 = (short) ((iblock.val1 & 0xffff) >> iblock.val2);
        	cpu.addCycles(iblock.val2 * 2);
            break;

        case InstructionTable.Isla:
        	iblock.val1 = (short) (iblock.val1 << iblock.val2);
        	cpu.addCycles(iblock.val2 * 2);
            break;

        case InstructionTable.Isrc:
        	iblock.val1 = (short) ((iblock.val1 & 0xffff) >> iblock.val2 | (iblock.val1 & 0xffff) << 16 - iblock.val2);
        	cpu.addCycles(iblock.val2 * 2);
            break;

        case InstructionTable.Ijmp:
        	iblock.pc = iblock.val1;
        	cpu.addCycles(2);
            break;
        case InstructionTable.Ijlt:
        	if (iblock.status.isLT()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijle:
        	if (iblock.status.isLE()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;

        case InstructionTable.Ijeq:
        	if (iblock.status.isEQ()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijhe:
        	if (iblock.status.isHE()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijgt:
        	if (iblock.status.isGT()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijne:
        	if (iblock.status.isNE()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijnc:
        	if (!iblock.status.isC()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijoc:
        	if (iblock.status.isC()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijno:
        	if (!iblock.status.isO()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijl:
        	if (iblock.status.isL()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijh:
        	if (iblock.status.isH()) {
        		iblock.pc = iblock.val1;
        		cpu.addCycles(2);
            }
            break;

        case InstructionTable.Ijop:
            // jump on ODD parity
            if (iblock.status.isP()) {
				iblock.pc = iblock.val1;
				cpu.addCycles(2);
            }
            break;

        case InstructionTable.Isbo:
        	machine.getCruManager().writeBits(iblock.val1, 1, 1);
            break;

        case InstructionTable.Isbz:
        	machine.getCruManager().writeBits(iblock.val1, 0, 1);
            break;

        case InstructionTable.Itb:
        	iblock.val1 = (short) machine.getCruManager().readBits(iblock.val1, 1);
        	iblock.val2 = 0;
            break;

        case InstructionTable.Icoc:
        	iblock.val2 = (short) (iblock.val1 & iblock.val2);
            break;

        case InstructionTable.Iczc:
        	iblock.val2 = (short) (iblock.val1 & ~iblock.val2);
            break;

        case InstructionTable.Ixor:
        	iblock.val2 ^= iblock.val1;
            break;

        case InstructionTable.Ixop:
        	iblock.wp = memory.readWord(iblock.val2 * 4 + 0x40);
            iblock.pc = memory.readWord(iblock.val2 * 4 + 0x42);
            memory.writeWord(iblock.wp + 11 * 2, iblock.ea1);
            break;

        case InstructionTable.Impy:
            int val = (iblock.val1 & 0xffff)
                    * (iblock.val2 & 0xffff);
            // manually write second reg
            iblock.val3 = (short) val;
            //memory.writeWord(block.op2.ea + 2, (short) val);
            iblock.val2 = (short) (val >> 16);
            break;

        case InstructionTable.Idiv:
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

        case InstructionTable.Ildcr:
        	machine.getCruManager().writeBits(
                    memory.readWord(iblock.wp + 12 * 2), iblock.val1,
                    iblock.val2);
            break;

        case InstructionTable.Istcr:
        	iblock.val1 = (short) machine.getCruManager().readBits(
        			memory.readWord(iblock.wp + 12 * 2), iblock.val2);
            break;
        case InstructionTable.Iszc:
        case InstructionTable.Iszcb:
        	iblock.val2 &= ~iblock.val1;
            break;

        case InstructionTable.Is:
        case InstructionTable.Isb:
        	iblock.val2 -= iblock.val1;
            break;

        case InstructionTable.Ic:
        case InstructionTable.Icb:
            break;

        case InstructionTable.Ia:
        case InstructionTable.Iab:
        	iblock.val2 += iblock.val1;
            break;

        case InstructionTable.Imov:
        case InstructionTable.Imovb:
        	iblock.val2 = iblock.val1;
            break;

        case InstructionTable.Isoc:
        case InstructionTable.Isocb:
        	iblock.val2 |= iblock.val1;
            break;

        case InstructionTable.Idsr:
        	machine.getDSRManager().handleDSR(iblock);
        	break;
        	
        case InstructionTable.Iticks: {
        	int count = machine.getCpu().getTickCount();
        	iblock.val1 = (short) (count >> 16);
        	iblock.val2 = (short) (count & 0xffff);
        	break;
        }
        case InstructionTable.Idbg:
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