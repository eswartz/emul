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
import v9t9.engine.Client;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionAction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.Status;
import v9t9.engine.memory.MemoryDomain;
import v9t9.utils.Utils;

/**
 * This class interprets 9900 instructions one by one.
 * 
 * @author ejs
 */
public class Interpreter {
//    Cpu cpu;

    Machine machine;

    MemoryDomain memory;

    Map<Integer, Instruction> instructions;
    
    InstructionAction.Block iblock;

    private Client client;
    
    public Interpreter(Machine machine) {
        this.machine = machine;
        this.memory = machine.getCpu().getConsole();
        instructions = new HashMap<Integer, Instruction>();
        iblock = new InstructionAction.Block();
        iblock.domain = memory;
     }

    /**
     * Execute an instruction
     * @param cpu
     * @param op_x if not-null, execute the instruction from an X instruction
     */
    public void execute(Cpu cpu, Short op_x) {
        /*
         * decode one instruction and update any autoincrement registers
         * referenced in operands
         */
        Instruction ins;
        int pc = cpu.getPC();
        this.client = machine.getClient();
        
        int origCycleCount = cpu.getCurrentCycleCount();
       
        short op = op_x != null ? op_x : cpu.getConsole().readWord(pc);

        if (pc >= 0x6000 && (op & 0xffc0) == 0x680) 
            	pc += 0;

        // always re-read and re-fetch to ensure we get proper cycle counts
        // for reading the memory of the instruction
        //if ((ins = instructions.get(pc)) != null) {
        //    ins = ins.update(op, (short)pc, cpu.getConsole());
        //} else {
            ins = new Instruction(InstructionTable.decodeInstruction(op, pc, cpu.getConsole()));
        //}
        instructions.put(pc, ins);

        MachineOperand mop1 = (MachineOperand) ins.op1;
        MachineOperand mop2 = (MachineOperand) ins.op2;

        /* dump instruction */
        PrintWriter dumpfull = machine.getExecutor().getDumpfull(); 
        PrintWriter dump = machine.getExecutor().getDump();
        
        if (dumpfull != null) {
            dumpFullStart(ins, dumpfull);
        }
        if (dump != null) {
            dumpStart(cpu, ins, dump);
        }

        /* generate the functor for the instruction */
        InstructionAction act = getInterpretAction(cpu, ins);

        /* get current operand values and instruction timings */
        fetchOperands(cpu, ins, cpu.getWP(), cpu.getStatus());

        /* dump values before execution */
        if (dumpfull != null) {
            dumpFullMid(mop1, mop2, dumpfull);
        }

        /* do pre-instruction status word updates */
        if (ins.stsetBefore != Instruction.st_NONE) {
            updateStatus(ins.stsetBefore);
        }

        /* execute */
        if (act != null) {
			act.act(iblock);
		}

        /* do post-instruction status word updates */
        if (ins.stsetAfter != Instruction.st_NONE) {
            updateStatus(ins.stsetAfter);
        }

        /* save any operands */
        flushOperands(cpu, ins);
        
        cpu.addCycles(ins.cycles + mop1.cycles + mop2.cycles);

        /* dump values after execution */
        if (dumpfull != null) {
            dumpFullEnd(cpu, origCycleCount, mop1, mop2, dumpfull);
        }
    }

	private void dumpFullEnd(Cpu cpu, int origCycleCount, MachineOperand mop1,
			MachineOperand mop2, PrintWriter dumpfull) {
		String str;
		if (mop1.type != MachineOperand.OP_NONE
		        && mop1.dest != Operand.OP_DEST_FALSE) {
		    str = mop1.valueString(iblock.ea1, iblock.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2.type != MachineOperand.OP_NONE
		        && mop2.dest != Operand.OP_DEST_FALSE) {
		    str = mop2.valueString(iblock.ea2, iblock.val2);
		    if (str != null) {
				dumpfull.print("op2=" + str + " ");
			}
		}
		dumpfull.print("st="
		        + Integer.toHexString(cpu.getStatus().flatten() & 0xffff)
		                .toUpperCase() + " wp="
		        + Integer.toHexString(cpu.getWP() & 0xffff).toUpperCase());
		
		int cycles = cpu.getCurrentCycleCount() - origCycleCount;
		dumpfull.print(" @ " + cycles);
		dumpfull.println();
		dumpfull.flush();
	}

	private void dumpFullMid(MachineOperand mop1, MachineOperand mop2,
			PrintWriter dumpfull) {
		String str;
		if (mop1.type != MachineOperand.OP_NONE
		        && mop1.dest != Operand.OP_DEST_KILLED) {
		    str = mop1.valueString(iblock.ea1, iblock.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2.type != MachineOperand.OP_NONE
		        && mop2.dest != Operand.OP_DEST_KILLED) {
		    str = mop2.valueString(iblock.ea2, iblock.val2);
		    if (str != null) {
				dumpfull.print("op2=" + str);
			}
		}
		dumpfull.print(" || ");
	}

	private void dumpFullStart(Instruction ins, PrintWriter dumpfull) {
		dumpfull.print(Utils.toHex4(ins.pc) + ": "
		        + ins.toString() + " ==> ");
	}

	private void dumpStart(Cpu cpu, Instruction ins, PrintWriter dump) {
		if (cpu.getMachine() instanceof TI994A) {
		    TI994A ti = (TI994A) cpu.getMachine();
		    dump.println(Utils.toHex4(ins.pc) 
		            + " "
		            + Utils.toHex4(cpu.getWP())
		            + " "
		            + Utils.toHex4(cpu.getStatus().flatten())
		            + " "
		            + Utils.toHex4(ti.getVdpMmio().getAddr())
		            + " "
		            + Utils.toHex4(ti.getGplMmio().getAddr()));
		} else {
		    dump.println(Utils.toHex4(ins.pc) 
		            + " "
		            + Utils.toHex4(cpu.getStatus().flatten())
		    );
		    
		}
		dump.flush();
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
        //iblock.cycles = ins.cycles;
        
        MachineOperand mop1 = (MachineOperand) iblock.inst.op1;
        MachineOperand mop2 = (MachineOperand) iblock.inst.op2;

        if (mop1.type != MachineOperand.OP_NONE) {
        	mop1.cycles = 0;
			iblock.ea1 = mop1.getEA(memory, iblock.inst.pc, wp);
			//iblock.cycles += mop1.cycles;
		}
        if (mop2.type != MachineOperand.OP_NONE) {
        	mop2.cycles = 0;
			iblock.ea2 = mop2.getEA(memory, iblock.inst.pc, wp);
			//iblock.cycles += mop2.cycles;
		}
        if (mop1.type != MachineOperand.OP_NONE) {
        	if (ins.inst != InstructionTable.Ili)
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
			}
        }
        if (mop2.dest != Operand.OP_DEST_FALSE) {
        	if (ins.inst == InstructionTable.Icb)
        		mop2.dest = 1;
            if (mop2.byteop) {
				memory.writeByte(iblock.ea2, (byte) iblock.val2);
			} else {
                memory.writeWord(iblock.ea2, iblock.val2);
                if (ins.inst == InstructionTable.Impy || ins.inst == InstructionTable.Idiv) {
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
     * Get interpret-time behavior
     * 
     * @param ins
     */
    public InstructionAction getInterpretAction(final Cpu cpu, Instruction ins) {
        InstructionAction act = null;

        switch (ins.inst) {
        case InstructionTable.Idata:
            break;
        case InstructionTable.Ili:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = block.val2;
                }
            };
            break;
        case InstructionTable.Iai:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 += block.val2;
                }
            };
            break;
        case InstructionTable.Iandi:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 &= block.val2;
                }
            };
            break;
        case InstructionTable.Iori:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 |= block.val2;
                }
            };
            break;
        case InstructionTable.Ici:
            break;
        case InstructionTable.Istwp:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = block.wp;
                }
            };
            break;
        case InstructionTable.Istst:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = block.status.flatten();
                }
            };
            break;
        case InstructionTable.Ilwpi:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.wp = block.val1;
                }
            };
            break;
        case InstructionTable.Ilimi:
            // all done in status (Status#setIntMask() performed as post-instruction
        	// action due to ST_INT effect)
            break;
        case InstructionTable.Iidle:
            //cpu.idle(); // TODO
            break;
        case InstructionTable.Irset:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = 0;
                }
            };
            //cpu.rset(); // TODO
            break;
        case InstructionTable.Irtwp:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.status.expand(memory.readWord(block.wp + 15 * 2));
                    //block.val1 = memory.readWord(block.wp + 15 * 2);
                    block.pc = memory.readWord(block.wp + 14 * 2);
                    block.wp = memory.readWord(block.wp + 13 * 2);
                }
            };
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
            act = new InstructionAction() {
                public void act(Block block) {
                    /*
                    //TODO NB: must flush values now to share code
                    cpu.setPC((short) block.pc);
                    cpu.contextSwitch(block.val1);
                    block.pc = cpu.getPC();
                    block.wp = cpu.getWP();
                    */
                    block.wp = memory.readWord(block.val1);
                    block.pc = memory.readWord(block.val1 + 2);
                }
            };
            break;

        case InstructionTable.Ib:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.pc = block.val1;
                }
            };
            break;
        case InstructionTable.Ix:
            act = new InstructionAction() {
                public void act(Block block) {
                    short newPc = block.pc;
                    execute(cpu, block.val1);
                    block.pc = newPc;
                }
            };
            break;
        case InstructionTable.Iclr:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = 0;
                }
            };
            break;
        case InstructionTable.Ineg:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = (short) -block.val1;
                }
            };
            break;
        case InstructionTable.Iinv:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = (short) ~block.val1;
                }
            };
            break;
        case InstructionTable.Iinc:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1++;
                }
            };
            break;
        case InstructionTable.Iinct:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 += 2;
                }
            };
            break;
        case InstructionTable.Idec:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1--;
                }
            };
            break;
        case InstructionTable.Idect:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 -= 2;
                }
            };
            break;
        case InstructionTable.Ibl:
            act = new InstructionAction() {
                public void act(Block block) {
                    memory.writeWord(block.wp + 11 * 2, block.pc);
                    block.pc = block.val1;
                }
            };
            break;
        case InstructionTable.Iswpb:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = (short) (block.val1 >> 8 & 0xff | block.val1 << 8 & 0xff00);
                }
            };
            break;
        case InstructionTable.Iseto:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = -1;
                }
            };
            break;
        case InstructionTable.Iabs:
            act = new InstructionAction() {
                public void act(Block block) {
                    if ((block.val1 & 0x8000) != 0) {
						block.val1 = (short) -block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;
        case InstructionTable.Isra:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = (short) (block.val1 >> block.val2);
                    cpu.addCycles(block.val2 * 2);
                }
            };
            break;
        case InstructionTable.Isrl:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = (short) ((block.val1 & 0xffff) >> block.val2);
                    cpu.addCycles(block.val2 * 2);
                }
            };
            break;

        case InstructionTable.Isla:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = (short) (block.val1 << block.val2);
                    cpu.addCycles(block.val2 * 2);
                }
            };
            break;

        case InstructionTable.Isrc:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = (short) ((block.val1 & 0xffff) >> block.val2 | (block.val1 & 0xffff) << 16 - block.val2);
                    cpu.addCycles(block.val2 * 2);
                }
            };
            break;

        case InstructionTable.Ijmp:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.pc = block.val1;
                    cpu.addCycles(2);
                }
            };
            break;
        case InstructionTable.Ijlt:
            act = new InstructionAction() {
                public void act(Block block) {
                    if (block.status.isLT()) {
						block.pc = block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;
        case InstructionTable.Ijle:
            act = new InstructionAction() {
                public void act(Block block) {
                    if (block.status.isLE()) {
						block.pc = block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;

        case InstructionTable.Ijeq:
            act = new InstructionAction() {
                public void act(Block block) {
                    if (block.status.isEQ()) {
						block.pc = block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;
        case InstructionTable.Ijhe:
            act = new InstructionAction() {
                public void act(Block block) {
                    if (block.status.isHE()) {
						block.pc = block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;
        case InstructionTable.Ijgt:
            act = new InstructionAction() {
                public void act(Block block) {
                    if (block.status.isGT()) {
						block.pc = block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;
        case InstructionTable.Ijne:
            act = new InstructionAction() {
                public void act(Block block) {
                    if (block.status.isNE()) {
						block.pc = block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;
        case InstructionTable.Ijnc:
            act = new InstructionAction() {
                public void act(Block block) {
                    if (!block.status.isC()) {
						block.pc = block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;
        case InstructionTable.Ijoc:
            act = new InstructionAction() {
                public void act(Block block) {
                    if (block.status.isC()) {
						block.pc = block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;
        case InstructionTable.Ijno:
            act = new InstructionAction() {
                public void act(Block block) {
                    if (!block.status.isO()) {
						block.pc = block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;
        case InstructionTable.Ijl:
            act = new InstructionAction() {
                public void act(Block block) {
                    if (block.status.isL()) {
						block.pc = block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;
        case InstructionTable.Ijh:
            act = new InstructionAction() {
                public void act(Block block) {
                    if (block.status.isH()) {
						block.pc = block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;

        case InstructionTable.Ijop:
            act = new InstructionAction() {
                public void act(Block block) {
                    // jump on ODD parity
                    if (block.status.isP()) {
						block.pc = block.val1;
						cpu.addCycles(2);
					}
                }
            };
            break;

        case InstructionTable.Isbo:
            act = new InstructionAction() {
                public void act(Block block) {
                    client.getCruHandler().writeBits(block.val1, 1, 1);
                }
            };
            break;

        case InstructionTable.Isbz:
            act = new InstructionAction() {
                public void act(Block block) {
                    client.getCruHandler().writeBits(block.val1, 0, 1);
                }
            };
            break;

        case InstructionTable.Itb:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = (short) client.getCruHandler().readBits(block.val1, 1);
                    block.val2 = 0;
                }
            };
            break;

        case InstructionTable.Icoc:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val2 = (short) (block.val1 & block.val2);
                }
            };
            break;

        case InstructionTable.Iczc:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val2 = (short) (block.val1 & ~block.val2);
                }
            };
            break;

        case InstructionTable.Ixor:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val2 ^= block.val1;
                }
            };
            break;

        case InstructionTable.Ixop:
            act = new InstructionAction() {
                public void act(Block block) {
                    /*
                    //TODO NB: must flush values now to share code
                    cpu.setPC(block.pc);
                    cpu.contextSwitch(block.val1 * 4 + 0x40);
                    block.pc = cpu.getPC();
                    block.wp = cpu.getWP();
                    */
                    block.wp = memory.readWord(block.val1 * 4 + 0x40);
                    block.pc = memory.readWord(block.val1 * 4 + 0x42);
                }
            };
            break;

        case InstructionTable.Impy:
            act = new InstructionAction() {
                public void act(Block block) {
                    int val = (block.val1 & 0xffff)
                            * (block.val2 & 0xffff);
                    // manually write second reg
                    block.val3 = (short) val;
                    //memory.writeWord(block.op2.ea + 2, (short) val);
                    block.val2 = (short) (val >> 16);
                }
            };
            break;

        case InstructionTable.Idiv:
            act = new InstructionAction() {
                public void act(Block block) {
                    // manually read second reg
                    if (block.val1 > block.val2) {
                        short low = block.val3;
                        //short low = memory.readWord(block.op2.ea + 2);
                        int val = (block.val2 & 0xffff) << 16
                                | low & 0xffff;
                        block.val2 = (short) (val / (block.val1 & 0xffff));
                        block.val3 = (short) (val % (block.val1 & 0xffff));
                        //memory.writeWord(block.op2.ea + 2,
                        //        (short) (val % (block.val1 & 0xffff)));
                        //inst.op2.value = (short) val;
                    } else {
                    	cpu.addCycles((124 + 92) / 2 - 16);
                    }
                }
            };
            break;

        case InstructionTable.Ildcr:
            act = new InstructionAction() {
                public void act(Block block) {
                    client.getCruHandler().writeBits(
                            memory.readWord(block.wp + 12 * 2), block.val1,
                            block.val2);
                }
            };
            break;

        case InstructionTable.Istcr:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val1 = (short) client.getCruHandler().readBits(
                            memory.readWord(block.wp + 12 * 2), block.val2);
                }
            };
            break;
        case InstructionTable.Iszc:
        case InstructionTable.Iszcb:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val2 &= ~block.val1;
                }
            };
            break;

        case InstructionTable.Is:
        case InstructionTable.Isb:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val2 -= block.val1;
                }
            };
            break;

        case InstructionTable.Ic:
        case InstructionTable.Icb:
            break;

        case InstructionTable.Ia:
        case InstructionTable.Iab:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val2 += block.val1;
                }
            };
            break;

        case InstructionTable.Imov:
        case InstructionTable.Imovb:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val2 = block.val1;
                }
            };
            break;

        case InstructionTable.Isoc:
        case InstructionTable.Isocb:
            act = new InstructionAction() {
                public void act(Block block) {
                    block.val2 |= block.val1;
                }
            };
            break;

        }

        return act;
    }
}