/*
  InstTable9900.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.cpu;

import static v9t9.machine.ti99.cpu.InstPattern9900.CNT;
import static v9t9.machine.ti99.cpu.InstPattern9900.GEN;
import static v9t9.machine.ti99.cpu.InstPattern9900.IMM;
import static v9t9.machine.ti99.cpu.InstPattern9900.NONE;
import static v9t9.machine.ti99.cpu.InstPattern9900.OFF;
import static v9t9.machine.ti99.cpu.InstPattern9900.REG;

import java.util.HashMap;
import java.util.Map;

import ejs.base.utils.BinaryUtils;

import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.CycleCounts;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.memory.IMemoryDomain;

/**
 * This class takes an Instruction and generates its opcode.
 * 
 * @author ejs
 * 
 */
public class InstTable9900 {
	final static InstPattern9900 NONE_NONE = new InstPattern9900(NONE, NONE);
	final static InstPattern9900 IMM_NONE = new InstPattern9900(IMM, NONE);
	final static InstPattern9900 REG_IMM = new InstPattern9900(REG, IMM);
	final static InstPattern9900 GEN_NONE = new InstPattern9900(GEN, NONE);
	final static InstPattern9900 REG_CNT = new InstPattern9900(REG, CNT, 4);
	final static InstPattern9900 JMP_NONE = new InstPattern9900(OFF, NONE);
	final static InstPattern9900 GEN_REG = new InstPattern9900(GEN, REG, 6);
	final static InstPattern9900 GEN6_REG = new InstPattern9900(GEN, REG, 6);
	final static InstPattern9900 GEN_CNT = new InstPattern9900(GEN, CNT, 6);
	final static InstPattern9900 GEN_GEN = new InstPattern9900(GEN, GEN, 6);
	final static InstPattern9900 REG_NONE = new InstPattern9900(REG, NONE);
	final static InstPattern9900 CNT_NONE = new InstPattern9900(CNT, NONE);
	final static InstPattern9900 OFF_NONE = new InstPattern9900(OFF, NONE);

	static Map<Integer, InstPattern9900> instEntries = new HashMap<Integer, InstPattern9900>();
	static Map<Integer, Integer> instOpcodes = new HashMap<Integer, Integer>();
	static Map<Integer, Integer> instMasks = new HashMap<Integer, Integer>();
	

	public static Map<String, Integer> nameToInst = new HashMap<String, Integer>();
	public static Map<Integer, String> instToName = new HashMap<Integer, String>();

	public static Map<Integer, ICycleCalculator> instCycles = new HashMap<Integer, ICycleCalculator>();


	public interface ICycleCalculator {
		void addCycles(InstructionWorkBlock origBlock, InstructionWorkBlock block, CycleCounts counts);
		void setLoads(boolean b);
		void setStores(boolean b);
		boolean loads();
		boolean stores();
	}
	public static abstract class BaseCycleCalculator implements ICycleCalculator {

		protected boolean loads;
		protected boolean stores;
		
		public BaseCycleCalculator() {
		}

		public void setLoads(boolean b) {
			this.loads = b;
		}
		public void setStores(boolean b) {
			this.stores = b;
		}
		
		public boolean loads() {
			return loads;
		}
		public boolean stores() {
			return stores;
		}
		
		@Override
		final public void addCycles(InstructionWorkBlock origBlock_,
				InstructionWorkBlock block_, 
				CycleCounts counts) {
			InstructionWorkBlock9900 origBlock = (InstructionWorkBlock9900) origBlock_;
			
			RawInstruction inst = origBlock.inst;
			counts.addFetch(origBlock.inst.fetchCycles);
			
			MachineOperand9900 op1 = (MachineOperand9900) inst.getOp1();
			MachineOperand9900 op2 = (MachineOperand9900) inst.getOp2();
			
			if (op1 != null) {
				counts.addFetch(op1.cycles);
				if (op2 != null) {
					counts.addFetch(op2.cycles);
				}
			}
			
			addCustomCycles(origBlock_, block_, counts);
		}
		
		abstract protected void addCustomCycles(InstructionWorkBlock origBlock_,
				InstructionWorkBlock block_, 
				CycleCounts counts);
		
	}
	
	
	public static class NullCycleCalculator implements ICycleCalculator {
		@Override
		public void addCycles(InstructionWorkBlock origBlock, InstructionWorkBlock block, CycleCounts counts) {
		}

		/* (non-Javadoc)
		 * @see v9t9.machine.ti99.cpu.InstTable9900.ICycleCalculator#setLoads(boolean)
		 */
		@Override
		public void setLoads(boolean b) {
			throw new UnsupportedOperationException();
		}

		/* (non-Javadoc)
		 * @see v9t9.machine.ti99.cpu.InstTable9900.ICycleCalculator#setStores(boolean)
		 */
		@Override
		public void setStores(boolean b) {
			throw new UnsupportedOperationException();
		}

		/* (non-Javadoc)
		 * @see v9t9.machine.ti99.cpu.InstTable9900.ICycleCalculator#loads()
		 */
		@Override
		public boolean loads() {
			return false;
		}

		/* (non-Javadoc)
		 * @see v9t9.machine.ti99.cpu.InstTable9900.ICycleCalculator#stores()
		 */
		@Override
		public boolean stores() {
			return false;
		}
		
	}
	public static class FixedCycleCalculator extends BaseCycleCalculator {
		
		private int exec;
		protected int memAccesses;

		public FixedCycleCalculator(int exec, int memAccesses) {
			super();
			this.exec = exec;
			this.memAccesses = memAccesses;
		}
		
		@Override
		protected void addCustomCycles(InstructionWorkBlock origBlock, InstructionWorkBlock block, CycleCounts counts) {
			counts.addExecute(exec);
		}

	}

	static class InstBuilder {
		private int inst;
		private ICycleCalculator cycleCounter;

		public static InstBuilder from(String name, int inst, int opcode, InstPattern9900 entry, int mask) {
			InstBuilder b = new InstBuilder();
			
			nameToInst.put(name.toUpperCase(), inst);
			instToName.put(inst, name.toUpperCase());
			
			instEntries.put(inst, entry);
			instOpcodes.put(inst, opcode);
			instMasks.put(inst, mask);
			
			b.cycleCounter = new NullCycleCalculator();
			b.inst = inst;
			return b;
		}
		
		public static InstBuilder from(String name, int inst, int opcode, InstPattern9900 entry) {
			return from(name, inst, opcode, entry,
					entry == NONE_NONE || entry == IMM_NONE ? opcode : 0xffff);
		}
		
		public void register() {
			instCycles.put(inst, cycleCounter);
		}
		
		public InstBuilder withCycles(int exec, int memAccesses) {
			cycleCounter = new FixedCycleCalculator(exec, memAccesses);
			return this;
		}
		public InstBuilder withCycleCalculator(ICycleCalculator calculator) {
			cycleCounter = calculator;
			return this;
		}
		public InstBuilder source() {
			cycleCounter.setLoads(true);
			return this;
		}
		public InstBuilder destination() {
			cycleCounter.setStores(true);
			return this;
		}
	}
	
	static class JumpCycleCounter extends BaseCycleCalculator {

		public static final ICycleCalculator INSTANCE = new JumpCycleCounter();

		@Override
		protected void addCustomCycles(InstructionWorkBlock origBlock,
				InstructionWorkBlock block, CycleCounts counts) {
			if (block.pc != origBlock.pc) {
				counts.addExecute(10);
			} else {
				counts.addExecute(8);
			}
		}
		
	}
	
	static class ShiftCycleCalculator extends BaseCycleCalculator {
		
		public static final ICycleCalculator INSTANCE = new ShiftCycleCalculator();
		
		@Override
		protected void addCustomCycles(InstructionWorkBlock origBlock,
				InstructionWorkBlock block, CycleCounts counts) {
			int disp = ((InstructionWorkBlock9900) block).val2;
			counts.addExecute(2 * disp);
			if (!((MachineOperand9900) origBlock.inst.getOp2()).isRegister()) {
				counts.addExecute(12);
			} else {
				counts.addExecute(20);
			}
		}
		
	}
	
	static {
		InstBuilder.from("data", InstTableCommon.Idata, 0x0000, IMM_NONE, 0xffff)
			.withCycles(6, 1)
			.register();
		InstBuilder.from("byte", InstTableCommon.Ibyte, 0x0000, IMM_NONE, 0xffff)
			.withCycles(6, 1)
			.register();

		InstBuilder.from("li", Inst9900.Ili, 0x0200, REG_IMM, 0x20f)
			.withCycles(12, 3)
			.register();
		InstBuilder.from("ai", Inst9900.Iai, 0x0220, REG_IMM, 0x22f)
			.withCycles(14, 4)
			.register();
		InstBuilder.from("andi", Inst9900.Iandi, 0x0240, REG_IMM, 0x24f)
			.withCycles(14, 4)
			.register();
		InstBuilder.from("ori", Inst9900.Iori, 0x0260, REG_IMM, 0x26f)
			.withCycles(14, 4)
			.register();
		InstBuilder.from("ci", Inst9900.Ici, 0x0280, REG_IMM, 0x28f)
			.withCycles(14, 3)
			.register();

		InstBuilder.from("stwp", Inst9900.Istwp, 0x02a0, REG_NONE, 0x2af)
			.withCycles(8, 2)
			.register();
		InstBuilder.from("stst", Inst9900.Istst, 0x02c0, REG_NONE, 0x2cf)
			.withCycles(8, 2)
			.register();
		InstBuilder.from("lwpi", Inst9900.Ilwpi, 0x02e0, IMM_NONE)
			.withCycles(10, 2)
			.register();
		InstBuilder.from("limi", Inst9900.Ilimi, 0x0300, IMM_NONE)
			.withCycles(16, 2)
			.register();
		InstBuilder.from("idle", Inst9900.Iidle, 0x0340, NONE_NONE)
			.withCycles(12, 1)
			.register();
		InstBuilder.from("rset", Inst9900.Irset, 0x0360, NONE_NONE)
			.withCycles(12, 1)
			.register();
		InstBuilder.from("rtwp", Inst9900.Irtwp, 0x0380, NONE_NONE)
			.withCycles(14, 4)
			.register();
		InstBuilder.from("ckon", Inst9900.Ickon, 0x03a0, NONE_NONE)
			.withCycles(12, 1)
			.register();
		InstBuilder.from("ckof", Inst9900.Ickof, 0x03c0, NONE_NONE)
			.withCycles(12, 1)
			.register();
		InstBuilder.from("lrex", Inst9900.Ilrex, 0x03e0, NONE_NONE)
			.withCycles(12, 1)
			.register();

		InstBuilder.from("blwp", Inst9900.Iblwp, 0x0400, GEN_NONE)
			.withCycles(26, 6)
			.source()
			.register();
		InstBuilder.from("b", Inst9900.Ib, 0x0440, GEN_NONE)
			.withCycles(8, 2)
			.register();
		InstBuilder.from("x", Inst9900.Ix, 0x0480, GEN_NONE)
			.withCycles(8, 2)
			.register();
		InstBuilder.from("clr", Inst9900.Iclr, 0x04c0, GEN_NONE)
			.withCycles(10, 3)
			.source()
			.register();
		InstBuilder.from("neg", Inst9900.Ineg, 0x0500, GEN_NONE)
			.withCycles(12, 3)
			.register();
		InstBuilder.from("inv", Inst9900.Iinv, 0x0540, GEN_NONE)
			.withCycles(10, 3)
			.source()
			.register();
		InstBuilder.from("inc", Inst9900.Iinc, 0x0580, GEN_NONE)
			.withCycles(10, 3)
			.source()
			.register();
		InstBuilder.from("inct", Inst9900.Iinct, 0x05c0, GEN_NONE)
			.withCycles(10, 3)
			.source()
			.register();
		InstBuilder.from("dec", Inst9900.Idec, 0x0600, GEN_NONE)
			.withCycles(10, 3)
			.source()
			.register();
		InstBuilder.from("dect", Inst9900.Idect, 0x0640, GEN_NONE)
			.withCycles(10, 3)
			.source()
			.register();
		InstBuilder.from("bl", Inst9900.Ibl, 0x0680, GEN_NONE)
			.withCycles(12, 3)
			.source()
			.register();
		InstBuilder.from("swpb", Inst9900.Iswpb, 0x06c0, GEN_NONE)
			.withCycles(10, 3)
			.source()
			.register();
		InstBuilder.from("seto", Inst9900.Iseto, 0x0700, GEN_NONE)
			.withCycles(10, 3)
			.source()
			.register();
		InstBuilder.from("abs", Inst9900.Iabs, 0x0740, GEN_NONE)
			.withCycleCalculator(new BaseCycleCalculator() {
				
				@Override
				protected void addCustomCycles(InstructionWorkBlock origBlock, InstructionWorkBlock block, CycleCounts counts) {
					if (((InstructionWorkBlock9900) origBlock).val1 >= 0) {
						counts.addExecute(12);
					} else {
						counts.addExecute(14);
					}
				}
			})
			.source().destination()
			.register();

		InstBuilder.from("sra", Inst9900.Isra, 0x0800, REG_CNT)
			.withCycleCalculator(ShiftCycleCalculator.INSTANCE)
			.register();
		InstBuilder.from("srl", Inst9900.Isrl, 0x0900, REG_CNT)
			.withCycleCalculator(ShiftCycleCalculator.INSTANCE)
			.register();
		InstBuilder.from("sla", Inst9900.Isla, 0x0a00, REG_CNT)
			.withCycleCalculator(ShiftCycleCalculator.INSTANCE)
			.register();
		InstBuilder.from("src", Inst9900.Isrc, 0x0b00, REG_CNT)
			.withCycleCalculator(ShiftCycleCalculator.INSTANCE)
			.register();

		InstBuilder.from("dsr", InstTableCommon.Idsr, 0x0c00, OFF_NONE, 0xcff).register();
		InstBuilder.from("kysl", InstTableCommon.Ikysl, 0x0d40, NONE_NONE, 0xd40).register();
		InstBuilder.from("ticks", InstTableCommon.Iticks, 0x0d60, REG_NONE, 0xd60).register();
		InstBuilder.from("emitchar", InstTableCommon.Iemitchar, 0x0dc0, REG_CNT, 0xdcf).register();
		InstBuilder.from("dbg", InstTableCommon.Idbg, 0x0de0, NONE_NONE, 0xde0).register();
		InstBuilder.from("dbgf", InstTableCommon.Idbgf, 0x0de1, NONE_NONE, 0xde1).register();

		InstBuilder.from("jmp", Inst9900.Ijmp, 0x1000, JMP_NONE)
			.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();
		InstBuilder.from("jlt", Inst9900.Ijlt, 0x1100, JMP_NONE)
		.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();
		InstBuilder.from("jle", Inst9900.Ijle, 0x1200, JMP_NONE)
			.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();
		InstBuilder.from("jeq", Inst9900.Ijeq, 0x1300, JMP_NONE)
			.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();
		InstBuilder.from("jhe", Inst9900.Ijhe, 0x1400, JMP_NONE)
			.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();
		InstBuilder.from("jgt", Inst9900.Ijgt, 0x1500, JMP_NONE)
			.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();
		InstBuilder.from("jne", Inst9900.Ijne, 0x1600, JMP_NONE)
			.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();
		InstBuilder.from("jnc", Inst9900.Ijnc, 0x1700, JMP_NONE)
			.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();
		InstBuilder.from("joc", Inst9900.Ijoc, 0x1800, JMP_NONE)
			.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();
		InstBuilder.from("jno", Inst9900.Ijno, 0x1900, JMP_NONE)
			.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();
		InstBuilder.from("jl", Inst9900.Ijl, 0x1a00, JMP_NONE)
			.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();
		InstBuilder.from("jh", Inst9900.Ijh, 0x1b00, JMP_NONE)
			.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();
		InstBuilder.from("jop", Inst9900.Ijop, 0x1c00, JMP_NONE)
			.withCycleCalculator(JumpCycleCounter.INSTANCE)
			.register();

		InstBuilder.from("sbo", Inst9900.Isbo, 0x1d00, OFF_NONE)
			.withCycles(12, 2)
			.register();
		InstBuilder.from("sbz", Inst9900.Isbz, 0x1e00, OFF_NONE)
			.withCycles(12, 2)
			.register();
		InstBuilder.from("tb", Inst9900.Itb, 0x1f00, OFF_NONE)
			.withCycles(12, 2)
			.register();

		InstBuilder.from("coc", Inst9900.Icoc, 0x2000, GEN6_REG)
			.withCycles(14, 3)
			.source()
			.register();
		InstBuilder.from("czc", Inst9900.Iczc, 0x2400, GEN6_REG)
			.withCycles(14, 3)
			.source()
			.register();
		InstBuilder.from("xor", Inst9900.Ixor, 0x2800, GEN6_REG)
			.withCycles(14, 4)
			.source()
			.register();
		InstBuilder.from("xop", Inst9900.Ixop, 0x2c00, GEN6_REG)
			.withCycles(36, 8)
			.source()
			.register();
		
		InstBuilder.from("ldcr", Inst9900.Ildcr, 0x3000, GEN_CNT)
			.withCycleCalculator(new BaseCycleCalculator() {
				
				@Override
				public void addCustomCycles(InstructionWorkBlock origBlock, InstructionWorkBlock block, CycleCounts counts) {
					counts.addExecute(20 + 2 * ((InstructionWorkBlock9900) origBlock).val2);
				}
			})
			.source()
			.register();
		InstBuilder.from("stcr", Inst9900.Istcr, 0x3400, GEN_CNT)
			.withCycleCalculator(new BaseCycleCalculator() {
				
				@Override
				protected void addCustomCycles(InstructionWorkBlock origBlock, InstructionWorkBlock block, CycleCounts counts) {
					int disp = ((InstructionWorkBlock9900) origBlock).val2;
					if (disp > 0 && disp <= 7)
						counts.addExecute(42);
					else if (disp == 8)
						counts.addExecute(44);
					else if (disp > 0 && disp <= 15)
						counts.addExecute(58);
					else /* disp == 0 or 16 */
						counts.addExecute(60);
				}
			})
			.register();
		InstBuilder.from("mpy", Inst9900.Impy, 0x3800, GEN_REG)
			.withCycles(52, 5)
			.source()
			.register();
		InstBuilder.from("div", Inst9900.Idiv, 0x3c00, GEN_REG)
			.withCycleCalculator(new BaseCycleCalculator() {
				
				@Override
				public void addCustomCycles(InstructionWorkBlock origBlock, InstructionWorkBlock block, CycleCounts counts) {
					if ((((InstructionWorkBlock9900) block).st & Status9900.ST_O) != 0) {
						counts.addExecute(16);
					} else {
						int a;
						int b;
						a = ((InstructionWorkBlock9900) origBlock).val2 & 0xffff;
						b = ((InstructionWorkBlock9900) origBlock).val3 & 0xffff;
//						a = ((InstructionWorkBlock9900) block).val2 & 0xffff;
//						b = ((InstructionWorkBlock9900) block).val3 & 0xffff;
//						counts.addExecute(124);
//						counts.addExecute(92);
//						counts.addExecute(92 
//								+ BinaryUtils.numberOfBits(a) 
//								+ BinaryUtils.numberOfBits(b));
						counts.addExecute(92 + 
								+ BinaryUtils.maxBitNumber((a << 16) | b)); 
//						counts.addExecute((92+124)/2); 
//						counts.addExecute(92 
//								+ BinaryUtils.numberOfBits(((InstructionWorkBlock9900) origBlock).val1 & 0xffff)*2); 
							
					}
				}
			})
			.source()
			.register();


		InstBuilder.from("szc", Inst9900.Iszc, 0x4000, GEN_GEN)
			.withCycles(14, 4)
			.source().destination()
			.register();
		InstBuilder.from("szcb", Inst9900.Iszcb, 0x5000, GEN_GEN)
			.withCycles(14, 4)
			.source().destination()
			.register();
		InstBuilder.from("s", Inst9900.Is, 0x6000, GEN_GEN)
			.withCycles(14, 4)
			.source().destination()
			.register();
		InstBuilder.from("sb", Inst9900.Isb, 0x7000, GEN_GEN)
			.withCycles(14, 4)
			.source().destination()
			.register();
		InstBuilder.from("c", Inst9900.Ic, 0x8000, GEN_GEN)
			.withCycles(14, 3)
			.source().destination()
			.register();
		InstBuilder.from("cb", Inst9900.Icb, 0x9000, GEN_GEN)
			.withCycles(14, 3)
			.source().destination()
			.register();
		InstBuilder.from("a", Inst9900.Ia, 0xa000, GEN_GEN)
			.withCycles(14, 4).source().destination()
			.register();
		InstBuilder.from("ab", Inst9900.Iab, 0xb000, GEN_GEN)
			.withCycles(14, 4).source().destination()
			.register();
		InstBuilder.from("mov", Inst9900.Imov, 0xc000, GEN_GEN)
			.withCycles(14, 4)
			.source().destination()
			.register();
		InstBuilder.from("movb", Inst9900.Imovb, 0xd000, GEN_GEN)
			.withCycles(14, 4)
			.source().destination()
			.register();
		InstBuilder.from("soc", Inst9900.Isoc, 0xe000, GEN_GEN)
			.withCycles(14, 4)
			.source().destination()
			.register();
		InstBuilder.from("socb", Inst9900.Isocb, 0xf000, GEN_GEN)
			.withCycles(14, 4)
			.source().destination()
			.register();

	};

	public static short[] encode(RawInstruction rawInstruction) throws IllegalArgumentException {
		Integer opcodeI = instOpcodes.get(rawInstruction.getInst());
		if (opcodeI == null)
			throw new IllegalArgumentException("Non-machine instruction");
		int opcode = opcodeI;
		
		InstPattern9900 pattern = instEntries.get(rawInstruction.getInst());
		if (pattern == null)
			throw new IllegalArgumentException("Non-encoded instruction");
		
		if (!(rawInstruction.getOp1() instanceof IMachineOperand))
			throw new IllegalArgumentException("Non-machine operand 1: " + rawInstruction.getOp1());
		if (!(rawInstruction.getOp2() instanceof IMachineOperand))
			throw new IllegalArgumentException("Non-machine operand 2: " + rawInstruction.getOp1());
		
		rawInstruction.setSize(2);	// at least (for jumps)
		coerceOperandTypes(rawInstruction);
		
		MachineOperand9900 mop1 = (MachineOperand9900) rawInstruction.getOp1();
		MachineOperand9900 mop2 = (MachineOperand9900) rawInstruction.getOp2();
		
		assertOperandMatches(mop1, pattern.op1);
		assertOperandMatches(mop2, pattern.op2);
		
		opcode |= (mop2.getBits() << pattern.off1) | mop1.getBits();
		
		short[] words = { (short)opcode, 0, 0 };
		int wordCount = rawInstruction.getInst() != InstTableCommon.Idata ? 1 : 0;
		if (mop1.hasImmediate()) {
			words[wordCount++] = mop1.immed;
		}
		if (mop2.hasImmediate()) {
			words[wordCount++] = mop2.immed;
		}
		
		rawInstruction.setSize(wordCount * 2);
		
		if (wordCount == 1)
			return new short[] { words[0] };
		else if (wordCount == 2)
			return new short[] { words[0], words[1] };
		else
			return words;
	}

	private static void assertOperandMatches(BaseMachineOperand mop, int op) {
		switch (op) {
		case NONE:
			if (mop.type != IMachineOperand.OP_NONE
					&& mop.type != MachineOperand9900.OP_STATUS
					&& mop.type != MachineOperand9900.OP_INST)
				throw new IllegalArgumentException("Unexpected operand: " + mop);
			break;
		case IMM:
			if (mop.type != MachineOperand9900.OP_IMMED)
				throw new IllegalArgumentException("Expected immediate: " + mop);
			break;
		case CNT:
			if (mop.type != MachineOperand9900.OP_CNT && mop.type != MachineOperand9900.OP_IMMED)
				throw new IllegalArgumentException("Expected count: " + mop);
			break;
		case OFF:
			if (mop.type != MachineOperand9900.OP_CNT && mop.type != MachineOperand9900.OP_IMMED
					&& mop.type != MachineOperand9900.OP_OFFS_R12
					&& mop.type != MachineOperand9900.OP_JUMP)
				throw new IllegalArgumentException("Expected offset: " + mop);
			break;
		case REG:
			if (mop.type != MachineOperand9900.OP_REG && mop.type != MachineOperand9900.OP_REG0_SHIFT_COUNT)
				throw new IllegalArgumentException("Expected immediate: " + mop);
			break;
		case GEN:
			if (mop.type != MachineOperand9900.OP_REG 
					&& mop.type != MachineOperand9900.OP_INC
					&& mop.type != MachineOperand9900.OP_IND
					&& mop.type != MachineOperand9900.OP_ADDR)
				throw new IllegalArgumentException("Expected general operand: " + mop);
			break;
		}
	}

	/** 
	 * Ensure that operands have the final types needed for machine code.
	 * operands as needed
	 * @param instruction
	 * @param finalPass if true, performs final coercions before generating
	 * machine code; otherwise, just overcomes 
	 */
	public static void coerceOperandTypes(RawInstruction instruction) {
		InstPattern9900 pattern = instEntries.get(instruction.getInst());
		if (pattern == null)
			throw new IllegalArgumentException("Non-encoded instruction");
		
		if (instruction.getOp1() instanceof IMachineOperand) {
			coerceOperandType(instruction, (BaseMachineOperand) instruction.getOp1(), pattern.op1);
		}
		if (instruction.getOp2() instanceof IMachineOperand) {
			coerceOperandType(instruction, (BaseMachineOperand) instruction.getOp2(), pattern.op2);
		}		
		
	}
	private static void coerceOperandType(RawInstruction instruction, BaseMachineOperand mop, int op) {
		switch (op) {
		case NONE:
			if (mop.type == MachineOperand9900.OP_STATUS
					|| mop.type == MachineOperand9900.OP_INST
					|| (instruction.getInst() >= Inst9900.Iinc && instruction.getInst() <= Inst9900.Idect))
				mop.type = IMachineOperand.OP_NONE;
			break;
		case IMM:
			if (mop.type == MachineOperand9900.OP_REG)
				mop.type = MachineOperand9900.OP_IMMED;
			break;
		case CNT:
			if (mop.type == MachineOperand9900.OP_REG
					|| mop.type == MachineOperand9900.OP_IMMED
					|| mop.type == MachineOperand9900.OP_REG0_SHIFT_COUNT)
				mop.type = MachineOperand9900.OP_CNT;
			if (mop.val == 16)
				mop.val = 0;
			break;
		case OFF:
			if (isJumpInst(instruction.getInst())) {
				if (mop.type == MachineOperand9900.OP_IMMED) {
					// convert address to offset from this inst
					mop.type = MachineOperand9900.OP_JUMP;
					mop.val = (mop.val - instruction.pc);
				}
			} else {
				if (mop.type == MachineOperand9900.OP_IMMED) {
					mop.type = MachineOperand9900.OP_OFFS_R12;
					mop.val = mop.immed;
				}
			}
			break;
		case REG:
			if (mop.type == MachineOperand9900.OP_IMMED)
				mop.type = MachineOperand9900.OP_REG;
			break;
		case GEN:
			if (mop.type == MachineOperand9900.OP_IMMED)
				mop.type = MachineOperand9900.OP_REG;
			
			if (mop.type == MachineOperand9900.OP_ADDR && mop.val == MachineOperand9900.PCREL) {
				mop.immed = (short) (instruction.getPc() + mop.immed);
				mop.val = 0;
			}
			if (mop.type == MachineOperand9900.OP_JUMP) {
				mop.type = MachineOperand9900.OP_ADDR;
				mop.immed = (short) (instruction.getPc() + mop.val);
				mop.val = 0;
			}
			break;
		}
	}
	
	public static int coerceInstructionOpcode(int inst, int opcode) {
		Integer mask = instMasks.get(inst);
		if (mask == null)
			return opcode;
		return opcode & mask;
	}
	public static InstPattern9900 lookupEncodePattern(int inst) {
		return instEntries.get(inst);
	}
	
	
	static { registerAlias(Inst9900.Ijeq, "je"); }
	static { registerAlias(Inst9900.Ijoc, "jc"); }

	public static void registerAlias(int inst, String str) {
	    Integer i = new Integer(inst);
	    nameToInst.put(str.toUpperCase(), i);
	}
	/**
	 * Get the instruction code for the given instruction
	 * @param str
	 * @return
	 */
	public static Integer lookupInst(String str) {
		str = str.toUpperCase();
		Integer inst = nameToInst.get(str);
		return inst;
	}
	/**
	 * Get an instruction by name
	 * @param inst
	 * @return
	 */
	public static String getInstName(int inst) {
		return instToName.get(inst);
	}
	
   /**
     * Decode instruction with opcode 'op' at 'addr' into 'ins'.
     * 
     * @param domain
     *            provides read access to memory, to decode registers and
     *            instructions
     */
    public static RawInstruction decodeInstruction(int op, int pc, IMemoryDomain domain) {
    	RawInstruction inst = new RawInstruction();
    	inst.pc = pc;
    	
        /* deal with it unsigned */
        op &= 0xffff;
    
        inst.opcode = (short) op;
        inst.setInst(InstTableCommon.Idata);
        inst.setName("DATA");
        inst.setSize(0);
        MachineOperand9900 mop1 = new MachineOperand9900(IMachineOperand.OP_NONE);
        MachineOperand9900 mop2 = new MachineOperand9900(IMachineOperand.OP_NONE);
        inst.setOp1(mop1);
        inst.setOp2(mop2);
    
        // Collect the instruction name
        // and operand structure.
    
        // Initially, inst.op?.val is incomplete, and is whatever
        // raw data from the opcode we can decode;
        // inst.op?.ea is that of the instruction or immediate
        // if the operand needs it.
    
        // after decoding the instruction, we complete
        // the operand, making inst.op?.val and inst.op?.ea valid.
    
        if (op < 0x200) {
        } else if (op < 0x2a0) {
            mop1.type = MachineOperand9900.OP_REG;
            mop1.val = op & 15;
            mop2.type = MachineOperand9900.OP_IMMED;
            switch ((op & 0x1e0) >> 5) {
            case 0:
                inst.setInst(Inst9900.Ili);
                break;
            case 1:
                inst.setInst(Inst9900.Iai);
                break;
            case 2:
                inst.setInst(Inst9900.Iandi);
                break;
            case 3:
                inst.setInst(Inst9900.Iori);
                break;
            case 4:
                inst.setInst(Inst9900.Ici);
                break;
            }
    
        } else if (op < 0x2e0) {
            mop1.type = MachineOperand9900.OP_REG;
            mop1.val = op & 15;
            switch ((op & 0x1e0) >> 5) {
            case 5:
                inst.setInst(Inst9900.Istwp);
                break;
            case 6:
                inst.setInst(Inst9900.Istst);
                break;
            }
    
        } else if (op < 0x320) {
            mop1.type = MachineOperand9900.OP_IMMED;
    
            switch ((op & 0x1e0) >> 5) {
            case 7:
                inst.setInst(Inst9900.Ilwpi);
                break;
            case 8:
                inst.setInst(Inst9900.Ilimi);
                break;
            }
    
        } else if (op < 0x400) {
            switch ((op & 0x1e0) >> 5) {
            case 10:
                inst.setInst(Inst9900.Iidle);
                break;
            case 11:
                inst.setInst(Inst9900.Irset);
                break;
            case 12:
                inst.setInst(Inst9900.Irtwp);
                break;
            case 13:
                inst.setInst(Inst9900.Ickon);
                break;
            case 14:
                inst.setInst(Inst9900.Ickof);
                break;
            case 15:
                inst.setInst(Inst9900.Ilrex);
                break;
            }
    
        } else if (op < 0x800) {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
    
            switch ((op & 0x3c0) >> 6) {
            case 0:
                inst.setInst(Inst9900.Iblwp);
                break;
            case 1:
                inst.setInst(Inst9900.Ib);
                break;
            case 2:
                inst.setInst(Inst9900.Ix);
                break;
            case 3:
                inst.setInst(Inst9900.Iclr);
                break;
            case 4:
                inst.setInst(Inst9900.Ineg);
                break;
            case 5:
                inst.setInst(Inst9900.Iinv);
                break;
            case 6:
                inst.setInst(Inst9900.Iinc);
                break;
            case 7:
                inst.setInst(Inst9900.Iinct);
                break;
            case 8:
                inst.setInst(Inst9900.Idec);
                break;
            case 9:
                inst.setInst(Inst9900.Idect);
                break;
            case 10:
                inst.setInst(Inst9900.Ibl);
                break;
            case 11:
                inst.setInst(Inst9900.Iswpb);
                break;
            case 12:
                inst.setInst(Inst9900.Iseto);
                break;
            case 13:
                inst.setInst(Inst9900.Iabs);
                break;
            }
    
        } else if (op < 0xc00) {
            mop1.type = MachineOperand9900.OP_REG;
            mop1.val = op & 15;
            mop2.type = MachineOperand9900.OP_CNT;
            mop2.val = (op & 0xf0) >> 4;
    
            switch ((op & 0x700) >> 8) {
            case 0:
                inst.setInst(Inst9900.Isra);
                break;
            case 1:
                inst.setInst(Inst9900.Isrl);
                break;
            case 2:
                inst.setInst(Inst9900.Isla);
                break;
            case 3:
                inst.setInst(Inst9900.Isrc);
                break;
            }
    
        } else if (op < 0x1000) {
            switch ((op & 0x1e0) >> 5) {
        	// 0xc00
    		case 0:				/* DSR, OP_DSR */
    			inst.setInst(InstTableCommon.Idsr);
    			mop1.type = MachineOperand9900.OP_OFFS_R12;
    			mop1.val = (byte) (op & 0xff);
    			break;
  			// 0xd60
    		case 11:			/* TICKS */
    			inst.setInst(InstTableCommon.Iticks);
    			mop1.type = MachineOperand9900.OP_REG;
                mop1.val = (byte) (op & 0xf);
    			break;
    		// 0xde0
    		case 15:			/* DBG, -DBG */
    			inst.setInst(InstTableCommon.Idbg);
    			mop1.type = MachineOperand9900.OP_CNT;
    			mop1.val = (byte) (op & 0xf);
    			break;
    			
            // TODO: extended instructions
            }
    
        } else if (op < 0x2000) {
            mop1.type = MachineOperand9900.OP_IMMED;
            mop1.val = (byte) (op & 0xff);
            if (op < 0x1D00) {
                mop1.val = (mop1.val << 1) + 2;
                mop1.type = MachineOperand9900.OP_JUMP;
            } else {
            	mop1.type = MachineOperand9900.OP_OFFS_R12;
            }
    
            switch ((op & 0xf00) >> 8) {
            case 0:
                inst.setInst(Inst9900.Ijmp);
                break;
            case 1:
                inst.setInst(Inst9900.Ijlt);
                break;
            case 2:
                inst.setInst(Inst9900.Ijle);
                break;
            case 3:
                inst.setInst(Inst9900.Ijeq);
                break;
            case 4:
                inst.setInst(Inst9900.Ijhe);
                break;
            case 5:
                inst.setInst(Inst9900.Ijgt);
                break;
            case 6:
                inst.setInst(Inst9900.Ijne);
                break;
            case 7:
                inst.setInst(Inst9900.Ijnc);
                break;
            case 8:
                inst.setInst(Inst9900.Ijoc);
                break;
            case 9:
                inst.setInst(Inst9900.Ijno);
                break;
            case 10:
                inst.setInst(Inst9900.Ijl);
                break;
            case 11:
                inst.setInst(Inst9900.Ijh);
                break;
            case 12:
                inst.setInst(Inst9900.Ijop);
                break;
            case 13:
                inst.setInst(Inst9900.Isbo);
                break;
            case 14:
                inst.setInst(Inst9900.Isbz);
                break;
            case 15:
                inst.setInst(Inst9900.Itb);
                break;
            }
    
        } else if (op < 0x4000 && !(op >= 0x3000 && op < 0x3800)) {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
            mop2.type = MachineOperand9900.OP_REG;
            mop2.val = (op & 0x3c0) >> 6;
    
            switch ((op & 0x1c00) >> 10) {
            case 0:
                inst.setInst(Inst9900.Icoc);
                break;
            case 1:
                inst.setInst(Inst9900.Iczc);
                break;
            case 2:
                inst.setInst(Inst9900.Ixor);
                break;
            case 3:
                inst.setInst(Inst9900.Ixop);
                break;
            case 6:
                inst.setInst(Inst9900.Impy);
                break;
            case 7:
                inst.setInst(Inst9900.Idiv);
                break;
            }
    
        } else if (op >= 0x3000 && op < 0x3800) {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
            mop2.type = MachineOperand9900.OP_CNT;
            mop2.val = (op & 0x3c0) >> 6;
    
            if (op < 0x3400) {
                inst.setInst(Inst9900.Ildcr);
            } else {
                inst.setInst(Inst9900.Istcr);
            }
    
        } else {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
            mop2.type = (op & 0x0c00) >> 10;
            mop2.val = (op & 0x3c0) >> 6;
    
            switch ((op & 0xf000) >> 12) {
            case 4:
                inst.setInst(Inst9900.Iszc);
                break;
            case 5:
                inst.setInst(Inst9900.Iszcb);
                break;
            case 6:
                inst.setInst(Inst9900.Is);
                break;
            case 7:
                inst.setInst(Inst9900.Isb);
                break;
            case 8:
                inst.setInst(Inst9900.Ic);
                break;
            case 9:
                inst.setInst(Inst9900.Icb);
                break;
            case 10:
                inst.setInst(Inst9900.Ia);
                break;
            case 11:
                inst.setInst(Inst9900.Iab);
                break;
            case 12:
                inst.setInst(Inst9900.Imov);
                break;
            case 13:
                inst.setInst(Inst9900.Imovb);
                break;
            case 14:
                inst.setInst(Inst9900.Isoc);
                break;
            case 15:
                inst.setInst(Inst9900.Isocb);
                break;
            }
        }
    
        if (inst.getInst() == 0) // data
        {
            mop1.type = MachineOperand9900.OP_IMMED;
            mop1.val = mop1.immed = (short) op;
            inst.setSize(2);
        } else {
            // Finish reading operand immediates
            pc += 2;
            pc = mop1.fetchOperandImmediates(domain, (short)pc);
            pc = mop2.fetchOperandImmediates(domain, (short)pc);
            inst.setSize(pc - inst.pc);
            inst.setName(InstTable9900.getInstName(inst.getInst()));
        }

        return inst;
    }
    
    public static void calculateInstructionSize(RawInstruction target) {
    	if (target.getInst() == InstTableCommon.Idata) {
    		target.setSize(2);
    		return;
    	} else if (target.getInst() == InstTableCommon.Ibyte) {
    		target.setSize(1);
    		return;
    	}
    	target.setSize(2);
    	InstPattern9900 pattern = lookupEncodePattern(target.getInst());
		if (pattern == null)
			return;
		
		if ((pattern.op1 == GEN && ((BaseMachineOperand)target.getOp1()).type == MachineOperand9900.OP_ADDR)
				|| (pattern.op1 == IMM)) 
			target.setSize(target.getSize() + 2);
		if ((pattern.op2 == GEN && ((BaseMachineOperand)target.getOp2()).type == MachineOperand9900.OP_ADDR)
				|| (pattern.op2 == IMM)) 
			target.setSize(target.getSize() + 2);
	
    }
	/**
	 * @param inst
	 * @return
	 */
	public static boolean isJumpInst(int inst) {
		return inst >= Inst9900.Ijmp && inst <= Inst9900.Ijop;
	}
	/**
	 * @param inst
	 * @return
	 */
	public static boolean isByteInst(int inst) {
		return inst == Inst9900.Isocb || inst == Inst9900.Icb || inst == Inst9900.Iab 
		|| inst == Inst9900.Isb || inst == Inst9900.Iszcb || inst == Inst9900.Imovb;
	}
}
