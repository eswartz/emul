/*
  VdpTMS9918AConsts.java

  (c) 2011 Edward Swartz

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
public class VdpTMS9918AConsts {
	protected VdpTMS9918AConsts() { }
	
	public final static int VDP_INTERRUPT = 0x80;
	public final static int VDP_COINC = 0x40;
	public final static int VDP_FIVE_SPRITES = 0x20;
	public final static int VDP_FIFTH_SPRITE = 0x1f;
	public final static int R0_M3 = 0x2; // bitmap
	public final static int R0_EXTERNAL = 1;
	public final static int R1_RAMSIZE = 128;
	public final static int R1_NOBLANK = 64;
	public final static int R1_INT = 32;
	public final static int R1_M1 = 0x10; // text
	public final static int R1_M2 = 0x8; // multi
	public final static int R1_SPR4 = 2;
	public final static int R1_SPRMAG = 1;
	
	public final static int MODE_GRAPHICS = 0;
	public final static int MODE_TEXT = 1;
	public final static int MODE_MULTI = 2;
	public final static int MODE_BITMAP = 4;
	public final static int MODE_LAST_STANDARD = 4;
	
	public final static int REG_ST = -1;
	public final static int REG_SCANLINE = -2;

}
