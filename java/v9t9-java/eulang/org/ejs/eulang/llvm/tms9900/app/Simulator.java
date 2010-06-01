/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.app;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.TypeEngine.Alignment;
import org.ejs.eulang.TypeEngine.Target;
import org.ejs.eulang.llvm.tms9900.AsmInstruction;
import org.ejs.eulang.llvm.tms9900.Block;
import org.ejs.eulang.llvm.tms9900.BuildOutput;
import org.ejs.eulang.llvm.tms9900.CodeVisitor;
import org.ejs.eulang.llvm.tms9900.DataBlock;
import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.InstrSelection;
import org.ejs.eulang.llvm.tms9900.Locals;
import org.ejs.eulang.llvm.tms9900.RegisterLocal;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.ejs.eulang.llvm.tms9900.StackLocal;
import org.ejs.eulang.llvm.tms9900.asm.CompareOperand;
import org.ejs.eulang.llvm.tms9900.asm.CompositePieceOperand;
import org.ejs.eulang.llvm.tms9900.asm.ISymbolOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.ejs.eulang.llvm.tms9900.asm.SymbolOperand;
import org.ejs.eulang.llvm.tms9900.asm.TupleTempOperand;
import org.ejs.eulang.llvm.tms9900.asm.ZeroInitOperand;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLAggregateType;
import org.ejs.eulang.types.LLType;

import v9t9.emulator.hardware.memory.EnhancedRamArea;
import v9t9.emulator.runtime.TerminatedException;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.Status;
import v9t9.engine.cpu.Instruction.Effects;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.IRegisterOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIncOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.RegisterOperand;

/**
 * This is a simulator for assembler-level instructions.  It has no support
 * for special memory areas or I/O or interrupts at all.  
 * @author ejs
 *
 */
public class Simulator {

	private MemoryDomain memory;
	private TreeMap<Short, AsmInstruction> pcToInstrMap;
	private HashMap<ISymbol, Short> symbolToAddrMap;
	private TreeMap<Short, ISymbol> addrToSymbolMap;
	private BuildOutput buildOutput;
	private Cpu cpu;
	private HashMap<ISymbol, Short> vrToAddrMap;
	private short vrAddr;
	private final ITarget target;


	/**
	 * This interface receives details about an instruction's effects
	 * @author ejs
	 *
	 */
	public interface InstructionListener {
		void executed(InstructionWorkBlock before, InstructionWorkBlock after);
	}


	public static class InstructionWorkBlock {
	    /** our CPU memory */
	    public MemoryDomain domain;
	    /** the instruction (in) */
	    public AsmInstruction inst;	
	    public short instPC; 
	    /** EAs for operands 1 and 2 */
	    public short ea1, ea2, ea3;
	    /** values for operands 1 and 2 (in: EAs or values, out: value)
	    for MPY/DIV, val3 holds lo reg */
	    public short val1, val2, val3;	
	    /** values (in: original, out: changed, if needed) */
	    public short pc, wp;
	    /** status word (in/out) */
	    public Status status = new Status();
	    /** cycle count */
	    public int cycles;
	    
	    public void copyTo(InstructionWorkBlock copy) {
	    	copy.domain = domain;
	    	copy.inst = inst;
	    	copy.instPC = instPC;
	    	copy.ea1 = ea1;
	    	copy.ea2 = ea2;
	    	copy.val1 = val1;
	    	copy.val2 = val2;
	    	copy.val3 = val2;
	    	copy.pc = pc;
	    	copy.wp = wp;
	    	status.copyTo(copy.status);
	    }
	}
	
	static public class Cpu {
		short PC, WP;
		
	    public short getPC() {
	        return PC;
	    }

	    public void setPC(short pc) {
	        PC = pc;
	    }

	    Status status = new Status();
		private int cycles;

	    public short getST() {
	        return status.flatten();
	    }

	    public void setST(short st) {
	        status.expand(st);
	    }

	    public Status getStatus() {
	        return status;
	    }

	    public void setStatus(Status status) {
	        this.status = status;
	    }

	    public short getWP() {
	        return WP;
	    }

	    public void setWP(short wp) {
	        WP = wp;
	    }

		public void addCycles(int i) {
			cycles += i;
		}

		public int getCurrentCycleCount() {
			return cycles;
		}

		/**
		 * @param wp2
		 * @param pc2
		 */
		public void contextSwitch(short wp2, short pc2) {
			assert false;
		}

	}
	
	public class DumpFullReporter implements InstructionListener {

		private PrintWriter dumpfull;


		/**
		 * 
		 */
		public DumpFullReporter() {
			dumpfull = new PrintWriter(System.out);

		}
		/* (non-Javadoc)
		 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
		 */
		public void executed(InstructionWorkBlock before, InstructionWorkBlock after) {
			if (dumpfull == null) return;
			dumpFullStart(before, before.inst, dumpfull);
			dumpFullMid(before, before.inst.getOp1(), before.inst.getOp2(), dumpfull);
			dumpFullEnd(after, before.cycles, after.inst.getOp1(), after.inst.getOp2(), dumpfull);
		}

		private void dumpFullStart(InstructionWorkBlock iinstructionWorkBlock,
				AsmInstruction ins, PrintWriter dumpfull) {
			
			ISymbol sym = getSymbol(iinstructionWorkBlock.instPC);
			if (sym != null)
				dumpfull.println('"' + sym.getUniqueName() + "\" ");
			StringBuilder sb = new StringBuilder();
			sb.append(HexUtils.toHex4(iinstructionWorkBlock.instPC)).append(": ").append(ins.toBaseString());
			while (sb.length() < 40)
				sb.append(' ');
			dumpfull.print(sb + " ==> ");
		}
		private void dumpFullMid(InstructionWorkBlock iinstructionWorkBlock,
				AssemblerOperand mop1, AssemblerOperand mop2,
				PrintWriter dumpfull) {
			String str;
			if (mop1 != null
			        && iinstructionWorkBlock.inst.getEffects().mop1_dest != Operand.OP_DEST_KILLED) {
			    str = valueString(iinstructionWorkBlock.ea1, iinstructionWorkBlock.val1,
			    		iinstructionWorkBlock.inst.getEffects().byteop);
			    if (str != null) {
			        dumpfull.print("op1=" + str + " ");
			    }
			}
			if (mop2 != null
			        && iinstructionWorkBlock.inst.getEffects().mop2_dest != Operand.OP_DEST_KILLED) {
			    str = valueString(iinstructionWorkBlock.ea2, iinstructionWorkBlock.val2,
			    		iinstructionWorkBlock.inst.getEffects().byteop);
			    if (str != null) {
					dumpfull.print("op2=" + str);
				}
			}
			dumpfull.print(" || ");
		}
		private void dumpFullEnd(InstructionWorkBlock iinstructionWorkBlock, 
				int origCycleCount, AssemblerOperand mop1,
				AssemblerOperand mop2, PrintWriter dumpfull) {
			String str;
			if (mop1 != null
			        && (iinstructionWorkBlock.inst.getEffects().mop1_dest != Operand.OP_DEST_FALSE
			        		|| mop1 instanceof RegIncOperand)) {
			    str = valueString(iinstructionWorkBlock.ea1, iinstructionWorkBlock.val1,
			    		iinstructionWorkBlock.inst.getEffects().byteop);
			    if (str != null) {
			        dumpfull.print("op1=" + str + " ");
			    }
			}
			if (mop2 != null
					&& (iinstructionWorkBlock.inst.getEffects().mop2_dest != Operand.OP_DEST_FALSE
			        		|| mop2 instanceof RegIncOperand)) {
			    str = valueString(iinstructionWorkBlock.ea2, iinstructionWorkBlock.val2,
			    		iinstructionWorkBlock.inst.getEffects().byteop);
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
		

	    /**
	     * @param byteop 
	     * @return
	     */
	    public String valueString(short ea, short theValue, boolean byteop) {
	       if (byteop) {
	    	   theValue &= 0xff;
	       }
	        return Integer.toHexString(theValue & 0xffff).toUpperCase()
	        	+"(@"+Integer.toHexString(ea & 0xffff).toUpperCase()+")"; 
	    }


	}

	public Simulator(ITarget target, BuildOutput output) {
		
		this.target = target;
		this.buildOutput = output;
		
		this.cpu = new Cpu();
		
        iblock = new InstructionWorkBlock();
        
        memory = new MemoryDomain("CPU");
		MemoryArea bigRamArea = new EnhancedRamArea(0, 0x10000); 
		MemoryEntry bigRamEntry = new MemoryEntry("RAM", memory, 0, MemoryDomain.PHYSMEMORYSIZE, 
				bigRamArea);
		memory.mapEntry(bigRamEntry);
		
        pcToInstrMap = new TreeMap<Short, AsmInstruction>();
        symbolToAddrMap = new HashMap<ISymbol, Short>();
        addrToSymbolMap = new TreeMap<Short, ISymbol>();
        
        vrToAddrMap = new HashMap<ISymbol, Short>();
	}
	
	/**
	 * 
	 */
	public void init() {
		
        pcToInstrMap.clear();
        symbolToAddrMap.clear();
        addrToSymbolMap.clear();
        vrToAddrMap.clear();
        
        short pc = 0x100;
        for (Routine routine : buildOutput.getRoutines()) {
        	pc = emitRoutine(pc, routine);
        }
        
        short addr = (short) 0x8000;
        for (DataBlock data : buildOutput.getDataBlocks()) {
        	addr = emitDataBlock(addr, data);
        }
        
        vrAddr = addr; 
	}

	
	private short emitDataBlock(short addr, DataBlock data) {
		System.out.println("alloc " + HexUtils.toHex4(addr)+": " + data.getName() + " = " + data.getValue());
		symbolToAddrMap.put(data.getName(), addr);
		addrToSymbolMap.put(addr, data.getName());
		
		Alignment align = target.getTypeEngine().new Alignment(Target.STRUCT);
		emitData(addr, align, data.getName().getType(), data.getValue());
		return (short) (addr + align.sizeof() / 8);
	}

	/**
	 * @param addr
	 * @param align
	 * @param type
	 * @param op
	 */
	private void emitData(short addr, Alignment align, LLType type,
			AssemblerOperand op) {
		if (op instanceof NumberOperand) {
			if (type.getBits() <= 8) {
				memory.writeByte(addr, (byte) ((NumberOperand) op).getValue());
			} else if (type.getBits() == 16) {
				memory.writeWord(addr, (short) ((NumberOperand) op).getValue());
			} else {
				assert false;
			}
		} else if (op instanceof SymbolOperand) {
			assert type.getBits() == 16;
			memory.writeWord(addr, getAddress(((SymbolOperand) op).getSymbol()));
		} else if (op instanceof ZeroInitOperand) {
			for (int i = 0; i < type.getBits() / 8; i++) {
				memory.writeByte(addr + i, (byte) 0);
			}
		} else if (op instanceof TupleTempOperand) {
			// TODO: clean up this pattern
			TupleTempOperand tto = (TupleTempOperand) op;
			AssemblerOperand[] components = tto.getComponents();
			Alignment subAlign = target.getTypeEngine().new Alignment(Target.STRUCT);
			for (int i = 0; i < components.length; i++) {
				LLType subType = type instanceof LLAggregateType ? ((LLAggregateType) type).getType(i) : type.getSubType();
				AssemblerOperand subOp = components[i];
				short subAddr = (short) (addr + (subAlign.sizeof() + subAlign.alignmentGap(subType)) / 8);
				emitData(subAddr, subAlign, subType, subOp);
			}
		} else {
			assert false;
		}
		
		align.alignAndAdd(type);
	}

	private short emitRoutine(final short pc, Routine routine) {
		final short[] thePc = { pc };
		routine.accept(new CodeVisitor() {
			@Override
			public Walk getWalk() {
				return Walk.LINEAR;
			}
			
			@Override
			public boolean enterRoutine(Routine routine) {
				System.out.println(HexUtils.toHex4(thePc[0])+": " + routine.getName());
				symbolToAddrMap.put(routine.getName(), thePc[0]);
				addrToSymbolMap.put(thePc[0], routine.getName());
				return true;
			}
			
			@Override
			public boolean enterBlock(Block block) {
				System.out.println(HexUtils.toHex4(thePc[0])+": " + block.getLabel());
				symbolToAddrMap.put(block.getLabel(), thePc[0]);
				if (!addrToSymbolMap.containsKey(thePc[0]))
					addrToSymbolMap.put(thePc[0], block.getLabel());
				return true;
			}
			
			@Override
			public boolean enterInstr(Block block, AsmInstruction instr) {
				System.out.println(HexUtils.toHex4(thePc[0])+": " + instr);
				pcToInstrMap.put(thePc[0], instr);
				thePc[0] += 2;
				return false;
			}
		});
		
		return thePc[0];
	}

	InstructionWorkBlock iblock;
	private int debugCount;
	private List<InstructionListener> listeners;

    /**
     * Execute an instruction: general entry point
     * @param cpu
     * @param op_x if not-null, execute the instruction from an X instruction
     */
    public void execute(Cpu cpu) {
		//PrintWriter dump = machine.getExecutor().getDump();
		//PrintWriter dumpfull = machine.getExecutor().getDumpfull();
		
        AsmInstruction ins = getInstruction(cpu);
        Effects fx = ins.getEffects();
        
        /*
        if (dumpfull != null) {
            dumpFullStart(ins, dumpfull);
        }
        if (dump != null) {
            dumpStart(cpu, ins, dump);
        }*/

        iblock.cycles = cpu.getCurrentCycleCount();
        
        /* get current operand values and instruction timings */
        fetchOperands(cpu, ins);

        InstructionWorkBlock block = new InstructionWorkBlock();
        this.iblock.copyTo(block);
        
        /* dump values before execution */
        /*
        if (dumpfull != null) {
            dumpFullMid(mop1, mop2, dumpfull);
        }*/

        /* do pre-instruction status word updates */
        if (fx.stsetBefore != Instruction.st_NONE) {
            updateStatus(fx.stsetBefore);
        }

        /* execute */
        interpret(cpu, ins);
        
        /* do post-instruction status word updates */
        if (fx.stsetAfter != Instruction.st_NONE) {
            updateStatus(fx.stsetAfter);
        }

        /* save any operands */
        flushOperands(cpu, ins);
        
        //cpu.addCycles(ins.cycles + mop1.cycles + mop2.cycles);

        block.cycles = cpu.getCurrentCycleCount();
        
        /* dump values after execution */
        /*
        if (dumpfull != null) {
            dumpFullEnd(cpu, origCycleCount, mop1, mop2, dumpfull);
        }*/
		
        /* notify listeners */
        if (listeners != null) {
        	for (InstructionListener listener : listeners) {
        		listener.executed(block, iblock);
        	}
        }
	}

	private AsmInstruction getInstruction(Cpu cpu) {
		AsmInstruction ins;
	    int pc = cpu.getPC() & 0xfffe;
	    ins = pcToInstrMap.get((short) pc);
	    assert ins != null;
		return ins;
	}

    /** Fetch operands for instruction (runtime)
     * @param ins
     * @param memory2
     */
    private void fetchOperands(Cpu cpu, AsmInstruction ins) {
    	Status st = cpu.getStatus();
    	short pc = cpu.getPC();
        iblock.inst = ins;
        iblock.instPC = pc;
        iblock.pc = (short) (pc + 2);	// operands...
        iblock.wp = cpu.getWP();
        iblock.status = st;
        
        AssemblerOperand mop1 = (AssemblerOperand) iblock.inst.getOp1();
        AssemblerOperand mop2 = (AssemblerOperand) iblock.inst.getOp2();
        AssemblerOperand mop3 = (AssemblerOperand) iblock.inst.getOp3();

        if (mop1 != null) {
			iblock.ea1 = getEA(mop1);
			if (ins.getInst() == InstructionTable.Ibl 
					|| ins.getInst() == InstructionTable.Ib 
					|| ins.getInst() == InstructionTable.Iblwp
					|| ins.getEffects().jump != Instruction.INST_JUMP_FALSE) {
				if (!(ins.getInst() == InstrSelection.Pjcc && !(mop1 instanceof CompareOperand))) {
					iblock.val1 = iblock.ea1;
					mop1 = null;
				}
			}
		}
        if (mop2 != null) {
			iblock.ea2 = getEA(mop2);
			if (ins.getInst() >= InstructionTable.Isra && ins.getInst() <= InstructionTable.Isrc) {
				if (!(mop2 instanceof NumberOperand)) {
					iblock.ea2 = (short) (memory.readWord(iblock.ea2) & 0xf);
					if (iblock.ea2 == 0)
						iblock.ea2 = 16;
				}
				iblock.val2 = iblock.ea2;
				mop2 = null;
			}
		}
        if (mop3 != null) {
			iblock.ea3 = getEA(mop3);
		}
        if (mop1 != null) {
        	iblock.val1 = getValue(iblock.ea1, mop1);
		}
        if (mop2 != null) {
			iblock.val2 = getValue(iblock.ea2, mop2);
		}
        if (mop3 != null) {
        	iblock.val3 = getValue(iblock.ea3, mop3);
        }
        if (iblock.inst.getInst() == InstructionTable.Idiv) {
            iblock.val3 = memory.readWord(iblock.ea2 + 2);
        }
    }


	private short getEA(AssemblerOperand op) {
		if (op instanceof CompositePieceOperand) {
			CompositePieceOperand c = (CompositePieceOperand) op;
			short offs = evaluate(c.getOffset());
			short ea = getEA(c.getAddr());
			if (c.getAddr().isRegister())
				ea = memory.readWord(ea);
			return (short) (ea + offs);
		} else if (op instanceof AddrOperand) {
			short addr = evaluate(((AddrOperand) op).getAddr());
			return addr; 
		} else if (op instanceof NumberOperand) {
			return (short) ((NumberOperand) op).getValue();
		} else if (op instanceof RegIndOperand) {
			short ea = getRegisterEA(((RegisterOperand) op).getReg());
			ea = memory.readWord(ea);
			return ea;
		} else if (op instanceof RegIncOperand) {
			short ea = getRegisterEA(((RegisterOperand) op).getReg());
			short val = memory.readWord(ea);
			memory.writeWord(ea, (short)(val + (iblock.inst.getEffects().byteop ? 1 : 2)));
			return val;
		} else if (op instanceof RegOffsOperand) {
			short ea = (short) getRegisterEA(((RegOffsOperand) op).getReg());
			ea = memory.readWord(ea);
			ea += evaluate(((RegOffsOperand) op).getAddr());
			return ea;
		} else if (op instanceof RegisterOperand) {
			//short ea = (short) (iblock.wp + evaluate(((RegisterOperand) op).getReg()) * 2);
			short ea = getRegisterEA(((RegisterOperand) op).getReg());
			return ea;
		} else if (op instanceof RegTempOperand) {
			short ea;
			if (((RegTempOperand) op).getLocal().isPhysReg()) {
				ea = getRegisterEA(((RegTempOperand) op).getReg());
			} else {
				ea = symbolToAddrMap.get(((RegTempOperand) op).getLocal().getName());
			}
			if (((RegTempOperand) op).isRegPair() && !((RegTempOperand) op).isHighReg())
				ea += 2;
			return ea;
		} else if (op instanceof ISymbolOperand) {
			return symbolToAddrMap.get(((ISymbolOperand) op).getSymbol()); 
		}
		assert false;
		return 0;
	}

	private short getRegisterEA(AssemblerOperand reg) {
		short ea;
		if (reg instanceof NumberOperand)
			ea = (short) (iblock.wp + evaluate(reg) * 2);
		else
			ea = symbolToAddrMap.get(((ISymbolOperand) reg).getSymbol());
		return ea;
	}
	
    /**
	 * @param op
	 * @return
	 */
	private short evaluate(AssemblerOperand op) {
		if (op instanceof NumberOperand)
			return (short) ((NumberOperand) op).getValue();
		if (op.isRegister()) {
			if (op instanceof ISymbolOperand)
				return memory.readWord(symbolToAddrMap.get(((ISymbolOperand) op).getSymbol()));
			else
				return memory.readWord(iblock.wp + evaluate(((IRegisterOperand) op).getReg()));
		}
		if (op instanceof ISymbolOperand) {
			return symbolToAddrMap.get(((ISymbolOperand) op).getSymbol());
		}
		assert false;
		return 0;
	}

	/**
	 * @param ea1
	 * @param op
	 * @return
	 */
	private short getValue(short ea1, AssemblerOperand op) {
		short value = 0;

		boolean byteop = iblock.inst.getEffects().byteop;
		
		if (op instanceof NumberOperand || op instanceof SymbolOperand) {
			return ea1;
		}
		if (op.isMemory() || op.isRegister()) {
			if (byteop)
				value = memory.readByte(ea1);
			else
				value = memory.readWord(ea1);
		}
        return value;
	}


	/**
     * 
     */
    private void flushOperands(Cpu cpu, AsmInstruction ins) {
    	Effects fx = ins.getEffects();
        if (fx.mop1_dest != Operand.OP_DEST_FALSE) {
            if (fx.byteop) {
				memory.writeByte(iblock.ea1, (byte) iblock.val1);
			} else {
				memory.writeWord(iblock.ea1, iblock.val1);
				if (ins.getInst() == InstructionTable.Iticks) {
					memory.writeWord(iblock.ea1 + 2, iblock.val2);
				}
			}
				
        }
        if (fx.mop2_dest != Operand.OP_DEST_FALSE) {
        	if (ins.getInst() == InstructionTable.Icb)
        		fx.mop2_dest = 1;	// TODO
            if (fx.byteop) {
				memory.writeByte(iblock.ea2, (byte) iblock.val2);
			} else {
                memory.writeWord(iblock.ea2, iblock.val2);
                if (ins.getInst() == InstructionTable.Impy 
                		|| ins.getInst() == InstructionTable.Idiv) {
                    memory.writeWord(iblock.ea2 + 2, iblock.val3);
                }
            }
        }

        if ((fx.writes & Instruction.INST_RSRC_ST) != 0) {
			cpu.setStatus(iblock.status);
		}

        /* do this after flushing status */
        if ((fx.writes & Instruction.INST_RSRC_CTX) != 0) {
            /* update PC first */
            cpu.setPC(iblock.pc);
            cpu.contextSwitch(iblock.wp, iblock.pc);
        } else {
            /* flush register changes */
            cpu.setPC(iblock.pc);
            if ((fx.writes & Instruction.INST_RSRC_WP) != 0) {
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

    private short readRegister(int r) {
    	short addr = (short) ((iblock.wp + r + r) & 0xfffe);
    	return memory.readWord(addr);
    }
    private void writeRegister(int r, short val) {
    	short addr = (short) ((iblock.wp + r + r) & 0xfffe);
    	memory.writeWord(addr, val);
    }
    /**
     * Execute an instruction
     * 
     * @param cpu
     * @param ins
     */
    private void interpret(final Cpu cpu, AsmInstruction ins) {
        switch (ins.getInst()) {
        case InstrSelection.Pprolog: {
        	Routine routine = getBuildOutput().getRoutine(getSymbol(iblock.instPC));
        	assert routine != null;
        	Locals locals = routine.getLocals();
        	
        	int theSP = target.getSP();
        	int theFP = target.getFP();
        	
        	short SP = readRegister(theSP);
        	short FP = readRegister(theFP);
        	
        	short savedRegsSize = 4 + 0;// R11 and FP
        	
        	SP -= savedRegsSize;		
        	
        	memory.writeWord(SP, FP);
        	memory.writeWord(SP+2, readRegister(11));
        	FP = SP;
        	
        	writeRegister(theFP, FP);
        	
        	SP -= locals.getFrameSize();
        	writeRegister(theSP, SP);
        	
			for (ILocal local : locals.getAllLocals()) {
        		short addr;
        		if (local instanceof RegisterLocal) {
        			int vr = ((RegisterLocal) local).getVr();
        			if (vr < 16) {
        				addr = (short) (iblock.wp + vr * 2);
        			} else {
        				int size = 2;
        				if (((RegisterLocal) local).isRegPair())
        					size = 4;
        				Short vrAddr = vrToAddrMap.get(local.getName());
        				if (vrAddr != null)
        					addr = vrAddr;
        				else {
        					addr = this.vrAddr;
        					this.vrAddr += size;
        				}
        			}
        		}
        		else if (local instanceof StackLocal) {
        			if (((StackLocal)local).getOffset() < 0) {
        				addr = (short) (((StackLocal) local).getOffset() + FP);
        			} else {
        				addr = (short) (((StackLocal) local).getOffset() + FP + savedRegsSize);
        			}
        		}
        		else {
        			assert false;
        			addr = 0;
        		}
        		
        		System.out.println("alloc " + HexUtils.toHex4(addr)+": " + local);
        		vrToAddrMap.put(local.getName(), addr);
        		symbolToAddrMap.put(local.getName(), addr);
        	}

        	break;
        }
        
        case InstrSelection.Pepilog: {
        	int theSP = target.getSP();
        	int theFP = target.getFP();
        	
        	short SP = readRegister(theSP);
        	short FP = readRegister(theFP);
        	
        	SP = FP;
        	FP = memory.readWord(SP); SP += 2;
        	short ret = memory.readWord(SP);
        	writeRegister(11, ret); SP += 2;
        	writeRegister(theSP, SP);
        	writeRegister(theFP, FP);
        	break;
        }
        
        case InstrSelection.Pjcc: 
        	if (!(ins.getOp1() instanceof CompareOperand)) {
        		if (iblock.val1 != 0) {
	        		iblock.pc = iblock.ea2;
	        	} else {
	        		iblock.pc = iblock.ea3;
	        	}
        		break;
        	}
        	// fallthrough
        	
        case InstrSelection.Piset:
        {
        	boolean taken = false;
        	switch (iblock.val1) {
        	case CompareOperand.CMP_EQ:
        		taken = iblock.status.isEQ(); break;
        	case CompareOperand.CMP_NE:
        		taken = iblock.status.isNE(); break;
        	case CompareOperand.CMP_SGE:
        		taken = iblock.status.isGT() || iblock.status.isEQ(); break;
        	case CompareOperand.CMP_SGT:
        		taken = iblock.status.isGT(); break;
        	case CompareOperand.CMP_SLE:
        		taken = iblock.status.isLT() || iblock.status.isEQ(); break;
        	case CompareOperand.CMP_SLT:
        		taken = iblock.status.isLT(); break;
        	case CompareOperand.CMP_UGE:
        		taken = iblock.status.isHE(); break;
        	case CompareOperand.CMP_UGT:
        		taken = iblock.status.isH(); break;
        	case CompareOperand.CMP_ULE:
        		taken = iblock.status.isLE(); break;
        	case CompareOperand.CMP_ULT:
        		taken = iblock.status.isL(); break;
    		default:
    			assert false;
        	}
        	if (ins.getInst() == InstrSelection.Pjcc) {
	        	if (taken) {
	        		iblock.pc = iblock.ea2;
	        	} else {
	        		iblock.pc = iblock.ea3;
	        	}
        	} else {
        		iblock.val2 = (short) (taken ? 0x0100 : 0x000);
        	}
        	break;
        }
        	
        case InstrSelection.Plea: {
        	iblock.val2 = iblock.ea1;
        	break;
        }
        
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
        	assert false;
        	/*
        	short newPc = iblock.pc;
        	execute(cpu, iblock.val1);
        	iblock.pc = newPc;
        	*/
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
        	iblock.val1 += 1;
            break;
        case InstructionTable.Iinct:
        	iblock.val1 += 2;
            break;
        case InstructionTable.Idec:
        	iblock.val1 -= 1;
            break;
        case InstructionTable.Idect:
        	iblock.val1 -= 2;
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
        	//machine.getCruManager().writeBits(iblock.val1, 1, 1);
            break;

        case InstructionTable.Isbz:
        	//machine.getCruManager().writeBits(iblock.val1, 0, 1);
            break;

        case InstructionTable.Itb:
        	//iblock.val1 = (short) machine.getCruManager().readBits(iblock.val1, 1);
        	iblock.val1 = 0;
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
        	//machine.getCruManager().writeBits(
            //       memory.readWord(iblock.wp + 12 * 2), iblock.val1,
           //         iblock.val2);
            break;

        case InstructionTable.Istcr:
        	//iblock.val1 = (short) machine.getCruManager().readBits(
        	//		memory.readWord(iblock.wp + 12 * 2), iblock.val2);
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
        	//machine.getDsrManager().handleDSR(iblock);
        	break;
        	
        case InstructionTable.Iticks: {
        	int count = 0; //machine.getCpu().getTickCount();
        	iblock.val1 = (short) (count >> 16);
        	iblock.val2 = (short) (count & 0xffff);
        	break;
        }
        case InstructionTable.Idbg:
        	//int oldCount = debugCount; 
        	if (iblock.val1 == 0)
        		debugCount++;
        	else
        		debugCount--;
        	//if ((oldCount == 0) != (debugCount == 0))
        	//	Executor.settingDumpFullInstructions.setBoolean(iblock.val1 == 0);
        	break;
        	
        	
        }
    }

	/**
	 * @return
	 */
	public BuildOutput getBuildOutput() {
		return buildOutput;
	}

	public short getAddress(ISymbol name) {
		return symbolToAddrMap.get(name);
	}
	public ISymbol getSymbol(short addr) {
		return addrToSymbolMap.get(addr);
	}

	/**
	 * @param pc
	 * @param wp
	 */
	public void executeAt(short pc, short wp, int timeout) {
		cpu.setPC(pc);
		cpu.setWP(wp);
		cpu.setST((short) 0);
		
		while (cpu.getPC() != 0 && timeout-- > 0) {
			execute(cpu);
		}
		if (timeout <= 0)
			throw new TerminatedException();
	}

	/**
	 * @return
	 */
	public MemoryDomain getMemory() {
		return memory;
	}

	/**
	 * @param dumpFullReporter
	 */
	public void addInstructionListener(InstructionListener listener) {
		if (listeners == null)
			listeners = new ArrayList<InstructionListener>();
		listeners.add(listener);
	}

	/**
	 * @return
	 */
	public Cpu getCPU() {
		return cpu;
	}


}
