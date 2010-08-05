/**
 * 
 */
package v9t9.engine.cpu;

import static v9t9.engine.cpu.InstPatternMFP201.CNT;
import static v9t9.engine.cpu.InstPatternMFP201.GEN;
import static v9t9.engine.cpu.InstPatternMFP201.NONE;
import static v9t9.engine.cpu.InstPatternMFP201.OFF;
import static v9t9.engine.cpu.InstPatternMFP201.IMM;
import static v9t9.engine.cpu.InstPatternMFP201.REG;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ejs
 * 
 */
public class InstTableMFP201 {
	public final static byte _IMM8 = 0x10;
	public final static byte _IMM16 = 0x11;
	public final static byte _IMM8_16 = 0x12;
	public final static byte _OFF16 = 0x13;
	/** if the immediate size is a byte, at the bit specified in lower nybble */
	public final static byte _IMM_SZ = 0x30;
	/** if the immediate size is a byte, at the bit specified in lower nybble, of byte 0 */
	public final static byte _IMM_SZ_0 = 0x40;
	/** skip to the next byte */
	public final static byte _NEXTB = -2;
	/** skip to the next operand */
	public final static byte _NEXTO = -3;
	/** skip to the next byte and operand */
	public final static byte _NEXT = -4;
	/** emit the opcode */
	public final static byte _OPC = -1;
	/** emit the opcode (high byte) */
	public final static byte _OPC_HI = -5;
	/** emit the opcode (low byte) */
	public final static byte _OPC_LO = -6;
	/** force mem/size byte and set SZ=1 (byte) */
	public final static byte _SZ = -7;

	final static InstPatternMFP201 DATA_IMM8 = new InstPatternMFP201(
			IMM, new byte[] { _IMM8 });
	final static InstPatternMFP201 DATA_IMM16 = new InstPatternMFP201(
			IMM, new byte[] { _IMM16 });
	final static InstPatternMFP201 NONE_ = new InstPatternMFP201(
			new byte[] { _OPC } );
	final static InstPatternMFP201 IMM_ = new InstPatternMFP201(
			IMM, new byte[] { _OPC, _NEXTO, _IMM16 });
	final static InstPatternMFP201 OFF_ = new InstPatternMFP201(
			OFF, new byte[] { _OPC, _NEXTO, _OFF16 });
	final static InstPatternMFP201 IMMx_REG = new InstPatternMFP201(
			IMM, REG, new byte[] { _OPC, _IMM_SZ_0 | 0, _IMM8_16, _NEXTO, 0 });
	final static InstPatternMFP201 IMM8_REG = new InstPatternMFP201(
			IMM, REG, new byte[] { _OPC, _IMM8, _NEXTO, 0 });
	final static InstPatternMFP201 IMM16_REG = new InstPatternMFP201(
			IMM, REG, new byte[] { _OPC, _IMM16, _NEXTO, 0 });
	final static InstPatternMFP201 GEN_SR_GEN = new InstPatternMFP201(
			GEN, GEN, 
			new byte[] { _OPC_HI, 0, _NEXT, _OPC_LO, 0 });
	final static InstPatternMFP201 GEN_SR_GEN_SZ = new InstPatternMFP201(
			GEN, GEN, 
			new byte[] { _SZ, _OPC_HI, 0, _NEXT, _OPC_LO, 0 });
	final static InstPatternMFP201 GEN_REG_GEN = new InstPatternMFP201(
			GEN, REG, GEN, 
			new byte[] { _OPC, 0, _NEXT, 4, _NEXTO, 0 });
	final static InstPatternMFP201 GEN_REG_GEN_B = new InstPatternMFP201(
			GEN, REG, GEN, 
			new byte[] { _SZ, _OPC, 0, _NEXT, 4, _NEXTO, 0 });
	
	/** mappings for R13, R14, R15 in src1R position of 3-op logical instruction */
	public static final int[][] LOGICAL_INST_CONSTANTS = {
		{ 1, 0x8000, 0xffff },
		{ 1, 0x80, 0xff }
	};
	/** mappings for R13, R14, R15 in src1R position of 3-op arith instruction */
	public static final int[] ARITHMETIC_INST_CONSTANTS = {
		1, 2, 0
	};

	static Map<Integer, InstPatternMFP201[]> instEntries = new HashMap<Integer, InstPatternMFP201[]>();
	static Map<Integer, Integer> instMasks = new HashMap<Integer, Integer>();

	private static void register(int inst, int opcode, InstPatternMFP201 entry, int mask) {
		
		entry = entry.opcode(opcode);
		
		InstPatternMFP201[] entries = instEntries.get(inst);
		InstPatternMFP201[] newEntries;
		if (entries == null) {
			newEntries = new InstPatternMFP201[] { entry };
		} else {
			newEntries = new InstPatternMFP201[entries.length + 1];
			System.arraycopy(entries, 0, newEntries, 0, entries.length);
			newEntries[entries.length] = entry;
		}
			
		instEntries.put(inst, newEntries);
		instMasks.put(inst, mask);
	}
	
	private static void register4(int inst, int opcode, int mask) {
		register(inst, opcode, GEN_REG_GEN, mask);
		register(inst + 1, opcode, GEN_REG_GEN_B, mask);
		
		// in 3-op form, non-writing ADD->TST, ADC->TSTN
		if (inst == InstMFP201.Iadd) 
			inst = InstMFP201.Itst;
		else if (inst == InstMFP201.Iadc) 
			inst = InstMFP201.Itstn;
		else if (inst == InstMFP201.Iand || inst == InstMFP201.Inand)
			// these do not have no-write forms, since we write SR
			return;
		else 
			inst += 2;
		
		register(inst, opcode, GEN_REG_GEN, mask);
		register(inst + 1, opcode, GEN_REG_GEN_B, mask);
	}
		
	static {
		/*
		 * Multiple patterns may be registered for the same instruction.
		 * They are selected according to the order registered here.  
		 */
		register(InstTableCommon.Idata, 0x0000, DATA_IMM16, 0xffff);
		register(InstTableCommon.Ibyte, 0x0000, DATA_IMM8, 0xffff);

		register(InstMFP201.Ibkpt, 0x0, NONE_, 0x0);
		register(InstMFP201.Iret, 0x1, NONE_, 0x1);
		register(InstMFP201.Ireti, 0x2, NONE_, 0x2);
		register(InstMFP201.Ibr, 0x4, OFF_, 0x4);
		register(InstMFP201.Ibra, 0x5, IMM_, 0x5);
		register(InstMFP201.Icall, 0x6, OFF_, 0x6);
		register(InstMFP201.Icalla, 0x7, IMM_, 0x7);
		
		/* Register + immediate versions */
		register(InstMFP201.Ior, 0x800, IMMx_REG, 0x90f);
		register(InstMFP201.Iorb, 0x900, IMM8_REG, 0x90f);
		register(InstMFP201.Iorq, 0x810, IMMx_REG, 0x91f);
		register(InstMFP201.Iorbq, 0x910, IMM8_REG, 0x91f);
		
		register(InstMFP201.Iand, 0x820, IMMx_REG, 0x92f);
		register(InstMFP201.Iandb, 0x920, IMM8_REG, 0x92f);
		register(InstMFP201.Itst, 0x830, IMMx_REG, 0x93f);
		register(InstMFP201.Itstb, 0x930, IMM8_REG, 0x93f);
		
		register(InstMFP201.Inand, 0x840, IMMx_REG, 0x94f);
		register(InstMFP201.Inandb, 0x940, IMM8_REG, 0x94f);
		register(InstMFP201.Itstn, 0x850, IMMx_REG, 0x95f);
		register(InstMFP201.Itstnb, 0x950, IMM8_REG, 0x95f);

		register(InstMFP201.Ixor, 0x860, IMMx_REG, 0x96f);
		register(InstMFP201.Ixorb, 0x960, IMM8_REG, 0x96f);
		register(InstMFP201.Ixorq, 0x870, IMMx_REG, 0x97f);
		register(InstMFP201.Ixorbq, 0x970, IMM8_REG, 0x97f);
		
		register(InstMFP201.Iadd, 0x880, IMMx_REG, 0x98f);
		register(InstMFP201.Iaddb, 0x980, IMM8_REG, 0x98f);
		register(InstMFP201.Iaddq, 0x890, IMMx_REG, 0x99f);
		register(InstMFP201.Iaddbq, 0x990, IMM8_REG, 0x99f);

		register(InstMFP201.Isub, 0x8a0, IMMx_REG, 0x9af);
		register(InstMFP201.Isubb, 0x9a0, IMM8_REG, 0x9af);
		register(InstMFP201.Icmp, 0x8b0, IMMx_REG, 0x9bf);
		register(InstMFP201.Icmpb, 0x9b0, IMM8_REG, 0x9bf);

		register(InstMFP201.Iadc, 0x8c0, IMMx_REG, 0x9cf);
		register(InstMFP201.Iadcb, 0x9c0, IMM8_REG, 0x9cf);
		register(InstMFP201.Iadcq, 0x8d0, IMMx_REG, 0x9df);
		register(InstMFP201.Iadcbq, 0x9d0, IMM8_REG, 0x9df);
		
		/* Only immediate version available */
		register(InstMFP201.Ildc, 0x8e0, IMMx_REG, 0x9ef);
		register(InstMFP201.Ildcb, 0x9e0, IMM8_REG, 0x9ef);
		register(InstMFP201.Ildcq, 0x8f0, IMMx_REG, 0x9ff);
		register(InstMFP201.Ildcbq, 0x9f0, IMM8_REG, 0x9ff);
		
		/* two-op versions with duplicated  */
		/*
		register(InstMFP201.Ior, 0x80f0, GEN_SR_GEN, 0x8ff0);
		register(InstMFP201.Iorb, 0x80f0, GEN_SR_GEN_SZ, 0x8ff0);
		
		register(InstMFP201.Iand, 0x90f0, GEN_SR_GEN, 0x9ff0);
		register(InstMFP201.Iandb, 0x90f0, GEN_SR_GEN_SZ, 0x9ff0);
		
		register(InstMFP201.Inand, 0xa0f0, GEN_SR_GEN, 0xaff0);
		register(InstMFP201.Inandb, 0xa0f0, GEN_SR_GEN_SZ, 0xaff0);
		
		register(InstMFP201.Ixor, 0xb0f0, GEN_SR_GEN, 0xbff0);
		register(InstMFP201.Ixorb, 0xb0f0, GEN_SR_GEN_SZ, 0xbff0);
		*/
		
		/* three-op instructions */
		register4(InstMFP201.Ior, 0x80, 0x8f);
		register4(InstMFP201.Iand, 0x90, 0x9f);
		register4(InstMFP201.Inand, 0xa0, 0xaf);
		register4(InstMFP201.Ixor, 0xb0, 0xbf);
		register4(InstMFP201.Iadd, 0xc0, 0xcf);
		register4(InstMFP201.Iadc, 0xd0, 0xdf);
		register4(InstMFP201.Isub, 0xe0, 0xef);
		register4(InstMFP201.Isbb, 0xf0, 0xff);
		
		/*
		register(InstTableCommon.Idsr, 0x0c00, OFF_NONE, 0xcff);
		register(InstTableCommon.Ikysl, 0x0d40, NONE_, 0xd40);
		register(InstTableCommon.Iticks, 0x0d60, REG_NONE, 0xd60);
		register(InstTableCommon.Iemitchar, 0x0dc0, REG_CNT, 0xdcf);
		register(InstTableCommon.Idbg, 0x0de0, NONE_, 0xde0);
		register(InstTableCommon.Idbgf, 0x0de1, NONE_, 0xde1);
		 */

	};
	

	public static Map<String, Integer> nameToInst = new HashMap<String, Integer>();
	public static Map<Integer, String> instToName = new HashMap<Integer, String>();
	
	static { registerInstruction(InstTableCommon.Idata, "data"); }
	static { registerInstruction(InstTableCommon.Ibyte, "byte"); }
	static { registerInstruction(InstMFP201.Ibkpt, "bkpt"); }
	static { registerInstruction(InstMFP201.Iret, "ret"); }
	static { registerInstruction(InstMFP201.Ireti, "reti"); }
	static { registerInstruction(InstMFP201.Ibr, "br"); }
	static { registerInstruction(InstMFP201.Ibra, "bra"); }
	static { registerInstruction(InstMFP201.Icall, "call"); }
	static { registerInstruction(InstMFP201.Icalla, "calla"); }
	
	static { registerInstruction(InstMFP201.Ior, "or"); }
	static { registerInstruction(InstMFP201.Iorb, "or.b"); }
	static { registerInstruction(InstMFP201.Iorq, "or?"); }
	static { registerInstruction(InstMFP201.Iorbq, "or.b?"); }
	
	static { registerInstruction(InstMFP201.Iand, "and"); }
	static { registerInstruction(InstMFP201.Iandb, "and.b"); }
	static { registerInstruction(InstMFP201.Itst, "tst"); }
	static { registerInstruction(InstMFP201.Itstb, "tst.b"); }
	
	static { registerInstruction(InstMFP201.Inand, "nand"); }
	static { registerInstruction(InstMFP201.Inandb, "nand.b"); }
	static { registerInstruction(InstMFP201.Itstn, "tstn"); }
	static { registerInstruction(InstMFP201.Itstnb, "tstn.b"); }
	
	static { registerInstruction(InstMFP201.Ixor, "xor"); }
	static { registerInstruction(InstMFP201.Ixorb, "xor.b"); }
	static { registerInstruction(InstMFP201.Ixorq, "xor?"); }
	static { registerInstruction(InstMFP201.Ixorbq, "xor.b?"); }
	
	static { registerInstruction(InstMFP201.Iadd, "add"); }
	static { registerInstruction(InstMFP201.Iaddb, "add.b"); }
	static { registerInstruction(InstMFP201.Iaddq, "add?"); }
	static { registerInstruction(InstMFP201.Iaddbq, "add.b?"); }
	
	static { registerInstruction(InstMFP201.Isub, "sub"); }
	static { registerInstruction(InstMFP201.Isubb, "sub.b"); }
	static { registerInstruction(InstMFP201.Icmp, "cmp"); }
	static { registerInstruction(InstMFP201.Icmpb, "cmp.b"); }
	
	static { registerInstruction(InstMFP201.Iadc, "adc"); }
	static { registerInstruction(InstMFP201.Iadcb, "adc.b"); }
	static { registerInstruction(InstMFP201.Iadcq, "adc?"); }
	static { registerInstruction(InstMFP201.Iadcbq, "adc.b?"); }
	
	static { registerInstruction(InstMFP201.Isbb, "sbb"); }
	static { registerInstruction(InstMFP201.Isbbb, "sbb.b"); }
	static { registerInstruction(InstMFP201.Icmpr, "cmpr"); }
	static { registerInstruction(InstMFP201.Icmprb, "cmpr.b"); }
	

	static { registerInstruction(InstMFP201.Ildc, "ldc"); }
	static { registerInstruction(InstMFP201.Ildcb, "ldc.b"); }
	static { registerInstruction(InstMFP201.Ildcq, "ldc?"); }
	static { registerInstruction(InstMFP201.Ildcbq, "ldc.b?"); }
	
	static { registerInstruction(InstTableCommon.Idsr, "dsr"); }
	static { registerInstruction(InstTableCommon.Ikysl, "kysl"); }
	static { registerInstruction(InstTableCommon.Iticks, "ticks"); }
	static { registerInstruction(InstTableCommon.Iemitchar, "emitchar"); }
	static { registerInstruction(InstTableCommon.Idbg, "dbg"); }
	static { registerInstruction(InstTableCommon.Idbgf, "dbgf"); }
	static { registerInstruction(InstTableCommon.Ibyte, "byte"); }

	//static { registerAlias(InstMFP201.Ijeq, "je"); }
	//static { registerAlias(InstMFP201.Ijoc, "jc"); }


	public static byte[] encode(RawInstruction rawInst) throws IllegalArgumentException {
		int inst = rawInst.getInst();
		//int variant = rawInst instanceof InstructionMFP201 ? 
		//		((InstructionMFP201) rawInst).variant : InstructionMFP201.VARIANT_NONE;
				
		InstPatternMFP201[] patterns = instEntries.get(inst);
		if (patterns == null)
			throw new IllegalArgumentException("Non-encoded instruction");
		
		if (rawInst.getOp1() != null && !(rawInst.getOp1() instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 1: " + rawInst.getOp1());
		if (rawInst.getOp2() != null && !(rawInst.getOp2() instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 2: " + rawInst.getOp2());
		if (rawInst.getOp3() != null && !(rawInst.getOp3() instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 3: " + rawInst.getOp3());
		
		coerceOperandTypes(rawInst);
		
		MachineOperandMFP201 mop1 = (MachineOperandMFP201) rawInst.getOp1();
		MachineOperandMFP201 mop2 = (MachineOperandMFP201) rawInst.getOp2();
		MachineOperandMFP201 mop3 = (MachineOperandMFP201) rawInst.getOp3();
		
		IllegalArgumentException lastException = null;
		for (InstPatternMFP201 pattern : patterns) {
			try {
				assertOperandMatches(rawInst, mop1, pattern.op1);
				assertOperandMatches(rawInst, mop2, pattern.op2);
				assertOperandMatches(rawInst, mop3, pattern.op3);
				lastException = null;
			} catch (IllegalArgumentException e) {
				lastException = e;
				continue;
			}
			
			// hi 2 bytes: loop prefix, lo byte: mem/size byte 
			int loopAndMemSize = 0;
	
			MachineOperandMFP201[] mops = { mop1, mop2, mop3 };
			
			byte[] immeds = { 0, 0, 0, 0 };
			int immIdx = 0;
			
			byte[] work = { 0, 0, 0, 0, 0, 0 };
			
			int mopIdx = 0;
			int workIdx = -1;
			
			MachineOperandMFP201 mop = null;
	
			int opcode = pattern.opcode;
			
			for (int idx = 0; idx < pattern.enc.length; idx++) {
				byte enc = pattern.enc[idx];
				if (enc == _OPC) {
					workIdx++;
					if (opcode >= 0x100) {
						work[workIdx++] = (byte) (opcode >> 8);
					}
					work[workIdx] = (byte) (opcode & 0xff);
				}
				else if (enc == _OPC_HI) {
					work[workIdx] = (byte) (opcode >> 8);
				}
				else if (enc == _OPC_LO) {
					work[workIdx] = (byte) (opcode & 0xff);
				}
				else if (enc == _SZ) {
					loopAndMemSize |= InstructionMFP201.MEM_SIZE_OPCODE | InstructionMFP201.MEM_SIZE_SZ;
				}
				else {
					if (enc == _NEXT) {
						workIdx++;
						mop = null;
						continue;
					}
					if (enc == _NEXTB) {
						workIdx++;
					}
					if (enc == _NEXTO) {
						mop = null;
						continue;
					}
					if (mop == null) {
						mop = mops[mopIdx++];
						
						if (mop != null) {
							if ((mop.type >= MachineOperandMFP201.OP_REG
									&& mop.type <= MachineOperandMFP201.OP_INC) 
									|| mop.type == MachineOperandMFP201.OP_DEC) {
								int bit = pattern.length == 3 && mopIdx == 1 ? 2 : 0;
								int Am = mop.type & 0x3;
								if (Am != 0)
									loopAndMemSize |= InstructionMFP201.MEM_SIZE_OPCODE | (Am << bit);
								
							}
							
							int opm = pattern.op(mopIdx);
							if (mop.hasImmediate() && opm == InstPatternMFP201.GEN) 
							{
								if (mop.encoding == MachineOperandMFP201.OP_ENC_IMM8) {
									immeds[immIdx++] = (byte) (mop.immed & 0xff);
								}
								/*
								else if (mop.encoding == MachineOperandMFP201.OP_ENC_PCREL12) {
									immeds[immIdx++] = (byte) (mop.immed & 0xff);
									loopAndMemSize |= (mop.immed >> 8) & 0xf;
								}
								else if (mop.encoding == MachineOperandMFP201.OP_ENC_PCREL16) {
									immeds[immIdx++] = (byte) (mop.immed & 0xff);
									loopAndMemSize |= (mop.immed >> 8) & 0xf;
									immeds[immIdx++] = (byte) (mop.immed >> 12);
								}
								*/
								else {
									immeds[immIdx++] = (byte) (mop.immed >> 8);
									immeds[immIdx++] = (byte) (mop.immed & 0xff);
								}
							}
						}
					}
	
					if (enc < 8) {
						work[workIdx] |= (byte) ((mop.val & 0xf) << enc); 
					}
					else if (enc == _IMM8) {
						immeds[immIdx++] = (byte) (mop.immed & 0xff); 
					}
					else if (enc == _IMM16) {
						immeds[immIdx++] = (byte) ((mop.immed & 0xff00) >> 8); 
						immeds[immIdx++] = (byte) (mop.immed & 0xff); 
					}
					else if (enc == _IMM8_16) {
						if (!isImm8(mop.immed))
							immeds[immIdx++] = (byte) ((mop.immed & 0xff00) >> 8);
						immeds[immIdx++] = (byte) (mop.immed & 0xff);
					}
					else if (enc == _OFF16) {
						int val;
						if (mop.type == MachineOperandMFP201.OP_PCREL) {
							val = mop.val - workIdx - immIdx - 1;
						} else {
							val = mop.immed - workIdx - immIdx - 1 - rawInst.pc;
						}
						immeds[immIdx++] = (byte) ((val & 0xff00) >> 8); 
						immeds[immIdx++] = (byte) (val & 0xff); 
					}
					else if ((enc & 0xf0) == _IMM_SZ) {	
						if (isImm8(mop.immed))
							work[workIdx] |= (byte) (1 << (enc & 0xf));
					}
					else if ((enc & 0xf0) == _IMM_SZ_0) {	
						if (isImm8(mop.immed))
							work[0] |= (byte) (1 << (enc & 0xf));
					}
				}
			}
			workIdx++;
			
			int offs = 0;
			if ((loopAndMemSize & 0xff) != 0)
				offs++;
			if (loopAndMemSize >= 0x10000) {
				offs += 2;
			}
			byte[] bytes = new byte[workIdx + immIdx + offs];
			System.arraycopy(work, 0, bytes, offs, workIdx);
			System.arraycopy(immeds, 0, bytes, workIdx + offs, immIdx);
			
			offs = 0;
			if (loopAndMemSize >= 0x10000) {
				bytes[offs++] = (byte) ((loopAndMemSize & 0xff000000) >> 24);
				bytes[offs++] = (byte) ((loopAndMemSize & 0xff0000) >> 16);
				offs += 2;
			}
			if ((loopAndMemSize & 0xff) != 0) {
				bytes[offs++] = (byte) (loopAndMemSize & 0xff);	
			}
			return bytes;
		}
		throw lastException;
	}

	/**
	 * @param inst
	 * @return
	 */
	public static boolean canHaveMemSizeByte(int inst) {
		return inst >= InstMFP201._IlastSimpleImmediate;
	}

	public static boolean canBeSimpleImmediateInst(int inst) {
		return (inst >= InstMFP201.Ior && inst <= InstMFP201.Ildcbq);
	}

	/**
	 * @param immed
	 * @return
	 */
	public static boolean isImm8(short immed) {
		return (byte) (immed & 0xff) == immed;
	}

	private static void assertOperandMatches(RawInstruction inst, BaseMachineOperand mop, int op) {
		if (mop == null && op != NONE)
			throw new IllegalArgumentException("Operand missing in " + inst);
		else if (mop != null && mop.type != MachineOperand.OP_NONE && op == NONE)
			throw new IllegalArgumentException("Unexpected operand: " + mop + " in " + inst);
		
		switch (op) {
		case IMM:
			if (!mop.isConstant())
				throw new IllegalArgumentException("Expected immediate: " + mop + " in " + inst);
			break;
		case CNT:
			if (mop.type != MachineOperandMFP201.OP_CNT && mop.type != MachineOperandMFP201.OP_IMM)
				throw new IllegalArgumentException("Expected count: " + mop + " in " + inst);
			break;
		case OFF:
			if (mop.type != MachineOperandMFP201.OP_CNT && mop.type != MachineOperandMFP201.OP_IMM
					&& mop.type != MachineOperandMFP201.OP_PCREL)
				throw new IllegalArgumentException("Expected offset: " + mop + " in " + inst);
			break;
		case REG:
			if (mop.type != MachineOperandMFP201.OP_REG && mop.type != MachineOperandMFP201.OP_REG0_SHIFT_COUNT)
				throw new IllegalArgumentException("Expected register: " + mop + " in " + inst);
			break;
		case GEN:
			if (mop.type == MachineOperandMFP201.OP_NONE)
				throw new IllegalArgumentException("Expected general operand: " + mop + " in " + inst);
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
	public static InstPatternMFP201 coerceOperandTypes(RawInstruction instruction) {
		InstPatternMFP201[] patterns = instEntries.get(instruction.getInst());
		if (patterns == null)
			throw new IllegalArgumentException("Non-encoded instruction");
		
		// find a match
		IllegalArgumentException lastException = null;
		
		for (InstPatternMFP201 pattern : patterns) {
			MachineOperandMFP201 
				mop1 = (MachineOperandMFP201) instruction.getOp1(), 
				mop2 = (MachineOperandMFP201) instruction.getOp2(), 
				mop3 = (MachineOperandMFP201) instruction.getOp3();
			
			if (mop3 == null && pattern.length >= 3)
				continue;
			if (mop2 == null && pattern.length >= 2)
				continue;
			if (mop1 == null && pattern.length >= 1)
				continue;
			
			mop1 = coerceOperandType(instruction, mop1, pattern.op1);
			mop2 = coerceOperandType(instruction, mop2, pattern.op2);
			mop3 = coerceOperandType(instruction, mop3, pattern.op3);
			
			try {
				assertOperandMatches(instruction, mop1, pattern.op1);
				assertOperandMatches(instruction, mop2, pattern.op2);
				assertOperandMatches(instruction, mop3, pattern.op3);
				
				instruction.setOp1(mop1);
				instruction.setOp2(mop2);
				instruction.setOp3(mop3);
				return pattern;
			} catch (IllegalArgumentException e) {
				lastException = e;
			}
		}
		if (lastException != null)
			throw lastException;
		
		return null;
	}
	
	private static MachineOperandMFP201 coerceOperandType(RawInstruction instruction, MachineOperandMFP201 mop, int op) {
		if (op == NONE)
			return mop;
		
		if (mop == null)
			return null;
			
		switch (op) {
		case IMM:
			break;
		case CNT:
			if (mop.type == MachineOperandMFP201.OP_REG0_SHIFT_COUNT) {
				return MachineOperandMFP201.createGeneralOperand(MachineOperandMFP201.OP_CNT, 
						mop.val);
			}
			if (mop.type == MachineOperandMFP201.OP_IMM) {
				return MachineOperandMFP201.createGeneralOperand(MachineOperandMFP201.OP_CNT,
						mop.immed);
			}
			break;
		case OFF:
			if (mop.type == MachineOperandMFP201.OP_IMM) {
				// convert address to offset from this inst
				return MachineOperandMFP201.createGeneralOperand(MachineOperandMFP201.OP_PCREL,
						mop.val - instruction.pc);
			}
			break;
		case REG:
			break;
		case GEN:
			if (mop.type == MachineOperandMFP201.OP_IMM) {
				// immediate is *PC+
				MachineOperandMFP201 immed = MachineOperandMFP201.createGeneralOperand(
						MachineOperandMFP201.OP_INC,
						MachineOperandMFP201.PC,
						mop.immed);
				if (!isByteInst(instruction.getInst()) && isImm8(mop.immed)) {
					// if all imm or reg ops, we can convert this to a byte
					if ((instruction.getOp2() != null 
							&& instruction.getOp2() != mop 
							&& ((MachineOperand) instruction.getOp2()).isRegister())
						&& 
						((instruction.getOp3() == null 
								|| (instruction.getOp3() != mop 
								&& ((MachineOperand) instruction.getOp3()).isRegister())))
						) 
					{
						instruction.setInst(instruction.getInst() | 1);
					}
								
				}
				immed.encoding = isByteInst(instruction.getInst()) ? 
						MachineOperandMFP201.OP_ENC_IMM8 : MachineOperandMFP201.OP_ENC_IMM16;
				return immed;
			}
			else if (mop.type == MachineOperandMFP201.OP_PCREL) {
				// PC-rel is @x(PC)
				MachineOperandMFP201 pcrel = MachineOperandMFP201.createGeneralOperand(
						MachineOperandMFP201.OP_OFFS,
						MachineOperandMFP201.PC,
						mop.immed);
				return pcrel;
			}
			break;
		}
		return mop;
	}
	
	public static int coerceInstructionOpcode(int inst, int opcode) {
		Integer mask = instMasks.get(inst);
		if (mask == null)
			return opcode;
		return opcode & mask;
	}
	public static InstPatternMFP201[] lookupEncodePatterns(int inst) {
		return instEntries.get(inst);
	}
	
	public static void registerInstruction(int inst, String str) {
	    Integer i = new Integer(inst);
	    nameToInst.put(str.toUpperCase(), i);
	    instToName.put(i, str.toUpperCase());
	}
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
	/*
    public static RawInstruction decodeInstruction(int op, int pc, MemoryDomain domain) {
    	RawInstruction inst = new RawInstruction();
    	inst.pc = pc;
    	
        // deal with it unsigned 
        op &= 0xffff;
    
        inst.opcode = (short) op;
        inst.setInst(InstTableCommon.Idata);
        inst.setName("DATA");
        inst.size = 0;
        MachineOperandMFP201 mop1 = new MachineOperandMFP201(MachineOperand.OP_NONE);
        MachineOperandMFP201 mop2 = new MachineOperandMFP201(MachineOperand.OP_NONE);
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
            mop1.type = MachineOperandMFP201.OP_REG;
            mop1.val = op & 15;
            mop2.type = MachineOperandMFP201.OP_IMM;
            switch ((op & 0x1e0) >> 5) {
            case 0:
                //inst.name = "LI";
                inst.setInst(InstMFP201.Ili);
                break;
            case 1:
                //inst.name = "AI";
                inst.setInst(InstMFP201.Iai);
                break;
            case 2:
                //inst.name = "ANDI";
                inst.setInst(InstMFP201.Iandi);
                break;
            case 3:
                //inst.name = "ORI";
                inst.setInst(InstMFP201.Iori);
                break;
            case 4:
                //inst.name = "CI";
                inst.setInst(InstMFP201.Ici);
                break;
            }
    
        } else if (op < 0x2e0) {
            mop1.type = MachineOperandMFP201.OP_REG;
            mop1.val = op & 15;
            switch ((op & 0x1e0) >> 5) {
            case 5:
                //inst.name = "STWP";
                inst.setInst(InstMFP201.Istwp);
                break;
            case 6:
                //inst.name = "STST";
                inst.setInst(InstMFP201.Istst);
                break;
            }
    
        } else if (op < 0x320) {
            mop1.type = MachineOperandMFP201.OP_IMM;
    
            switch ((op & 0x1e0) >> 5) {
            case 7:
                //inst.name = "LWPI";
                inst.setInst(InstMFP201.Ilwpi);
                break;
            case 8:
                //inst.name = "LIMI";
                inst.setInst(InstMFP201.Ilimi);
                break;
            }
    
        } else if (op < 0x400) {
            switch ((op & 0x1e0) >> 5) {
            case 10:
                //inst.name = "IDLE";
                inst.setInst(InstMFP201.Iidle);
                break;
            case 11:
                //inst.name = "RSET";
                inst.setInst(InstMFP201.Irset);
                break;
            case 12:
                //inst.name = "RTWP";
                inst.setInst(InstMFP201.Irtwp);
                break;
            case 13:
                //inst.name = "CKON";
                inst.setInst(InstMFP201.Ickon);
                break;
            case 14:
                //inst.name = "CKOF";
                inst.setInst(InstMFP201.Ickof);
                break;
            case 15:
                //inst.name = "LREX";
                inst.setInst(InstMFP201.Ilrex);
                break;
            }
    
        } else if (op < 0x800) {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
    
            switch ((op & 0x3c0) >> 6) {
            case 0:
                //inst.name = "BLWP";
                inst.setInst(InstMFP201.Iblwp);
                break;
            case 1:
                //inst.name = "B";
                inst.setInst(InstMFP201.Ib);
                break;
            case 2:
                //inst.name = "X";
                inst.setInst(InstMFP201.Ix);
                break;
            case 3:
                //inst.name = "CLR";
                inst.setInst(InstMFP201.Iclr);
                break;
            case 4:
                //inst.name = "NEG";
                inst.setInst(InstMFP201.Ineg);
                break;
            case 5:
                //inst.name = "INV";
                inst.setInst(InstMFP201.Iinv);
                break;
            case 6:
                //inst.name = "INC";
                inst.setInst(InstMFP201.Iinc);
                break;
            case 7:
                //inst.name = "INCT";
                inst.setInst(InstMFP201.Iinct);
                break;
            case 8:
                //inst.name = "DEC";
                inst.setInst(InstMFP201.Idec);
                break;
            case 9:
                //inst.name = "DECT";
                inst.setInst(InstMFP201.Idect);
                break;
            case 10:
                //inst.name = "BL";
                inst.setInst(InstMFP201.Ibl);
                break;
            case 11:
                //inst.name = "SWPB";
                inst.setInst(InstMFP201.Iswpb);
                break;
            case 12:
                //inst.name = "SETO";
                inst.setInst(InstMFP201.Iseto);
                break;
            case 13:
                //inst.name = "ABS";
                inst.setInst(InstMFP201.Iabs);
                break;
            }
    
        } else if (op < 0xc00) {
            mop1.type = MachineOperandMFP201.OP_REG;
            mop1.val = op & 15;
            mop2.type = MachineOperandMFP201.OP_CNT;
            mop2.val = (op & 0xf0) >> 4;
    
            switch ((op & 0x700) >> 8) {
            case 0:
                //inst.name = "SRA";
                inst.setInst(InstMFP201.Isra);
                break;
            case 1:
                //inst.name = "SRL";
                inst.setInst(InstMFP201.Isrl);
                break;
            case 2:
                //inst.name = "SLA";
                inst.setInst(InstMFP201.Isla);
                break;
            case 3:
                //inst.name = "SRC";
                inst.setInst(InstMFP201.Isrc);
                break;
            }
    
        } else if (op < 0x1000) {
}    
        if (inst.getInst() == 0) // data
        {
            mop1.type = MachineOperandMFP201.OP_IMM;
            mop1.val = mop1.immed = (short) op;
            //inst.name = "DATA";
            inst.size = 2;
        } else {
        	// inst.completeInstruction(pc);
            // Finish reading operand immediates
            pc += 2;
            pc = mop1.fetchOperandImmediates(domain, (short)pc);
            pc = mop2.fetchOperandImmediates(domain, (short)pc);
            inst.size = pc - inst.pc;
            inst.setName(InstTableMFP201.getInstName(inst.getInst()));
        }

        return inst;
    }
    */
    
	/*
    public static void calculateInstructionSize(RawInstruction target) {
    	if (target.getInst() == InstTableCommon.Idata) {
    		target.setSize(2);
    		return;
    	} else if (target.getInst() == InstTableCommon.Ibyte) {
    		target.setSize(1);
    		return;
    	}
    	target.setSize(0);
    	InstPatternMFP201 pattern = lookupEncodePattern(target.getInst());
		if (pattern == null)
			return;
		
		byte[] bytes = encode(target);
		target.setSize(target.getSize() + bytes.length);
    }
    */
	
	public static boolean isJumpInst(int inst) {
		//return inst >= InstMFP201.Ijmp && inst <= InstMFP201.Ijop;
		return false;
	}
	public static boolean isByteInst(int inst) {
		return (inst & 1) != 0;
	}

	
	public static boolean canBeThreeOpInst(int inst) {
		return inst >= InstMFP201._IfirstPossibleThreeOp 
			&& inst <= InstMFP201._IlastPossibleThreeOp;
	}
	public static boolean isLogicalOpInst(int inst) {
		return inst >= InstMFP201._IfirstLogicalOp 
			&& inst <= InstMFP201._IlastLogicalOp;
	}
	public static boolean isLoadConstInst(int inst) {
		return inst >= InstMFP201.Ildc && inst <= InstMFP201.Ildcbq;
	}
	
	public static boolean isNonWritingInst(int inst) {
		if (canBeThreeOpInst(inst) || isLoadConstInst(inst)) {
			return (inst & 2) != 0;
		}
		else {
			return false;
		}
	}

	/**
	 * @param inst
	 * @return
	 */
	public static boolean isCommutativeInst(int inst) {
		if (canBeThreeOpInst(inst)) {
			inst &= ~3;
			if (inst == InstMFP201.Iadd 
					|| inst == InstMFP201.Iand
					|| inst == InstMFP201.Ior
					|| inst == InstMFP201.Ixor
					|| inst == InstMFP201.Inand) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param inst
	 * @return
	 */
	public static int getReversedInst(int inst) {
		if (inst >= InstMFP201.Icmp && inst <= InstMFP201.Icmpb)
			return inst - InstMFP201.Icmp + InstMFP201.Icmpr;
		if (inst >= InstMFP201.Icmpr && inst <= InstMFP201.Icmprb)
			return inst - InstMFP201.Icmpr + InstMFP201.Icmp;
		if (isCommutativeInst(inst))
			return inst;
		return 0;
	}

	/**
	 * @param inst
	 * @return
	 */
	public static boolean isArithOpInst(int inst) {
		return inst >= InstMFP201._IfirstArithOp 
		&& inst <= InstMFP201._IlastArithOp;
	}
}
