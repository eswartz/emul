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
	//public static final int Iret = 4;
	//public static final int Ireti = 6;
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
	
	public static final int _IfirstArithOp = 32;

	public static final int Iadd = 32;
	public static final int Iaddb = 33;
	
	public static final int Iadc = 36;
	public static final int Iadcb = 37;
	
	public static final int Isub = 40;
	public static final int Isubb = 41;
	public static final int Icmp = 42;
	public static final int Icmpb = 43;
	
	public static final int Isbb = 44;
	public static final int Isbbb = 45;
	public static final int Icmpr = 46;
	public static final int Icmprb = 47;

	public static final int _IlastArithOp = 47;

	public static final int _IlastPossibleThreeOp = 47;

	public static final int _IlastPossibleByteOp = 51;
	
	public static final int Ildc = 48;
	
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
	
	public static final int _IfirstJumpOp = 72;
	
	public static final int Ijne = 72;
	public static final int Ijeq = 74;
	public static final int Ijnc = 76;
	public static final int Ijc = 78;
	public static final int Ijn = 80;
	public static final int Ijge = 82;
	public static final int Ijl = 84;
	public static final int Ijmp = 86;
	
	public static final int _IlastJumpOp = 86;
	
	public static final int _IfirstMovOp = 88;
	public static final int _IfirstPossibleByteOp3 = 88;
	
	public static final int Imovne = 88;
	public static final int Imovneb = 89;
	public static final int Imoveq = 90;
	public static final int Imoveqb = 91;
	public static final int Imovnc = 92;
	public static final int Imovncb = 93;
	public static final int Imovc = 94;
	public static final int Imovcb = 95;
	public static final int Imovn = 96;
	public static final int Imovnb = 97;
	public static final int Imovge = 98;
	public static final int Imovgeb = 99;
	public static final int Imovl = 100;
	public static final int Imovlb = 101;
	public static final int Imov = 102;
	public static final int Imovb = 103;
	
	public static final int _IlastMovOp = 103;
	
	public static final int Ilsh = 104;
	public static final int Ilshb = 105;
	public static final int Irsh = 106;
	public static final int Irshb = 107;
	public static final int Iash = 108;
	public static final int Iashb = 109;
	public static final int Irol = 110;
	public static final int Irolb = 111;
	public static final int Imul = 112;
	public static final int Imulb = 113;
	public static final int Idiv = 114;
	public static final int Idivb = 115;
	public static final int Imuld = 116;
	public static final int Imuldb = 117;
	public static final int Idivd = 118;
	public static final int Idivdb = 119;
	
	public static final int _IlastPossibleByteOp3 = 119;

	public static final int Ilea = 120;

	public static final int _IfirstLoopStepInst = 122;
	public static final int _IfirstLoopInst = 122;
	
	public static final int Iloopne = 122;
	public static final int Iloopeq = 124;
	public static final int Iloopnc = 126;
	public static final int Iloopc = 128;
	public static final int Iloopn = 130;
	public static final int Iloopge = 132;
	public static final int Iloopl = 134;
	public static final int Iloop = 136;
	
	public static final int _IlastLoopInst = 136;
	
	public static final int _IfirstStepInst = 138;
	
	public static final int Istepne = 138;
	public static final int Istepeq = 140;
	public static final int Istepnc = 142;
	public static final int Istepc = 144;
	public static final int Isteps = 146;
	public static final int Istepge = 148;
	public static final int Istepl = 150;
	public static final int Istep = 152;
	
	public static final int _IlastStepInst = 152;
	public static final int _IlastLoopStepInst = 152;
	
	/* all the instructions below may only appear in HL or LL instructions;
	 * they are converted to primitive forms in RawInstruction
	 */
	public static final int _IfirstPseudoInst = 200;
	
	public static final int Pclr = 200;
	public static final int Pclrb = 201;
	public static final int Pclrq = 202;
	public static final int Pclrbq = 203;
	public static final int Pseto = 204;
	public static final int Psetob = 205;
	public static final int Psetoq = 206;
	public static final int Psetobq = 207;
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
	
	public static final int _IlastPseudoInst = 300;
	
}
