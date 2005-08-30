/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.cpu;

import java.io.PrintWriter;

import v9t9.Machine;
import v9t9.MemoryDomain;

/**
 * This class interprets 9900 instructions one by one.
 * 
 * @author ejs
 */
public class Interpreter {
    Cpu cpu;

    Machine machine;

    MemoryDomain memory;

    PrintWriter dump, dumpfull;

    java.util.Map instructions;
    
    Executor.Action.Block iblock;
    Executor exec;
    
    public Interpreter(Cpu cpu) {
        this.cpu = cpu;
        this.machine = cpu.getMachine();
        this.memory = machine.getMemory().CPU;
        instructions = new java.util.TreeMap();
        iblock = new Executor.Action.Block();
        iblock.domain = memory;
     }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
        super.finalize();
        if (dump != null)
            dump.close();
        if (dumpfull != null)
            dumpfull.close();
    }

    public void execute(short op) {
        /*
         * decode one instruction and update any autoincrement registers
         * referenced in operands
         */
        Instruction ins;
        Integer thePc = new Integer(cpu.getPC());

        if (dump == null) {
            dump = machine.getExecutor().dump;
            dumpfull = machine.getExecutor().dumpfull;
        }
        
        if ((ins = (Instruction)instructions.get(thePc)) != null) {
            ins = ins.update(op, (short)thePc.intValue(), cpu.console);
        } else 
        {
            ins = new Instruction(op, (short)thePc.intValue(), cpu.console);
        }
        instructions.put(thePc, ins);

        /* dump instruction */
        if (dumpfull != null) {
            dumpfull.print(Integer.toHexString(ins.pc).toUpperCase() + ": "
                    + ins.toString() + " ==> ");
        }
        if (dump != null) {
            dump.println(v9t9.Globals.padAddress(Integer.toHexString(
                    ins.pc & 0xffff).toUpperCase())
                    + " "
                    + v9t9.Globals.padAddress(Integer.toHexString(
                            cpu.getStatus().flatten() & 0xffff).toUpperCase())
                    + " "
                    + v9t9.Globals.toHex4(cpu.memory.getVdpMmio().getAddr())
                    + " "
                    + v9t9.Globals.toHex4(cpu.memory.getGplMmio().getAddr()));
            dump.flush();
        }

        /* generate the functor for the instruction */
        Executor.Action act = getInterpretAction(ins);

        /* get current operand values and instruction timings */
        fetchOperands(ins, cpu.getWP(), cpu.getStatus());

        /* dump values before execution */
        String str;

        if (dumpfull != null) {
            if (ins.op1.type != Operand.OP_NONE
                    && ins.op1.dest != Operand.OP_DEST_KILLED) {
                str = ins.op1.valueString(iblock.ea1, iblock.val1);
                if (str != null) {
                    dumpfull.print("op1=" + str + " ");
                }
            }
            if (ins.op2.type != Operand.OP_NONE
                    && ins.op2.dest != Operand.OP_DEST_KILLED) {
                str = ins.op2.valueString(iblock.ea2, iblock.val2);
                if (str != null)
                    dumpfull.print("op2=" + str);
            }
            dumpfull.print(" || ");
        }

        /* do pre-instruction status word updates */
        if (ins.stsetBefore != Instruction.st_NONE) {
            updateStatus(ins.stsetBefore);
        }

        /* execute */
        if (act != null)
            act.act(iblock);

        /* do post-instruction status word updates */
        if (ins.stsetAfter != Instruction.st_NONE) {
            updateStatus(ins.stsetAfter);
        }

        /* save any operands */
        flushOperands(ins);
        
        /* dump values after execution */
        if (dumpfull != null) {
            if (ins.op1.type != Operand.OP_NONE
                    && ins.op1.dest != Operand.OP_DEST_FALSE) {
                str = ins.op1.valueString(iblock.ea1, iblock.val1);
                if (str != null) {
                    dumpfull.print("op1=" + str + " ");
                }
            }
            if (ins.op2.type != Operand.OP_NONE
                    && ins.op2.dest != Operand.OP_DEST_FALSE) {
                str = ins.op2.valueString(iblock.ea2, iblock.val2);
                if (str != null)
                    dumpfull.print("op2=" + str + " ");
            }
            dumpfull.print("st="
                    + Integer.toHexString(cpu.getStatus().flatten() & 0xffff)
                            .toUpperCase() + " wp="
                    + Integer.toHexString(cpu.getWP() & 0xffff).toUpperCase());
            dumpfull.println();
            dumpfull.flush();
        }
    }

    /** Fetch operands for instruction (runtime)
     * @param ins
     * @param memory2
     */
    private void fetchOperands(Instruction ins, short wp, Status st) {
        iblock.inst = ins;
        iblock.pc = (short) (iblock.inst.pc + iblock.inst.size);
        iblock.wp = cpu.getWP();
        //if (((iblock.inst.reads | iblock.inst.writes) & Instruction.INST_RSRC_WP) != 0)
        //    iblock.wp = iblock.inst.wp;
        //if (((iblock.inst.reads | iblock.inst.writes) & Instruction.INST_RSRC_ST) != 0)
        //    iblock.status = iblock.inst.status;
        iblock.status = st;
        if (iblock.inst.op1.type != Operand.OP_NONE)
            iblock.ea1 = iblock.inst.op1.getEA(memory, iblock.inst.pc, wp);
        if (iblock.inst.op1.type != Operand.OP_NONE)
            iblock.ea2 = iblock.inst.op2.getEA(memory, iblock.inst.pc, wp);
        if (iblock.inst.op1.type != Operand.OP_NONE)
            iblock.val1 = iblock.inst.op1.getValue(memory, iblock.ea1);
        if (iblock.inst.op2.type != Operand.OP_NONE)
            iblock.val2 = iblock.inst.op2.getValue(memory, iblock.ea2);
        if (iblock.inst.inst == Instruction.Idiv) {
            iblock.val3 = memory.readWord(iblock.ea2 + 2);
        }
    }

    /**
     * 
     */
    private void flushOperands(Instruction ins) {
        if (ins.op1.dest != Operand.OP_DEST_FALSE) {
            if (ins.op1.byteop)
                memory.writeByte(iblock.ea1, (byte) iblock.val1);
            else
                memory.writeWord(iblock.ea1, iblock.val1);
        }
        if (ins.op2.dest != Operand.OP_DEST_FALSE) {
            if (ins.op2.byteop)
                memory.writeByte(iblock.ea2, (byte) iblock.val2);
            else {
                memory.writeWord(iblock.ea2, iblock.val2);
                if (ins.inst == Instruction.Impy || ins.inst == Instruction.Idiv) {
                    memory.writeWord(iblock.ea2 + 2, iblock.val3);
                }
            }
        }

        if ((ins.writes & Instruction.INST_RSRC_ST) != 0)
            cpu.setStatus(iblock.status);

        /* do this after flushing status */
        if ((ins.writes & Instruction.INST_RSRC_CTX) != 0) {
            /* update PC first */
            cpu.setPC((short) (iblock.inst.pc + iblock.inst.size));
            cpu.contextSwitch(iblock.wp, iblock.pc);
        } else {
            /* flush register changes */
            cpu.setPC(iblock.pc);
            if ((ins.writes & Instruction.INST_RSRC_WP) != 0)
                cpu.setWP(iblock.wp);
        }
    }

    /**
     * @param iblock
     * @param ins
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
    public Executor.Action getInterpretAction(Instruction ins) {
        Executor.Action act = null;

        switch (ins.inst) {
        case Instruction.Idata:
            break;
        case Instruction.Ili:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = block.val2;
                }
            };
            break;
        case Instruction.Iai:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 += block.val2;
                }
            };
            break;
        case Instruction.Iandi:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 &= block.val2;
                }
            };
            break;
        case Instruction.Iori:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 |= block.val2;
                }
            };
            break;
        case Instruction.Ici:
            break;
        case Instruction.Istwp:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = block.wp;
                }
            };
            break;
        case Instruction.Istst:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = block.status.flatten();
                }
            };
            break;
        case Instruction.Ilwpi:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.wp = block.val1;
                }
            };
            break;
        case Instruction.Ilimi:
            // all done in status
            break;
        case Instruction.Iidle:
            //cpu.idle(); // TODO
            break;
        case Instruction.Irset:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = 0;
                }
            };
            //cpu.rset(); // TODO
            break;
        case Instruction.Irtwp:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.status.expand(memory.readWord(block.wp + 15 * 2));
                    //block.val1 = memory.readWord(block.wp + 15 * 2);
                    block.pc = (memory.readWord(block.wp + 14 * 2));
                    block.wp = (memory.readWord(block.wp + 13 * 2));
                }
            };
            break;
        case Instruction.Ickon:
            // TODO
            break;
        case Instruction.Ickof:
            // TODO
            break;
        case Instruction.Ilrex:
            // TODO
            break;
        case Instruction.Iblwp:
            act = new Executor.Action() {
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

        case Instruction.Ib:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.pc = block.val1;
                }
            };
            break;
        case Instruction.Ix:
            act = new Executor.Action() {
                public void act(Block block) {
                    execute(block.val1);
                }
            };
            break;
        case Instruction.Iclr:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = 0;
                }
            };
            break;
        case Instruction.Ineg:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = (short) -block.val1;
                }
            };
            break;
        case Instruction.Iinv:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = (short) ~block.val1;
                }
            };
            break;
        case Instruction.Iinc:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1++;
                }
            };
            break;
        case Instruction.Iinct:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 += 2;
                }
            };
            break;
        case Instruction.Idec:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1--;
                }
            };
            break;
        case Instruction.Idect:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 -= 2;
                }
            };
            break;
        case Instruction.Ibl:
            act = new Executor.Action() {
                public void act(Block block) {
                    memory.writeWord(block.wp + 11 * 2, block.pc);
                    block.pc = (block.val1);
                }
            };
            break;
        case Instruction.Iswpb:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = (short) (((block.val1 >> 8) & 0xff) | ((block.val1 << 8) & 0xff00));
                }
            };
            break;
        case Instruction.Iseto:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = -1;
                }
            };
            break;
        case Instruction.Iabs:
            act = new Executor.Action() {
                public void act(Block block) {
                    if ((block.val1 & 0x8000) != 0)
                        block.val1 = (short) -block.val1;
                }
            };
            break;
        case Instruction.Isra:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = (short) (block.val1 >> block.val2);
                }
            };
            break;
        case Instruction.Isrl:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = (short) ((block.val1 & 0xffff) >> block.val2);
                }
            };
            break;

        case Instruction.Isla:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = (short) (block.val1 << block.val2);
                }
            };
            break;

        case Instruction.Isrc:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = (short) (((block.val1 & 0xffff) >> block.val2) | ((block.val1 & 0xffff) << (16 - block.val2)));
                }
            };
            break;

        case Instruction.Ijmp:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.pc = block.val1;
                }
            };
            break;
        case Instruction.Ijlt:
            act = new Executor.Action() {
                public void act(Block block) {
                    if (block.status.isLT())
                        block.pc = block.val1;
                }
            };
            break;
        case Instruction.Ijle:
            act = new Executor.Action() {
                public void act(Block block) {
                    if (block.status.isLE())
                        block.pc = block.val1;
                }
            };
            break;

        case Instruction.Ijeq:
            act = new Executor.Action() {
                public void act(Block block) {
                    if (block.status.isEQ())
                        block.pc = block.val1;
                }
            };
            break;
        case Instruction.Ijhe:
            act = new Executor.Action() {
                public void act(Block block) {
                    if (block.status.isHE())
                        block.pc = block.val1;
                }
            };
            break;
        case Instruction.Ijgt:
            act = new Executor.Action() {
                public void act(Block block) {
                    if (block.status.isGT())
                        block.pc = block.val1;
                }
            };
            break;
        case Instruction.Ijne:
            act = new Executor.Action() {
                public void act(Block block) {
                    if (block.status.isNE())
                        block.pc = block.val1;
                }
            };
            break;
        case Instruction.Ijnc:
            act = new Executor.Action() {
                public void act(Block block) {
                    if (!block.status.isC())
                        block.pc = block.val1;
                }
            };
            break;
        case Instruction.Ijoc:
            act = new Executor.Action() {
                public void act(Block block) {
                    if (block.status.isC())
                        block.pc = block.val1;
                }
            };
            break;
        case Instruction.Ijno:
            act = new Executor.Action() {
                public void act(Block block) {
                    if (!block.status.isO())
                        block.pc = block.val1;
                }
            };
            break;
        case Instruction.Ijl:
            act = new Executor.Action() {
                public void act(Block block) {
                    if (block.status.isL())
                        block.pc = block.val1;
                }
            };
            break;
        case Instruction.Ijh:
            act = new Executor.Action() {
                public void act(Block block) {
                    if (block.status.isH())
                        block.pc = block.val1;
                }
            };
            break;

        case Instruction.Ijop:
            act = new Executor.Action() {
                public void act(Block block) {
                    // jump on ODD parity
                    if (block.status.isP())
                        block.pc = block.val1;
                }
            };
            break;

        case Instruction.Isbo:
            act = new Executor.Action() {
                public void act(Block block) {
                    machine.getCru().writeBits(block.val1, 1, 1);
                }
            };
            break;

        case Instruction.Isbz:
            act = new Executor.Action() {
                public void act(Block block) {
                    machine.getCru().writeBits(block.val1, 0, 1);
                }
            };
            break;

        case Instruction.Itb:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = (short) machine.getCru().readBits(block.val1, 1);
                    block.val2 = 0;
                }
            };
            break;

        case Instruction.Icoc:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val2 = (short) (block.val1 & block.val2);
                }
            };
            break;

        case Instruction.Iczc:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val2 = (short) (block.val1 & ~block.val2);
                }
            };
            break;

        case Instruction.Ixor:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val2 ^= block.val1;
                }
            };
            break;

        case Instruction.Ixop:
            act = new Executor.Action() {
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

        case Instruction.Impy:
            act = new Executor.Action() {
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

        case Instruction.Idiv:
            act = new Executor.Action() {
                public void act(Block block) {
                    // manually read second reg
                    if (block.val1 > block.val2) {
                        short low = block.val3;
                        //short low = memory.readWord(block.op2.ea + 2);
                        int val = ((block.val2 & 0xffff) << 16)
                                | (low & 0xffff);
                        block.val2 = (short) (val / (block.val1 & 0xffff));
                        block.val3 = (short) (val % (block.val1 & 0xffff));
                        //memory.writeWord(block.op2.ea + 2,
                        //        (short) (val % (block.val1 & 0xffff)));
                        //inst.op2.value = (short) val;
                    }
                }
            };
            break;

        case Instruction.Ildcr:
            act = new Executor.Action() {
                public void act(Block block) {
                    machine.getCru().writeBits(
                            memory.readWord(block.wp + 12 * 2), block.val1,
                            block.val2);
                }
            };
            break;

        case Instruction.Istcr:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val1 = (short) machine.getCru().readBits(
                            memory.readWord(block.wp + 12 * 2), block.val2);
                }
            };
            break;
        case Instruction.Iszc:
        case Instruction.Iszcb:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val2 &= ~block.val1;
                }
            };
            break;

        case Instruction.Is:
        case Instruction.Isb:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val2 -= block.val1;
                }
            };
            break;

        case Instruction.Ic:
        case Instruction.Icb:
            break;

        case Instruction.Ia:
        case Instruction.Iab:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val2 += block.val1;
                }
            };
            break;

        case Instruction.Imov:
        case Instruction.Imovb:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val2 = block.val1;
                }
            };
            break;

        case Instruction.Isoc:
        case Instruction.Isocb:
            act = new Executor.Action() {
                public void act(Block block) {
                    block.val2 |= block.val1;
                }
            };
            break;

        }

        return act;
    }
}