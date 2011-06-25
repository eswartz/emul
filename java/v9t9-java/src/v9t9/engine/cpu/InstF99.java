/**
 * Oct 14 2010
 */
package v9t9.engine.cpu;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ejs.coffee.core.utils.Pair;

/**
 * FORTH-99 processor opcodes
 * @author Ed
 */
public class InstF99 {
	
	public static final int Inop = 0;
	public static final int _Iext = 32;
	
	public static final int Iarith_start = 1;
	
	public static final int Ibinop = Iarith_start + 0;
	public static final int Ibinop_d = Ibinop + _Iext;

	public static final int Iunaryop = Iarith_start + 1;
	public static final int Iunaryop_d = Iunaryop + _Iext;

	public static final int Iadd = Iarith_start + 2;
	//public static final int Iadd_d = Iadd + _Iext;
	//public static final int Isub = Iarith_start + 2;
	//public static final int Isub_d = Isub + _Iext;
	//public static final int I2times = Iarith_start + 2;
	//public static final int I2div = I2times + _Iext;
	//public static final int Ilsh_d = Ilsh + _Iext;
	//public static final int Iash = Iarith_start + 3;  // signed
	//public static final int Irsh = Iash + _Iext;  // unsigned
	/** um*: s * s -> d */
	public static final int Iumul = Iarith_start + 3;
	/** um/mod: ud u1 -- div mod */
	public static final int Iudivmod = Iumul + _Iext;

	//public static final int I1plus = Iarith_start + 4;
	//public static final int I2plus = I1plus + _Iext;
	
	//////
	public static final int Ilog_start = Iarith_start + 4;
	
	//public static final int Iand = Ilog_start + 0;

	//public static final int Ior = Ilog_start + 1;
	//public static final int Ixor = Ior + _Iext;
	//public static final int Iinvert = Ilog_start + 0;
	
	public static final int I0cmp = Ilog_start + 0;
	public static final int I0cmp_d = I0cmp + _Iext;
	public static final int Icmp = Ilog_start + 1;
	public static final int Icmp_d = Icmp + _Iext;
	public static final int I0equ = Ilog_start + 2;
	public static final int I0equ_d = I0equ + _Iext;
	public static final int Iequ = Ilog_start + 3;
	public static final int Iequ_d = Iequ + _Iext;
	
	public static final int Imem_start = Ilog_start + 4;
	
	public static final int Iload = Imem_start + 0;
	public static final int Icload = Iload + _Iext;
	
	public static final int Istore = Imem_start + 1;
	public static final int Icstore = Istore + _Iext;

	public static final int Iload_d = Imem_start + 2;
	public static final int Istore_d = Iload_d + _Iext;

	public static final int IplusStore = Imem_start + 3;
	public static final int IplusStore_d = IplusStore + _Iext;

	
	//////
	public static final int Istack_start = Imem_start + 4;
	
	public static final int Ispidx = Istack_start + 0;
	public static final int Iqdup = Ispidx + _Iext;
	public static final int Irpidx = Istack_start + 1;
	public static final int Idrop = Istack_start + 2;
	public static final int Iuser = Idrop + _Iext;
	public static final int Idup = Istack_start + 3;
	public static final int Idup_d = Idup + _Iext;
	public static final int Iswap = Istack_start + 4;
	public static final int Iswap_d = Iswap + _Iext;

	public static final int Iover = Istack_start + 5;
	public static final int Irot = Iover + _Iext;
	public static final int ItoR = Istack_start + 6;
	public static final int ItoR_d = ItoR + _Iext;
	public static final int IRfrom = Istack_start + 7;
	public static final int IRfrom_d = IRfrom + _Iext;
	
	public static final int Iexit = Istack_start + 8;
	public static final int Iexiti = Iexit + _Iext;

	/** next full word is jump offset */
	public static final int I0branch = Istack_start + 9;
	/** next full word is offset in words */
	public static final int Ibranch = I0branch + _Iext;		

	/** next full word is jump offset */
	public static final int I0branch_f = Istack_start + 10;
	/** next full word is offset in words */
	public static final int Ibranch_f = I0branch_f + _Iext;		

	/** next full word is jump offset */
	public static final int Iloop = Istack_start + 11;
	public static final int IplusLoop = Iloop + _Iext;

	public static final int Irdrop = Istack_start + 12;
	public static final int IatR = Irdrop + _Iext;
	
	//////
	public static final int Ispecial_start = Istack_start + 13;

	public static final int Iexecute = Ispecial_start + 0;
	public static final int IuplusLoop = Iexecute + _Iext;

	/** @see CTX_... */
	public static final int IcontextFrom = Ispecial_start + 1;
	/** @see CTX_... */
	public static final int ItoContext = IcontextFrom + _Iext;

	/** next full word is pushed */
	public static final int Ilit = Ispecial_start + 2;
	public static final int Ilit_d = Ilit + _Iext;
	/** next signed field is pushed */
	public static final int IfieldLit = Ispecial_start + 3;
	public static final int IfieldLit_d = IfieldLit + _Iext;

	public static final int _Ilast = IfieldLit + 1;
	
	public static final int Iext = 31;
	
	public static final int Icall = 32;

   
   

	public static final int CTX_SP = 0;
	public static final int CTX_SP0 = 1;
	public static final int CTX_RP = 2;
	public static final int CTX_RP0 = 3;
	public static final int CTX_UP = 4;
	public static final int CTX_PC = 6;
	public static final int CTX_INT = 7;
	
	public static final String[] ctxStrings = {
		"SP",
		"SP0",
		"RP",
		"RP0",
		"UP",
		"UP0",
		"PC",
	};
	public static final int OP_ADD = 0;
	public static final int OP_SUB = 1;
	public static final int OP_AND = 2;
	public static final int OP_OR = 3;
	public static final int OP_XOR = 4;
	public static final int OP_LSH = 5;
	public static final int OP_RSH = 6;
	public static final int OP_ASH = 7;
	public static final int OP_CSH = 8;
	
	public static final int OP_NEG = 16;
	public static final int OP_INV = 17;
	public static final int OP_NOT = 18;
	public static final int OP_1PLUS = 19;
	public static final int OP_2PLUS = 20;
	public static final int OP_1MINUS = 21;
	public static final int OP_2MINUS = 22;
	public static final int OP_2TIMES = 23;
	public static final int OP_2DIV = 24;
	
	public static final String[] opStrings = {
	    	"+",
	    	"-",
	    	"AND",
	    	"OR",
	    	"XOR",
	    	"LSH",
	    	"RSH",
	    	"ASH",
	    	"CSH",
	    	null,	// 9
	    	null, 	//10
	    	null, 	//11
	    	null, 	//12
	    	null, 	//13
	    	null, 	//14
	    	null, 	//15
	    	
	    	"NEG",
	    	"INV",
	    	"NOT",
	    	"1+",
	    	"2+",
	    	"1-",
	    	"2-",
	    	"2*",
	    	"2/",
	    };
	 
	public static final int CMP_LT = 0;
	public static final int CMP_LE = 1;
	public static final int CMP_GT = 2;
	public static final int CMP_GE = 3;
	public static final int CMP_ULT = 4;
	public static final int CMP_ULE = 5;
	public static final int CMP_UGT = 6;
	public static final int CMP_UGE = 7;
	
	public static final String[] cmpStrings = {
	    	"<",
	    	"<=",
	    	">",
	    	">=",
	    	"U<",
	    	"U<=",
	    	"U>",
	    	"U>=",
	    };

	/** for each inst:  SP read, SP left
	 *	RP read, RP left;
	 *  neg means unsure
	 */
	static final int[] instArgs = {
		Inop, 0, 0, 0, 0,
		Idup, 1, 2, 0, 0,
		Iqdup, 2, -1, 0, 0,
		Iload, 1, 1, 0, 0,
		Istore, 2, 0, 0, 0,
		Icload, 1, 1, 0, 0,
		Icstore, 2, 0, 0, 0,
		Iload_d, 1, 2, 0, 0,
		Istore_d, 3, 0, 0, 0,
		IplusStore, 2, 0, 0, 0,
		IplusStore_d, 3, 0, 0, 0,
		IfieldLit, 0, 1, 0, 0,
		I0branch, 1, 0, 0, 0,
		I0branch_f, 1, 0, 0, 0,
		Ibranch, 0, 0, 0, 0,
		Ibranch_f, 0, 0, 0, 0,
		Iloop, 0, 0, 2, 2,
		IplusLoop, 1, 0, 2, 2,
		IuplusLoop, 1, 0, 2, 2,
		Iover, 2, 3, 0, 0,
		Irot, 3, 3, 0, 0,
		ItoR, 1, 0, 0, 1,
		ItoR_d, 2, 0, 0, 2,
		IRfrom, 0, 1, 1, 0,
		IRfrom_d, 0, 2, 2, 0,
		IatR, 0, 1, 1, 1,
		Iexit, 0, 0, 1, 0,
		Iexiti, 0, 0, 2, 0,
		Irdrop, 0, 0, 1, 0,
		Idup_d, 2, 4, 0, 0,
		IcontextFrom, 0, 1, 0, 0,
		ItoContext, 1, 0, 0, 0,
		Ispidx, 0, 1, 0, 0,
		Irpidx, 0, 1, 0, 0,
		Iswap, 2, 2, 0, 0,
		Iswap_d, 4, 4, 0, 0,
		Idrop, 1, 0, 0, 0,
		
		//I1plus, 1, 1, 0, 0,
		//I2plus, 1, 1, 0, 0,
		Ibinop, 2, 1, 0, 0,
		Ibinop_d, 4, 2, 0, 0,
		Iunaryop, 1, 1, 0, 0,
		Iunaryop_d, 2, 2, 0, 0,
		Iadd, 2, 1, 0, 0,
		//I2times, 1, 1, 0, 0,
		//I2div, 1, 1, 0, 0,
		//Iadd_d, 4, 2, 0, 0,
		//Isub, 2, 1, 0, 0,
		//Isub_d, 4, 2, 0, 0,
		Iumul, 2, 2, 0, 0,
		Iudivmod, 3, 2, 0, 0,
		//Ineg, 1, 1, 0, 0,
		//Ineg_d, 2, 2, 0, 0,
		//Iand, 2, 1, 0, 0,
		//Ior, 2, 1, 0, 0,
		//Ixor, 2, 1, 0, 0,
		//Iinvert, 1, 1, 0, 0,
		//Ilsh, 1, 1, 0, 0,
		//Ilsh_d, 2, 2, 0, 0,
		//Iash, 1, 1, 0, 0,
		//Irsh, 1, 1, 0, 0,
		I0equ, 1, 1, 0, 0,
		I0equ_d, 2, 1, 0, 0,
		I0cmp, 1, 1, 0, 0,
		I0cmp_d, 2, 1, 0, 0,
		Icmp, 2, 1, 0, 0,
		Icmp_d, 4, 1, 0, 0,
		Iequ, 2, 1, 0, 0,
		Iequ_d, 4, 1, 0, 0,
		Iexecute, 1, -1, -1, -1,
		Icall, 1, -1, -1, -1,
		Ilit, 0, 1, 0, 0,
		IfieldLit, 0, 1, 0, 0,
		Ilit_d, 0, 2, 0, 0,
		IfieldLit_d, 0, 2, 0, 0,
		Iext, 0, 0, 0, 0,
		Iuser, 1, 1, 0, 0,
	};

	static final Map<Integer, String> instNames = new HashMap<Integer, String>(64);
	
	static {
		Field[] fields = InstF99.class.getDeclaredFields();
		Set<Integer> vals = new TreeSet<Integer>();
		
		if (_Ilast >= _Iext)
			throw new IllegalStateException("too many opcodes: " + _Ilast);
		
		for (Field field : fields) {
			if (field.getName().endsWith("_start") || !field.getName().startsWith("I"))
				continue;
			Integer val;
			try {
				val = (Integer) field.get(InstF99.class);
			} catch (Exception e) {
				throw new IllegalArgumentException();
			}
			if (vals.contains(val))
				throw new IllegalStateException("field " + field + " duplicates value " + val);
			instNames.put(val, field.getName().substring(1).toUpperCase());
			vals.add(val);
				
			if (getStackEffects(val) == null)
				throw new IllegalStateException("opcode " + field + " has no stack effects");
		}
		
		
		instNames.put(Istore, "!");
		instNames.put(Icstore, "C!");
		instNames.put(Istore_d, "D!");
		instNames.put(Iload, "@");
		instNames.put(Icload, "C@");
		instNames.put(Iload_d, "D@");
		
		instNames.put(ItoR, ">R");
		instNames.put(ItoR_d, "2>R");
		instNames.put(IRfrom, "R>");
		instNames.put(IRfrom_d, "2R>");
		instNames.put(IatR, "R@");
		instNames.put(Idup_d, "2DUP");
		instNames.put(Iqdup, "?DUP");
		//instNames.put(Iadd_d, "D+");
		instNames.put(IfieldLit_d, "DLIT.F");
		instNames.put(IfieldLit, "LIT.F");
		instNames.put(Ilit_d, "DLIT");
		instNames.put(Ilit, "LIT");
		//instNames.put(Ilsh_d, "DLSH");
		instNames.put(I0equ, "0=");
		instNames.put(I0equ_d, "D0=");
		instNames.put(IplusLoop, "+LOOP");
		instNames.put(IuplusLoop, "U+LOOP");
		instNames.put(IcontextFrom, "(CONTEXT>)");
		instNames.put(ItoContext, "(>CONTEXT)");
		
		instNames.put(IplusStore, "+!");
	}

	public static void main(String[] args) {
		System.out.println(_Ilast);
		
	}
	/**
	 * @param inst
	 * @return
	 */
	public static String getInstName(int inst) {
		return instNames.get(inst);
	}

	/**
	 * Tell if the instruction references PC
	 * @param inst
	 * @return
	 */
	public static boolean isAligningPCReference(int inst) {
		switch (inst) {
		case InstF99.Iexecute:
		case InstF99.Ibranch:
		case InstF99.Ibranch_f:
		case InstF99.Iexiti:
		case InstF99.Iexit:
			return true;
		}
		return false;
	}
	
	/** Get pair for items read and items left */
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
	/**
	 * @param opcode
	 * @return
	 */
	public static boolean opcodeHasFieldArgument(int opcode) {
		switch (opcode) {
		case Ibranch_f:
		case I0branch_f:
		case IfieldLit:
		case IfieldLit_d:
		case Ispidx:
		case Irpidx:
		case I0cmp:
		case I0cmp_d:
		case Icmp:
		case Icmp_d:
		case Ibinop:
		case Ibinop_d:
		case IcontextFrom:
		case ItoContext:
			return true;
			
		}
		return false;
	}
}
