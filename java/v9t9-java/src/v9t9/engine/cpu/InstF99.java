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
	
	public static final int I3bit_start = 0;
	
	public static final int Inop = I3bit_start + 0;
	public static final int Idup = I3bit_start + 1;
	public static final int Ifetch = I3bit_start + 2;
	public static final int Istore = I3bit_start + 3;
	public static final int Izero = I3bit_start + 4;
	public static final int Ione = I3bit_start + 5;
	/** next signed field is pushed */
	public static final int IfieldLit = I3bit_start + 6;
	/** next full word is jump offset */
	public static final int I0branch = I3bit_start +7;
	
	public static final int Irstack_start = 8;
	
	public static final int Itwo = Irstack_start + 0;
	public static final int InegOne = Irstack_start + 1;
	
	public static final int Ido = Irstack_start + 2;
	/** next full word is jump offset */
	public static final int Iloop = Irstack_start + 3;
	public static final int Iover = Irstack_start + 4;
	public static final int Irot = Irstack_start + 5;
	public static final int ItoR = Irstack_start + 6;
	public static final int IRfrom = Irstack_start + 7;
	public static final int IatR = Irstack_start + 8;
	public static final int Iexit = Irstack_start + 9;
	public static final int Irdrop = Irstack_start + 10;
	public static final int Ij = Irstack_start + 11;
	public static final int I2dup = Irstack_start + 12;
	public static final int IplusLoop = Irstack_start + 13;
	//public static final int Iiprime = Irstack_start + 11;
	
	public static final int Ispecial_start = 22;
	
	/** 0=SP, 1=SP0, 2=RP, 3=RP0, 4=UP, 5=UP0, 6=PC */
	public static final int IcontextFrom = Ispecial_start + 0;
	/** 0=SP, 1=SP0, 2=RP, 3=RP0, 4=UP, 5=UP0, 6=PC */
	public static final int ItoContext = Ispecial_start + 1;
	
	public static final int Ibranches_start = 24;
	
	/** next field is offset in words */
	//public static final int I0fieldBranch = Ibranches_start + 0;
	/** next field is offset in words */
	//public static final int IfieldBranch = Ibranches_start + 1;
	/** next full word is offset in words */
	public static final int Ibranch = Ibranches_start + 0;		
	
	public static final int Istack_start = 27;
	
	public static final int Iswap = Istack_start + 0;
	public static final int Idrop = Istack_start + 1;
	
	public static final int Imemory_start = 29;
	
	public static final int Iplusadd = Imemory_start + 0;
	public static final int Icload = Imemory_start + 1;
	public static final int Icstore = Imemory_start + 2;
	
	public static final int Iarith_start = 32;
	public static final int I1plus = Iarith_start + 0;
	public static final int I1minus = Iarith_start + 1;
	public static final int I2plus = Iarith_start + 2;
	public static final int I2minus = Iarith_start + 3;
	public static final int I2times = Iarith_start + 4;
	public static final int I2div = Iarith_start + 5;
	public static final int Iadd = Iarith_start + 6;
	public static final int Iadc = Iarith_start + 7;
	public static final int Isub = Iarith_start + 8;
	/** um*: s * s -> d */
	public static final int Iumul = Iarith_start + 9;
	/** um/mod: ud u1 -- div mod */
	public static final int Iudivmod = Iarith_start + 10;
	public static final int Ineg = Iarith_start + 11;
	public static final int Iand = Iarith_start + 12;
	public static final int Ior = Iarith_start + 13;
	public static final int Ixor = Iarith_start + 14;
	public static final int Inot = Iarith_start + 15;
	public static final int Ilsh = Iarith_start + 16;
	public static final int Iash = Iarith_start + 17;  // signed
	public static final int Irsh = Iarith_start + 18;  // unsigned
	public static final int Idadd = Iarith_start + 19;
	public static final int Idneg = Iarith_start + 20;
	
	public static final int I2rdrop = Iarith_start + 21;
	
	public static final int Icond_start = 54;
	
	public static final int I0equ = Icond_start + 0;
	public static final int Iequ = Icond_start + 1;
	public static final int I0lt = Icond_start + 2;
	public static final int Ilt = Icond_start + 3;
	public static final int Iult = Icond_start + 4;
	
	public static final int Icontrol_start = 59;

	public static final int Isyscall = Icontrol_start + 0;
	public static final int Icli = Icontrol_start + 1;
	public static final int Isti = Icontrol_start + 2;
	public static final int Iexecute = Icontrol_start + 3;
	/** next full word is pushed */
	public static final int Ilit = Icontrol_start + 4;

	public static final int Icall = 64;
	public static final int _Ilast = 64;

	static final Map<Integer, String> instNames = new HashMap<Integer, String>(64);
	
	static {
		Field[] fields = InstF99.class.getDeclaredFields();
		Set<Integer> vals = new TreeSet<Integer>();
		
		for (Field field : fields) {
			if (field.getName().endsWith("_start") || !field.getName().startsWith("I"))
				continue;
			Integer val;
			try {
				val = (Integer) field.get(InstF99.class);
			} catch (Exception e) {
				throw new IllegalArgumentException();
			}
			if (val > _Ilast)
				throw new IllegalStateException("field " + field + " out of range: " + val);
			if (vals.contains(val))
				throw new IllegalStateException("field " + field + " duplicates value " + val);
			instNames.put(val, field.getName().substring(1).toUpperCase());
			vals.add(val);
				
		}
		
		instNames.put(ItoR, ">R");
		instNames.put(IRfrom, "R>");
		instNames.put(IatR, "R@");
		
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
		case InstF99.Isyscall:
		case InstF99.IcontextFrom:
		case InstF99.Ibranch:
		//case InstF99.IfieldBranch:
			return true;
		}
		return false;
	}
	
	/** for each inst:  SP read, SP left
	 *	RP read, RP left;
	 *  neg means unsure
	 */
	static final int[] instArgs = {
		Inop, 0, 0, 0, 0,
		Idup, 1, 2, 0, 0,
		Ifetch, 1, 1, 0, 0,
		Istore, 2, 0, 0, 0,
		Izero, 0, 1, 0, 0,
		Ione, 0, 1, 0, 0,
		IfieldLit, 0, 1, 0, 0,
		I0branch, 1, 0, 0, 0,
		Itwo, 0, 1, 0, 0,
		InegOne, 0, 1, 0, 0,
		Ido, 2, 0, 0, 0,
		IplusLoop, 0, 0, 2, 0,
		Iover, 2, 3, 0, 0,
		Irot, 3, 3, 0, 0,
		ItoR, 1, 0, 0, 1,
		IRfrom, 0, 1, 1, 0,
		IatR, 0, 1, 1, 0,
		Iexit, 0, 0, 1, 0,
		Irdrop, 0, 0, 1, 0,
		I2dup, 2, 4, 0, 0,
		//Iiprime, 0, 1, 2, 2,
		Ij, 0, 1, 3, 3,
		IcontextFrom, 1, 1, 0, 0,
		ItoContext, 2, 0, 0, 0,
		//I0fieldBranch, 1, 0, 0, 0, 
		//IfieldBranch, 0, 0, 0, 0,
		Ibranch, 0, 0, 0, 0,
		Iswap, 2, 2, 0, 0,
		Idrop, 1, 0, 0, 0,
		Iplusadd, 2, 0, 0, 0,
		Icload, 1, 1, 0, 0,
		Icstore, 2, 0, 0, 0,
		I1plus, 1, 1, 0, 0,
		I1minus, 1, 1, 0, 0,
		I2plus, 1, 1, 0, 0,
		I2minus, 1, 1, 0, 0,
		I2times, 1, 1, 0, 0,
		I2div, 1, 1, 0, 0,
		Iadd, 2, 1, 0, 0,
		Iadc, 2, 2, 0, 0,
		Isub, 2, 1, 0, 0,
		Iumul, 2, 2, 0, 0,
		Iudivmod, 3, 2, 0, 0,
		Ineg, 1, 1, 0, 0,
		Iand, 2, 1, 0, 0,
		Ior, 2, 1, 0, 0,
		Ixor, 2, 1, 0, 0,
		Inot, 1, 1, 0, 0,
		Ilsh, 2, 2, 0, 0,
		Iash, 2, 2, 0, 0,
		Irsh, 2, 2, 0, 0,
		Idadd, 4, 2, 0, 0,
		Idneg, 2, 2, 0, 0,
		I0equ, 1, 1, 0, 0,
		Iequ, 2, 1, 0, 0,
		I0lt, 1, 1, 0, 0,
		Iult, 2, 1, 0, 0,
		Isyscall, -1, -1, -1, -1,
		Icli, 0, 0, 0, 0,
		Isti, 0, 0, 0, 0,
		Iexecute, 1, -1, -1, -1,
		Ilit, 0, 1, 0, 0,
	};

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
}
