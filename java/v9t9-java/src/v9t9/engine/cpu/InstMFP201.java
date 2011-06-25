/**
 * Aug 7 2010
 */
package v9t9.engine.cpu;

/**
 * @author Ed
 * Note: odd-numbered instructions are byte instructions
 */
public class InstMFP201 {
	public static final int _IfirstSimpleImmediate = 2;
	public static final int Ibkpt = 2;
	public static final int Ibr = 8;
	public static final int Ibra = 10;
	public static final int Icall = 12;
	public static final int Icalla = 14;
	public static final int _IlastSimpleImmediate = 14;

	public static final int _IfirstPossibleByteOp = 16;
	public static final int _IfirstPossibleThreeOp = 16;
	public static final int _IfirstLogicalOp = 16;

	public static final int Ior = 16;
	public static final int Iorb = 17;
	public static final int Iand = 20;
	public static final int Iandb = 21;
	public static final int Inand = 24;
	public static final int Inandb = 25;
	public static final int Ixor = 28;
	public static final int Ixorb = 29;
	public static final int Itst = 22;
	public static final int Itstb = 23;
	public static final int Itstn = 26;
	public static final int Itstnb = 27;

	
	public static final int _IlastLogicalOp = 31;

	
	public static final int _IfirstArithOp = 32;

	public static final int Iadd = 32;
	public static final int Iaddb = 33;
	
	public static final int Isub = 34;
	public static final int Isubb = 35;
	
	public static final int Iadc = 36;
	public static final int Iadcb = 37;
	
	public static final int Isbb = 38;
	public static final int Isbbb = 39;

	public static final int _IlastArithOp = 39;
	public static final int _IlastPossibleThreeOp = 39;

	public static final int Icmp = 40;
	public static final int Icmpb = 41;

	public static final int Ildc = 42;
	public static final int Ildcb = 43;

	public static final int _IlastPossibleByteOp = 43;
	
	public static final int Isext = 56;
	public static final int Iextl = 58;
	public static final int Iexth = 60;
	public static final int Iswpb = 62;

	public static final int _IfirstPossibleByteOp2 = 64;
	public static final int _IfirstPushPopOp = 64;
	public static final int Ipush = 64;
	public static final int Ipushb = 65;
	public static final int Ipop = 66;
	public static final int Ipopb = 67;
	public static final int _IfirstPushPopNOp = 68;
	public static final int Ipushn = 68;
	public static final int Ipushnb = 69;
	public static final int Ipopn = 70;
	public static final int Ipopnb = 71;
	public static final int _IlastPushPopNOp = 71;
	public static final int _IlastPushPopOp = 71;
	public static final int _IlastPossibleByteOp2 = 71;
	
	public static final int Ijmp = 80;
	
	
	public static final int _IfirstPossibleByteOp3 = 82;
	
	public static final int Imov = 82;
	public static final int Imovb = 83;
	
	public static final int Ilsh = 84;
	public static final int Ilshb = 85;
	// LSH As bits
	public static final int Irol = 86;
	public static final int Irolb = 87;
	public static final int Ilshc = 88;
	public static final int Ilshcb = 89;
	public static final int Ilshz = 90;
	public static final int Ilshzb = 91;

	public static final int Irsh = 92;
	public static final int Irshb = 93;

	// RSH As bits
	public static final int Iash = 94;
	public static final int Iashb = 95;
	public static final int Irshc = 96;
	public static final int Irshcb = 97;
	public static final int Irshz = 98;
	public static final int Irshzb = 99;

	
	public static final int Imul = 112;
	public static final int Imulb = 113;
	public static final int Idiv = 114;
	public static final int Idivb = 115;
	
	public static final int _IlastPossibleByteOp3 = 119;

	public static final int Ilea = 120;

	public static final int _IfirstIfOp = 128;
	public static final int Iifne = 128;
	public static final int Iifeq = 129;
	public static final int Iifnc = 130;
	public static final int Iifc = 131;
	public static final int Iifge = 132;
	public static final int Iiflt = 133;
	public static final int Iifns = 134;
	public static final int Iifs = 135;
	public static final int Iifno = 136;
	public static final int Iifo = 137;
	public static final int Iifh = 138;
	public static final int Iifbe = 139;
	public static final int Iifgt = 140;
	public static final int Iifle = 141;
	public static final int Iifnsz = 142;
	public static final int Iifse = 143;
	public static final int _IlastIfOp = 143;

	
	public static final int _IfirstLoopStepInst = 144;
	public static final int _IfirstLoopInst = 144;
	public static final int Iloopne = 144;
	public static final int Iloopeq = 145;
	public static final int Iloopnc = 146;
	public static final int Iloop = 147;
	public static final int _IlastLoopInst = 147;
	
	public static final int _IfirstStepInst = 148;
	public static final int Istepne = 148;
	public static final int Istepeq = 149;
	public static final int Istepnc = 150;
	public static final int Istep = 151;
	public static final int _IlastStepInst = 151;
	public static final int _IlastLoopStepInst = 151;
	
	/* all the instructions below may only appear in HL or LL instructions;
	 * they are converted to primitive forms in RawInstruction
	 */
	public static final int _IfirstPseudoInst = 200;
	
	public static final int Pclr = 200;
	public static final int Pclrb = 201;
	public static final int Pseto = 204;
	public static final int Psetob = 205;
	public static final int Pinv = 208;
	public static final int Pinvb = 209;

	
	public static final int Pinc = 220;
	public static final int Pincb = 221;
	public static final int Pinct = 222;
	public static final int Pinctb = 223;
	public static final int Pdec = 224;
	public static final int Pdecb = 225;
	public static final int Pdect = 226;
	public static final int Pdectb = 227;
	
	public static final int Pnop = 230;
	public static final int Pret = 232;
	public static final int Preti = 234;
	
	public static final int _IfirstPseudoCondInst = 256;
	
	public static final int Pjcc = 256;
	//public static final int Pjcc = 256+16;
	public static final int Pmovcc = 272;
	//public static final int Pmovcc = 272+16;
	
	public static final int _IlastPseudoInst = 400;
	
}
