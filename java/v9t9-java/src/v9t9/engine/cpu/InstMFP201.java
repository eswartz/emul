/**
 * 
 */
package v9t9.engine.cpu;

/**
 * @author Ed
 * Note: odd-numbered instructions are byte instructions
 */
public class InstMFP201 {
	public static final int Ibkpt = 2;
	public static final int Iret = 4;
	public static final int Ireti = 6;
	public static final int Ibr = 8;
	public static final int Ibra = 10;
	public static final int Icall = 12;
	public static final int Icalla = 14;
	public static final int _IlastSimpleImmediate = 14;

	public static final int _IfirstPossibleThreeOp = 16;
	public static final int _IfirstLogicalOp = 16;

	public static final int Ior = 16;
	public static final int Iorb = 17;
	public static final int Iorq = 18;
	public static final int Iorbq = 19;
	
	public static final int Iand = 20;
	public static final int Iandb = 21;
	public static final int Itst = 22;
	public static final int Itstb = 23;
	
	public static final int Inand = 24;
	public static final int Inandb = 25;
	public static final int Itstn = 26;
	public static final int Itstnb = 27;
	
	public static final int Ixor = 28;
	public static final int Ixorb = 29;
	public static final int Ixorq = 30;
	public static final int Ixorbq = 31;
	
	public static final int _IlastLogicalOp = 31;
	
	public static final int Iadd = 32;
	public static final int Iaddb = 33;
	public static final int Iaddq = 34;
	public static final int Iaddbq = 35;
	
	public static final int Isub = 36;
	public static final int Isubb = 37;
	public static final int Icmp = 38;
	public static final int Icmpb = 39;
	
	public static final int Iadc = 40;
	public static final int Iadcb = 41;
	public static final int Iadcq = 42;
	public static final int Iadcbq = 43;
	
	public static final int Isbb = 44;
	public static final int Isbbb = 45;
	public static final int Icmpr = 46;
	public static final int Icmprb = 47;
	
	public static final int _IlastPossibleThreeOp = 47;

	public static final int Ildc = 48;
	public static final int Ildcb = 49;
	public static final int Ildcq = 50;
	public static final int Ildcbq = 51;
	
	public static final int Iloop = 60;
	
}
