/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import static v9t9.engine.cpu.InstructionTable.Ia;
import static v9t9.engine.cpu.InstructionTable.Iab;
import static v9t9.engine.cpu.InstructionTable.Iai;
import static v9t9.engine.cpu.InstructionTable.Iandi;
import static v9t9.engine.cpu.InstructionTable.Ic;
import static v9t9.engine.cpu.InstructionTable.Icb;
import static v9t9.engine.cpu.InstructionTable.Ici;
import static v9t9.engine.cpu.InstructionTable.Iclr;
import static v9t9.engine.cpu.InstructionTable.Idec;
import static v9t9.engine.cpu.InstructionTable.Idect;
import static v9t9.engine.cpu.InstructionTable.Idiv;
import static v9t9.engine.cpu.InstructionTable.Iinc;
import static v9t9.engine.cpu.InstructionTable.Iinct;
import static v9t9.engine.cpu.InstructionTable.Ili;
import static v9t9.engine.cpu.InstructionTable.Impy;
import static v9t9.engine.cpu.InstructionTable.Ineg;
import static v9t9.engine.cpu.InstructionTable.Iori;
import static v9t9.engine.cpu.InstructionTable.Is;
import static v9t9.engine.cpu.InstructionTable.Isb;
import static v9t9.engine.cpu.InstructionTable.Isla;
import static v9t9.engine.cpu.InstructionTable.Isoc;
import static v9t9.engine.cpu.InstructionTable.Isocb;
import static v9t9.engine.cpu.InstructionTable.Isra;
import static v9t9.engine.cpu.InstructionTable.Isrc;
import static v9t9.engine.cpu.InstructionTable.Isrl;
import static v9t9.engine.cpu.InstructionTable.Iszc;
import static v9t9.engine.cpu.InstructionTable.Iszcb;
import static v9t9.engine.cpu.InstructionTable.Ixor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.ITarget.Intrinsic;
import org.ejs.eulang.types.BasicType;

/**
 * @author ejs
 *
 */
public class InstrSelectionTable {

	/** These ll instructions are handled specially; do not make patterns for them */
	static final Pattern hardcodedInstrs = 
		Pattern.compile("\\b(load|store|call|ret|br|switch|phi|getelementptr|insertvalue|extractvalue)\\b");
	/** Raw patterns.  These are converted at runtime. */ 
	static final InstrSelection.IPattern[] patterns = {
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "bitcast", 
				new InstrSelection.If[] { InstrSelection.If.PASS },
				new InstrSelection.As[] { InstrSelection.As.GEN_R },
				new InstrSelection.DoRes( 0, -1, 0 )
		),
		
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "trunc", 
				new InstrSelection.If[] { 
					InstrSelection.If.PASS,
					InstrSelection.If.IS_I8,
				},
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM_8 }, 
				new InstrSelection.DoRes( 0, Isla, 0, 1 ) 
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "sext", 
				new InstrSelection.If[] { 
					InstrSelection.If.PASS,
					InstrSelection.If.IS_I16,
				},
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM_8 }, 
				new InstrSelection.DoRes( 0, Isra, 0, 1 ) 
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "zext", 
				new InstrSelection.If[] { 
					InstrSelection.If.PASS,
					InstrSelection.If.IS_I16,
				},
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM_8 }, 
				new InstrSelection.DoRes( 0, Isrl, 0, 1 ) 
		),
		
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "add", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_1 },
				new InstrSelection.As[] { InstrSelection.As.GEN_RW }, 
				new InstrSelection.DoRes( 0, Iinc, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "add", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_2 },
				new InstrSelection.As[] { InstrSelection.As.GEN_RW }, 
				new InstrSelection.DoRes( 0, Iinct, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "add", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST },
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM }, 
				new InstrSelection.DoRes( 0, Iai, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "add", 
		 		null,
		 		new InstrSelection.As[] { InstrSelection.As.GEN_RW, InstrSelection.As.GEN_R }, 
		 		new InstrSelection.DoRes( 1, Ia, 1, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "add", 
				null,
				new InstrSelection.As[] { InstrSelection.As.GEN_RW, InstrSelection.As.GEN_R }, 
				new InstrSelection.DoRes( 1, Iab, 1, 0 )
		),
		
		// negative constants
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "sub", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_1 },
				new InstrSelection.As[] { InstrSelection.As.GEN_RW }, 
				new InstrSelection.DoRes( 0, Idec, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "sub", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_2 },
				new InstrSelection.As[] { InstrSelection.As.GEN_RW }, 
				new InstrSelection.DoRes( 0, Idect, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "sub", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_N1 },
				new InstrSelection.As[] { InstrSelection.As.GEN_RW }, 
				new InstrSelection.DoRes( 0, Iinc, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "sub", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_N2 },
				new InstrSelection.As[] { InstrSelection.As.GEN_RW }, 
				new InstrSelection.DoRes( 0, Iinct, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "sub", 
				new InstrSelection.If[] { InstrSelection.If.IS_CONST_0, InstrSelection.If.IS_CONST },
				new InstrSelection.As[] { InstrSelection.As.REG_W, InstrSelection.As.IMM_NEG }, 
				new InstrSelection.DoRes( 0, Ili, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16 | InstrSelection.I8, "sub", 
				 new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST },
				 new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM_NEG }, 
				 new InstrSelection.DoRes( 0, Iai, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "sub", 
		 		null,
		 		new InstrSelection.As[] { InstrSelection.As.GEN_RW, InstrSelection.As.GEN_R }, 
		 		new InstrSelection.DoRes( 1, Is, 1, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "sub", 
				null,
				new InstrSelection.As[] { InstrSelection.As.GEN_RW, InstrSelection.As.GEN_R }, 
				new InstrSelection.DoRes( 1, Isb, 1, 0 )
		),
		
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8, "and", 
				 new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST },
				 new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM }, 
				 new InstrSelection.DoRes( 0, Iandi, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "and", 
		 		null,
		 		new InstrSelection.As[] { InstrSelection.As.GEN_RW, InstrSelection.As.GEN_R }, 
		 		new InstrSelection.DoRes( 1, Iszc, 1, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "and", 
				null,
				new InstrSelection.As[] { InstrSelection.As.GEN_RW, InstrSelection.As.GEN_R }, 
				new InstrSelection.DoRes( 1, Iszcb, 1, 0 )
		),
	
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8, "or", 
				 new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST },
				 new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM }, 
				 new InstrSelection.DoRes( 0, Iori, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "or", 
		 		null,
		 		new InstrSelection.As[] { InstrSelection.As.GEN_RW, InstrSelection.As.GEN_R }, 
		 		new InstrSelection.DoRes( 1, Isoc, 1, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8|InstrSelection.I1, "or", 
				null,
				new InstrSelection.As[] { InstrSelection.As.GEN_RW, InstrSelection.As.GEN_R }, 
				new InstrSelection.DoRes( 1, Isocb, 1, 0 )
		),
		
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "xor", 
		 		null,
		 		new InstrSelection.As[] { InstrSelection.As.GEN_RW, InstrSelection.As.GEN_R }, 
		 		new InstrSelection.DoRes( 1, Ixor, 1, 0 )
		),
		
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "shl", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_0 },
		 		null
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "shl", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_16 },
				new InstrSelection.As[] { InstrSelection.As.IMM_0 },
				new InstrSelection.DoRes( 0, -1, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "shl", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_1_15 },
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM }, 
				new InstrSelection.DoRes( 0, Isla, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "shl", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.PASS },
		 		new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.REG_0_CNT_W },
		 		new InstrSelection.DoRes( 0, Isla, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "ashr", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_0 },
		 		null
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "ashr", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_16 },
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.REG_0_CNT_W },
				new InstrSelection.Do( Iclr, 1 ),
				new InstrSelection.DoRes( 0, Isra, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "ashr", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_1_15 },
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM }, 
				new InstrSelection.DoRes( 0, Isra, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "ashr", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.PASS },
		 		new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.REG_0_CNT_W },
		 		new InstrSelection.DoRes( 0, Isra, 0, 1 )
		),
		
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "lshr", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_0 },
		 		null
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "lshr", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_16 },
				new InstrSelection.As[] { InstrSelection.As.IMM_0 },
				new InstrSelection.DoRes( 0, -1, 0  )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "lshr", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_1_15 },
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM }, 
				new InstrSelection.DoRes( 0, Isrl, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "lshr", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.PASS },
		 		new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.REG_0_CNT_W },
		 		new InstrSelection.DoRes( 0, Isrl, 0, 1 )
		),
		
		// synthetic instr generated by intrinsic
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "src", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_0 },
		 		null
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "src", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_16 },
		 		null
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "src", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_1_15 },
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM }, 
				new InstrSelection.DoRes( 0, Isrc, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "src", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.PASS },
		 		new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.REG_0_CNT_W },
		 		new InstrSelection.DoRes( 0, Isrc, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "src", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_0 },
		 		null
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "src", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_16 },
		 		null
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "src", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_1_15 },
				new InstrSelection.As[] { InstrSelection.As.REG_RW_DUP, InstrSelection.As.IMM }, 
				new InstrSelection.DoRes( 0, Isrc, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "src", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.PASS },
		 		new InstrSelection.As[] { InstrSelection.As.REG_RW_DUP, InstrSelection.As.REG_0_CNT_W },
				new InstrSelection.DoRes( 0, Isrc, 0, 1 )
		),
		
		// another variant
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "slc", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_0 },
		 		null
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "slc", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_16 },
		 		null
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "slc", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_1_15 },
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM_NEG_15 }, 
				new InstrSelection.DoRes( 0, Isrc, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16, "slc", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.PASS },
		 		new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.REG_0_CNT_W },
		 		new InstrSelection.Do( Ineg, 1 ),
		 		new InstrSelection.DoRes( 0, Isrc, 0, 1 )
		),
		
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "slc", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_0 },
		 		null
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "slc", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_16 },
		 		null
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "slc", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_1_15 },
				new InstrSelection.As[] { InstrSelection.As.REG_RW_DUP, InstrSelection.As.IMM_NEG_15 }, 
				new InstrSelection.DoRes( 0, Isrc, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I8, "slc", 
		 		new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.PASS },
		 		new InstrSelection.As[] { InstrSelection.As.REG_RW_DUP, InstrSelection.As.REG_0_CNT_W },
		 		new InstrSelection.Do( Ineg, 1 ),
				new InstrSelection.DoRes( 0, Isrc, 0, 1 )
		),
		
		new InstrSelection.IPattern( BasicType.BOOL, InstrSelection.I1, "icmp", 
				new InstrSelection.If[] { InstrSelection.If.IS_I16, 
					InstrSelection.If.IS_CONST_0 },
				new InstrSelection.As[] { InstrSelection.As.REG_R, InstrSelection.As.CMP, InstrSelection.As.REG_W },
				new InstrSelection.Do( Ic, 0, 0 ),
				new InstrSelection.DoRes( 1, InstrSelection.Piset, 1, 2 )
		),
		new InstrSelection.IPattern( BasicType.BOOL, InstrSelection.I1, "icmp", 
				new InstrSelection.If[] { InstrSelection.If.IS_INT, 
				InstrSelection.If.IS_CONST_0 },
				new InstrSelection.As[] { InstrSelection.As.REG_R, InstrSelection.As.CMP, InstrSelection.As.REG_W },
				new InstrSelection.Do( Icb, 0, 0 ),
				new InstrSelection.DoRes( 1, InstrSelection.Piset, 1, 2 )
		),
		new InstrSelection.IPattern( BasicType.BOOL, InstrSelection.I1, "icmp", 
				new InstrSelection.If[] { InstrSelection.If.IS_I16, 
				InstrSelection.If.IS_CONST },
				new InstrSelection.As[] { InstrSelection.As.REG_R, InstrSelection.As.IMM, InstrSelection.As.CMP, InstrSelection.As.REG_W },
				new InstrSelection.Do( Ici, 0, 1 ),
				new InstrSelection.DoRes( 1, InstrSelection.Piset, 2, 3 )
		),
		new InstrSelection.IPattern( BasicType.BOOL, InstrSelection.I1, "icmp", 
				new InstrSelection.If[] { InstrSelection.If.IS_INT,	// handles 8 or 1 
				InstrSelection.If.IS_CONST },
				new InstrSelection.As[] { InstrSelection.As.REG_R, InstrSelection.As.CONST_POOL, InstrSelection.As.CMP, InstrSelection.As.REG_W },
				new InstrSelection.Do( Icb, 0, 1 ),
				new InstrSelection.DoRes( 1, InstrSelection.Piset, 2, 3 )
		),
		new InstrSelection.IPattern( BasicType.BOOL, InstrSelection.I1, "icmp", 
				new InstrSelection.If[] { InstrSelection.If.IS_I16, 
				InstrSelection.If.PASS },
				new InstrSelection.As[] { InstrSelection.As.REG_R, InstrSelection.As.REG_R, InstrSelection.As.CMP, InstrSelection.As.REG_W },
				new InstrSelection.Do( Ic, 0, 1 ),
				new InstrSelection.DoRes( 1, InstrSelection.Piset, 2, 3 )
		),
		
		new InstrSelection.IPattern( BasicType.BOOL, InstrSelection.I1, "icmp", 
				new InstrSelection.If[] { InstrSelection.If.IS_INT,	// handles 8 and 1 
				InstrSelection.If.PASS },
				new InstrSelection.As[] { InstrSelection.As.GEN_R, InstrSelection.As.REG_R, InstrSelection.As.CMP, InstrSelection.As.REG_W },
				new InstrSelection.Do( Icb, 0, 1 ),
				new InstrSelection.DoRes( 1, InstrSelection.Piset, 2, 3 )
		),
	
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "mul", 
				 new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_0 },
				 new InstrSelection.As[] { InstrSelection.As.IMM_0 },
				 new InstrSelection.DoRes( 0, -1, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "mul", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_1 },
				new InstrSelection.As[] { InstrSelection.As.GEN_R },
				new InstrSelection.DoRes( 0, -1, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "mul", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_POW_2 },
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM_LOG_2 }, 
				new InstrSelection.DoRes( 0, Isla, 0, 1 )
		),
	
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "mul", 
				 new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.PASS },
				 new InstrSelection.As[] { InstrSelection.As.REG_HI_W, InstrSelection.As.REG_R, InstrSelection.As.REG_LO_W },
				 new InstrSelection.DoRes( 2, Impy, 1, 0, 2 )	// fake 3rd op
		),
	
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "sdiv", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_1 },
				new InstrSelection.As[] { InstrSelection.As.GEN_R },
				new InstrSelection.DoRes( 0, -1, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "udiv", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_1 },
				new InstrSelection.As[] { InstrSelection.As.GEN_R },
				new InstrSelection.DoRes( 0, -1, 0 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "sdiv", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_POW_2 },
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM_LOG_2 }, 
				new InstrSelection.DoRes( 0, Isra, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "udiv", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.IS_CONST_POW_2 },
				new InstrSelection.As[] { InstrSelection.As.REG_RW, InstrSelection.As.IMM_LOG_2 }, 
				new InstrSelection.DoRes( 0, Isrl, 0, 1 )
		),
	
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "udiv", 
				 new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.PASS },
				 new InstrSelection.As[] { InstrSelection.As.REG_LO_W, InstrSelection.As.GEN_R, InstrSelection.As.REG_HI_W },
				 new InstrSelection.Do( Iclr, 2 ),
				 new InstrSelection.DoRes( 2, Idiv, 1, 2, 0 )		// fake 3rd op
		),
		
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "sdiv", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.PASS },
				new InstrSelection.As[] { InstrSelection.As.GEN_R, InstrSelection.As.GEN_R },
				new InstrSelection.DoIntrinsic( Intrinsic.SIGNED_DIVISION, 0, 1 )
		),
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "urem", 
				 new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.PASS },
				 new InstrSelection.As[] { InstrSelection.As.REG_LO_W, InstrSelection.As.GEN_R, InstrSelection.As.REG_HI_W },
				 new InstrSelection.Do( Iclr, 2 ),
				 new InstrSelection.DoRes( 1, Idiv, 1, 2, 0 )		// fake 3rd op
		),
		
		new InstrSelection.IPattern( BasicType.INTEGRAL, InstrSelection.I16|InstrSelection.I8|InstrSelection.I1, "srem", 
				new InstrSelection.If[] { InstrSelection.If.PASS, InstrSelection.If.PASS },
				new InstrSelection.As[] { InstrSelection.As.GEN_R, InstrSelection.As.GEN_R },
				new InstrSelection.DoIntrinsic( Intrinsic.SIGNED_REMAINDER, 0, 1)
		),
	};
	static HashMap<Pair<Pair<BasicType, Integer>, String>, List<InstrSelection.IPattern>> patternMap;
	static List<InstrSelection.IPattern> otherPatterns;
	/**
	 * Arrange patterns for quick lookup.  Those with well-known types
	 * go into patternMap, while others are in otherPatterns and scanned
	 * linearly.
	 */
	static void setupPatterns() {
		if (patternMap != null)
			return;
		
		patternMap = new LinkedHashMap<Pair<Pair<BasicType, Integer>, String>, List<InstrSelection.IPattern>>();
		otherPatterns = new ArrayList<InstrSelection.IPattern>();
		for (InstrSelection.IPattern pattern : patterns) {
	
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
					List<InstrSelection.IPattern> list = patternMap.get(key);
					if (list == null) {
						list = new ArrayList<InstrSelection.IPattern>();
						patternMap.put(key, list);
					}
					list.add(pattern);
				}
			}
		}
	}

}
