/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import static v9t9.engine.cpu.InstructionTable.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.ICallingConvention;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ICallingConvention.CallerStackLocation;
import org.ejs.eulang.ICallingConvention.Location;
import org.ejs.eulang.ICallingConvention.RegisterLocation;
import org.ejs.eulang.ICallingConvention.StackBarrierLocation;
import org.ejs.eulang.ICallingConvention.StackLocation;
import org.ejs.eulang.ITarget.Intrinsic;
import org.ejs.eulang.TypeEngine.Alignment;
import org.ejs.eulang.TypeEngine.Target;
import org.ejs.eulang.llvm.FunctionConvention;
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLCodeVisitor;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDeclareDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.instrs.LLAllocaInstr;
import org.ejs.eulang.llvm.instrs.LLAssignInstr;
import org.ejs.eulang.llvm.instrs.LLBinaryInstr;
import org.ejs.eulang.llvm.instrs.LLBranchInstr;
import org.ejs.eulang.llvm.instrs.LLCallInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr;
import org.ejs.eulang.llvm.instrs.LLCompareInstr;
import org.ejs.eulang.llvm.instrs.LLExtractValueInstr;
import org.ejs.eulang.llvm.instrs.LLGetElementPtrInstr;
import org.ejs.eulang.llvm.instrs.LLInsertValueInstr;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.instrs.LLLoadInstr;
import org.ejs.eulang.llvm.instrs.LLRetInstr;
import org.ejs.eulang.llvm.instrs.LLStoreInstr;
import org.ejs.eulang.llvm.instrs.LLTypedInstr;
import org.ejs.eulang.llvm.instrs.LLUncondBranchInstr;
import org.ejs.eulang.llvm.ops.LLArrayOp;
import org.ejs.eulang.llvm.ops.LLBitcastOp;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLStructOp;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.ops.LLTempOp;
import org.ejs.eulang.llvm.ops.LLUndefOp;
import org.ejs.eulang.llvm.ops.LLZeroInitOp;
import org.ejs.eulang.llvm.tms9900.asm.LocalOffsOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOffsOperand;
import org.ejs.eulang.llvm.tms9900.asm.StackLocalOffsOperand;
import org.ejs.eulang.llvm.tms9900.asm.AsmOperand;
import org.ejs.eulang.llvm.tms9900.asm.CompareOperand;
import org.ejs.eulang.llvm.tms9900.asm.ISymbolOperand;
import org.ejs.eulang.llvm.tms9900.asm.Label;
import org.ejs.eulang.llvm.tms9900.asm.NumOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.ejs.eulang.llvm.tms9900.asm.StackLocalOperand;
import org.ejs.eulang.llvm.tms9900.asm.SymbolLabelOperand;
import org.ejs.eulang.llvm.tms9900.asm.SymbolOperand;
import org.ejs.eulang.llvm.tms9900.asm.TupleTempOperand;
import org.ejs.eulang.llvm.tms9900.asm.ZeroInitOperand;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLAggregateType;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLInstanceField;
import org.ejs.eulang.types.LLPointerType;
import org.ejs.eulang.types.LLType;

import v9t9.engine.cpu.InstructionTable;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.ConstPoolRefOperand;
import v9t9.tools.asm.assembler.operand.hl.IRegisterOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.RegisterOperand;

/**
 * This selects the 9900 instructions from the LLVM code.  Subclass
 * to make use of the instructions or handle register allocation.
 * <p>
 * Most instructions are handled by patterns which encode a dense
 * set of checks.  Operands are constructed implicitly.
 * @author ejs
 *
 */
public abstract class InstrSelection extends LLCodeVisitor {
	public boolean DUMP = false;
	
	/** Codes for type checking */
	public static final int I8 = 1;
	public static final int I16 = 2;
	public static final int I1 = 4;
	public static final int I32 = 8;
	public static final int I64 = 16;
	
	/** Tests for instruction selection.  These correspond to LL operand positions. */
	enum If {
		/** no test */
		PASS,
		
		/** and of next two tests */
		AND,
		
		/** if the LL operand is a constant */
		IS_CONST,
		/** if the LL operand is a constant zero */
		IS_CONST_0,
		/** if the LL operand is a constant one */
		IS_CONST_1,
		/** if the LL operand is a constant negative one */
		IS_CONST_N1,
		/** if the LL operand is a constant one */
		IS_CONST_2,
		/** if the LL operand is a constant negative two */
		IS_CONST_N2,
		/** if the LL operand is a constant from 1 to 15 */
		IS_CONST_1_15,
		/** if the LL operand is a constant from 16 */
		IS_CONST_16,
		/** if the LL operand is a constant power of 2 (>0) */
		IS_CONST_POW_2,
		
		/** if the LL operand is allocated to a physical register */
		IN_PHYS_REG,
		/** if the LL operand is allocated to physical register 0 */
		IN_PHYS_REG_0,
		/** if the LL operand is allocated to a local which came in as a register local or is in a temp.
		 * Use this when the register might be spilled. */
		IN_REG_LOCAL,
		/** if the LL operand is allocated to a register temp */
		IN_TEMP,

		/** last use of a temporary */
		//IS_TEMP_LAST_USE,

		/** if the LL operand is allocated to a stack local (argument from stack or otherwise known or forced to stack) */
		ON_STACK,
		
		/** if the operand is in memory (global, stack, ...) */
		IN_MEMORY,
		
		/** is integral */
		IS_INT,
		/** is 8 bit int */
		IS_I8,
		/** is 16 bit int */
		IS_I16,
		/** is 1 bit int */
		IS_I1,
		
	};
	
	enum As {
		/** throw away */
		IGNORE,
		/** self */
		SELF,
		/** put the LL operand into a general operand which be used */
		GEN_R,
		/** put the LL operand into a general operand which be killed */
		GEN_W,
		/** put the LL operand into a general operand which will be read and written */
		GEN_RW,
		/** put the LL operand into a register operand which will be used */
		REG_R,
		/** put the LL operand into a register operand which will be killed */
		REG_W,
		/** put the LL operand into a register operand which will be read and written */
		REG_RW,
		/** put the byte value of the LL operand into two halves of a register operand  */
		REG_RW_DUP,
		/** put the LL operand into physical register #0, shifted low for a shift/IO count instruction */
		REG_0_CNT_W,
		/** put the LL operand into a high entry (N) of an adjacent physical register pair */
		REG_HI_W,
		/** put the LL operand into a low entry (N + 1) of an adjacent physical register pair */
		REG_LO_W,
		
		/** put the LL operand into an immediate */
		IMM,
		/** put the LL operand into an immediate, negated */
		IMM_NEG,
		/** put the LL operand into an immediate, negated and masked to 15 */
		IMM_NEG_15,
		/** put the LL operand into an immediate, as log 2(value) */
		IMM_LOG_2,
		/** put the LL constant operand into a const pool */
		CONST_POOL,

		/** reuse the asm operand generated for operand #0 */
		SAME_0,
		/** reuse the asm operand generated for operand #1 */
		SAME_1,
		/** reuse the asm operand generated for operand #2 */
		SAME_2,
		
		/** synthesize immediate 8 */
		IMM_15,
		/** synthesize immediate 8 */
		IMM_8,
		/** synthesize immediate 1 */
		IMM_1,
		/** synthesize immediate 0 */
		IMM_0,
		/** synthesize immediate -1 */
		IMM_N1,
		
		/** for ICMP or FCMP, the comparison (NumOperand, value: CMP_xxx) */
		CMP,
	};
	
	/** Pseudo-instructions */
	final static int Ipseudo = Iuser;
	final public static int 
		Pprolog = Ipseudo + 0,
		Pepilog = Ipseudo + 1,
		Piset = Ipseudo + 2,
		Pjcc = Ipseudo + 3,
		Pcopy = Ipseudo + 4,
		Plea = Ipseudo + 5
	;
	static {
		InstructionTable.registerInstruction(Pprolog, "PROLOG");
		InstructionTable.registerInstruction(Pepilog, "EPILOG");
		InstructionTable.registerInstruction(Piset, "ISET");
		InstructionTable.registerInstruction(Pjcc, "JCC");
		InstructionTable.registerInstruction(Pcopy, "COPY");
		InstructionTable.registerInstruction(Plea, "LEA");
	}
	public static final As[] NO_AS = new As[0];
	public static final Do[] NO_DO = new Do[0];
	public static final If[] NO_IFS = new If[0];
	
	static class Do {
		int inst;
		int[] ops;
		
		Do(int inst, int... ops) {
			this.inst = inst;
			this.ops = ops;
		}
	};

	static class DoRes extends Do {
		int result;
		
		DoRes(int result, int inst, int... ops) {
			super(inst, ops);
			this.result = result;
		}
	}
	static class DoIntrinsic extends DoRes {
		Intrinsic intrinsic;
		
		DoIntrinsic(Intrinsic intrinsic, int... ops) {
			super(0, 0, ops);
			this.intrinsic = intrinsic;
		}
	}

	static class IPattern {
		BasicType basicType;
		int typeMask;
		String llInst;
		If[] opconds;
		As[] ases;
		Do[] dos;
		public IPattern(BasicType basicType, int typeMask, String llInst,
				If[] opconds, As[] ases, Do... dos) {
			this.basicType = basicType;
			this.typeMask = typeMask;
			this.llInst = llInst;
			this.opconds = opconds != null ? opconds : NO_IFS;
			this.ases = ases != null ? ases : NO_AS; 
			this.dos = dos != null ? dos : NO_DO;
		}

		
	};
	
	abstract protected void newRoutine(Routine routine);
	
	/** Called when an instruction has been generated */
	abstract protected void emit(AsmInstruction instr);
	
	/** Called when a new block has been created */
	abstract protected void newBlock(Block block);
	
	private LLDefineDirective def;
	private ICallingConvention cc;

	private IPattern thePattern;
	private AssemblerOperand[] asmOps = new AssemblerOperand[4];
	
	private Locals locals;
	private Routine routine;
	private Block block;
	
	private HashMap<ISymbol, Block> blockMap;
	/** dests of SSA operands, which do not change value */
	private HashMap<LLOperand, AssemblerOperand> ssaTempTable;
	private LLInstr instr;
	private LLBlock llblock;
	private TypeEngine typeEngine;
	private RegisterLocal regPair;
	private final LLModule module;
	/** may be null */
	private Block epilogBlock;
	/** may be null */
	private ISymbol epilogLabel;
	
	/**
	 * 
	 */
	public InstrSelection(LLModule module) {
		this.module = module;
		this.routine = null;
		ssaTempTable = new LinkedHashMap<LLOperand, AssemblerOperand>();;
		InstrSelectionTable.setupPatterns();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterCode(org.ejs.eulang.llvm.directives.LLDefineDirective)
	 */
	@Override
	public boolean enterCode(LLDefineDirective directive) {
		def = directive;
		routine = new LinkedRoutine(def);
		locals = routine.getLocals();
		typeEngine = def.getTarget().getTypeEngine();
		cc = def.getTarget().getCallingConvention(def.getConvention());

		newRoutine(routine);
		locals.buildLocalTable();
		
		blockMap = new LinkedHashMap<ISymbol, Block>();
		ssaTempTable.clear();

		if (def.flags().contains(LLDefineDirective.MULTI_RET)) {
			epilogLabel = locals.getScope().add("$exit", true);
			epilogLabel.setType(typeEngine.LABEL);
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#exitCode(org.ejs.eulang.llvm.directives.LLDefineDirective)
	 */
	@Override
	public void exitCode(LLDefineDirective directive) {
		super.exitCode(directive);
		
		if (def.flags().contains(LLDefineDirective.MULTI_RET)) {
			// although we generate only one return, the optimizer may add more,
			// so divert all returns to the tail block
			
			epilogBlock = new Block(new Label(epilogLabel.getUniqueName()));
			
			newBlock(epilogBlock);
			routine.addBlock(epilogBlock);
			routine.setExit(epilogBlock);
		}
		
		AsmInstruction epilog = AsmInstruction.create(Pepilog);
		List<ISymbol> retSyms = new ArrayList<ISymbol>();
		for (ILocal local : locals.getAllLocals()) {
			if (local.isOutgoing())
				retSyms.add(local.getName());
		}
		epilog.setImplicitSources((ISymbol[]) retSyms.toArray(new ISymbol[retSyms.size()]));
		emitInstr(epilog);
		
		AsmInstruction[] rets = routine.generateReturn();
		for (AsmInstruction ret : rets) {
			emitInstr(ret);
		}
		
		
		if (DUMP) {
			System.out.println("SSA Temp Table:");
			for (Map.Entry<LLOperand, AssemblerOperand> entry : ssaTempTable.entrySet()) {
				System.out.println("\t" + entry.getKey() + " [" + entry.getKey().getType() + "] -> " + entry.getValue());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterBlock(org.ejs.eulang.llvm.LLBlock)
	 */
	@Override
	public boolean enterBlock(LLBlock llblock) {
		ISymbol lllabelSym = llblock.getLabel();
		Label label = new Label(lllabelSym.getUniqueName());
		this.llblock = llblock;
		block = new Block(label);
		
		newBlock(block);
		routine.addBlock(block);
		
		if (blockMap.isEmpty()) {
			AsmInstruction inst = AsmInstruction.create(Pprolog);
			emitInstr(inst);
			routine.setEntry(block);
		}
		
		blockMap.put(lllabelSym, block);

		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#exitBlock(org.ejs.eulang.llvm.LLBlock)
	 */
	@Override
	public void exitBlock(LLBlock llblock) {
		this.llblock = null;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterInstr(org.ejs.eulang.llvm.instrs.LLInstr)
	 */
	@Override
	public boolean enterInstr(LLBlock block, LLInstr instr) {
		if (DUMP) {
			System.out.println();
			System.out.println("; " + instr);
		}
		
		asmOps[0] = asmOps[1] = asmOps[2] = null;
		this.instr = instr;
		regPair = null;
		
		if (instr instanceof LLAllocaInstr)
			return false;
		
		if (instr instanceof LLLoadInstr) {
			handleLoadInstr((LLLoadInstr) instr);
			return false;
		}
		if (instr instanceof LLStoreInstr) {
			handleStoreInstr((LLStoreInstr) instr);
			return false;
		}
		else if (instr instanceof LLCallInstr) {
			handleCallInstr((LLCallInstr) instr);
			return false;
		}
		else if (instr instanceof LLRetInstr) {
			handleRetInstr((LLRetInstr) instr);
			return false;
		} 
		else if (instr instanceof LLUncondBranchInstr) {
			handleUncondBranchInstr((LLUncondBranchInstr) instr);
			return false;
		} 
		else if (instr instanceof LLBranchInstr) {
			handleBranchInstr((LLBranchInstr) instr);
			return false;
		} 
		else if (instr instanceof LLGetElementPtrInstr) {
			handleGetElementPtrInstr((LLGetElementPtrInstr) instr);
			return false;
		}
		else if (instr instanceof LLInsertValueInstr) {
			handleInsertValueInstr((LLInsertValueInstr) instr);
			return false;
		}
		else if (instr instanceof LLExtractValueInstr) {
			handleExtractValueInstr((LLExtractValueInstr) instr);
			return false;
		}
		else if (instr.getName().equals("phi")) {
			handlePhiInstr((LLAssignInstr) instr);
			return false;
		}
		
		if (instr instanceof LLTypedInstr) {
			LLTypedInstr typed = (LLTypedInstr) instr;
			int bitMask = 0;
			switch (typed.getType().getBits()) {
			case 1:
				bitMask = I1;
				break;
			case 8:
				bitMask = I8;
				break;
			case 16:
				bitMask = I16;
				break;
			case 32:
				bitMask = I32;
				break;
			default:
				assert false;
			}
			
			BasicType basicType = typed.getType().getBasicType();
			if (isIntType(typed.getType()))
				basicType = BasicType.INTEGRAL;
			
			Pair<Pair<BasicType, Integer>, String> key = new Pair<Pair<BasicType, Integer>, String>(
					new Pair<BasicType, Integer>(basicType, bitMask), instr.getName());
			List<IPattern> patterns = InstrSelectionTable.patternMap.get(key);
			if (patterns != null) {
				for (IPattern pattern : patterns) {
					if (matches(pattern, bitMask, typed)) {
						thePattern = pattern;
						return true;
					}
				}
			}
			for (IPattern pattern : InstrSelectionTable.otherPatterns) {
				if (matches(pattern, bitMask, typed)) {
					thePattern = pattern;
					return true;
				}
			}
		}
		assert false : "unhandled instr " + instr;
		return false;
	}

	private void emitInstr(AsmInstruction inst) {
		if (inst.getInst() == Ili && isZero(inst.getOp2()))
			emit(AsmInstruction.create(Iclr, inst.getOp1()));
		else if (!isNoOp(inst))
			emit(inst);
	}


	static class Mismatch extends Exception {
		private static final long serialVersionUID = -2512539517516164144L;
		public static final Mismatch INSTANCE = new Mismatch();
	};
	/**
	 * @param pattern
	 * @param typed
	 * @return
	 */
	private boolean matches(IPattern pattern, int bitMask, LLTypedInstr typed) {
		if (!pattern.llInst.equals(typed.getName()))
			return false;
		if (!(pattern.basicType == null || pattern.basicType.equals(typed.getType().getBasicType()))
			&& (pattern.typeMask & bitMask) == 0)
			return false;

		int opidx = 0;
		for (int i = 0; i < pattern.opconds.length; ) {
			try {
				i = execMatch(pattern.opconds, typed, opidx, i);
			} catch (Mismatch e) {
				return false;
			}
			opidx++;
		}

		return true;
	}

	private int execMatch(If[] opconds, LLInstr instr, int opidx, int i) throws Mismatch {
		LLOperand[] operands = instr.getOperands();
		
		LLOperand op = opidx < operands.length ? operands[opidx] : null;
		If opcond = opconds[i++];
		
		if (opcond == If.AND) {
			i = execMatch(opconds, instr, opidx, i++);
			i = execMatch(opconds, instr, opidx, i++);
		} else {
			if (!matches(opcond, instr, op)) {
				throw Mismatch.INSTANCE;
			}
		}
		return i;
	}

	private boolean matches(If opcond, LLInstr instr, LLOperand op) {
		switch (opcond) {
		case PASS:
			return true;
			
		case IS_CONST:
			return op instanceof LLConstOp;
			
		case IS_CONST_0:
			return op instanceof LLConstOp && isInt((LLConstOp) op, 0, 0);
		case IS_CONST_1:
			return op instanceof LLConstOp && isInt((LLConstOp) op, 1, 1);
		case IS_CONST_2:
			return op instanceof LLConstOp && isInt((LLConstOp) op, 2, 2);
		case IS_CONST_N1:
			return op instanceof LLConstOp && isInt((LLConstOp) op, -1, -1);
		case IS_CONST_N2:
			return op instanceof LLConstOp && isInt((LLConstOp) op, -2, -2);
		case IS_CONST_1_15:
			return op instanceof LLConstOp && isInt((LLConstOp) op, 1, 15);
		case IS_CONST_16:
			return op instanceof LLConstOp && isInt((LLConstOp) op, 16, 16);
		case IS_CONST_POW_2:
			return op instanceof LLConstOp && isIntPow2((LLConstOp) op);

		//case IS_TEMP_LAST_USE:
		case IN_PHYS_REG: 
		case IN_PHYS_REG_0: 
		case IN_REG_LOCAL:
		{
			ILocal local = locals.getFinalLocal(op);
			RegisterLocal regLocal = null;
			if (local instanceof RegisterLocal)
				regLocal = (RegisterLocal) local;
			else
				return false;
			
			if (opcond == If.IN_PHYS_REG) {
				return regLocal.isPhysReg();
			}
			else if (opcond == If.IN_PHYS_REG_0) {
				return regLocal.getVr() == 0;
			}
			else if (opcond == If.IN_REG_LOCAL) {
				return true;
			}
			/*
			else if (opcond == If.IS_TEMP_LAST_USE) {
				return isLastUse(op);
			} 
			*/
			else
				assert false;
		}
		
		case ON_STACK:
		case IN_MEMORY:
		{
			ILocal local = locals.getFinalLocal(op);
			if (local instanceof StackLocal)
				return true;
			
			if (opcond == If.ON_STACK) {
				return false;
			} else if (opcond == If.IN_MEMORY) {
				return local instanceof RegisterLocal;
			}
			else
				assert false;
		}
			
		case IS_INT:
			return op != null ? isIntOp(op) 
					: isIntType(((LLCastInstr) instr).getToType());
		case IS_I8:
			return op != null ? isIntOp(op, 8) 
					: isIntType(((LLCastInstr) instr).getToType(), 8);
			
		case IS_I16:
			return op != null ? isIntOp(op, 16) 
					: isIntType(((LLCastInstr) instr).getToType(), 16);
			
		case IS_I1:
			return op != null ? isIntOp(op, 1) 
					: isIntType(((LLCastInstr) instr).getToType(), 1);
			
		default:
			assert false;
		}

		return false;
	}

	/**
	 * @param type
	 * @param i
	 * @return
	 */
	private boolean isIntType(LLType type, int i) {
		if (type.getBits() != i)
			return false;
		return isIntType(type);
		
	}

	private boolean isIntType(LLType type) {
		if (!(type.getBasicType() == BasicType.INTEGRAL
				|| type.getBasicType() == BasicType.POINTER
				|| type.getBasicType() == BasicType.BOOL))
			return false;
		return true;
	}

	private boolean isIntPow2(LLConstOp op) {
		if (!isIntType(op.getType()))
			return false;
		long val = op.getValue().longValue();
		while ((val & 1) == 0) {
			val = (val >>> 1);
		}
		return val == 1;
	}
	
	private boolean isInt(LLConstOp op, int i, int j) {
		if (!isIntType(op.getType()))
			return false;
		return op.getValue().longValue() >= i && op.getValue().longValue() <= j; 
	}


	private void handleLoadInstr(LLLoadInstr instr) {
		LLOperand src = instr.getOperands()[0];
		ILocal local = locals.getFinalLocal(src);
		if (local != null && local.getType().equals(instr.getType())) {
			//if (!asmOp.isMemory() && !asmOp.isRegister()) {
			AssemblerOperand asmOp = generateOperand(src);
			asmOp = generateGeneralOperand(src, asmOp);
			ssaTempTable.put(instr.getResult(), asmOp);
		} else {
			AssemblerOperand asmOp = generateOperand(src);
			if (!asmOp.isMemory())
				asmOp = asmOp instanceof IRegisterOperand ? new RegIndOperand(asmOp) : new AddrOperand(asmOp); 
			AssemblerOperand tmpOp = moveToTemp(null, instr.getType(), asmOp);
			ssaTempTable.put(instr.getResult(), tmpOp);
		}
	}

	private void handleStoreInstr(LLStoreInstr instr) {
		LLOperand src = instr.getOperands()[0];
		AssemblerOperand srcOp = generateOperand(src);

		LLOperand dst = instr.getOperands()[1];
		ILocal dstLocal = locals.getFinalLocal(dst);

		if (srcOp instanceof SymbolOperand) {
			// storing an address into the destination
			ILocal local = locals.getFinalLocal(src);
			srcOp = generateGeneralOperand(src, srcOp);
			if (local != null && !local.getType().equals(src.getType())) {
				AssemblerOperand dstOp = generateOperand(dst);
				dstOp = generateGeneralOperand(dst, dstOp);
	
				getEffectiveAddress(instr.getType(), srcOp, dstOp);
				return;
			}
		}
		else if (srcOp instanceof NumberOperand) {
			// ok
		}
		else  {
			srcOp = generateGeneralOperand(src, srcOp);
		}
		
		AssemblerOperand dstOp;
		if (dstLocal != null && dstLocal.getType().equals(instr.getType())) {
			dstOp = generateOperand(dst);
			dstOp = generateGeneralOperand(dst, dstOp);
		} else {
			dstOp = generateOperand(dst);
			if (!dstOp.isMemory() && !dstOp.isRegister())
				dstOp = generateGeneralOperand(dst, dstOp);
			else if (dstOp instanceof AsmOperand) {
				LLType dstType = ((AsmOperand) dstOp).getType();
				if (!dstType.matchesExactly(dst.getType().getSubType()))
					dstOp = dstOp instanceof IRegisterOperand ? new RegIndOperand(dstOp) : new AddrOperand(dstOp);
			}
		}
		if (dstOp.isMemory() && srcOp instanceof NumOperand)
			srcOp = generateGeneralOperand(src, srcOp);
		moveTo(src, instr.getType(), srcOp, dstOp);
	}
	
	/**
	 * @param llinst  
	 */
	private void handlePhiInstr(LLAssignInstr llinst) {
		assert false;
	}

	private void handleBranchInstr(LLBranchInstr instr) {
		LLOperand[] llops = instr.getOperands();
		
		AssemblerOperand test = generateOperand(llops[0]);
		LLSymbolOp trueTarget = (LLSymbolOp) llops[1];
		LLSymbolOp falseTarget = (LLSymbolOp) llops[2];
		AssemblerOperand trueOp = new SymbolLabelOperand(trueTarget.getType(), trueTarget.getSymbol());
		AssemblerOperand falseOp = new SymbolLabelOperand(falseTarget.getType(), falseTarget.getSymbol());
		
		AsmInstruction inst = AsmInstruction.create(Pjcc, test, trueOp, falseOp);
		inst.setImplicitTargets(new ISymbol[0]);
		emitInstr(inst);
	}
	private void handleUncondBranchInstr(LLUncondBranchInstr instr) {
		LLOperand[] llops = instr.getOperands();
		
		LLSymbolOp target = (LLSymbolOp) llops[0];
		AssemblerOperand asmOp = new SymbolLabelOperand(target.getType(), target.getSymbol());
		
		AsmInstruction inst = AsmInstruction.create(Ijmp, asmOp);
		emitInstr(inst);
	}
	private void handleRetInstr(LLRetInstr instr) {
		LLOperand[] llops = instr.getOperands();
		if (llops.length == 0) {
			
		} else if (llops.length == 1) {
			LLOperand operand = llops[0];
			AssemblerOperand asmOp = generateOperand(operand);
			
			Location[] retLocs = cc.getReturnLocations();
			for (Location retLoc : retLocs) {
				if (retLoc instanceof CallerStackLocation) {
					CallerStackLocation stackLoc = (CallerStackLocation) retLoc;
					
					ISymbol retName = locals.getScope().get(stackLoc.name);
					assert retName != null;
					
					// should be a pointer
					RegisterLocal local = (RegisterLocal) locals.getFinalLocal(retName);
					assert local != null;
					
					RegTempOperand retRegOp = new RegTempOperand(operand.getType(), local);
					AssemblerOperand mem = new RegIndOperand(retRegOp);
					moveTo(llops[0], stackLoc.type.getSubType(), asmOp, mem);
					
					local.setOutgoing(true);
				} else if (retLoc instanceof RegisterLocation) {
					RegisterLocation regLoc = (RegisterLocation) retLoc;
					assert regLoc.bitOffset == 0;
					AssemblerOperand ret = copyIntoRegister(operand, instr, asmOp, regLoc.number);
					ILocal local = ((ISymbolOperand) ret).getLocal();
					local.setOutgoing(true);
				}
			}
			
		} else {
			assert false;
		}
		
		if (def.flags().contains(LLDefineDirective.MULTI_RET)) {
			// we generate only one return, but the optimizer may add more
			AsmInstruction inst = AsmInstruction.create(Ijmp, new SymbolLabelOperand(typeEngine.LABEL, epilogLabel));
			emitInstr(inst);
		}
		
	}
	private void handleCallInstr(LLCallInstr llinst) {
		LLBaseDirective directive = null;
		
		LLOperand function = llinst.getFunction();
		if (function instanceof LLSymbolOp) {
			LLSymbolOp symOp = (LLSymbolOp) function;
			if (isIntrinsic(symOp, ITarget.Intrinsic.SHIFT_RIGHT_CIRCULAR)) {
				LLInstr instr = new LLBinaryInstr("src", llinst.getResult(), llinst.getType(), 
						llinst.getOperands()[0], llinst.getOperands()[1]);
				instr.setNumber(llinst.getNumber());
				instr.accept(llblock, this);
				return;
			}
			if (isIntrinsic(symOp, ITarget.Intrinsic.SHIFT_LEFT_CIRCULAR)) {
				LLInstr instr = new LLBinaryInstr("slc", llinst.getResult(), llinst.getType(), 
						llinst.getOperands()[0], llinst.getOperands()[1]);
				instr.setNumber(llinst.getNumber());
				instr.accept(llblock, this);
				return;
			}
			
			directive = module.lookup(symOp.getSymbol());
		} 

		// TODO: unify the types here
		FunctionConvention fconv = null;
		if (directive instanceof LLDeclareDirective)
			fconv = ((LLDeclareDirective) directive).getConvention();
		else if (directive instanceof LLDefineDirective)
			fconv = ((LLDefineDirective) directive).getConvention();
		
		if (fconv == null) {
			LLType fType = function.getType();
			if (fType instanceof LLPointerType)
				fType = fType.getSubType();
			fconv = FunctionConvention.create(typeEngine, null, (LLCodeType) fType);
		}
		
		ICallingConvention cconv = def.getTarget().getCallingConvention(fconv);
		
		LLOperand[] ops = llinst.getOperands();
		Location[] argLocs = cconv.getArgumentLocations();
		
		int stackSpace = 0;
		for (int i = 0; i < argLocs.length; i++) {
			if (argLocs[i] instanceof ICallingConvention.StackBarrierLocation) {
				ICallingConvention.StackBarrierLocation stackLoc = (StackBarrierLocation) argLocs[i];
				stackSpace = stackLoc.getPushedArgumentsSize();
			}
		}

		NumberOperand sp = new NumberOperand(def.getTarget().getSP());
		
		// adjust stack
		if (stackSpace == 1 || stackSpace == 2) {
			emitInstr(AsmInstruction.create(Idect, new RegisterOperand(sp)));
		} else if (stackSpace > 2) {
			emitInstr(AsmInstruction.create(Iai, new RegisterOperand(sp), new NumberOperand(-stackSpace)));
		}
		
		Map<Integer, AssemblerOperand> callerRets = new HashMap<Integer, AssemblerOperand>();
		List<ISymbol> sources = new ArrayList<ISymbol>();

		int argIdx = 0;
		for (int i = 0; i < argLocs.length; i++) {
			if (argLocs[i] instanceof CallerStackLocation) {
				// allocate space for return value
				CallerStackLocation stackLoc = (CallerStackLocation) argLocs[i];
				ISymbol retName = locals.getScope().add(stackLoc.name, true);
				
				assert stackLoc.type instanceof LLPointerType;
				LLType objType = stackLoc.type.getSubType();
				
				retName.setType(objType);
				StackLocal local = locals.allocateLocal(retName, objType);
				AssemblerOperand asmOp = new StackLocalOperand(llinst.getResult().getType(),  local);
				
				// make tmp pointing to local for the arg
				ISymbol retAddrName = locals.getScope().add(local.getName().getName() + "$p", true);
				retAddrName.setType(stackLoc.type);
				RegisterLocal retAddr = (RegisterLocal) locals.allocateTemp(retAddrName, retAddrName.getType());
				if (!locals.forceToRegister(retAddr, stackLoc.number))
					assert false;
				RegTempOperand ptr = new RegTempOperand(llinst.getResult().getType(), retAddr);
				AddrOperand asmAddrOp = new AddrOperand(asmOp);
				emitInstr(AsmInstruction.create(Plea, asmAddrOp, ptr));
				
				if (objType instanceof LLAggregateType) {
					// return is a tuple
					TupleTempOperand tup = new TupleTempOperand(objType);
					LLAggregateType agg = (LLAggregateType) objType;
					Alignment align = typeEngine.new Alignment(Target.STACK);
					for (int j = 0; j < agg.getCount(); j++) {
						LLType comp = agg.getType(j);
						int offs = align.alignAndAdd(comp);
						tup = tup.put(j, new StackLocalOffsOperand(comp, new NumberOperand(offs / 8), 
								asmOp));
					}
					asmOp = tup;
				}
				callerRets.put(stackLoc.number, asmOp);

			}
			else if (argLocs[i] instanceof RegisterLocation) {
				AssemblerOperand arg = generateOperand(ops[argIdx]);
				RegisterLocation regLoc = (RegisterLocation) argLocs[i];
				assert regLoc.bitOffset == 0;
				arg = copyIntoRegister(ops[argIdx], llinst, arg, regLoc.number);
				argIdx++;
				addSymbol(sources, arg);
			}
			else if (argLocs[i] instanceof StackLocation) {
				AssemblerOperand arg = generateOperand(ops[argIdx]);
				StackLocation stackLoc = (StackLocation) argLocs[i];
				AssemblerOperand stackOp = new RegOffsOperand(
						new NumberOperand(stackLoc.offset + stackSpace),
						sp);
				moveTo(ops[argIdx], ops[argIdx].getType(), arg, stackOp);
				argIdx++;
				addSymbol(sources, stackOp);
			}
			else if (argLocs[i] instanceof StackBarrierLocation) {
				// ignore
			}
			else
				assert false;
		}
		
		// now call
		AssemblerOperand func = generateOperand(function);
		
		// TODO: other func types
		if (function.getType().getBasicType() == BasicType.POINTER && 
				!func.isRegister()) {
			func = moveToTemp(function, function.getType(), func);
		}
		
		addSymbol(sources, func);
		
		AsmInstruction blInst = AsmInstruction.create(Ibl, func instanceof IRegisterOperand ? new RegIndOperand(func) : new AddrOperand(func));
		
		List<ISymbol> targets = new ArrayList<ISymbol>();
		Location[] retLocs = cconv.getReturnLocations();
		for (int i = 0; i < retLocs.length; i++) {
			if (retLocs[i] instanceof CallerStackLocation) {

				ICallingConvention.CallerStackLocation stackLoc = (CallerStackLocation) argLocs[i];
				AssemblerOperand callerRet = callerRets.get(stackLoc.number);
				addSymbol(targets, callerRet);
			}
			else if (retLocs[i] instanceof RegisterLocation) {
				RegisterLocation regLoc = (RegisterLocation) retLocs[i];
				
				RegisterOperand retOp = new RegisterOperand(new NumberOperand(regLoc.number));
				addSymbol(targets, retOp);
			}
			else 
				assert false;
		}
		
		blInst.setImplicitTargets((ISymbol[]) targets.toArray(new ISymbol[targets.size()]));
		blInst.setImplicitSources((ISymbol[]) sources.toArray(new ISymbol[sources.size()]));
		
		emitInstr(blInst);
		
		routine.setHasBlCalls(true);
		
		// handle the return value
		LLOperand result = ((LLAssignInstr) instr).getResult();
		for (int i = 0; i < retLocs.length; i++) {
			if (retLocs[i] instanceof CallerStackLocation) {

				ICallingConvention.CallerStackLocation stackLoc = (CallerStackLocation) argLocs[i];
				AssemblerOperand callerRet = callerRets.get(stackLoc.number);

				asmOps[0] = callerRet;
				addSymbol(targets, callerRet);
				if (result != null)
					ssaTempTable.put(result, callerRet);
				
			}
			else if (retLocs[i] instanceof RegisterLocation) {
				RegisterLocation regLoc = (RegisterLocation) retLocs[i];
				
				RegisterOperand retOp = new RegisterOperand(new NumberOperand(regLoc.number));
				RegisterLocal regLocal = newTempRegister(routine, instr, getTempSymbol(result), regLoc.type);
				RegTempOperand asmOp = new RegTempOperand(llinst.getResult().getType(), regLocal);
				moveTo(result, regLoc.type, retOp, asmOp);
				
				asmOps[0] = asmOp;
				addSymbol(targets, asmOp);
				ssaTempTable.put(result, asmOp);
			}
			else 
				assert false;
		}
		
		blInst.setImplicitTargets((ISymbol[]) targets.toArray(new ISymbol[targets.size()]));
	}

	/**
	 * @param targets
	 * @param asmOp
	 */
	private void addSymbol(List<ISymbol> syms, AssemblerOperand asmOp) {
		if (asmOp instanceof ISymbolOperand) {
			ISymbol sym = ((ISymbolOperand) asmOp).getSymbol();
			if (sym != null)
				syms.add(sym);
		}
	}

	private boolean isIntrinsic(LLSymbolOp symOp, Intrinsic intrinsic) {
		LLType type = symOp.getType();
		if (type instanceof LLCodeType)
			type = ((LLCodeType) type).getRetType();
		return symOp.getSymbol().equals(def.getTarget().getIntrinsic(def, intrinsic, type));
	}

	/**
	 * 
	 */
	private void handleGetElementPtrInstr(LLGetElementPtrInstr instr) {
		LLOperand[] ops = instr.getOperands();
		
		LLType type = ops[0].getType();
		AssemblerOperand asmOp = generateOperand(ops[0]);
		int offs = 0;
		
		for (int idx = 1; idx < ops.length; idx++) {
			int item = ((LLConstOp) ops[idx]).getValue().intValue();
			if (idx == 1) {
				// step over pointer
				offs += type.getBits() * item / 8;
				type = typeEngine.getRealType(type.getSubType());
				asmOp = generateGeneralOperand(ops[0], asmOp);
			} else {
				if (type instanceof LLDataType) {
					LLDataType dataType = (LLDataType) type;
					LLInstanceField field = dataType.getInstanceFields()[item];
					type = field.getType();
					assert field.getOffset() % 8 == 0;
					offs += field.getOffset() / 8;
				} else if (type instanceof LLArrayType) {
					LLArrayType arrayType = (LLArrayType) type;
					type = arrayType.getSubType();
					offs += type.getBits() * item / 8;
				} else {
					assert false;
				}
			}
		}
		
		if (offs != 0) {
			if (!asmOp.isRegister())
				asmOp = getEffectiveAddress(typeEngine.getPointerType(type), asmOp, asmOp);
			asmOp = new RegTempOffsOperand(type, new NumberOperand(offs), 
					asmOp);
		}
		asmOps[0] = asmOp;
		ssaTempTable.put(instr.getResult(), asmOp);
	}


	/**
	 * 
	 * @param instr
	 */
	private void handleExtractValueInstr(LLExtractValueInstr instr) {
		// get current base
		AssemblerOperand aggOp = generateOperand(instr.getOperands()[0]);
		AssemblerOperand asmOp = null;
		if (aggOp instanceof TupleTempOperand) {
			TupleTempOperand op = (TupleTempOperand) aggOp;
			assert op != null;
			asmOp = op.get(instr.getIndex());
		} else if (aggOp instanceof AddrOperand) {
			// not straight-line code; get value from local
			Alignment align = typeEngine.new Alignment(Target.STACK);
			int offs = 0;
			for (int i = 0; i <= instr.getIndex(); i++)
				offs = align.alignAndAdd(((LLAggregateType) instr.getOperands()[0].getType()).getType(i));
			assert offs % 8 == 0;
			offs /= 8;
			
			asmOp = getEffectiveAddress(typeEngine.getPointerType(instr.getType()), aggOp, aggOp);
			asmOp = new RegTempOffsOperand(instr.getType(), new NumberOperand(offs), 
					asmOp);
		} else {
			assert false;
		}
		AssemblerOperand val = moveToTemp(instr.getResult(), instr.getResult().getType(), asmOp);
		ssaTempTable.put(instr.getResult(), val);
	}

	/**
	 * When inserting values, we don't actually duplicate the entirety of
	 * the possibly huge tuple.  Instead, we track individual temps
	 * for every component of the tuple and keep track of the state of
	 * temps for each temp. 
	 * @param instr
	 */
	private void handleInsertValueInstr(LLInsertValueInstr instr) {
		// calculate new piece
		AssemblerOperand val = generateOperand(instr.getElement());
		
		// get current base
		TupleTempOperand op = (TupleTempOperand) generateOperand(instr.getOperands()[0]);
		if (op == null) {
			op = new TupleTempOperand(instr.getType());
		}
		op = op.put(instr.getIndex(), val);
		
		ssaTempTable.put(instr.getResult(), op);
	}

	@Override
	public boolean enterOperand(LLInstr instr, int num, LLOperand operand) {
		assert thePattern != null;
		
		handleAs(num, operand);
		return false;
	}

	private void handleAs(int num, LLOperand operand) {

		if (num >= thePattern.ases.length) {
			AssemblerOperand asmOp = operand != null ? generateOperand(operand) : null;
			asmOps[num] = asmOp;
			return;
		}
		As as = thePattern.ases[num];
		
		if (as == As.IGNORE) {
			asmOps[num] = new NumOperand(operand.getType(), 0);
			return;
		}
		
		AssemblerOperand asmOp = operand != null ? generateOperand(operand) : null;

		//boolean isByte = !(instr instanceof LLCastInstr) && ((LLTypedInstr) instr).getType().getBits() <= 8;
		LLType type = !(instr instanceof LLStoreInstr) && operand != null ? operand.getType() : ((LLTypedInstr) instr).getType();
		switch (as) {
		case SELF:
			break;
		case GEN_R:
		case GEN_W:
			if (asmOp != null) {
				asmOp = generateGeneralOperand(operand, asmOp);
			} else {
				// make a new temp
				ILocal temp = locals.allocateTemp(getTempSymbol(operand), type);
				if (temp instanceof RegisterLocal)
					asmOp = new RegTempOperand(operand.getType(), (RegisterLocal) temp);
				else
					asmOp = new StackLocalOperand(operand.getType(), (StackLocal) temp);
			}
			break;
		case GEN_RW:
			asmOp = generateGeneralOperand(operand, asmOp);
			//if (!isFirstUse(operand)) {
			//	if (!isLastUse(operand) || !isLastUse(asmOp))
			if (operandNeedsTemp(operand, asmOp)) {
					asmOp = moveToTemp(operand, operand.getType(), asmOp);
			}
			break;
		case REG_R:
			asmOp = generateRegisterOperand(operand, asmOp);
			break;
		case REG_RW_DUP:
		case REG_RW:
			asmOp = generateRegisterOperand(operand, asmOp);
			//if (!isFirstUse(operand)) {
			//	if (!isLastUse(operand) || !isLastUse(asmOp))
			if (operandNeedsTemp(operand, asmOp)) {
					asmOp = moveToTemp(operand, operand.getType(), asmOp);
			}
			if (as == As.REG_RW_DUP) {
				AssemblerOperand copy = moveToTemp(operand, operand.getType(), asmOp);
				emit(AsmInstruction.create(InstructionTable.Iswpb, copy));
				emit(AsmInstruction.create(InstructionTable.Imovb, asmOp, copy));
				asmOp = copy;
			}
			break;
		case REG_W:
			if (asmOp != null) {
				// just be sure it's a reg
				asmOp = generateRegisterOperand(operand, asmOp);
			} else {
				// make a new temp
				RegisterLocal temp = newTempRegister(routine, instr, getTempSymbol(operand), type);
				asmOp = new RegTempOperand(((LLAssignInstr) instr).getResult().getType(), temp);
			}
			break;
		case REG_0_CNT_W:
			asmOp = copyIntoRegister(operand, instr, asmOp, 0);
			if (operand.getType().getBits() <= 8) {
				emitInstr(AsmInstruction.create(Iswpb, asmOp));
			}
			break;

		case REG_HI_W: 
			asmOp = copyIntoRegPair(operand, instr, asmOp, true);
			break;
		case REG_LO_W:
			asmOp = copyIntoRegPair(operand, instr, asmOp, false);
			break;
			
			
		case CONST_POOL:
			assert asmOp instanceof NumOperand;
			asmOp = new ConstPoolRefOperand(asmOp);
			break;
		case IMM:
			assert asmOp instanceof NumOperand;
			if (type.getBits() <= 8)
				asmOp = new NumOperand(typeEngine.INT, (((NumOperand) asmOp).getValue() << 8) & 0xff00);

			break;
		case IMM_NEG:
			assert asmOp instanceof NumOperand;
			asmOp = new NumOperand(typeEngine.INT, -((NumOperand) asmOp).getValue());
			break;
		case IMM_NEG_15: {
			assert asmOp instanceof NumOperand;
			int mask = 0xf; 
			asmOp = new NumOperand(typeEngine.INT, (-((NumOperand) asmOp).getValue()) & mask);
			break;
		}
		case IMM_LOG_2: {
			assert asmOp instanceof NumOperand && isIntPow2((LLConstOp) operand);
			int log = 0;
			int val = ((LLConstOp) operand).getValue().intValue();
			while (val != 1) {
				val >>>= 1;
				log++;
			}
			asmOp = new NumOperand(typeEngine.INT, log);
			break;
		}
			
		case IMM_15:
			asmOp = new NumOperand(typeEngine.INT, 15);
			break;
		case IMM_8:
			asmOp = new NumOperand(typeEngine.INT, 8);
			break;
		case IMM_1:
			asmOp = new NumOperand(typeEngine.INT, 1);
			break;
		case IMM_0:
			asmOp = new NumOperand(typeEngine.INT, 0);
			break;
		case IMM_N1:
			asmOp = new NumOperand(typeEngine.INT, -1);
			break;
		case CMP: {
			assert instr instanceof LLCompareInstr;
			asmOp = new CompareOperand(((LLCompareInstr) instr).getCmp());
			break;
		}
		default:
				assert false;
		}
		
		asmOps[num] = asmOp;
	}

	/**
	 * When using an operand for both reads and writes, it must live in a 
	 * general operand.  Also, we must not modify an SSA value. 
	 * @param operand
	 * @param asmOp
	 * @return
	 */
	private boolean operandNeedsTemp(LLOperand operand, AssemblerOperand asmOp) {
		// when defining a value, certainly it needs no new temp
		if (isDefinition(operand) && isGeneralOperand(asmOp)) 
			return false;
		if ((ssaTempTable.containsKey(operand) || asmOpMatchesTemp(operand, asmOp)))
			return true;
		return !isGeneralOperand(asmOp);
	}

	private boolean asmOpMatchesTemp(LLOperand operand, AssemblerOperand asmOp) {
		if (operand instanceof LLTempOp && asmOp instanceof ISymbolOperand) {
			ILocal local = ((ISymbolOperand) asmOp).getLocal();
			return local.getName().getName().equals(((LLTempOp) operand).getName());
		}
		return false;
	}
	
	/**
	 * Tell if this operand is defined in this instruction
	 */
	private boolean isDefinition(LLOperand operand) {
		return (instr instanceof LLAssignInstr && ((LLAssignInstr) instr).getResult() != null && ((LLAssignInstr) instr).getResult().equals(operand))
		|| (instr instanceof LLStoreInstr && ((LLStoreInstr) instr).getOperands()[1].equals(operand));
	}

	/**
	 * Put the operand into an assembler operand.
	 * @param operand
	 * @return
	 */
	public AssemblerOperand generateOperand(LLOperand operand) {
		AssemblerOperand asmOp = ssaTempTable.get(operand);
		if (asmOp != null)
			return asmOp;
		
		if (operand instanceof LLConstOp) {
			if (isIntOp(operand)) {
				int val = ((LLConstOp) operand).getValue().intValue();
				//if (((LLConstOp) operand).getType().getBits() <= 8)
				//	val = (val << 8) & 0xff00;
				return new NumOperand(operand.getType(), val);
			}
			assert false;
		}
		if (operand instanceof LLTempOp) {
			ILocal local = locals.getFinalLocal(operand);
			return createLocalOperand(operand.getType(), local);
		}
		if (operand instanceof LLSymbolOp) {
			ISymbol symbol = ((LLSymbolOp) operand).getSymbol();
			ILocal local = locals.getFinalLocal(symbol);
			return new SymbolOperand(operand.getType(), symbol, local);
		}
		if (operand instanceof LLStructOp) {
			return generateTupleTempOperand(((LLStructOp) operand));
		}
		if (operand instanceof LLArrayOp) {
			return generateTupleTempOperand(((LLArrayOp) operand));
		}
		if (operand instanceof LLBitcastOp) {
			LLBitcastOp bop = ((LLBitcastOp) operand);
			AssemblerOperand op = generateOperand(bop.getValue());
			if (op instanceof AsmOperand)
				((AsmOperand) op).setType(bop.getType());
			else
				assert false;
			return op;
		}
		if (operand instanceof LLZeroInitOp) {
			if (isIntOp(operand)) {
				return new NumOperand(operand.getType(), 0);
			}
			return new ZeroInitOperand(operand.getType());
		}
		if (operand instanceof LLUndefOp) {
			return null;
		}
		assert false;
		return null;
	}

	private TupleTempOperand generateTupleTempOperand(LLStructOp llOp) {
		if (llOp.getType() instanceof LLAggregateType) {
			LLAggregateType type = (LLAggregateType) llOp.getType();
			LLOperand[] elements = llOp.getElements();
			assert elements.length == type.getCount();
			AssemblerOperand[] ops = new AssemblerOperand[elements.length];
			for (int i= 0; i < elements.length; i++) {
				ops[i] = generateOperand(elements[i]);
			}
			return new TupleTempOperand(type, ops);
		} else {
			assert false;
			return null;
		}
	}
	private TupleTempOperand generateTupleTempOperand(LLArrayOp llOp) {
		if (llOp.getType() instanceof LLArrayType) {
			LLArrayType type = (LLArrayType) llOp.getType();
			LLOperand[] elements = llOp.getElements();
			assert elements.length == type.getArrayCount();
			AssemblerOperand[] ops = new AssemblerOperand[elements.length];
			for (int i= 0; i < elements.length; i++) {
				ops[i] = generateOperand(elements[i]);
			}
			return new TupleTempOperand(type, ops);
		} else {
			assert false;
			return null;
		}
	}

	private AssemblerOperand createLocalOperand(LLType type, ILocal local) {
		if (local instanceof RegisterLocal)
			return new RegTempOperand(type, (RegisterLocal) local);
		else if (local instanceof StackLocal)
			return new AddrOperand(new StackLocalOperand(type, (StackLocal) local));
		else 
			assert false;
		return null;
	}

	private boolean isIntOp(LLOperand operand) {
		return isIntType(operand.getType())
				&& operand.getType().getBits() <= 16;
	}
	private boolean isIntOp(LLOperand operand, int bits) {
		return isIntType(operand.getType())
		&& operand.getType().getBits() == bits;
	}
	private boolean isBoolOp(LLOperand operand) {
		return operand.getType().equals(typeEngine.BOOL);
	}
	private boolean isBoolType(LLType type) {
		return type.equals(typeEngine.BOOL);
	}


	/** Called to fetch a new temp register 
	 * @param routine TODO
	 * @param instr 
	 * @param symbol the symbol on which to base the name 
	 * */
	protected RegisterLocal newTempRegister(Routine routine, LLInstr instr, ISymbol symbol, LLType type) {
		ILocal local = routine.getLocals().allocateTemp(symbol, type);
		if (!(local instanceof RegisterLocal))
			throw new IllegalStateException("cannot force " + symbol + " of " + type + " into a register");
		RegisterLocal regLocal = (RegisterLocal) local;
		return regLocal;
	}
	
	/**
	 * Generate a register operand into the given register number.
	 * @param llOperand 
	 * @param operand
	 * @param num
	 * @param llOperand 
	 */
	private AssemblerOperand copyIntoRegister(LLOperand llOperand, LLInstr instr, AssemblerOperand operand, int num) {
		LLType type = llOperand.getType();
		RegisterLocal regLocal = newTempRegister(routine, instr, 
				getTempSymbol(llOperand), llOperand != null ? type : ((LLTypedInstr) instr).getType());
		if (!locals.forceToRegister(regLocal, num))
			assert false;
		AssemblerOperand ret = new RegTempOperand(type, regLocal);
		if (llOperand != null && !ret.equals(operand)) {
			moveTo(llOperand, type, operand, ret);
		}
		return ret;
	}
	
	private AssemblerOperand copyIntoRegPair(LLOperand llOperand, LLInstr instr, AssemblerOperand operand, boolean high) {
		//LLOperand llOperand = getLLOperand(operand);
		RegisterLocal regLocal = getRegisterPair(llOperand);
		LLOperand llOp = llOperand != null ? llOperand : ((LLAssignInstr) instr).getResult();
		AssemblerOperand ret = new RegTempOperand(llOp.getType(), regLocal, high);
		if (llOperand != null) {
			moveTo(llOp, ((LLAssignInstr) instr).getResult().getType(), operand, ret);
		}
		return ret;
	}

	/**
	 * @param llOperand 
	 * @return
	 */
	private RegisterLocal getRegisterPair(LLOperand llOperand) {
		if (regPair == null) {
			regPair = newTempRegister(routine, instr, getTempSymbol(llOperand), typeEngine.INT);
			regPair.setRegPair(true);
		}
		return regPair;
	}

	private AssemblerOperand generateRegisterOperand(LLOperand llOp, AssemblerOperand operand) {
		//LLOperand llOp = getLLOperand(operand);
		LLType type = llOp.getType(); // getLLType(operand);
		
		if (operand != null && operand.isRegister())
			return operand;

		AssemblerOperand dest = new RegTempOperand(llOp.getType(),
				newTempRegister(routine, instr, getTempSymbol(llOp), type));
		
		if (operand != null)
			return moveTo(llOp, type, operand, dest);
		else
			return dest;
	}
	
	private AssemblerOperand generateGeneralOperand(LLOperand llOp, AssemblerOperand operand) {
		if (operand.isMemory()) {
			return operand;
		}
		if (operand.isRegister()) {
			if (isIntOp(llOp) || isBoolOp(llOp)) {
				return operand;
			}
			assert false;
		}
		if (operand instanceof NumOperand) {
			if (isIntOp(llOp)) {
				RegisterLocal regLocal = newTempRegister(routine, instr, getTempSymbol(llOp), llOp.getType());
				AssemblerOperand ret = new RegTempOperand(llOp.getType(), regLocal);
				if (isIntOp(llOp)) {
					if (llOp.getType().getBits() <= 8)
						operand = new NumOperand(typeEngine.INT, (((NumOperand) operand).getValue() << 8) & 0xff00);

					emitInstr(AsmInstruction.create(Ili, ret, operand));
					/*
				} if (isIntOp(llOp, 16)) {
					emitInstr(AsmInstruction.create(Ili, ret, operand));
				} else if (isIntOp(llOp, 8)) {
					operand = new NumOperand((((NumOperand) operand).getValue() << 8) & 0xFF00);
					emitInstr(AsmInstruction.create(Ili, ret, operand));
					*/
				} else
					assert false;
				return ret;
			} else if (isBoolOp(llOp)) {
				RegisterLocal regLocal = newTempRegister(routine, instr, getTempSymbol(llOp), llOp.getType());
				AssemblerOperand ret = new RegTempOperand(llOp.getType(), regLocal);
				if (((NumOperand) operand).getValue() == 0)
					emitInstr(AsmInstruction.create(Iclr, ret));
				else
					emitInstr(AsmInstruction.create(Iseto, ret));
				return ret;
			}
			assert false;
		}
		if (operand instanceof SymbolOperand) {
			// we're dereferencing it
			ISymbol sym = ((SymbolOperand) operand).getSymbol();
			ILocal local = locals.getFinalLocal(sym);
			if (local instanceof RegisterLocal)
				return new RegTempOperand(llOp.getType(), (RegisterLocal) local);
			else if (local instanceof StackLocal)
				return new AddrOperand(new StackLocalOperand(llOp.getType(), (StackLocal) local));
			else {
				if (sym.getType().matchesExactly(llOp.getType().getSubType())) {
					// get the address in a var
					return moveToTemp(llOp, llOp.getType(), new SymbolOperand(llOp.getType(), sym, local));
				} else {
					return new AddrOperand(new SymbolOperand(llOp.getType(), sym, local));
				}
			}
				
		}
		if (operand instanceof TupleTempOperand) {
			return operand;
		}
		assert false;
		return null;
	}

	private boolean isZero(AssemblerOperand op) {
		return op instanceof NumOperand && ((NumOperand) op).getValue() == 0;
	}

	private AssemblerOperand moveToTemp(LLOperand llOp, LLType type, AssemblerOperand operand) {
		ILocal temp = locals.allocateTemp(getTempSymbol(null), type != null ? type : ((LLTypedInstr) instr).getType());
		AssemblerOperand dest = createLocalOperand(type, temp);
		return moveTo(llOp, type, operand, dest);
	}

	private ISymbol getTempSymbol(LLOperand llOp) {
		String baseName;
		if (llOp instanceof LLTempOp) {
			baseName = ((LLTempOp) llOp).getName();
		} else if (llOp instanceof LLSymbolOp)
			baseName = ((LLSymbolOp) llOp).getSymbol().getUniqueName();
		else if (instr instanceof LLAssignInstr && ((LLAssignInstr) instr).getResult() != null)
			return getTempSymbol(((LLAssignInstr) instr).getResult());
		else
			baseName = "reg";
		ISymbol temp = locals.getScope().add(baseName, true);
		temp.setType(llOp != null ? llOp.getType() : ((LLTypedInstr) instr).getType());
		return temp;
	}

	private AssemblerOperand moveTo(LLOperand llOp, LLType type, AssemblerOperand from, AssemblerOperand dest) {
		if (isIntType(type) || isBoolType(type)) {
			if (from.isRegister() || from.isMemory()) {
				int op = (type.getBits() <= 8) ? Imovb : Imov;
				AsmInstruction inst = AsmInstruction.create(op, from, dest);
				emitInstr(inst);
			} else if (from instanceof NumOperand) {
				if (((NumOperand) from).getType().getBits() <= 8)
					from = new NumOperand(((NumOperand) from).getType(), (((NumOperand) from).getValue() << 8) & 0xff00);

				AsmInstruction inst = AsmInstruction.create(Ili, dest, from);
				emitInstr(inst);
			} else if (from instanceof ISymbolOperand) {
				// when loading the address of a local, just construct an operand if possible
				ILocal fromLocal = ((ISymbolOperand) from).getLocal();
				ILocal dstLocal = null;
				if (dest instanceof ISymbolOperand)
					dstLocal = locals.getLocal(((ISymbolOperand) dest).getSymbol());
				// getting the address
				if (fromLocal != null) {
					if (!from.isMemory() && !from.isRegister()) {
						from = generateGeneralOperand(llOp, from);
					}
					if (dstLocal != null && dstLocal instanceof RegisterLocal) {
						// cheat and avoid new temp
						RegTempOperand ptr = new RegTempOperand(type, (RegisterLocal) dstLocal);
						emitInstr(AsmInstruction.create(Plea, from, ptr));
						from = ptr;
					} else {
						RegisterLocal addr = (RegisterLocal) locals.allocateTemp(type);
						RegTempOperand ptr = new RegTempOperand(type, addr);
						emitInstr(AsmInstruction.create(Plea, from, ptr));
						from = ptr;
					}
				} else {
					AsmInstruction inst = AsmInstruction.create(Ili, dest, from);
					emitInstr(inst);
				}
			} else {
				assert false;
			}
			return dest;
		}
		else {
			// it's a complex op: use a pseudo
			AsmInstruction inst = AsmInstruction.create(Pcopy, from, dest);
			emitInstr(inst);
			return dest;
		}
	}
	
	private AssemblerOperand getEffectiveAddress(LLType type, AssemblerOperand from, AssemblerOperand dest) {
		// when loading the address of a local, just construct an operand if possible
		ILocal fromLocal;
		if (from instanceof ISymbolOperand)
			fromLocal = ((ISymbolOperand) from).getLocal();
		else if (from instanceof AddrOperand)
			fromLocal = ((ISymbolOperand) (((AddrOperand)from).getAddr())).getLocal();
		else
			fromLocal = null;
		
		ILocal dstLocal = null;
		if (dest instanceof ISymbolOperand)
			dstLocal = locals.getFinalLocal(((ISymbolOperand) dest).getSymbol());
		
		if (from instanceof SymbolOperand && fromLocal != null) {
			if (fromLocal instanceof RegisterLocal) {
				dest = new RegTempOperand(type, (RegisterLocal) fromLocal);
			} 
			else if (fromLocal instanceof StackLocal) {
				dest = new StackLocalOperand(type, (StackLocal) fromLocal);
			}
			else 
				assert false;
			return dest;
		}
		else
		{
			// getting the address
			if (fromLocal != null) {
				if (dstLocal != null && dstLocal instanceof RegisterLocal && !dstLocal.equals(fromLocal)) {
					// cheat and avoid new temp
					RegTempOperand ptr = new RegTempOperand(type, (RegisterLocal) dstLocal);
					emitInstr(AsmInstruction.create(Plea, from, ptr));
					dest = ptr;
				} else {
					RegisterLocal addr = (RegisterLocal) locals.allocateTemp(type);
					RegTempOperand ptr = new RegTempOperand(type, addr);
					emitInstr(AsmInstruction.create(Plea, from, ptr));
					dest = ptr;
				}
			} else {
				// getting the address
				if (!dest.isRegister()) {
					RegisterLocal addr = (RegisterLocal) locals.allocateTemp(type);
					dest = new RegTempOperand(type, addr);
				}
				AsmInstruction inst = AsmInstruction.create(Ili, dest, from);
				emitInstr(inst);
			}
		}
		return dest;
		
	}
	private boolean isGeneralOperand(AssemblerOperand asmOp) {
		return asmOp.isRegister() || asmOp.isMemory();
		
	}
	
	/*
	private boolean isLastUse(LLOperand operand) {
		if (!(operand instanceof LLSymbolOp))
			return true;
		ILocal local = locals.getLocal(operand);
		return isLastUse(local);
	}
	private boolean isLastUse(AssemblerOperand operand) {
		if (!(operand instanceof ISymbolOperand))
			return true;
		ISymbol sym = ((ISymbolOperand) operand).getSymbol();
		ILocal local = locals.getLocal(sym);
		return isLastUse(local);
	}
	private boolean isLastUse(ILocal local) {
		if (local != null) {
			List<Integer> list = local.getUses().get(llblock);
			// a temp
			if (list == null)
				return true;
			int indexOf = Collections.binarySearch(list, instr.getNumber());
			if (indexOf < 0)
				// between instructions
				indexOf = -(indexOf + 1);
			if (instr.getNumber() >= list.get(list.size() - 1))
				return !isLocalUsedIn(local, llblock.succ);
		}
		return false;
	}
	private boolean isLocalUsedIn(ILocal local, List<LLBlock> succ) {
		if (succ == null || succ.isEmpty())
			return false;
		for (LLBlock s : succ)
			if (local.getUses().containsKey(s))
				return false;
			else
				return isLocalUsedIn(local, s.succ);
		return false;
	}
	*/
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#exitInstr(org.ejs.eulang.llvm.instrs.LLInstr)
	 */
	@Override
	public void exitInstr(LLBlock block, LLInstr instr) {
		if (thePattern != null) {
			// any leftover ones synthesize operands
			for (int i = instr.getOperands().length; i < thePattern.ases.length; i++) {
				handleAs(i, null);
			}
			for (Do d : thePattern.dos) {
				if (d instanceof DoIntrinsic) {
					// result goes into 0, args are in 1, ...
					assert instr instanceof LLAssignInstr;
					LLAssignInstr assn = (LLAssignInstr) instr;
					ISymbol sym = def.getTarget().getIntrinsic(def, ((DoIntrinsic) d).intrinsic, assn.getType());
					assert sym != null;

					handleCallInstr(new LLCallInstr(assn.getResult(), assn.getType(), 
							new LLSymbolOp(sym), (LLCodeType)sym.getType(), 
							assn.getOperands()));
				}
				else if (d.inst != -1) {
					AssemblerOperand op1 = d.ops.length >= 1 ? getAsmOp(d, 0) : null;
					AssemblerOperand op2 = d.ops.length >= 2 ? getAsmOp(d, 1) : null;
					AssemblerOperand op3 = d.ops.length >= 3 ? getAsmOp(d, 2) : null;
					AsmInstruction inst = AsmInstruction.create(d.inst, op1, op2, op3);
					
					// HACK
					if (inst.getInst() == InstructionTable.Impy || inst.getInst() == InstructionTable.Idiv) {
						inst.setImplicitSources(new ISymbol[] {
								((ISymbolOperand)inst.getOp1()).getSymbol(),
								((ISymbolOperand)inst.getOp2()).getSymbol() });
						inst.setImplicitTargets(new ISymbol[] { 
								((ISymbolOperand)inst.getOp2()).getSymbol(), 
								((ISymbolOperand)inst.getOp3()).getSymbol() });
					}
					emitInstr(inst);
				}


				if (d instanceof DoRes) {
					assert instr instanceof LLAssignInstr;
					
					AssemblerOperand dest = getAsmOp(d, ((DoRes) d).result);
					ssaTempTable.put(((LLAssignInstr) instr).getResult(), dest);
				}
			}
			
		}
	}

	/**
	 * @param d
	 * @param result
	 * @return
	 */
	private AssemblerOperand getAsmOp(Do d, int i) {
		int idx = d.ops[i];
		if (idx < 0) {
			assert instr instanceof LLAssignInstr;
			return generateOperand(((LLAssignInstr) instr).getResult());
		}
		return asmOps[idx];
	}

	/**
	 * Quickly eliminate useless instructions
	 * @param inst
	 * @return
	 */
	private boolean isNoOp(AsmInstruction inst) {
		if (inst.getInst() == Imov || inst.getInst() == Imovb 
				|| inst.getInst() == Pcopy) {
			AssemblerOperand op1 = inst.getOp1();
			AssemblerOperand op2 = inst.getOp2();
			if (op1.equals(op2))
				return true;
		}
		return false;
	}
}
