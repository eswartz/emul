/*
  VdpV9938Consts.java

  (c) 2011-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.hardware;

/**
 * @author ejs
 *
 */
public class VdpV9938Consts extends VdpTMS9918AConsts {
	public final static int REG_SR0 = 48;
	public final static int REG_PAL0 = 48 + 9;

	protected VdpV9938Consts() {}
	
	public final static int MODE_TEXT2 = 9;
	public final static int MODE_GRAPHICS3 = 8;
	public final static int MODE_GRAPHICS4 = 12;
	public final static int MODE_GRAPHICS5 = 16;
	public final static int MODE_GRAPHICS6 = 20;
	public final static int MODE_GRAPHICS7 = 28;

	
	/** 1: color bus in input mode, enable mouse */
	//final public public static int R8_MS = 0x80;
	/** 1: enable light pen */
	//final static int R8_LP = 0x40;
	/** 1: color 0 is from palette; 0: clear */
	final public static int R8_TP = 0x20;
	/** 1: color bus in input mode, 0: output mode */
	//final public static int R8_CB = 0x10;
	/** 1: video RAM type: 64k, 0: 16k */
	final public static int R8_VR = 0x08;
	/** 1: sprites off */
	final public static int R8_SPD = 0x02;
	/** 1: black & white, 0: color */
	final public static int R8_BW = 0x01;

	
	final public static int R0_IE1 = 0x10;
	final public static int R0_IE2 = 0x20;
	final public static int R0_M4 = 0x4;
	final public static int R0_M5 = 0x8;

	
	/** 1: 212, 0: 192 lines */
	final public static int R9_LN = 0x80;
	/** 1: simultaneous mode */
	//final public static int R9_S1 = 0x20;
	/** 1: simultaneous mode */
	//final public static int R9_S0 = 0x10;
	/** 1: interlace */
	final public static int R9_IL = 0x8;
	/** 1: interlace two screens on the even/odd field */
	final public static int R9_EO = 0x4;
	/** 1: PAL, 0: NTSC */
	//final public static int R9_NT = 0x2;
	/** 1: *DLCLK in input mode, else output */
	//final public static int R9_DC = 0x1;
	
	/** 1: expansion RAM, 0: video RAM */
	final public static int R45_MXC = 0x40;
	final public static int R45_MXD = 0x20;
	final public static int R45_MXS = 0x10;
	final public static int R45_DIY = 0x8;
	final public static int R45_DIX = 0x4;
	final public static int R45_EQ = 0x2;
	/** 0=X long, 1=Y long */
	final public static int R45_MAJ = 0x1;
	
	final public static int R32_SX_LO = 32;	// 0x20 1
	final public static int R33_SX_HI = 33;	// 0x21
	final public static int R34_SY_LO = 34;	// 0x22 2
	final public static int R35_SY_HI = 35;	// 0x23
	final public static int R36_DX_LO = 36;	// 0x24 1
	final public static int R37_DX_HI = 37;	// 0x25
	final public static int R38_DY_LO = 38;	// 0x26 2
	final public static int R39_DY_HI = 39;	// 0x27
	final public static int R40_NX_LO = 40;	// 0x28 1
	final public static int R41_NX_HI = 41;	// 0x29
	final public static int R42_NY_LO = 42;	// 0x2A 2
	final public static int R43_NY_HI = 43;	// 0x2B
	final public static int R44_CLR = 44;	// 0x2C
	final public static int R45_ARG = 45;	// 0x2D
	
	final public static int R46_CMD = 46;	// 0x2E
	final public static int R46_CMD_MASK = 0xf0;
	final public static byte R46_CMD_HMMC = (byte) 0xf0;
	final public static byte R46_CMD_YMMM = (byte) 0xe0;
	final public static byte R46_CMD_HMMM = (byte) 0xd0;
	final public static byte R46_CMD_HMMV = (byte) 0xc0;
	final public static byte R46_CMD_LMMC = (byte) 0xb0;
	final public static byte R46_CMD_LMCM = (byte) 0xa0;
	final public static byte R46_CMD_LMMM = (byte) 0x90;
	final public static byte R46_CMD_LMMV = (byte) 0x80;
	final public static byte R46_CMD_LINE = 0x70;
	final public static byte R46_CMD_SRCH = 0x60;
	final public static byte R46_CMD_PSET = 0x50;
	final public static byte R46_CMD_POINT = 0x40;
	final public static byte R46_CMD_STOP = 0x00;
	
	final public static int R46_LOGOP_MASK = 0xf;
	final public static int R46_LOGOP_INP = 0x0;
	final public static int R46_LOGOP_AND = 0x1;
	final public static int R46_LOGOP_OR  = 0x2;
	final public static int R46_LOGOP_EOR = 0x3;
	final public static int R46_LOGOP_NOT = 0x4;
	final public static int R46_LOGOP_TIMF = 0x8;
	final public static int R46_LOGOP_TAND = 0x9;
	final public static int R46_LOGOP_TOR  = 0xA;
	final public static int R46_LOGOP_TEOR = 0xB;
	final public static int R46_LOGOP_TNOT = 0xC;
	
	
	final public static int S2_TR = 0x80;
	final public static int S2_BD = 0x10;
	final public static int S2_EO = 0x2;
	final public static int S2_CE = 0x1;
	
	final public static int S3_COL_LO = 3;
	final public static int S4_COL_HI = 4;
	final public static int S5_ROW_LO = 5;
	final public static int S6_ROW_HI = 6;
	final public static int S7_CLR = 7;
	final public static int S8_BOR_HI = 8;
	final public static int S9_BOR_LO = 9;

	public static final int REG_COUNT = 1 /* status */ + 48 /* base */ + 9 /* status */ + 16 /* palette */;
	
	/** lightpen flag */
	public static final int ST1_LP = 0x80;
	/** horizontal scanning interrupt flag */
	public static final int ST1_VSIF = 0x01;
	

}
