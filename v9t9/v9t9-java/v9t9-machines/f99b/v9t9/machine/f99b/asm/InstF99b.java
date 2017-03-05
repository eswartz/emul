/*
  InstF99b.java

  (c) 2010-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.asm;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ejs.base.utils.Pair;


/**
 * FORTH-99b processor opcodes
 * @author Ed
 */
public class InstF99b {
	
	/*public static final int Inop = 0;*/
	
	/** Branch forward (targPC > PC) measures from PC+1 (0 = NOP) 
	 * backward (targPC <= PC) also measures from PC+1 (0xF = self). 
	 */
	public static final int IbranchX = 0;
	/** Branch forward (targPC > PC) measures from PC+1 (0 = NOP) 
	 * backward (targPC <= PC) also measures from PC+1 (0xF = self). 
	 */
	public static final int I0branchX = 0x10;
	public static final int IlitX = 0x20;
	
	public static final int Istack_start = 0x30;
	
	public static final int Idup = Istack_start + 0;
	public static final int Iswap = Istack_start + 1;
	public static final int Idrop = Istack_start + 2;
	public static final int Irot = Istack_start + 3;
	public static final int Iover = Istack_start + 4;
	public static final int Iqdup = Istack_start + 5;
	public static final int Ispidx = Istack_start + 6;
	
	// [[GAP]] Imisc_start + 7 
	
	public static final int ItoR = Istack_start + 8;
	public static final int IRfrom = Istack_start + 9;
	public static final int Irdrop = Istack_start + 10;
	public static final int IatR = Istack_start + 11;
	public static final int Irpidx = Istack_start + 12;
	
	public static final int Ilpidx = Istack_start + 13;
	public static final int Ilocal = Istack_start + 14;
	public static final int Ilalloc = Istack_start + 15;


	public static final int Imath_start = 0x40;
	
	public static final int OP_ADD = 0;
	public static final int OP_SUB = 1;
	public static final int OP_LSH = 2;
	public static final int OP_RSH = 3;
	public static final int OP_ASH = 4;
	public static final int OP_CSH = 5;
	
	public static final int Iadd = Imath_start + OP_ADD;
	public static final int Isub = Imath_start + OP_SUB;
	
	public static final int Ilsh = Imath_start + OP_LSH;
	public static final int Irsh = Imath_start + OP_RSH;
	public static final int Iash = Imath_start + OP_ASH;
	public static final int Icsh = Imath_start + OP_CSH;
	
	/** um*: s * s -> d */
	public static final int Iumul = Imath_start + 6;
	/** um/mod: ud u1 -- mod div */
	public static final int Iudivmod = Imath_start + 7;


	public static final int OP_NEG = 8;
	public static final int OP_INV = 9;
	public static final int OP_1PLUS = 10;
	public static final int OP_2PLUS = 11;
	public static final int OP_1MINUS = 12;
	public static final int OP_2MINUS = 13;
	public static final int OP_2TIMES = 14;
	public static final int OP_2DIV = 15;

	public static final int Ineg = Imath_start + OP_NEG;
	public static final int Iinv = Imath_start + OP_INV;
	public static final int I1plus = Imath_start + OP_1PLUS;
	public static final int I2plus = Imath_start + OP_2PLUS;
	public static final int I1minus = Imath_start + OP_1MINUS;
	public static final int I2minus = Imath_start + OP_2MINUS;
	public static final int I2times = Imath_start + OP_2TIMES;
	public static final int I2div = Imath_start + OP_2DIV;
	
	//////
	public static final int Ilog_start = 0x50;
	
	public static final int Iand = Ilog_start + 0;
	public static final int Ior = Ilog_start + 1;
	public static final int Ixor = Ilog_start + 2;
	public static final int Inot = Ilog_start + 3;
	
	public static final int I0equ = Ilog_start + 4;
	public static final int Iequ = Ilog_start + 5;
	
	/** ( mask val -- val&~mask ) */
	public static final int Inand = Ilog_start + 6;

	// [[GAP]] Ilog_start + 7 


	public static final int CMP_LT = 0;
	public static final int CMP_LE = 1;
	public static final int CMP_GT = 2;
	public static final int CMP_GE = 3;
	public static final int CMP_ULT = 4;
	public static final int CMP_ULE = 5;
	public static final int CMP_UGT = 6;
	public static final int CMP_UGE = 7;
	
	public static final int Icmp = Ilog_start + 8;	// ... 15 (CMP_...); note Iequ and I0equ handle those
	
	public static final int Imem_start = 0x60;
	
	public static final int Iload = Imem_start + 0;
	public static final int Icload = Imem_start + 1;
	
	public static final int Istore = Imem_start + 2;
	public static final int Icstore = Imem_start + 3;

	public static final int IplusStore = Imem_start + 4;
	public static final int IcplusStore = Imem_start + 5;

	public static final int Iuser = Imem_start + 6;
	public static final int Iupidx = Imem_start + 7;
	
	public static final int Iloop_start = 0x68;
	
	/** next full word is jump offset */
	public static final int IloopUp = Iloop_start + 0;
//	public static final int IuloopUp = Iloop_start + 1;
	public static final int IplusLoopUp = Iloop_start + 2;
//	public static final int IuplusLoopUp = Iloop_start + 3;
	
	/** ( caddr len ch step ) */ 
	public static final int Icfill = Iloop_start + 4;
	/** ( caddr len ch step ) */ 
	public static final int Ifill = Iloop_start + 5;
	/** ( faddr taddr len fstep tstep ) */ 
	public static final int Icmove = Iloop_start + 6;
	/** ( faddr taddr len fstep tstep -- pos cmp ) */ 
	public static final int Iccompare = Iloop_start + 7;
	
	
	public static final int Imisc_start = 0x70;
	
	public static final int Iexit = Imisc_start + 0;
	public static final int Iexiti = Imisc_start + 1;
	public static final int Iexecute = Imisc_start + 2;
	public static final int Idovar = Imisc_start + 3;
	
	/** Idle until interrupt */
	public static final int SYSCALL_IDLE = 0;
	/** Start tracing */
	public static final int SYSCALL_DEBUG_ON = 1;
	/** Stop tracing */
	public static final int SYSCALL_DEBUG_OFF = 2;
	/** Register the given xt in the symbol table 
	 * ( name xt -- )  
	 */
	public static final int SYSCALL_REGISTER_SYMBOL = 3;
	/** Lookup the given string in the RAM dictionary
	 * ( caddr lfa -- caddr 0 | xt -1=immed | xt 1 ) 
	 */
	public static final int SYSCALL_FIND = 4;
	/** Lookup the given string in the GROM dictionary 
	 *
	 * ( caddr gDictEnd gDict -- caddr 0 | xt 1 | xt -1 )
	 */
	public static final int SYSCALL_GFIND = 5;
	/**
	 * Parse a number (>NUMBER), raw digits only
	 * 
	 * ( ud1 c-addr1 u1 base -- ud2 c-addr2 u2 )
	 */
	public static final int SYSCALL_NUMBER = 6;
	/**
	 * Parse a number (NUMBER) with sign, base conversions
	 * 
	 * ( c-addr1 u1 base -- ud  dpl t | f )
	 */
	public static final int SYSCALL_DECORATED_NUMBER = 7;
	/** 
	 * Spin for N cycles (cheap idle)
	 * 
	 * ( n -- )
	 */
	public static final int SYSCALL_SPIN = 8;
	
	public static final String[] syscallStrings = {
		"IDLE",
		"+DBG",
		"-DBG",
		"REGSYM",
		"FIND",
		"GFIND",
		">NUMBER",
		"(NUMBER)",
		"SPIN"
	};

	public static final int Isyscall = Imisc_start + 4;

	// [[GAP]] Imisc_start + 5 

	public static final int CTX_SP = 0;
	public static final int CTX_SP0 = 1;
	public static final int CTX_RP = 2;
	public static final int CTX_RP0 = 3;
	public static final int CTX_UP = 4;
	public static final int CTX_LP = 5;
	public static final int CTX_PC = 6;
	public static final int CTX_INT = 7;
	public static final int CTX_SR = 8;
	
	public static final String[] ctxStrings = {
		"SP",
		"SP0",
		"RP",
		"RP0",
		"UP",
		"LP",
		"PC",
		"INT",
		"SR"
	};

	/** @see CTX_... */
	public static final int IcontextFrom = Imisc_start + 6;
	/** @see CTX_... */
	public static final int ItoContext = Imisc_start + 7;
	
	public static final int Iimm_start = 0x78;
	
	/** next byte is pushed */
	public static final int IlitB = Iimm_start + 0;
	/** next word is pushed */
	public static final int IlitW = Iimm_start + 1;
	
	/** 
	 * Branch forward (targPC > PC) measures from PC+2 (0 = NOP) 
	 * backward (targPC <= PC) measures from PC+1 (0xFF = self). 
	 */
	public static final int IbranchB = Iimm_start + 2;
	/** 
	 * Branch forward (targPC > PC) measures from PC+3 (0 = NOP) 
	 * backward (targPC <= PC) measures from PC+1 (0xFFFF = self). 
	 */
	public static final int IbranchW = Iimm_start + 3;
	/** 
	 * Branch forward (targPC > PC) measures from PC+2 (0 = NOP) 
	 * backward (targPC <= PC) measures from PC+1 (0xF = self). 
	 */
	public static final int I0branchB = Iimm_start + 4;
	/** 
	 * Branch forward (targPC > PC) measures from PC+3 (0 = NOP) 
	 * backward (targPC <= PC) measures from PC+1 (0xFFFF = self). 
	 */
	public static final int I0branchW = Iimm_start + 5;
	
	public static final int Idouble = Iimm_start + 6;		// double-length the next
	public static final int Iext = Iimm_start + 7;		// TBD
	

	public static final int Icall = 0x80;  // ... 0xff

	////////////////////////
	
	public static final int IlitX_d = (Idouble << 8) + 0x20;
	
	public static final int Idstack_start = (Idouble << 8) + Istack_start;
	public static final int Idup_d = Idstack_start + 0;
	public static final int Iswap_d = Idstack_start + 1;
	public static final int Idrop_d = Idstack_start + 2;
	public static final int Irot_d = Idstack_start + 3;
	public static final int Iover_d = Idstack_start + 4;
	public static final int Iqdup_d = Idstack_start + 5;
	public static final int Ispidx_d = Idstack_start + 6;
	
	
	public static final int ItoR_d = Idstack_start + 8;
	public static final int IRfrom_d = Idstack_start + 9;
	public static final int Irdrop_d = Idstack_start + 10;
	public static final int IatR_d = Idstack_start + 11;
	public static final int Irpidx_d = Idstack_start + 12;
	
	public static final int Ilocal_d = Idstack_start + 13;
	
	public static final int ItoLocals = Idstack_start + 14;
	public static final int IfromLocals = Idstack_start + 15;


	public static final int Idmath_start =  (Idouble << 8) + 0x40;
	
	public static final int Iadd_d = Idmath_start + OP_ADD;
	public static final int Isub_d = Idmath_start + OP_SUB;
	
	public static final int Ilsh_d = Idmath_start + OP_LSH;
	public static final int Irsh_d = Idmath_start + OP_RSH;
	public static final int Iash_d = Idmath_start + OP_ASH;
	public static final int Icsh_d = Idmath_start + OP_CSH;
	
	/** um*: s * s -> d */
	public static final int Iumul_d = Idmath_start + 6;
	/** um/mod: ud u1 -- div mod */
	public static final int Iudivmod_d = Idmath_start + 7;

	public static final int Ineg_d = Idmath_start + OP_NEG;
	public static final int Iinv_d = Idmath_start + OP_INV;
	public static final int I1plus_d = Idmath_start + OP_1PLUS;
	public static final int I2plus_d = Idmath_start + OP_2PLUS;
	public static final int I1minus_d = Idmath_start + OP_1MINUS;
	public static final int I2minus_d = Idmath_start + OP_2MINUS;
	public static final int I2times_d = Idmath_start + OP_2TIMES;
	public static final int I2div_d = Idmath_start + OP_2DIV;
	

	//////
	public static final int Idlog_start = (Idouble << 8) + 0x50;
	
	public static final int Iand_d = Idlog_start + 0;
	public static final int Ior_d = Idlog_start + 1;
	public static final int Ixor_d = Idlog_start + 2;
	public static final int Inot_d = Idlog_start + 3;
	
	public static final int I0equ_d = Idlog_start + 4;
	public static final int Iequ_d = Idlog_start + 5;
	
	public static final int Inand_d = Idlog_start + 6;
	
	public static final int Icmp_d = Idlog_start + 8;	// ... 15
	
	public static final int Idmem_start = (Idouble << 8) + 0x60;
	
	public static final int Iload_d = Idmem_start + 0;
	public static final int Istore_d = Idmem_start + 2;
	public static final int IplusStore_d = Idmem_start + 4;
	public static final int Iuser_d = Idmem_start + 6;
	public static final int Iupidx_d = Idmem_start + 7;
	
	public static final int Idloop_start = (Idouble << 8) + 0x68;
	
	public static final int IloopUp_d = Idloop_start + 0;
//	public static final int IuloopUp_d = Idloop_start + 1;
	public static final int IplusLoopUp_d = Idloop_start + 2;
//	public static final int IuplusLoopUp_d = Idloop_start + 3;

	public static final int Idimm_start = (Idouble << 8) + 0x78;

	/** next byte is pushed */
	public static final int IlitB_d = Idimm_start + 0;
	/** next doubleword is pushed */
	public static final int IlitD_d = Idimm_start + 1;

	
	/** for each inst:  SP read, SP left
	 *	RP read, RP left;
	 *  neg means unsure
	 */
	static final int[] instArgs = {
		//Inop, 0, 0, 0, 0,
		
		Idup, 1, 2, 0, 0,
		Idup_d, 2, 4, 0, 0,
		Iqdup, 2, -1, 0, 0,
		Iqdup_d, 3, -1, 0, 0,
		
		Iload, 1, 1, 0, 0,
		Istore, 2, 0, 0, 0,
		Icload, 1, 1, 0, 0,
		Icstore, 2, 0, 0, 0,
		Iload_d, 1, 2, 0, 0,
		Istore_d, 3, 0, 0, 0,
		IplusStore, 2, 0, 0, 0,
		IcplusStore, 2, 0, 0, 0,
		IplusStore_d, 3, 0, 0, 0,
		Iuser, 1, 1, 0, 0,
		Iuser_d, 1, 2, 0, 0,
		Iupidx, 0, 1, 0, 0,
		Iupidx_d, 0, 2, 0, 0,

		Iswap, 2, 2, 0, 0,
		Iswap_d, 4, 4, 0, 0,
		Idrop, 1, 0, 0, 0,
		Idrop_d, 1, 0, 0, 0,
		Iover, 2, 3, 0, 0,
		Iover_d, 4, 6, 0, 0,
		Irot, 3, 3, 0, 0,
		Irot_d, 6, 6, 0, 0,
		
		ItoR, 1, 0, 0, 1,
		ItoR_d, 2, 0, 0, 2,
		IRfrom, 0, 1, 1, 0,
		IRfrom_d, 0, 2, 2, 0,
		IatR, 0, 1, 1, 1,
		IatR_d, 0, 2, 2, 2,
		Irdrop, 0, 0, 1, 0,
		Irdrop_d, 0, 0, 2, 0,
		Ispidx, 0, 1, 0, 0,
		Irpidx, 0, 1, 0, 0,
		Ispidx_d, 0, 2, 0, 0,
		Irpidx_d, 0, 2, 0, 0,
		
		ItoLocals, 0, 0, 0, 1,
		IfromLocals, 0, 0, 1, 0,
		Ilpidx, 0, 1, 0, 0,
		Ilocal, 0, 1, 0, 0,
		Ilocal_d, 0, 2, 0, 0,
		Ilalloc, -1, -1, -1, -1,

		I0branchX, 1, 0, 0, 0,
		I0branchB, 1, 0, 0, 0,
		I0branchW, 1, 0, 0, 0,
		IbranchX, 0, 0, 0, 0,
		IbranchB, 0, 0, 0, 0,
		IbranchW, 0, 0, 0, 0,
		
		IloopUp, 0, 1, 2, 2,
//		IuloopUp, 0, 1, 2, 2,
		IplusLoopUp, 1, 1, 2, 2,
//		IuplusLoopUp, 1, 1, 2, 2,
		IloopUp_d, 0, 1, 2, 2,
//		IuloopUp_d, 0, 1, 2, 2,
		IplusLoopUp_d, 1, 1, 2, 2,
//		IuplusLoopUp_d, 1, 1, 2, 2,
		
		Icfill, 4, 0, 0, 0,
		Ifill, 4, 0, 0, 0,
		Icmove, 5, 0, 0, 0,
		Iccompare, 5, 2, 0, 0,
		
		Iexit, 0, 0, 1, 0,
		Iexiti, 0, 0, 2, 0,
		IcontextFrom, 0, 1, 0, 0,
		ItoContext, 1, 0, 0, 0,
		
		I1plus, 1, 1, 0, 0,
		I2plus, 1, 1, 0, 0,
		I1plus_d, 2, 2, 0, 0,
		I2plus_d, 2, 2, 0, 0,
		I2times, 1, 1, 0, 0,
		I2div, 1, 1, 0, 0,
		I2times_d, 2, 2, 0, 0,
		I2div_d, 2, 2, 0, 0,
		Iadd, 2, 1, 0, 0,
		Iadd_d, 4, 2, 0, 0,
		Isub, 2, 1, 0, 0,
		Isub_d, 4, 2, 0, 0,
		Iumul, 2, 2, 0, 0,
		Iudivmod, 3, 2, 0, 0,
		Iumul_d, 4, 4, 0, 0,
		Iudivmod_d, 6, 4, 0, 0,

		I1plus, 1, 1, 0, 0,
		I2plus, 1, 1, 0, 0,
		I1minus, 1, 1, 0, 0,
		I2minus, 1, 1, 0, 0,
		I1plus_d, 2, 2, 0, 0,
		I2plus_d, 2, 2, 0, 0,
		I1minus_d, 2, 2, 0, 0,
		I2minus_d, 2, 2, 0, 0,
		
		Ineg, 1, 1, 0, 0,
		Ineg_d, 2, 2, 0, 0,
		Inot, 1, 1, 0, 0,
		Inot_d, 2, 2, 0, 0,
		Iand, 2, 1, 0, 0,
		Ior, 2, 1, 0, 0,
		Ixor, 2, 1, 0, 0,
		Inand, 2, 1, 0, 0,
		
		Iinv, 1, 1, 0, 0,
		Iand_d, 4, 2, 0, 0,
		Ior_d, 4, 2, 0, 0,
		Ixor_d, 4, 2, 0, 0,
		Inand_d, 4, 2, 0, 0,
		Iinv_d, 2, 2, 0, 0,
		
		Ilsh, 2, 1, 0, 0,
		Ilsh_d, 4, 2, 0, 0,
		Iash, 2, 1, 0, 0,
		Iash_d, 4, 1, 0, 0,
		Irsh, 2, 1, 0, 0,
		Irsh_d, 4, 1, 0, 0,
		Icsh, 2, 1, 0, 0,
		Icsh_d, 4, 1, 0, 0,
		I0equ, 1, 1, 0, 0,
		I0equ_d, 2, 1, 0, 0,
		Iequ, 2, 1, 0, 0,
		Iequ_d, 4, 1, 0, 0,

		Icmp, 2, 1, 0, 0,
		Icmp+1, 2, 1, 0, 0,
		Icmp+2, 2, 1, 0, 0,
		Icmp+3, 2, 1, 0, 0,
		Icmp+4, 2, 1, 0, 0,
		Icmp+5, 2, 1, 0, 0,
		Icmp+6, 2, 1, 0, 0,
		Icmp+7, 2, 1, 0, 0,
		Icmp_d, 4, 1, 0, 0,
		Icmp_d+1, 4, 1, 0, 0,
		Icmp_d+2, 4, 1, 0, 0,
		Icmp_d+3, 4, 1, 0, 0,
		Icmp_d+4, 4, 1, 0, 0,
		Icmp_d+5, 4, 1, 0, 0,
		Icmp_d+6, 4, 1, 0, 0,
		Icmp_d+7, 4, 1, 0, 0,
		
		Iexecute, 1, -1, -1, -1,
		Idovar, 0, 0, 1, 0,
		Icall, 1, -1, -1, -1,
		Isyscall, 1, -1, -1, -1,
		Idouble, -1, -1, -1, -1,
		Iext, -1, -1, -1, -1,
		
		IlitX, 0, 1, 0, 0,
		IlitB, 0, 1, 0, 0,
		IlitW, 0, 1, 0, 0,
		IlitX_d, 0, 2, 0, 0,
		IlitB_d, 0, 2, 0, 0,
		IlitD_d, 0, 2, 0, 0,
		
	};

	static final Map<Integer, String> instNames = new HashMap<Integer, String>(64);
	
	static {
		Field[] fields = InstF99b.class.getDeclaredFields();
		Set<Integer> vals = new TreeSet<Integer>();
		
		for (Field field : fields) {
			if (field.getName().endsWith("_start") || !field.getName().startsWith("I"))
				continue;
			Integer val;
			try {
				val = (Integer) field.get(InstF99b.class);
			} catch (Exception e) {
				throw new IllegalArgumentException();
			}
			if (vals.contains(val))
				throw new IllegalStateException("field " + field.getName() + " duplicates value " + val);
			instNames.put(val, field.getName().substring(1).toUpperCase());
			vals.add(val);
				
			if (getStackEffects(val) == null)
				throw new IllegalStateException("opcode " + field.getName() + " has no stack effects");
		}
		
		
		instNames.put(Icmp+CMP_GE, "CMP>=");
		instNames.put(Icmp+CMP_GT, "CMP>");
		instNames.put(Icmp+CMP_UGE, "CMPU>=");
		instNames.put(Icmp+CMP_UGT, "CMPU>");
		instNames.put(Icmp+CMP_LE, "CMP<=");
		instNames.put(Icmp+CMP_LT, "CMP<");
		instNames.put(Icmp+CMP_ULE, "CMPU<=");
		instNames.put(Icmp+CMP_ULT, "CMPU<");
		instNames.put(Icmp_d+CMP_GE, "DCMP>=");
		instNames.put(Icmp_d+CMP_GT, "DCMP>");
		instNames.put(Icmp_d+CMP_UGE, "DCMPU>=");
		instNames.put(Icmp_d+CMP_UGT, "DCMPU>");
		instNames.put(Icmp_d+CMP_LE, "DCMP<=");
		instNames.put(Icmp_d+CMP_LT, "DCMP<");
		instNames.put(Icmp_d+CMP_ULE, "DCMPU<=");
		instNames.put(Icmp_d+CMP_ULT, "DCMPU<");
		
		
		instNames.put(Istore, "!");
		instNames.put(Icstore, "C!");
		instNames.put(Istore_d, "D!");
		instNames.put(Iload, "@");
		instNames.put(Icload, "C@");
		instNames.put(Iload_d, "D@");
		
		instNames.put(IplusStore, "+!");
		instNames.put(IcplusStore, "C+!");
		instNames.put(IplusStore_d, "D+!");
		
		instNames.put(ItoR, ">R");
		instNames.put(ItoR_d, "2>R");
		instNames.put(IRfrom, "R>");
		instNames.put(IRfrom_d, "2R>");
		instNames.put(IatR, "R@");
		instNames.put(Idup_d, "2DUP");
		instNames.put(Iqdup, "?DUP");
		
		instNames.put(Iadd, "+");
		instNames.put(Isub, "-");
		instNames.put(Iumul, "U*");
		instNames.put(Iudivmod, "U/MOD");

		instNames.put(Iadd_d, "D+");
		instNames.put(Isub_d, "D+");
		instNames.put(Iumul_d, "DU*");
		instNames.put(Iudivmod_d, "DU/MOD");
		
		instNames.put(Ilsh_d, "DLSH");
		instNames.put(Irsh_d, "DCSH");
		instNames.put(Iash_d, "DASH");
		instNames.put(Icsh_d, "DCSH");
		
		instNames.put(Iequ, "=");
		instNames.put(Iequ_d, "D=");
		instNames.put(I0equ, "0=");
		instNames.put(I0equ_d, "D0=");
		
		instNames.put(IloopUp, "(LOOP)");
		instNames.put(IloopUp_d, "(DLOOP)");
		instNames.put(IplusLoopUp, "(+LOOP)");
//		instNames.put(IuplusLoopUp, "(U+LOOP)");
		instNames.put(IplusLoopUp_d, "(D+LOOP)");
//		instNames.put(IuplusLoopUp_d, "(DU+LOOP)");
		
		instNames.put(IcontextFrom, "(CONTEXT>)");
		instNames.put(ItoContext, "(>CONTEXT)");
		
		instNames.put(IlitX, "LIT4");
		instNames.put(IlitB, "LIT8");
		instNames.put(IlitW, "LIT16");
		
		instNames.put(IlitX_d, "DLIT4");
		instNames.put(IlitB_d, "DLIT8");
		instNames.put(IlitD_d, "DLIT32");
		
		instNames.put(ItoLocals, ">LOCALS");
		instNames.put(IfromLocals, "LOCALS>");
	}

	/**
	 * @param inst
	 * @return
	 */
	public static String getInstName(int inst) {
		return instNames.get(inst);
	}

	/** Get pair for items read and items left;
	 * negative values mean the actual amount is unknown (dependent on instruction) */
	public static Pair<Integer, Integer> getStackEffects(int inst) {
		for (int i = 0; i < instArgs.length; i += 5) {
			if (instArgs[i] == inst) {
				return new Pair<Integer, Integer>(instArgs[i+1], instArgs[i+2]);
			}
		}
		return null;
	}
	/** Get pair for items read and items left */
	public static Pair<Integer, Integer> getReturnStackEffects(int inst) {
		for (int i = 0; i < instArgs.length; i += 5) {
			if (instArgs[i] == inst) {
				return new Pair<Integer, Integer>(instArgs[i+3], instArgs[i+4]);
			}
		}
		return null;
	}
}
