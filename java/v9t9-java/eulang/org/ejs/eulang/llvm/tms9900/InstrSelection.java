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
import java.util.Map;
import java.util.regex.Pattern;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.ICallingConvention;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ICallingConvention.Location;
import org.ejs.eulang.ICallingConvention.RegisterLocation;
import org.ejs.eulang.ICallingConvention.StackBarrierLocation;
import org.ejs.eulang.ICallingConvention.StackLocation;
import org.ejs.eulang.ITarget.Intrinsic;
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
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.instrs.LLRetInstr;
import org.ejs.eulang.llvm.instrs.LLStoreInstr;
import org.ejs.eulang.llvm.instrs.LLTypedInstr;
import org.ejs.eulang.llvm.instrs.LLUncondBranchInstr;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.ops.LLTempOp;
import org.ejs.eulang.parser.EulangParser.binding_return;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;

import v9t9.engine.cpu.InstructionTable;
import v9t9.tools.asm.assembler.HLInstruction;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.ConstPoolRefOperand;
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
		/** if the LL operand is allocated to a register temp used only in one block */
		IN_REG_BLOCK,

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
		/** put the LL operand into physical register #1 */
		REG_1_W,
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
		
		/** for ICMP or FCMP, the comparison (NumberOperand, value: CMP_xxx) */
		CMP,
	};
	
	final static Map<String, Integer> compareToInt = new HashMap<String, Integer>();
	public final static int CMP_EQ = 0, CMP_NE = 1, CMP_SGT = 2, CMP_SLT = 3, CMP_SGE = 4, 
	CMP_SLE = 5, CMP_UGT = 6, CMP_ULT = 7, CMP_UGE = 8, CMP_ULE = 9;
	
	static {
		compareToInt.put("eq", CMP_EQ);
		compareToInt.put("ne", CMP_NE);
		compareToInt.put("sgt", CMP_SGT);
		compareToInt.put("slt", CMP_SLT);
		compareToInt.put("sge", CMP_SGE);
		compareToInt.put("sle", CMP_SLE);
		compareToInt.put("ugt", CMP_UGT);
		compareToInt.put("ult", CMP_ULT);
		compareToInt.put("uge", CMP_UGE);
		compareToInt.put("ule", CMP_ULE);
	}
	
	/** Pseudo-instructions */
	final static int Ipseudo = Iuser;
	final static int Iiset = Ipseudo + 1,
		Ijcc = Ipseudo + 2
	;
	static {
		InstructionTable.registerInstruction(Iiset, "ISET");
		InstructionTable.registerInstruction(Ijcc, "JCC");
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
	
	/** These ll instructions are handled specially; do not make patterns for them */
	private static final Pattern hardcodedInstrs = 
		Pattern.compile("\\b(call|ret|br|switch|phi)\\b");
	
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
		new IPattern( BasicType.INTEGRAL, I1|I8, "store", 
				new If[] { If.PASS, If.PASS },
				new As[] { As.GEN_R, As.GEN_W }, 
				new Do( Imovb, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16 | I8 | I1, "load", 
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
		
		new IPattern( BasicType.INTEGRAL, I16, "add", 
				new If[] { If.PASS, If.IS_CONST_1 },
				new As[] { As.GEN_RW }, 
				new DoRes( 0, Iinc, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "add", 
				new If[] { If.PASS, If.IS_CONST_2 },
				new As[] { As.GEN_RW }, 
				new DoRes( 0, Iinct, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "add", 
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
		
		// negative constants
		new IPattern( BasicType.INTEGRAL, I16, "sub", 
				new If[] { If.PASS, If.IS_CONST_1 },
				new As[] { As.GEN_RW }, 
				new DoRes( 0, Idec, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "sub", 
				new If[] { If.PASS, If.IS_CONST_2 },
				new As[] { As.GEN_RW }, 
				new DoRes( 0, Idect, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "sub", 
				new If[] { If.PASS, If.IS_CONST_N1 },
				new As[] { As.GEN_RW }, 
				new DoRes( 0, Iinc, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "sub", 
				new If[] { If.PASS, If.IS_CONST_N2 },
				new As[] { As.GEN_RW }, 
				new DoRes( 0, Iinct, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "sub", 
				new If[] { If.IS_CONST_0, If.IS_CONST },
				new As[] { As.REG_W, As.IMM_NEG }, 
				new DoRes( 0, Ili, 0, 1 )
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
		new IPattern( BasicType.INTEGRAL, I8|I1, "or", 
				null,
				new As[] { As.GEN_RW, As.GEN_R }, 
				new DoRes( 1, Isocb, 1, 0 )
		),
		
		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "xor", 
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
		
		// another variant
		new IPattern( BasicType.INTEGRAL, I16, "slc", 
		 		new If[] { If.PASS, If.IS_CONST_0 },
		 		null
		),
		new IPattern( BasicType.INTEGRAL, I16, "slc", 
		 		new If[] { If.PASS, If.IS_CONST_16 },
		 		null
		),
		new IPattern( BasicType.INTEGRAL, I16, "slc", 
				new If[] { If.PASS, If.IS_CONST_1_15 },
				new As[] { As.REG_RW, As.IMM_NEG_15 }, 
				new DoRes( 0, Isrc, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16, "slc", 
		 		new If[] { If.PASS, If.PASS },
		 		new As[] { As.REG_RW, As.REG_0_W },
		 		new Do( Ineg, 1 ),
		 		new DoRes( 0, Isrc, 0, 1 )
		),
		
		new IPattern( BasicType.INTEGRAL, I8, "slc", 
		 		new If[] { If.PASS, If.IS_CONST_0 },
		 		null
		),
		new IPattern( BasicType.INTEGRAL, I8, "slc", 
		 		new If[] { If.PASS, If.IS_CONST_16 },
		 		null
		),
		new IPattern( BasicType.INTEGRAL, I8, "slc", 
				new If[] { If.PASS, If.IS_CONST_1_15 },
				new As[] { As.REG_RW_DUP, As.IMM_NEG_15 }, 
				new DoRes( 0, Isrc, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I8, "slc", 
		 		new If[] { If.PASS, If.PASS },
		 		new As[] { As.REG_RW_DUP, As.REG_0_W },
		 		new Do( Ineg, 1 ),
				new DoRes( 0, Isrc, 0, 1 )
		),
		
		new IPattern( BasicType.BOOL, I1, "icmp", 
				new If[] { If.IS_I16, 
					If.IS_CONST_0 },
				new As[] { As.REG_R, As.CMP, As.REG_W },
				new Do( Ic, 0, 0 ),
				new DoRes( 1, Iiset, 1, 2 )
		),
		new IPattern( BasicType.BOOL, I1, "icmp", 
				new If[] { If.IS_INT, 
				If.IS_CONST_0 },
				new As[] { As.REG_R, As.CMP, As.REG_W },
				new Do( Icb, 0, 0 ),
				new DoRes( 1, Iiset, 1, 2 )
		),
		new IPattern( BasicType.BOOL, I1, "icmp", 
				new If[] { If.IS_I16, 
				If.IS_CONST },
				new As[] { As.REG_R, As.IMM, As.CMP, As.REG_W },
				new Do( Ici, 0, 1 ),
				new DoRes( 1, Iiset, 2, 3 )
		),
		new IPattern( BasicType.BOOL, I1, "icmp", 
				new If[] { If.IS_INT,	// handles 8 or 1 
				If.IS_CONST },
				new As[] { As.REG_R, As.CONST_POOL, As.CMP, As.REG_W },
				new Do( Icb, 0, 1 ),
				new DoRes( 1, Iiset, 2, 3 )
		),
		new IPattern( BasicType.BOOL, I1, "icmp", 
				new If[] { If.IS_I16, 
				If.PASS },
				new As[] { As.REG_R, As.REG_R, As.CMP, As.REG_W },
				new Do( Ic, 0, 1 ),
				new DoRes( 1, Iiset, 2, 3 )
		),
		
		new IPattern( BasicType.BOOL, I1, "icmp", 
				new If[] { If.IS_INT,	// handles 8 and 1 
				If.PASS },
				new As[] { As.GEN_R, As.REG_R, As.CMP, As.REG_W },
				new Do( Icb, 0, 1 ),
				new DoRes( 1, Iiset, 2, 3 )
		),

		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "mul", 
				 new If[] { If.PASS, If.IS_CONST_0 },
				 new As[] { As.IMM_0 },
				 new DoRes( 0, -1, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "mul", 
				new If[] { If.PASS, If.IS_CONST_1 },
				new As[] { As.GEN_R },
				new DoRes( 0, -1, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "mul", 
				new If[] { If.PASS, If.IS_CONST_POW_2 },
				new As[] { As.REG_RW, As.IMM_LOG_2 }, 
				new DoRes( 0, Isla, 0, 1 )
		),

		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "mul", 
				 new If[] { If.PASS, If.PASS },
				 new As[] { As.REG_HI_W, As.REG_LO_W },
				 new DoRes( 1, Impy, 1, 0 )
		),

		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "sdiv", 
				new If[] { If.PASS, If.IS_CONST_1 },
				new As[] { As.GEN_R },
				new DoRes( 0, -1, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "udiv", 
				new If[] { If.PASS, If.IS_CONST_1 },
				new As[] { As.GEN_R },
				new DoRes( 0, -1, 0 )
		),
		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "sdiv", 
				new If[] { If.PASS, If.IS_CONST_POW_2 },
				new As[] { As.REG_RW, As.IMM_LOG_2 }, 
				new DoRes( 0, Isra, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "udiv", 
				new If[] { If.PASS, If.IS_CONST_POW_2 },
				new As[] { As.REG_RW, As.IMM_LOG_2 }, 
				new DoRes( 0, Isrl, 0, 1 )
		),

		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "udiv", 
				 new If[] { If.PASS, If.PASS },
				 new As[] { As.REG_LO_W, As.GEN_R, As.REG_HI_W },
				 new Do( Iclr, 2 ),
				 new DoRes( 2, Idiv, 1, 2, 0 )		// fake 3rd op
		),
		
		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "sdiv", 
				new If[] { If.PASS, If.PASS },
				new As[] { As.GEN_R, As.GEN_R },
				new DoIntrinsic( Intrinsic.SIGNED_DIVISION, 0, 1 )
		),
		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "urem", 
				 new If[] { If.PASS, If.PASS },
				 new As[] { As.REG_LO_W, As.GEN_R, As.REG_HI_W },
				 new Do( Iclr, 2 ),
				 new DoRes( 1, Idiv, 1, 2, 0 )		// fake 3rd op
		),
		
		new IPattern( BasicType.INTEGRAL, I16|I8|I1, "srem", 
				new If[] { If.PASS, If.PASS },
				new As[] { As.GEN_R, As.GEN_R },
				new DoIntrinsic( Intrinsic.SIGNED_REMAINDER, 0, 1)
		),
	};
	
	/** Called to fetch a new temp register 
	 * @param instr TODO
	 * @param symbol TODO*/
	abstract protected RegisterLocal newTempRegister(LLInstr instr, ISymbol symbol, LLType type);
	
	/** Called when an instruction has been generated */
	abstract protected void emit(HLInstruction instr);
	
	/** Called when a new block has been created */
	abstract protected void newBlock(Block block);
	
	private final LLDefineDirective def;
	private final ICallingConvention cc;
	private HashMap<Pair<Pair<BasicType, Integer>, String>, List<IPattern>> patternMap;
	private List<IPattern> otherPatterns;

	private IPattern thePattern;
	private AssemblerOperand[] asmOps = new AssemblerOperand[4];
	
	private final Locals locals;
	private final Routine routine;
	private Block block;
	
	private HashMap<ISymbol, Block> blockMap;
	/** dests of SSA operands, which do not change value */
	private HashMap<LLOperand, AssemblerOperand> ssaTempTable;
	private LLInstr instr;
	private LLBlock llblock;
	private TypeEngine typeEngine;
	private RegisterLocal regPair;
	private final LLModule module;
	
	/**
	 * 
	 */
	public InstrSelection(LLModule module, Routine routine) {
		this.module = module;
		this.routine = routine;
		locals = routine.getLocals();
		def = routine.getDefinition();
		typeEngine = def.getTarget().getTypeEngine();
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
		ssaTempTable = new LinkedHashMap<LLOperand, AssemblerOperand>();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#exitCode(org.ejs.eulang.llvm.directives.LLDefineDirective)
	 */
	@Override
	public void exitCode(LLDefineDirective directive) {
		super.exitCode(directive);
		
		System.out.println("SSA Temp Table:");
		for (Map.Entry<LLOperand, AssemblerOperand> entry : ssaTempTable.entrySet()) {
			System.out.println("\t" + entry.getKey() + " [" + entry.getKey().getType() + "] -> " + entry.getValue());
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
		blockMap.put(lllabelSym, block);
		newBlock(block);
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
		regPair = null;
		
		if (instr instanceof LLAllocaInstr)
			return false;
		
		if (instr instanceof LLCallInstr) {
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
		case IS_CONST_POW_2:
			return op instanceof LLConstOp && isIntPow2((LLConstOp) op);

		//case IS_TEMP_LAST_USE:
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

	private void handlePhiInstr(LLAssignInstr llinst) {
		assert false;
	}

	private void handleBranchInstr(LLBranchInstr instr) {
		LLOperand[] llops = instr.getOperands();
		
		AssemblerOperand test = generateOperand(llops[0]);
		LLSymbolOp trueTarget = (LLSymbolOp) llops[1];
		LLSymbolOp falseTarget = (LLSymbolOp) llops[2];
		AssemblerOperand trueOp = new SymbolLabelOperand(trueTarget.getSymbol());
		AssemblerOperand falseOp = new SymbolLabelOperand(falseTarget.getSymbol());
		
		emitInstr(HLInstruction.create(Ijcc, test, trueOp, falseOp));
	}
	private void handleUncondBranchInstr(LLUncondBranchInstr instr) {
		LLOperand[] llops = instr.getOperands();
		
		LLSymbolOp target = (LLSymbolOp) llops[0];
		AssemblerOperand asmOp = new SymbolLabelOperand(target.getSymbol());
		
		emitInstr(HLInstruction.create(Ijmp, asmOp));
	}
	private void handleRetInstr(LLRetInstr instr) {
		LLOperand[] llops = instr.getOperands();
		if (llops.length == 0) {
			
		} else if (llops.length == 1) {
			LLOperand operand = llops[0];
			AssemblerOperand asmOp = generateOperand(operand);
			
			// TODO: use cconv
			asmOp = copyIntoRegister(instr, operand, asmOp, 0);
		} else {
			assert false;
		}
		
		HLInstruction[] rets = routine.generateReturn();
		for (HLInstruction ret : rets) {
			emitInstr(ret);
		}
		
	}
	private void handleCallInstr(LLCallInstr llinst) {
		LLBaseDirective directive = null;
		
		if (llinst.getFunction() instanceof LLSymbolOp) {
			LLSymbolOp symOp = (LLSymbolOp) llinst.getFunction();
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
		
		if (fconv == null)
			fconv = FunctionConvention.create(null, (LLCodeType) llinst.getFunction().getType());
		
		ICallingConvention cconv = def.getTarget().getCallingConvention(fconv);
		
		LLOperand[] ops = llinst.getOperands();
		Location[] argLocs = cconv.getArgumentLocations();
		assert ops.length == argLocs.length - 1;
		
		int stackSpace = 0;
		for (int i = 0; i < ops.length; i++) {
			if (argLocs[i] instanceof ICallingConvention.StackBarrierLocation) {
				ICallingConvention.StackBarrierLocation stackLoc = (StackBarrierLocation) argLocs[i];
				stackSpace = stackLoc.getPushedArgumentsSize();
			}
		}
		
		NumberOperand sp = new NumberOperand(def.getTarget().getSP());
		
		// adjust stack
		if (stackSpace == 1 || stackSpace == 2) {
			emitInstr(HLInstruction.create(Idect, new RegisterOperand(sp)));
		} else if (stackSpace > 2) {
			emitInstr(HLInstruction.create(Iai, new RegisterOperand(sp), new NumberOperand(-stackSpace)));
		}
		
		for (int i = 0; i < ops.length; i++) {
			AssemblerOperand arg = generateOperand(ops[i]);
			if (argLocs[i] instanceof ICallingConvention.RegisterLocation) {
				ICallingConvention.RegisterLocation regLoc = (RegisterLocation) argLocs[i];
				assert regLoc.bitOffset == 0;
				arg = copyIntoRegister(llinst, ops[i], arg, regLoc.number);
			}
			else if (argLocs[i] instanceof ICallingConvention.StackLocation) {
				ICallingConvention.StackLocation stackLoc = (StackLocation) argLocs[i];
				AssemblerOperand stackOp = new RegOffsOperand(
						new NumberOperand(stackLoc.offset + stackSpace),
						sp);
				moveTo(ops[i], arg, stackOp);
			}
			else
				assert false;
		}
		
		AssemblerOperand func = generateOperand(llinst.getFunction());
		
		// TODO: other func types
		emitInstr(HLInstruction.create(Ibl, new AddrOperand(func)));
		routine.setHasBlCalls(true);
		
		// handle the return value
		Location[] retLocs = cconv.getReturnLocations();
	
		LLOperand result = ((LLAssignInstr) instr).getResult();
		for (int i = 0; i < retLocs.length; i++) {
			if (retLocs[i] instanceof RegisterLocation) {
				RegisterLocation regLoc = (RegisterLocation) retLocs[i];
				
				RegisterOperand retOp = new RegisterOperand(new NumberOperand(regLoc.number));
				RegisterLocal regLocal = newTempRegister(instr, getTempSymbol(result), regLoc.type);
				RegisterTempOperand asmOp = new RegisterTempOperand(regLocal);
				moveTo(regLoc.type, retOp, asmOp);
				
				asmOps[0] = asmOp;
				ssaTempTable.put(result, asmOp);
			}
			else 
				assert false;
		}
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

		if (num >= thePattern.ases.length) {
			AssemblerOperand asmOp = operand != null ? generateOperand(operand) : null;
			asmOps[num] = asmOp;
			return;
		}
		As as = thePattern.ases[num];
		
		if (as == As.IGNORE) {
			asmOps[num] = new NumberOperand(0);
			return;
		}
		
		AssemblerOperand asmOp = operand != null ? generateOperand(operand) : null;

		//boolean isByte = !(instr instanceof LLCastInstr) && ((LLTypedInstr) instr).getType().getBits() <= 8;
		switch (as) {
		case GEN_R:
		case GEN_W:
			asmOp = generateGeneralOperand(operand, asmOp);
			break;
		case GEN_RW:
			asmOp = generateGeneralOperand(operand, asmOp);
			//if (!isFirstUse(operand)) {
			//	if (!isLastUse(operand) || !isLastUse(asmOp))
			if (operandNeedsTemp(operand, asmOp)) {
					asmOp = moveToTemp(operand, asmOp);
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
					asmOp = moveToTemp(operand, asmOp);
			}
			if (as == As.REG_RW_DUP) {
				AssemblerOperand copy = moveToTemp(operand, asmOp);
				emit(HLInstruction.create(InstructionTable.Iswpb, copy));
				emit(HLInstruction.create(InstructionTable.Imovb, asmOp, copy));
				asmOp = copy;
			}
			break;
		case REG_W: {
			RegisterLocal temp = newTempRegister(instr, getTempSymbol(operand), 
					operand != null ? operand.getType() : ((LLTypedInstr) instr).getType());
			asmOp = new RegisterTempOperand(temp); 
			break;
		}
		case REG_0_W:
			asmOp = copyIntoRegister(instr, operand, asmOp, 0);
			break;
		case REG_1_W:
			asmOp = copyIntoRegister(instr,operand, asmOp, 1);
			break;

		case REG_HI_W: 
			asmOp = copyIntoRegPair(instr, operand, asmOp, true);
			break;
		case REG_LO_W:
			asmOp = copyIntoRegPair(instr, operand, asmOp, false);
			break;
			
			
		case CONST_POOL:
			assert asmOp instanceof NumberOperand;
			asmOp = new ConstPoolRefOperand(asmOp);
			break;
		case IMM:
			assert asmOp instanceof NumberOperand;
			break;
		case IMM_NEG:
			assert asmOp instanceof NumberOperand;
			asmOp = new NumberOperand(-((NumberOperand) asmOp).getValue());
			break;
		case IMM_NEG_15: {
			assert asmOp instanceof NumberOperand;
			//int mask = isByte ? 0xf00 : 0xf; 
			int mask = 0xf; 
			asmOp = new NumberOperand((-((NumberOperand) asmOp).getValue()) & mask);
			break;
		}
		case IMM_LOG_2: {
			assert asmOp instanceof NumberOperand && isIntPow2((LLConstOp) operand);
			int log = 0;
			int val = ((LLConstOp) operand).getValue().intValue();
			while (val != 1) {
				val >>>= 1;
				log++;
			}
			//if (isByte)
			//	log <<= 8;
			asmOp = new NumberOperand(log);
			break;
		}
			
		case IMM_15:
			asmOp = new NumberOperand(15);
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
		case CMP: {
			assert instr instanceof LLCompareInstr;
			int code = compareToInt.get(((LLCompareInstr) instr).getCmp());
			asmOp = new NumberOperand(code);
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
		return operand instanceof LLTempOp && asmOp instanceof ISymbolOperand && 
			((ISymbolOperand) asmOp).getSymbol().equals(locals.getScope().get(((LLTempOp) operand).getName()));
	}
	
	/**
	 * @param operand
	 * @return
	 */
	private boolean isDefinition(LLOperand operand) {
		return (instr instanceof LLAssignInstr && ((LLAssignInstr) instr).getResult().equals(operand))
		|| (instr instanceof LLStoreInstr && ((LLStoreInstr) instr).getOperands()[1].equals(operand));
	}

	/**
	 * Put the operand into an assembler operand.
	 * @param operand
	 * @return
	 */
	private AssemblerOperand generateOperand(LLOperand operand) {
		AssemblerOperand asmOp = ssaTempTable.get(operand);
		if (asmOp != null)
			return asmOp;
		
		if (operand instanceof LLConstOp) {
			if (isIntOp(operand)) {
				int val = ((LLConstOp) operand).getValue().intValue();
				if (((LLTypedInstr) instr).getType().getBits() == 8)
					val = (val << 8) & 0xff00;
				return new NumberOperand(val);
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
			ISymbol symbol = ((LLSymbolOp) operand).getSymbol();
			ILocal local = locals.getFinalLocal(symbol);
			if (local instanceof RegisterLocal)
				return new RegisterTempOperand((RegisterLocal) local);
			else if (local instanceof StackLocal)
				return new StackLocalOperand((StackLocal) local);
			return new SymbolOperand(symbol);
		}
		
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

	/**
	 * Generate a register operand into the given register number.
	 * @param llOperand 
	 * @param operand
	 * @param num
	 */
	private AssemblerOperand copyIntoRegister(LLInstr instr, LLOperand llOperand, AssemblerOperand operand, int num) {
		RegisterLocal regLocal = newTempRegister(instr, getTempSymbol(llOperand), 
				llOperand != null ? llOperand.getType() : ((LLTypedInstr) instr).getType());
		if (!locals.forceToRegister(regLocal.getName(), num))
			assert false;
		AssemblerOperand ret = new RegisterTempOperand(regLocal);
		if (llOperand != null && !ret.equals(operand)) {
			moveTo(llOperand, operand, ret);
		}
		return ret;
	}
	private AssemblerOperand copyIntoRegPair(LLInstr instr, LLOperand llOperand, AssemblerOperand operand, boolean high) {
		RegisterLocal regLocal = getRegisterPair(llOperand);
		AssemblerOperand ret = new RegisterTempOperand(regLocal, high);
		if (llOperand != null) {
			moveTo(llOperand, operand, ret);
		}
		return ret;
	}

	/**
	 * @param llOperand 
	 * @return
	 */
	private RegisterLocal getRegisterPair(LLOperand llOperand) {
		if (regPair == null) {
			regPair = newTempRegister(instr, getTempSymbol(llOperand), typeEngine.INT);
			regPair.setRegPair(true);
		}
		return regPair;
	}

	private AssemblerOperand generateRegisterOperand(LLOperand llOp, AssemblerOperand operand) {
		if (operand.isRegister())
			return operand;

		AssemblerOperand dest = new RegisterTempOperand(newTempRegister(instr, getTempSymbol(llOp), llOp.getType()));
		
		return moveTo(llOp, operand, dest);
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
		if (operand instanceof NumberOperand) {
			if (isIntOp(llOp)) {
				RegisterLocal regLocal = newTempRegister(instr, getTempSymbol(llOp), llOp.getType());
				AssemblerOperand ret = new RegisterTempOperand(regLocal);
				if (isIntOp(llOp)) {
					emitInstr(HLInstruction.create(Ili, ret, operand));
					/*
				} if (isIntOp(llOp, 16)) {
					emitInstr(HLInstruction.create(Ili, ret, operand));
				} else if (isIntOp(llOp, 8)) {
					operand = new NumberOperand((((NumberOperand) operand).getValue() << 8) & 0xFF00);
					emitInstr(HLInstruction.create(Ili, ret, operand));
					*/
				} else
					assert false;
				return ret;
			} else if (isBoolOp(llOp)) {
				RegisterLocal regLocal = newTempRegister(instr, getTempSymbol(llOp), llOp.getType());
				AssemblerOperand ret = new RegisterTempOperand(regLocal);
				if (((NumberOperand) operand).getValue() == 0)
					emitInstr(HLInstruction.create(Iclr, ret));
				else
					emitInstr(HLInstruction.create(Iseto, ret));
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
		AssemblerOperand dest = new RegisterTempOperand(newTempRegister(instr, 
				getTempSymbol(llOp),
				llOp.getType()));
		return moveTo(llOp, operand, dest);
	}

	private ISymbol getTempSymbol(LLOperand llOp) {
		String baseName;
		if (llOp instanceof LLTempOp) {
			baseName = ((LLTempOp) llOp).getName();
		} else if (llOp instanceof LLSymbolOp)
			baseName = ((LLSymbolOp) llOp).getSymbol().getUniqueName();
		else
			baseName = "reg";
		ISymbol temp = locals.getScope().add(baseName, true);
		temp.setType(llOp != null ? llOp.getType() : ((LLTypedInstr) instr).getType());
		return temp;
	}

	private AssemblerOperand moveTo(LLOperand llOp, AssemblerOperand from, AssemblerOperand dest) {
		if (isIntOp(llOp) || isBoolOp(llOp)) {
			if (from.isRegister() || from.isMemory()) {
				int op = (llOp.getType().getBits() <= 8) ? Imovb : Imov;
				HLInstruction inst = HLInstruction.create(op, from, dest);
				emitInstr(inst);
			} else if (from instanceof NumberOperand) {
				HLInstruction inst = HLInstruction.create(Ili, dest, from);
				emitInstr(inst);
			} else if (from instanceof ISymbolOperand) {
				ILocal local = locals.getFinalLocal(((ISymbolOperand) from).getSymbol());
				if (local != null) {
					// it has a location
					
				} else {
					assert false;
				}
			} else {
				assert false;
			}
			return dest;
		}
		assert false;
		return dest;
	}
	private AssemblerOperand moveTo(LLType type, AssemblerOperand from, AssemblerOperand dest) {
		if (isIntType(type) || isBoolType(type)) {
			if (from.isRegister() || from.isMemory()) {
				int op = (type.getBits() <= 8) ? Imovb : Imov;
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
	
	private boolean isGeneralOperand(AssemblerOperand asmOp) {
		return asmOp.isRegister() || asmOp.isMemory();
		
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
					AssemblerOperand op1 = d.ops.length >= 1 ? asmOps[d.ops[0]] : null;
					AssemblerOperand op2 = d.ops.length >= 2 ? asmOps[d.ops[1]] : null;
					AssemblerOperand op3 = d.ops.length >= 3 ? asmOps[d.ops[2]] : null;
					HLInstruction inst = HLInstruction.create(d.inst, op1, op2, op3);
					
					emitInstr(inst);
				}


				if (d instanceof DoRes) {
					assert instr instanceof LLAssignInstr;
					
					AssemblerOperand dest = asmOps[d.ops[((DoRes) d).result]];
					ssaTempTable.put(((LLAssignInstr) instr).getResult(), dest);
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
