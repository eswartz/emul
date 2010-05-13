/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import static v9t9.engine.cpu.InstructionTable.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.ICallingConvention;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.ITarget.Intrinsic;
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLCodeVisitor;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.instrs.LLAllocaInstr;
import org.ejs.eulang.llvm.instrs.LLAssignInstr;
import org.ejs.eulang.llvm.instrs.LLBinaryInstr;
import org.ejs.eulang.llvm.instrs.LLCallInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.instrs.LLRetInstr;
import org.ejs.eulang.llvm.instrs.LLTypedInstr;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.ops.LLTempOp;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;

import v9t9.engine.cpu.InstructionTable;
import v9t9.tools.asm.assembler.HLInstruction;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;

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
	
	/** Codes for type checking */
	public static final int I8 = 1;
	public static final int I16 = 2;
	public static final int I1 = 4;
	
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
		
		/** if the LL operand is allocated to a physical register */
		IN_PHYS_REG,
		/** if the LL operand is allocated to physical register 0 */
		IN_PHYS_REG_0,
		/** if the LL operand is allocated to a local which came in as a register local or is in a temp.
		 * Use this when the register might be spilled. */
		IN_REG_LOCAL,
		/** if the LL operand is allocated to a register temp */
		IN_TEMP,
		/** if the LL operand is allocated to a register temp used only in one block */
		IN_REG_BLOCK,

		/** last use of a temporary */
		IS_TEMP_LAST_USE,

		/** if the LL operand is allocated to a stack local (argument from stack or otherwise known or forced to stack) */
		ON_STACK,
		
		/** if the operand is in memory (global, stack, ...) */
		IN_MEMORY,
		
		/** is 8 bit int */
		IS_I8,
		/** is 16 bit int */
		IS_I16,
		/** is 1 bit int */
		IS_I1,
		
	};
	
	enum As {
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
		/** put the LL operand into physical register #0 */
		REG_0_W,
		/** put the LL operand into an immediate */
		IMM,
		/** put the LL operand into an immediate, negated */
		IMM_NEG,
		
		/** reuse the asm operand generated for operand #0 */
		SAME_0,
		/** reuse the asm operand generated for operand #1 */
		SAME_1,
		/** reuse the asm operand generated for operand #2 */
		SAME_2,
		
		/** synthesize immediate 8 */
		IMM_8,
		/** synthesize immediate 1 */
		IMM_1,
		/** synthesize immediate 0 */
		IMM_0,
		/** synthesize immediate -1 */
		IMM_N1,
	};
	
	/** Pseudo-instructions */
	final static int Ipseudo = Iuser;
	final static int Iseteq = Ipseudo + 0,
		Isetne = Ipseudo + 1,
		Isetgt = Ipseudo + 2,
		Isetlt = Ipseudo + 3,
		Isetge = Ipseudo + 4,
		Isetle = Ipseudo + 5,
		Isetov = Ipseudo + 6,
		Isetnov = Ipseudo + 7
	;
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
	
	/** These ll instructions are handled specially; do not make patterns for them */
	private static final Pattern hardcodedInstrs = 
		Pattern.compile("\\b(call|ret|br|select|phi)\\b");
	
	/** Raw patterns.  These are converted at runtime. */ 
	private static final IPattern[] patterns = {
		new IPattern( BasicType.INTEGRAL, I16, "store", 
				new If[] { If.IS_CONST, If.IN_REG_LOCAL },
		 		new As[] { As.IMM, As.REG_RW }, 
		 		new Do( Ili, 1, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "store", 
				new If[] { If.PASS, If.PASS },
				new As[] { As.GEN_R, As.GEN_W }, 
				new Do( Imov, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I8, "store", 
				new If[] { If.PASS, If.PASS },
				new As[] { As.GEN_R, As.GEN_W }, 
				new Do( Imovb, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16 | I8, "load", 
				new If[] { If.PASS },
				new As[] { As.GEN_R },
				new DoRes( 0, -1, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "trunc", 
				new If[] { 
					If.IN_REG_LOCAL,
					If.IS_I8,
				},
				new As[] { As.REG_RW, As.IMM_8 }, 
				new DoRes( 0, Isla, 0, 1 ) 
		),
		new IPattern( BasicType.INTEGRAL, I8, "sext", 
				new If[] { 
					If.PASS,
					If.IS_I16,
				},
				new As[] { As.REG_RW, As.IMM_8 }, 
				new DoRes( 0, Isra, 0, 1 ) 
		),
		new IPattern( BasicType.INTEGRAL, I8, "zext", 
				new If[] { 
					If.PASS,
					If.IS_I16,
				},
				new As[] { As.REG_RW, As.IMM_8 }, 
				new DoRes( 0, Isrl, 0, 1 ) 
			),
		
		new IPattern( BasicType.INTEGRAL, I16|I8, "add", 
				 new If[] { If.PASS, If.IS_CONST },
				 new As[] { As.REG_RW, As.IMM }, 
				 new DoRes( 0, Iai, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "add", 
		 		null,
		 		new As[] { As.GEN_RW, As.GEN_R }, 
		 		new DoRes( 1, Ia, 1, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I8, "add", 
				null,
				new As[] { As.GEN_RW, As.GEN_R }, 
				new DoRes( 1, Iab, 1, 0 )
		),
		
		new IPattern( BasicType.INTEGRAL, I16 | I8, "sub", 
				 new If[] { If.PASS, If.IS_CONST },
				 new As[] { As.REG_RW, As.IMM_NEG }, 
				 new DoRes( 0, Iai, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "sub", 
		 		null,
		 		new As[] { As.GEN_RW, As.GEN_R }, 
		 		new DoRes( 1, Is, 1, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I8, "sub", 
				null,
				new As[] { As.GEN_RW, As.GEN_R }, 
				new DoRes( 1, Isb, 1, 0 )
		),
		
		new IPattern( BasicType.INTEGRAL, I16|I8, "and", 
				 new If[] { If.PASS, If.IS_CONST },
				 new As[] { As.REG_RW, As.IMM }, 
				 new DoRes( 0, Iandi, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "and", 
		 		null,
		 		new As[] { As.GEN_RW, As.GEN_R }, 
		 		new DoRes( 1, Iszc, 1, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I8, "and", 
				null,
				new As[] { As.GEN_RW, As.GEN_R }, 
				new DoRes( 1, Iszcb, 1, 0 )
		),

		new IPattern( BasicType.INTEGRAL, I16|I8, "or", 
				 new If[] { If.PASS, If.IS_CONST },
				 new As[] { As.REG_RW, As.IMM }, 
				 new DoRes( 0, Iori, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "or", 
		 		null,
		 		new As[] { As.GEN_RW, As.GEN_R }, 
		 		new DoRes( 1, Isoc, 1, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I8, "or", 
				null,
				new As[] { As.GEN_RW, As.GEN_R }, 
				new DoRes( 1, Isocb, 1, 0 )
		),
		
		new IPattern( BasicType.INTEGRAL, I16, "xor", 
		 		null,
		 		new As[] { As.REG_RW, As.GEN_R }, 
		 		new DoRes( 1, Ixor, 1, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I8, "xor", 
				null,
				new As[] { As.GEN_RW, As.GEN_R },
				new DoRes( 1, Ixor, 1, 0 )
		),
		
		
		new IPattern( BasicType.INTEGRAL, I16, "shl", 
		 		new If[] { If.PASS, If.IS_CONST_0 },
		 		null
		),
		new IPattern( BasicType.INTEGRAL, I16, "shl", 
				new If[] { If.PASS, If.IS_CONST_16 },
				new As[] { As.IMM_0 },
				new DoRes( 0, -1, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "shl", 
				new If[] { If.PASS, If.IS_CONST_1_15 },
				new As[] { As.REG_RW, As.IMM }, 
				new DoRes( 0, Isla, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "shl", 
		 		new If[] { If.PASS, If.PASS },
		 		new As[] { As.REG_RW, As.REG_0_W },
		 		new DoRes( 0, Isla, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "ashr", 
		 		new If[] { If.PASS, If.IS_CONST_0 },
		 		null
		),
		new IPattern( BasicType.INTEGRAL, I16, "ashr", 
				new If[] { If.PASS, If.IS_CONST_16 },
				new As[] { As.REG_RW, As.REG_0_W },
				new Do( Iclr, 1 ),
				new DoRes( 0, Isra, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "ashr", 
				new If[] { If.PASS, If.IS_CONST_1_15 },
				new As[] { As.REG_RW, As.IMM }, 
				new DoRes( 0, Isra, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "ashr", 
		 		new If[] { If.PASS, If.PASS },
		 		new As[] { As.REG_RW, As.REG_0_W },
		 		new DoRes( 0, Isra, 0, 1 )
		),
		
		new IPattern( BasicType.INTEGRAL, I16, "lshr", 
		 		new If[] { If.PASS, If.IS_CONST_0 },
		 		null
		),
		new IPattern( BasicType.INTEGRAL, I16, "lshr", 
				new If[] { If.PASS, If.IS_CONST_16 },
				new As[] { As.IMM_0 },
				new DoRes( 0, -1, 0  )
		),
		new IPattern( BasicType.INTEGRAL, I16, "lshr", 
				new If[] { If.PASS, If.IS_CONST_1_15 },
				new As[] { As.REG_RW, As.IMM }, 
				new DoRes( 0, Isrl, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "lshr", 
		 		new If[] { If.PASS, If.PASS },
		 		new As[] { As.REG_RW, As.REG_0_W },
		 		new DoRes( 0, Isrl, 0, 1 )
		),
		
		// synthetic instr generated by intrinsic
		new IPattern( BasicType.INTEGRAL, I16, "src", 
		 		new If[] { If.PASS, If.IS_CONST_0 },
		 		null
		),
		new IPattern( BasicType.INTEGRAL, I16, "src", 
		 		new If[] { If.PASS, If.IS_CONST_16 },
		 		null
		),
		new IPattern( BasicType.INTEGRAL, I16, "src", 
				new If[] { If.PASS, If.IS_CONST_1_15 },
				new As[] { As.REG_RW, As.IMM }, 
				new DoRes( 0, Isrc, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "src", 
		 		new If[] { If.PASS, If.PASS },
		 		new As[] { As.REG_RW, As.REG_0_W },
		 		new DoRes( 0, Isrc, 0, 1 )
		),
		
		new IPattern( BasicType.INTEGRAL, I8, "src", 
		 		new If[] { If.PASS, If.IS_CONST_0 },
		 		null
		),
		new IPattern( BasicType.INTEGRAL, I8, "src", 
		 		new If[] { If.PASS, If.IS_CONST_16 },
		 		null
		),
		new IPattern( BasicType.INTEGRAL, I8, "src", 
				new If[] { If.PASS, If.IS_CONST_1_15 },
				new As[] { As.REG_RW_DUP, As.IMM }, 
				new DoRes( 0, Isrc, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I8, "src", 
		 		new If[] { If.PASS, If.PASS },
		 		new As[] { As.REG_RW_DUP, As.REG_0_W },
		 		new DoRes( 0, Isrc, 0, 1 )
		),
		
	};
	
	/** Called to fetch a new temp register 
	 * @param instr TODO*/
	abstract protected RegisterLocal newTempRegister(LLInstr instr, LLType type);
	
	/** Called when an instruction has been generated */
	abstract protected void emit(HLInstruction instr);
	
	private final LLDefineDirective def;
	private final ICallingConvention cc;
	private HashMap<Pair<Pair<BasicType, Integer>, String>, List<IPattern>> patternMap;
	private List<IPattern> otherPatterns;

	private IPattern thePattern;
	private AssemblerOperand[] asmOps = new AssemblerOperand[3];
	
	private final Locals locals;
	private final Routine routine;
	private Block block;
	
	private HashMap<ISymbol, Block> blockMap;
	private HashMap<LLOperand, AssemblerOperand> tempTable;
	private LLInstr instr;
	private LLBlock llblock;
	
	/**
	 * 
	 */
	public InstrSelection(Routine routine) {
		this.routine = routine;
		locals = routine.getLocals();
		def = routine.getDefinition();
		this.cc = def.getTarget().getCallingConvention(def.getConvention());
		
		setupPatterns();
	}
	
	/**
	 * Arrange patterns for quick lookup.  Those with well-known types
	 * go into patternMap, while others are in otherPatterns and scanned
	 * linearly.
	 */
	private void setupPatterns() {
		patternMap = new LinkedHashMap<Pair<Pair<BasicType, Integer>, String>, List<IPattern>>();
		otherPatterns = new ArrayList<IPattern>();
		for (IPattern pattern : patterns) {

			assert !hardcodedInstrs.matcher(pattern.llInst).matches() : 
				"these instructions are handled specially";
			
			BasicType basicType;
			switch (pattern.basicType) {
			case INTEGRAL:
			case POINTER:
			case BOOL:
				basicType = BasicType.INTEGRAL;
				break;
			default:
				otherPatterns.add(pattern);
				continue;
			}
			
			for (int i = 1; i <= pattern.typeMask; i+=i) {
				if ((pattern.typeMask & i) != 0) {
					Pair<Pair<BasicType, Integer>, String> key = new Pair<Pair<BasicType, Integer>, String>(
							new Pair<BasicType, Integer>(basicType, i), pattern.llInst);
					List<IPattern> list = patternMap.get(key);
					if (list == null) {
						list = new ArrayList<IPattern>();
						patternMap.put(key, list);
					}
					list.add(pattern);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterCode(org.ejs.eulang.llvm.directives.LLDefineDirective)
	 */
	@Override
	public boolean enterCode(LLDefineDirective directive) {
		blockMap = new LinkedHashMap<ISymbol, Block>();
		tempTable = new HashMap<LLOperand, AssemblerOperand>();
		return true;
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
		blockMap.put(lllabelSym, block);
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#exitBlock(org.ejs.eulang.llvm.LLBlock)
	 */
	@Override
	public void exitBlock(LLBlock llblock) {
		routine.addBlock(block);
		this.llblock = null;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterInstr(org.ejs.eulang.llvm.instrs.LLInstr)
	 */
	@Override
	public boolean enterInstr(LLBlock block, LLInstr instr) {
		asmOps[0] = asmOps[1] = asmOps[2] = null;
		this.instr = instr;
		
		if (instr instanceof LLAllocaInstr)
			return false;
		
		if (instr instanceof LLCallInstr) {
			handleCallInstr((LLCallInstr) instr);
			return false;
		}
		else if (instr instanceof LLRetInstr) {
			handleRetInstr(block, (LLRetInstr) instr);
			return false;
		} 
		else if (instr.getName().equals("select")) {
			handleSelectInstr((LLAssignInstr) instr);
			return false;
		} 
		else if (instr.getName().equals("phi")) {
			handlePhiInstr((LLAssignInstr) instr);
			return false;
		}
		
		if (instr instanceof LLTypedInstr) {
			LLTypedInstr typed = (LLTypedInstr) instr;
			int bitMask = typed.getType().getBits() == 1 ? I1 : 
				typed.getType().getBits() == 8 ? I8 :
					typed.getType().getBits() == 16 ? I16 : 0;
			
			BasicType basicType = typed.getType().getBasicType();
			if (isIntType(typed.getType()))
				basicType = BasicType.INTEGRAL;
			
			Pair<Pair<BasicType, Integer>, String> key = new Pair<Pair<BasicType, Integer>, String>(
					new Pair<BasicType, Integer>(basicType, bitMask), instr.getName());
			List<IPattern> patterns = patternMap.get(key);
			if (patterns != null) {
				for (IPattern pattern : patterns) {
					if (matches(pattern, bitMask, typed)) {
						thePattern = pattern;
						return true;
					}
				}
			}
			for (IPattern pattern : otherPatterns) {
				if (matches(pattern, bitMask, typed)) {
					thePattern = pattern;
					return true;
				}
			}
		}
		assert false : "unhandled instr " + instr;
		return false;
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
		if (!(pattern.basicType == null || pattern.basicType.equals(typed.getType().getBasicType()))
				&&  (pattern.typeMask & bitMask) == 0)
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

		case IS_TEMP_LAST_USE:
		case IN_PHYS_REG: 
		case IN_PHYS_REG_0: 
		case IN_REG_LOCAL:
		case IN_REG_BLOCK: {
			ILocal local = locals.getFinalLocal(op);
			RegisterLocal regLocal = null;
			if (local instanceof RegisterLocal)
				regLocal = (RegisterLocal) local;
			else
				return false;
			
			if (opcond == If.IN_PHYS_REG) {
				return regLocal.getVr() < regLocal.getRegClass().getRegisterCount();
			}
			else if (opcond == If.IN_PHYS_REG_0) {
				return regLocal.getVr() == 0;
			}
			else if (opcond == If.IN_REG_BLOCK) {
				return regLocal.getUses().size() == 1;
			}
			else if (opcond == If.IN_REG_LOCAL) {
				return true;
			}
			else if (opcond == If.IS_TEMP_LAST_USE) {
				return isLastUse(op);
			} 
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

	private boolean isInt(LLConstOp op, int i, int j) {
		if (!isIntType(op.getType()))
			return false;
		return op.getValue().longValue() >= i && op.getValue().longValue() <= j; 
	}

	private void handlePhiInstr(LLAssignInstr llinst) {
		assert false;
	}

	private void handleSelectInstr(LLAssignInstr llinst) {
		assert false;
	}

	private void handleRetInstr(LLBlock block, LLRetInstr instr) {
		LLOperand[] llops = instr.getOperands();
		if (llops.length == 0) {
			
		} else if (llops.length == 1) {
			LLOperand operand = llops[0];
			AssemblerOperand asmOp = generateOperand(operand);
			
			// TODO: use cconv
			asmOp = copyIntoRegister(operand, asmOp, 0);
		} else {
			assert false;
		}
		
		HLInstruction[] rets = routine.generateReturn();
		for (HLInstruction ret : rets) {
			emitInstr(ret);
		}
		
	}
	private void handleCallInstr(LLCallInstr llinst) {
		if (llinst.getFunction() instanceof LLSymbolOp) {
			LLSymbolOp symOp = (LLSymbolOp) llinst.getFunction();
			if (isIntrinsic(symOp, ITarget.Intrinsic.SHIFT_RIGHT_CIRCULAR)) {
				LLInstr instr = new LLBinaryInstr("src", llinst.getResult(), llinst.getType(), 
						llinst.getOperands()[0], llinst.getOperands()[1]);
				instr.setNumber(llinst.getNumber());
				instr.accept(llblock, this);
				return;
			}
		}
		assert false;
		
	}

	private boolean isIntrinsic(LLSymbolOp symOp, Intrinsic intrinsic) {
		LLType type = symOp.getType();
		if (type instanceof LLCodeType)
			type = ((LLCodeType) type).getRetType();
		return symOp.getSymbol().equals(def.getTarget().getIntrinsic(def, intrinsic, type));
	}

	@Override
	public boolean enterOperand(LLInstr instr, int num, LLOperand operand) {
		assert thePattern != null;
		
		handleAs(num, operand);
		return false;
	}

	private void handleAs(int num, LLOperand operand) {
		AssemblerOperand asmOp = operand != null ? generateOperand(operand) : null;

		if (num >= thePattern.ases.length) {
			asmOps[num] = asmOp;
			return;
		}
		As as = thePattern.ases[num];
		switch (as) {
		case GEN_R:
		case GEN_W:
			asmOp = generateGeneralOperand(operand, asmOp);
			break;
		case GEN_RW:
			asmOp = generateGeneralOperand(operand, asmOp);
			if (!isLastUse(operand) || !isLastUse(asmOp))
				asmOp = moveToTemp(operand, asmOp);
			break;
		case REG_R:
			asmOp = generateRegisterOperand(operand, asmOp);
			break;
		case REG_RW_DUP:
		case REG_RW:
			asmOp = generateRegisterOperand(operand, asmOp);
			if (!isLastUse(operand) || !isLastUse(asmOp))
				asmOp = moveToTemp(operand, asmOp);
			if (as == As.REG_RW_DUP) {
				AssemblerOperand copy = moveToTemp(operand, asmOp);
				emit(HLInstruction.create(InstructionTable.Iswpb, copy));
				emit(HLInstruction.create(InstructionTable.Imovb, asmOp, copy));
				asmOp = copy;
			}
			break;
		case REG_0_W:
			asmOp = copyIntoRegister(operand, asmOp, 0);
			break;
		case IMM:
			assert asmOp instanceof NumberOperand;
			if (operand.getType().getBits() == 8)
				asmOp = new NumberOperand(((NumberOperand) asmOp).getValue() << 8);
				
			break;
		case IMM_NEG:
			assert asmOp instanceof NumberOperand;
			asmOp = new NumberOperand(-((NumberOperand) asmOp).getValue());
			break;
		case IMM_8:
			asmOp = new NumberOperand(8);
			break;
		case IMM_1:
			asmOp = new NumberOperand(1);
			break;
		case IMM_0:
			asmOp = new NumberOperand(0);
			break;
		case IMM_N1:
			asmOp = new NumberOperand(-1);
			break;
		}
		
		asmOps[num] = asmOp;
	}
	
	/**
	 * Put the operand into an assembler operand.
	 * @param operand
	 * @return
	 */
	private AssemblerOperand generateOperand(LLOperand operand) {
		AssemblerOperand asmOp = tempTable.get(operand);
		if (asmOp != null)
			return asmOp;
		
		if (operand instanceof LLConstOp) {
			if (isIntOp(operand)) {
				return new NumberOperand(((LLConstOp) operand).getValue().intValue());
			}
			assert false;
		}
		if (operand instanceof LLTempOp) {
			if (isIntOp(operand)) {
				ILocal local = locals.getFinalLocal(operand);
				if (local instanceof RegisterLocal)
					return new RegisterTempOperand((RegisterLocal) local);
				else if (local instanceof StackLocal)
					return new StackLocalOperand((StackLocal) local);
			}
		}
		if (operand instanceof LLSymbolOp) {
			return new SymbolOperand(((LLSymbolOp) operand).getSymbol());
		}
		
		assert false;
		return null;
	}

	private boolean isIntOp(LLOperand operand) {
		return operand.getType().getBasicType() == BasicType.INTEGRAL
				&& operand.getType().getBits() <= 16;
	}
	private boolean isIntOp(LLOperand operand, int bits) {
		return operand.getType().getBasicType() == BasicType.INTEGRAL
		&& operand.getType().getBits() == bits;
	}

	/**
	 * Generate a register operand into the given register number.
	 * @param llOperand 
	 * @param operand
	 * @param num
	 */
	private AssemblerOperand copyIntoRegister(LLOperand llOperand, AssemblerOperand operand, int num) {
		RegisterLocal regLocal = newTempRegister(instr, llOperand.getType());
		if (!locals.forceToRegister(regLocal.getName(), num))
			assert false;
		AssemblerOperand ret = new RegisterTempOperand(regLocal);
		if (!ret.equals(operand)) {
			moveTo(llOperand, operand, ret);
		}
		return ret;
	}

	private AssemblerOperand generateRegisterOperand(LLOperand llOp, AssemblerOperand operand) {
		if (operand.isRegister())
			return operand;

		AssemblerOperand dest = new RegisterTempOperand(newTempRegister(instr, llOp.getType()));
		
		return moveTo(llOp, operand, dest);
	}
	
	private AssemblerOperand generateGeneralOperand(LLOperand llOp, AssemblerOperand operand) {
		if (operand.isMemory()) {
			return operand;
		}
		if (operand.isRegister()) {
			if (isIntOp(llOp)) {
				return operand;
			}
			assert false;
		}
		if (operand instanceof NumberOperand) {
			if (isIntOp(llOp)) {
				RegisterLocal regLocal = newTempRegister(instr, llOp.getType());
				AssemblerOperand ret = new RegisterTempOperand(regLocal);
				if (isIntOp(llOp, 16)) {
					emitInstr(HLInstruction.create(Ili, ret, operand));
				} else if (isIntOp(llOp, 8)) {
					operand = new NumberOperand((((NumberOperand) operand).getValue() << 8) & 0xFF00);
					emitInstr(HLInstruction.create(Ili, ret, operand));
				} else
					assert false;
				return ret;
			}
			assert false;
		}
		if (operand instanceof SymbolOperand) {
			// we're dereferencing it
			ISymbol sym = ((SymbolOperand) operand).getSymbol();
			ILocal local = locals.getFinalLocal(sym);
			if (local instanceof RegisterLocal)
				return new RegisterTempOperand((RegisterLocal) local);
			else if (local instanceof StackLocal)
				return new AddrOperand(new StackLocalOperand((StackLocal) local));
			else
				return new AddrOperand(new SymbolOperand(sym));
				
		}
		assert false;
		return null;
	}

	/**
	 * @param inst
	 */
	private void emitInstr(HLInstruction inst) {
		if (inst.getInst() == Ili && isZero(inst.getOp2()))
			emit(HLInstruction.create(Iclr, inst.getOp1()));
		else if (!isNoOp(inst))
			emit(inst);
	}

	private boolean isZero(AssemblerOperand op) {
		return op instanceof NumberOperand && ((NumberOperand) op).getValue() == 0;
	}

	private AssemblerOperand moveToTemp(LLOperand llOp, AssemblerOperand operand) {
		AssemblerOperand dest = new RegisterTempOperand(newTempRegister(instr, llOp.getType()));
		return moveTo(llOp, operand, dest);
	}

	private AssemblerOperand moveTo(LLOperand llOp, AssemblerOperand from, AssemblerOperand dest) {
		if (llOp.getType().getBasicType() == BasicType.INTEGRAL) {
			if (from.isRegister() || from.isMemory()) {
				int op = (llOp.getType().getBits() <= 8) ? Imovb : Imov;
				HLInstruction inst = HLInstruction.create(op, from, dest);
				emitInstr(inst);
			} else if (from instanceof NumberOperand) {
				HLInstruction inst = HLInstruction.create(Ili, dest, from);
				emitInstr(inst);
			} else {
				assert false;
			}
			return dest;
		}
		assert false;
		return dest;
	}
	
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

	/**
	 * @param local
	 * @return
	 */
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

	/**
	 * @param succ
	 * @return
	 */
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
				if (d.inst != -1) {
					AssemblerOperand op1 = d.ops.length >= 1 ? asmOps[d.ops[0]] : null;
					AssemblerOperand op2 = d.ops.length >= 2 ? asmOps[d.ops[1]] : null;
					AssemblerOperand op3 = d.ops.length >= 3 ? asmOps[d.ops[2]] : null;
					HLInstruction inst = HLInstruction.create(d.inst, op1, op2, op3);
					
					emitInstr(inst);
				}
				
				if (d instanceof DoRes) {
					assert instr instanceof LLAssignInstr;
					
					AssemblerOperand dest = asmOps[d.ops[((DoRes) d).result]];
					tempTable.put(((LLAssignInstr) instr).getResult(), dest);
				}
			}
			
		}
	}

	/**
	 * Quickly eliminate useless instructions
	 * @param inst
	 * @return
	 */
	private boolean isNoOp(HLInstruction inst) {
		if (inst.getInst() == Imov || inst.getInst() == Imovb) {
			AssemblerOperand op1 = inst.getOp1();
			AssemblerOperand op2 = inst.getOp2();
			if (op1.equals(op2))
				return true;
		}
		return false;
	}
}
