/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.runtime.interpreter;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.TI994A;
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
import v9t9.utils.Utils;

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
    
    InstructionWorkBlock iinstructionWorkBlock;

    public Interpreter(Machine machine) {
        this.machine = machine;
        this.memory = machine.getCpu().getConsole();
        //instructions = new Instruction[65536/2];// HashMap<Integer, Instruction>();
        parsedInstructions = new HashMap<MemoryArea, Instruction[]>();
        iinstructionWorkBlock = new InstructionWorkBlock();
        iinstructionWorkBlock.domain = memory;
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

        iinstructionWorkBlock.cycles = cpu.getCurrentCycleCount();
        
        /* get current operand values and instruction timings */
        fetchOperands(cpu, ins, cpu.getWP(), cpu.getStatus());

        InstructionWorkBlock instructionWorkBlock = new InstructionWorkBlock();
        this.iinstructionWorkBlock.copyTo(instructionWorkBlock);
        
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

        instructionWorkBlock.cycles = cpu.getCurrentCycleCount();
        
        /* dump values after execution */
        /*
        if (dumpfull != null) {
            dumpFullEnd(cpu, origCycleCount, mop1, mop2, dumpfull);
        }*/
		
        /* notify listeners */
        if (instructionListeners != null) {
        	for (InstructionListener listener : instructionListeners) {
        		listener.executed(instructionWorkBlock, iinstructionWorkBlock);
        	}
        }
	}

	private Instruction getInstruction(Cpu cpu, Short op_x) {
		Instruction ins;
	    int pc = cpu.getPC() & 0xfffe;
	    
	    short op;
	    if (op_x != null) {
	    	op = op_x;
	    	ins = new Instruction(InstructionTable.decodeInstruction(op, pc, cpu.getConsole()));
	    } else {
	    	op = cpu.getConsole().readWord(pc);
	    	MemoryArea area = cpu.getConsole().getEntryAt(pc).getArea();
	    	Instruction[] instructions = parsedInstructions.get(area);
	    	if (instructions == null) {
	    		instructions = new Instruction[65536/2];
	    		parsedInstructions.put(area, instructions);
	    	}
	    	if ((ins = instructions[pc/2]) != null) {
	    		ins = ins.update(op, pc, cpu.getConsole());
	    	} else {
	    		ins = new Instruction(InstructionTable.decodeInstruction(op, pc, cpu.getConsole()));
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
        iinstructionWorkBlock.inst = ins;
        iinstructionWorkBlock.pc = (short) (iinstructionWorkBlock.inst.pc + iinstructionWorkBlock.inst.size);
        iinstructionWorkBlock.wp = cpu.getWP();
        iinstructionWorkBlock.status = st;
        
        MachineOperand mop1 = (MachineOperand) iinstructionWorkBlock.inst.op1;
        MachineOperand mop2 = (MachineOperand) iinstructionWorkBlock.inst.op2;

        if (mop1.type != MachineOperand.OP_NONE) {
        	mop1.cycles = 0;
			iinstructionWorkBlock.ea1 = mop1.getEA(memory, iinstructionWorkBlock.inst.pc, wp);
		}
        if (mop2.type != MachineOperand.OP_NONE) {
        	mop2.cycles = 0;
			iinstructionWorkBlock.ea2 = mop2.getEA(memory, iinstructionWorkBlock.inst.pc, wp);
		}
        if (mop1.type != MachineOperand.OP_NONE) {
        	//if (ins.inst != InstructionTable.Ili)		// even LI will read in the real hardware
        	iinstructionWorkBlock.val1 = mop1.getValue(memory, iinstructionWorkBlock.ea1);
		}
        if (mop2.type != MachineOperand.OP_NONE) {
			iinstructionWorkBlock.val2 = mop2.getValue(memory, iinstructionWorkBlock.ea2);
		}
        if (iinstructionWorkBlock.inst.inst == InstructionTable.Idiv) {
            iinstructionWorkBlock.val3 = memory.readWord(iinstructionWorkBlock.ea2 + 2);
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
				memory.writeByte(iinstructionWorkBlock.ea1, (byte) iinstructionWorkBlock.val1);
			} else {
				memory.writeWord(iinstructionWorkBlock.ea1, iinstructionWorkBlock.val1);
				if (ins.inst == InstructionTable.Iticks) {
					memory.writeWord(iinstructionWorkBlock.ea1 + 2, iinstructionWorkBlock.val2);
				}
			}
				
        }
        if (mop2.dest != Operand.OP_DEST_FALSE) {
        	if (ins.inst == InstructionTable.Icb)
        		mop2.dest = 1;
            if (mop2.byteop) {
				memory.writeByte(iinstructionWorkBlock.ea2, (byte) iinstructionWorkBlock.val2);
			} else {
                memory.writeWord(iinstructionWorkBlock.ea2, iinstructionWorkBlock.val2);
                if (ins.inst == InstructionTable.Impy 
                		|| ins.inst == InstructionTable.Idiv) {
                    memory.writeWord(iinstructionWorkBlock.ea2 + 2, iinstructionWorkBlock.val3);
                }
            }
        }

        if ((ins.writes & Instruction.INST_RSRC_ST) != 0) {
			cpu.setStatus(iinstructionWorkBlock.status);
		}

        /* do this after flushing status */
        if ((ins.writes & Instruction.INST_RSRC_CTX) != 0) {
            /* update PC first */
            cpu.setPC((short) (iinstructionWorkBlock.inst.pc + iinstructionWorkBlock.inst.size));
            cpu.contextSwitch(iinstructionWorkBlock.wp, iinstructionWorkBlock.pc);
        } else {
            /* flush register changes */
            cpu.setPC(iinstructionWorkBlock.pc);
            if ((ins.writes & Instruction.INST_RSRC_WP) != 0) {
				cpu.setWP(iinstructionWorkBlock.wp);
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
            iinstructionWorkBlock.status.setIntMask(iinstructionWorkBlock.val1);
            break;
        case Instruction.st_ADD_BYTE_LAECOP:
            iinstructionWorkBlock.status.set_ADD_BYTE_LAECOP((byte) iinstructionWorkBlock.val2,
                    (byte) iinstructionWorkBlock.val1);
            break;
        case Instruction.st_ADD_LAECO:
            iinstructionWorkBlock.status.set_ADD_LAECO(iinstructionWorkBlock.val2, iinstructionWorkBlock.val1);
            break;
        case Instruction.st_ADD_LAECO_REV:
            iinstructionWorkBlock.status.set_ADD_LAECO(iinstructionWorkBlock.val1, iinstructionWorkBlock.val2);
            break;
        case Instruction.st_SUB_BYTE_LAECOP:
            iinstructionWorkBlock.status.set_SUB_BYTE_LAECOP((byte) iinstructionWorkBlock.val2,
                    (byte) iinstructionWorkBlock.val1);
            break;
        case Instruction.st_SUB_LAECO:
            iinstructionWorkBlock.status.set_SUB_LAECO(iinstructionWorkBlock.val2, iinstructionWorkBlock.val1);
            break;

        case Instruction.st_BYTE_CMP:
            iinstructionWorkBlock.status.set_BYTE_CMP((byte) iinstructionWorkBlock.val1,
                    (byte) iinstructionWorkBlock.val2);
            break;

        case Instruction.st_CMP:
            iinstructionWorkBlock.status.set_CMP(iinstructionWorkBlock.val1, iinstructionWorkBlock.val2);
            break;
        case Instruction.st_DIV_O:
            iinstructionWorkBlock.status
                    .set_O((iinstructionWorkBlock.val1 & 0xffff) <= (iinstructionWorkBlock.val2 & 0xffff));
            break;
        case Instruction.st_E:
            iinstructionWorkBlock.status.set_E(iinstructionWorkBlock.val1 == iinstructionWorkBlock.val2);
            break;
        case Instruction.st_LAE:
            iinstructionWorkBlock.status.set_LAE(iinstructionWorkBlock.val2);
            break;
        case Instruction.st_LAE_1:
            iinstructionWorkBlock.status.set_LAE(iinstructionWorkBlock.val1);
            break;

        case Instruction.st_BYTE_LAEP:
            iinstructionWorkBlock.status.set_BYTE_LAEP((byte) iinstructionWorkBlock.val2);
            break;
        case Instruction.st_BYTE_LAEP_1:
            iinstructionWorkBlock.status.set_BYTE_LAEP((byte) iinstructionWorkBlock.val1);
            break;

        case Instruction.st_LAEO:
            iinstructionWorkBlock.status.set_LAEO(iinstructionWorkBlock.val1);

        case Instruction.st_O:
            iinstructionWorkBlock.status.set_O(iinstructionWorkBlock.val1 == (short) 0x8000);
            break;

        case Instruction.st_SHIFT_LEFT_CO:
            iinstructionWorkBlock.status.set_SHIFT_LEFT_CO(iinstructionWorkBlock.val1, iinstructionWorkBlock.val2);
            break;
        case Instruction.st_SHIFT_RIGHT_C:
            iinstructionWorkBlock.status.set_SHIFT_RIGHT_C(iinstructionWorkBlock.val1, iinstructionWorkBlock.val2);
            break;

        case Instruction.st_XOP:
            iinstructionWorkBlock.status.set_X();
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
        	iinstructionWorkBlock.val1 = iinstructionWorkBlock.val2;
            break;
        case InstructionTable.Iai:
        	iinstructionWorkBlock.val1 += iinstructionWorkBlock.val2;
            break;
        case InstructionTable.Iandi:
        	iinstructionWorkBlock.val1 &= iinstructionWorkBlock.val2;
            break;
        case InstructionTable.Iori:
        	iinstructionWorkBlock.val1 |= iinstructionWorkBlock.val2;
            break;
        case InstructionTable.Ici:
            break;
        case InstructionTable.Istwp:
        	iinstructionWorkBlock.val1 = iinstructionWorkBlock.wp;
            break;
        case InstructionTable.Istst:
        	iinstructionWorkBlock.val1 = iinstructionWorkBlock.status.flatten();
            break;
        case InstructionTable.Ilwpi:
        	iinstructionWorkBlock.wp = iinstructionWorkBlock.val1;
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
        	iinstructionWorkBlock.status.expand(memory.readWord(iinstructionWorkBlock.wp + 15 * 2));
        	iinstructionWorkBlock.pc = memory.readWord(iinstructionWorkBlock.wp + 14 * 2);
        	iinstructionWorkBlock.wp = memory.readWord(iinstructionWorkBlock.wp + 13 * 2);
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
        	iinstructionWorkBlock.wp = memory.readWord(iinstructionWorkBlock.val1);
        	iinstructionWorkBlock.pc = memory.readWord(iinstructionWorkBlock.val1 + 2);
            break;

        case InstructionTable.Ib:
        	iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
            break;
        case InstructionTable.Ix:
        	//short newPc = block.pc;
        	execute(cpu, iinstructionWorkBlock.val1);
        	//block.pc = newPc;
            break;
        case InstructionTable.Iclr:
        	iinstructionWorkBlock.val1 = 0;
            break;
        case InstructionTable.Ineg:
        	iinstructionWorkBlock.val1 = (short) -iinstructionWorkBlock.val1;
            break;
        case InstructionTable.Iinv:
        	iinstructionWorkBlock.val1 = (short) ~iinstructionWorkBlock.val1;
            break;
        case InstructionTable.Iinc:
        case InstructionTable.Iinct:
        case InstructionTable.Idec:
        case InstructionTable.Idect:
        	iinstructionWorkBlock.val1 += iinstructionWorkBlock.val2;
            break;
        case InstructionTable.Ibl:
        	memory.writeWord(iinstructionWorkBlock.wp + 11 * 2, iinstructionWorkBlock.pc);
        	iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
            break;
        case InstructionTable.Iswpb:
        	iinstructionWorkBlock.val1 = (short) (iinstructionWorkBlock.val1 >> 8 & 0xff | iinstructionWorkBlock.val1 << 8 & 0xff00);
            break;
        case InstructionTable.Iseto:
        	iinstructionWorkBlock.val1 = -1;
            break;
        case InstructionTable.Iabs:
        	if ((iinstructionWorkBlock.val1 & 0x8000) != 0) {
        		iinstructionWorkBlock.val1 = (short) -iinstructionWorkBlock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Isra:
        	iinstructionWorkBlock.val1 = (short) (iinstructionWorkBlock.val1 >> iinstructionWorkBlock.val2);
        	cpu.addCycles(iinstructionWorkBlock.val2 * 2);
            break;
        case InstructionTable.Isrl:
        	iinstructionWorkBlock.val1 = (short) ((iinstructionWorkBlock.val1 & 0xffff) >> iinstructionWorkBlock.val2);
        	cpu.addCycles(iinstructionWorkBlock.val2 * 2);
            break;

        case InstructionTable.Isla:
        	iinstructionWorkBlock.val1 = (short) (iinstructionWorkBlock.val1 << iinstructionWorkBlock.val2);
        	cpu.addCycles(iinstructionWorkBlock.val2 * 2);
            break;

        case InstructionTable.Isrc:
        	iinstructionWorkBlock.val1 = (short) ((iinstructionWorkBlock.val1 & 0xffff) >> iinstructionWorkBlock.val2 | (iinstructionWorkBlock.val1 & 0xffff) << 16 - iinstructionWorkBlock.val2);
        	cpu.addCycles(iinstructionWorkBlock.val2 * 2);
            break;

        case InstructionTable.Ijmp:
        	iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
        	cpu.addCycles(2);
            break;
        case InstructionTable.Ijlt:
        	if (iinstructionWorkBlock.status.isLT()) {
        		iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijle:
        	if (iinstructionWorkBlock.status.isLE()) {
        		iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
        		cpu.addCycles(2);
        	}
            break;

        case InstructionTable.Ijeq:
        	if (iinstructionWorkBlock.status.isEQ()) {
        		iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijhe:
        	if (iinstructionWorkBlock.status.isHE()) {
        		iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijgt:
        	if (iinstructionWorkBlock.status.isGT()) {
        		iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijne:
        	if (iinstructionWorkBlock.status.isNE()) {
        		iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijnc:
        	if (!iinstructionWorkBlock.status.isC()) {
        		iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijoc:
        	if (iinstructionWorkBlock.status.isC()) {
        		iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijno:
        	if (!iinstructionWorkBlock.status.isO()) {
        		iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijl:
        	if (iinstructionWorkBlock.status.isL()) {
        		iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
        		cpu.addCycles(2);
        	}
            break;
        case InstructionTable.Ijh:
        	if (iinstructionWorkBlock.status.isH()) {
        		iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
        		cpu.addCycles(2);
            }
            break;

        case InstructionTable.Ijop:
            // jump on ODD parity
            if (iinstructionWorkBlock.status.isP()) {
				iinstructionWorkBlock.pc = iinstructionWorkBlock.val1;
				cpu.addCycles(2);
            }
            break;

        case InstructionTable.Isbo:
        	machine.getCruManager().writeBits(iinstructionWorkBlock.val1, 1, 1);
            break;

        case InstructionTable.Isbz:
        	machine.getCruManager().writeBits(iinstructionWorkBlock.val1, 0, 1);
            break;

        case InstructionTable.Itb:
        	iinstructionWorkBlock.val1 = (short) machine.getCruManager().readBits(iinstructionWorkBlock.val1, 1);
        	iinstructionWorkBlock.val2 = 0;
            break;

        case InstructionTable.Icoc:
        	iinstructionWorkBlock.val2 = (short) (iinstructionWorkBlock.val1 & iinstructionWorkBlock.val2);
            break;

        case InstructionTable.Iczc:
        	iinstructionWorkBlock.val2 = (short) (iinstructionWorkBlock.val1 & ~iinstructionWorkBlock.val2);
            break;

        case InstructionTable.Ixor:
        	iinstructionWorkBlock.val2 ^= iinstructionWorkBlock.val1;
            break;

        case InstructionTable.Ixop:
        	iinstructionWorkBlock.wp = memory.readWord(iinstructionWorkBlock.val2 * 4 + 0x40);
            iinstructionWorkBlock.pc = memory.readWord(iinstructionWorkBlock.val2 * 4 + 0x42);
            memory.writeWord(iinstructionWorkBlock.wp + 11 * 2, iinstructionWorkBlock.ea1);
            break;

        case InstructionTable.Impy:
            int val = (iinstructionWorkBlock.val1 & 0xffff)
                    * (iinstructionWorkBlock.val2 & 0xffff);
            // manually write second reg
            iinstructionWorkBlock.val3 = (short) val;
            //memory.writeWord(block.op2.ea + 2, (short) val);
            iinstructionWorkBlock.val2 = (short) (val >> 16);
            break;

        case InstructionTable.Idiv:
            // manually read second reg
            if (iinstructionWorkBlock.val1 > iinstructionWorkBlock.val2) {
                short low = iinstructionWorkBlock.val3;
                //short low = memory.readWord(block.op2.ea + 2);
                int dval = (iinstructionWorkBlock.val2 & 0xffff) << 16
                        | low & 0xffff;
                try {
                    iinstructionWorkBlock.val2 = (short) (dval / (iinstructionWorkBlock.val1 & 0xffff));
                    iinstructionWorkBlock.val3 = (short) (dval % (iinstructionWorkBlock.val1 & 0xffff));
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
                    memory.readWord(iinstructionWorkBlock.wp + 12 * 2), iinstructionWorkBlock.val1,
                    iinstructionWorkBlock.val2);
            break;

        case InstructionTable.Istcr:
        	iinstructionWorkBlock.val1 = (short) machine.getCruManager().readBits(
        			memory.readWord(iinstructionWorkBlock.wp + 12 * 2), iinstructionWorkBlock.val2);
            break;
        case InstructionTable.Iszc:
        case InstructionTable.Iszcb:
        	iinstructionWorkBlock.val2 &= ~iinstructionWorkBlock.val1;
            break;

        case InstructionTable.Is:
        case InstructionTable.Isb:
        	iinstructionWorkBlock.val2 -= iinstructionWorkBlock.val1;
            break;

        case InstructionTable.Ic:
        case InstructionTable.Icb:
            break;

        case InstructionTable.Ia:
        case InstructionTable.Iab:
        	iinstructionWorkBlock.val2 += iinstructionWorkBlock.val1;
            break;

        case InstructionTable.Imov:
        case InstructionTable.Imovb:
        	iinstructionWorkBlock.val2 = iinstructionWorkBlock.val1;
            break;

        case InstructionTable.Isoc:
        case InstructionTable.Isocb:
        	iinstructionWorkBlock.val2 |= iinstructionWorkBlock.val1;
            break;

        case InstructionTable.Idsr:
        	machine.getDSRManager().handleDSR(iinstructionWorkBlock);
        	break;
        	
        case InstructionTable.Iticks:
        	iinstructionWorkBlock.val1 = (short) (machine.getCpu().getTickCount() >> 16);
        	iinstructionWorkBlock.val2 = (short) (machine.getCpu().getTickCount());
        	break;
        case InstructionTable.Idbg:
        	int oldCount = machine.getExecutor().debugCount; 
        	if (iinstructionWorkBlock.val1 == 0)
        		machine.getExecutor().debugCount++;
        	else
        		machine.getExecutor().debugCount--;
        	if ((oldCount == 0) != (machine.getExecutor().debugCount == 0))
        		Executor.settingDumpFullInstructions.setBoolean(iinstructionWorkBlock.val1 == 0);
        	break;
        	
        	
        }
    }
}