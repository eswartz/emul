/**
 * 
 */
package v9t9.emulator.clients.builtin.video.v9938;

import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.tms9918a.GraphicsModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.tms9918a.SpriteRedrawHandler;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.emulator.hardware.memory.mmio.Vdp9938Mmio;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.MemoryDomain;

/**
 * V9938 videp chip support.  This functions as a superset of the TMS9918A.
 * <p>
 * Mode bits:
 * <p>
 * R0:	M3 @ 1, M4 @ 2, M5 @ 3
 * R1:	M1 @ 4, M2 @ 3 
 * <p>
 * <pre>
 *                   M1  M2  M3  M4  M5     Mode #
 * Text 1 mode:      1   0   0   0   0		= 1
 * Text 2 mode:      1   0   0   1   0		= 9
 * Multicolor:       0   1   0   0   0		= 2
 * Graphics 1 mode:  0   0   0   0   0		= 0
 * Graphics 2 mode:  0   0   1   0   0		= 4
 * Graphics 3 mode:  0   0   0   1   0		= 8
 * Graphics 4 mode:  0   0   1   1   0		= 12
 * Graphics 5 mode:  0   0   0   0   1		= 16
 * Graphics 6 mode:  0   0   1   0   1		= 20
 * Graphics 7 mode:  0   0   1   1   1		= 28
 * </pre>
 * TODO: sprite updating not working
 * TODO: sprite 2 mode, + 512-pixel multiplex logic
 * TODO: page flip blinking
 * TODO: toying with R2 and the row masking 
 * @author ejs  
 *
 */
public class VdpV9938 extends VdpTMS9918A {

	private byte paletteidx;
	private boolean palettelatched;
	private byte palettelatch;
	private int statusidx;
	private byte[] statusvec = new byte[16];
	private int indreg;
	private boolean indinc;
	private int blinkPeriod;
	private int blinkOnPeriod;
	private int blinkOffPeriod;
	boolean blinkOn;
	
	public VdpV9938(MemoryDomain videoMemory, VdpCanvas vdpCanvas) {
		super(videoMemory, vdpCanvas);
		reset();
	}

	protected byte[] allocVdpRegs() {
		return new byte[48];
	}
	
	protected void reset() {
		vdpCanvas.setRGB(0, rgb3to8(0, 0, 0)); 
		vdpCanvas.setRGB(1, rgb3to8(0, 0, 0)); 
		vdpCanvas.setRGB(2, rgb3to8(1, 6, 1)); 
		vdpCanvas.setRGB(3, rgb3to8(3, 7, 3)); 
		vdpCanvas.setRGB(4, rgb3to8(1, 1, 7)); 
		vdpCanvas.setRGB(5, rgb3to8(2, 3, 7)); 
		vdpCanvas.setRGB(6, rgb3to8(5, 1, 1)); 
		vdpCanvas.setRGB(7, rgb3to8(2, 6, 7)); 
		vdpCanvas.setRGB(8, rgb3to8(7, 1, 1)); 
		vdpCanvas.setRGB(9, rgb3to8(7, 3, 3)); 
		vdpCanvas.setRGB(10, rgb3to8(6, 6, 1)); 
		vdpCanvas.setRGB(11, rgb3to8(6, 6, 4)); 
		vdpCanvas.setRGB(12, rgb3to8(4, 4, 1)); 
		vdpCanvas.setRGB(13, rgb3to8(6, 2, 5)); 
		vdpCanvas.setRGB(14, rgb3to8(5, 5, 5)); 
		vdpCanvas.setRGB(15, rgb3to8(7, 7, 7)); 
	}
	
	/** 1: expansion RAM, 0: video RAM */
	final public static int R45_MXC = 0x40;
	//final public static int R45_MXD = 0x20;
	//final public static int R45_MXS = 0x10;
	//final public static int R45_DIY = 0x8;
	//final public static int R45_DIX = 0x4;
	//final public static int R45_EQ = 0x2;
	//final public static int R45_MAJ = 0x1;
	
	public void writeRegisterIndirect(byte val) {
		if (indreg != 17) {
			writeVdpReg(indreg, val);
		}
		if (indinc) {
			indreg = (indreg + 1) & 0x3f;
		}
	}

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
	/** 1: sprites on */
	final public static int R8_SPD = 0x02;
	/** 1: black & white, 0: color */
	final public static int R8_BW = 0x01;

	
	final public static int R0_M4 = 0x4;
	final public static int R0_M5 = 0x8;

	
	/** 1: 212, 0: 192 lines */
	final public static int R9_LN = 0x80;
	/** 1: simultaneous mode */
	//final public static int R9_S1 = 0x20;
	/** 1: simultaneous mode */
	//final public static int R9_S0 = 0x10;
	/** 1: interlace */
	//final public static int R9_IL = 0x8;
	/** 1: interlace two screens on the even/odd field */
	//final public static int R9_EO = 0x4;
	/** 1: PAL, 0: NTSC */
	//final public static int R9_NT = 0x2;
	/** 1: *DLCLK in input mode, else output */
	//final public static int R9_DC = 0x1;
	
	public final static int MODE_TEXT2 = 9;
	public final static int MODE_GRAPHICS3 = 8;
	public final static int MODE_GRAPHICS4 = 12;
	public final static int MODE_GRAPHICS5 = 16;
	public final static int MODE_GRAPHICS6 = 20;
	public final static int MODE_GRAPHICS7 = 28;
	
	@Override
	protected int doWriteVdpReg(int reg, byte old, byte val) {
		//System.out.println(Utils.toHex2(reg) + " = " + Utils.toHex2(val));
		int redraw = super.doWriteVdpReg(reg, old, val);
		
		switch (reg) {
		case 0:
			if (CHANGED(old, val, R0_M4 + R0_M5)) {
				redraw |= REDRAW_MODE;
			}
			break;
		case 8:
			// input bus, display mode, line count
			if (CHANGED(old, val, R8_VR)) {
				switchBank();
				redraw |= REDRAW_MODE;
			}
			if (CHANGED(old, val, R8_BW)) {
				vdpCanvas.setGreyscale((val & R8_BW) != 0);
				redraw |= REDRAW_PALETTE;
			}
			if (CHANGED(old, val, R8_SPD)) {
				drawSprites = (val & R8_SPD) == 0;
				redraw |= REDRAW_SPRITES;
			}
			if (CHANGED(old, val, R8_TP)) {
				vdpCanvas.setClearFromPalette((val & R8_TP) != 0);
				redraw |= REDRAW_PALETTE;
			}
			break;
		case 9:
			// lines and stuff
			if (CHANGED(old, val, R9_LN)) {
				vdpCanvas.setSize(256, (val & R9_LN) != 0 ? 212 : 192);
				redraw |= REDRAW_MODE;
			}
			break;
		case 10:
		case 11:
			// color, sprite table high byte
			redraw |= REDRAW_MODE;
			break;
		case 12:
			// text 2 blinky (pg 6)
			redraw |= REDRAW_PALETTE;
			break;
			
		case 13:
			// blinking period (pg 6)
			blinkOnPeriod = ((val >> 4) & 0xf) * 10;
			blinkOffPeriod = (val & 0xf) * 10;
			blinkPeriod = blinkOffPeriod;
			blinkOn = false;
			// no redraw right now
			break;
			
			
		case 14:
			// memory bank 
			switchBank();
			break;
		case 15:
			// status register pointer
			statusidx = val & 0xf;
			break;
		case 16:
			// palette #
			paletteidx = val;
			palettelatched = false;
			break;
		case 17:
			// indirect register access: sets the next register whose value
			// can be set through port 3 (though, reg 17 itself will not be modified)
			indreg = val & 0x3f;
			indinc = (val & 0x80) == 0;
			break;
		case 18:
			// display adjust register (pg 6)
			int xoffs = (byte)(val << 4) >> 4;
			int yoffs = (val & 0xf0) >> 4;
			vdpCanvas.setOffset(xoffs, yoffs);
			dirtyAll();
			break;
		case 20:
		case 21:
		case 22:
			// color burst registers
			break;
			
		case 45:
			// argument register
			if (CHANGED(old, val, R45_MXC)) {
				redraw |= REDRAW_MODE;
				switchBank();
			}
			break;
		}
		return redraw;
	}

	private void switchBank() {
		if (getVdpMmio() instanceof Vdp9938Mmio) {
			Vdp9938Mmio vdp9938Mmio = (Vdp9938Mmio) getVdpMmio();
			BankedMemoryEntry memoryBank = vdp9938Mmio.getMemoryBank();
			int vdpbank = (vdpregs[14] & 0x7);
			
			// 16k mode?
			if ((vdpregs[8] & R8_VR) == 0 || memoryBank.getBankCount() == 1) {
				vdpbank = 0;
			} else {
				// expansion RAM?
				if ((vdpregs[45] & R45_MXC) != 0 && memoryBank.getBankCount() >= 8) {
					vdpbank = (vdpbank & 0x3) | 0x8;
				}
			}
			
			memoryBank.selectBank(vdpbank);
			System.out.println("-->vdpbank " + vdpbank);
		}
	}

	protected byte[] rgb3to8(int r, int g, int b) {
		return new byte[] { rgb3to8(r), rgb3to8(g), rgb3to8(b) };
	}
	public void writeColorData(byte val) {
		if (!palettelatched) {
			palettelatch = val;
			palettelatched = true;
		} else {
			vdpCanvas.setRGB(paletteidx & 0xff, rgb3to8(palettelatch >> 4, palettelatch & 0x7, val & 0x7));
			dirtyAll();
			
			paletteidx++;
			palettelatched = false;
		}
	}

	private byte rgb3to8(int val) {
		val &= 0x7;
		byte val8 = (byte) (val << 5);
		if (val > 4)
			val8 |= 0x1f;
		return val8;
		//return (byte) (val * 0x22); 
	}

	/* (non-Javadoc)
     * @see v9t9.handlers.VdpHandler#readVdpStatus()
     */
    public byte readVdpStatus() {
    	if (statusidx == 0)
    		return super.readVdpStatus();
    	
    	byte ret = statusvec[statusidx];
        return ret;
    }

	public boolean isEnhancedMode() {
		return (vdpregs[0] & R0_M4 + R0_M5) != 0;
	}

	/**
	 * Yields one of the MODE_xxx enums based on the current vdpregs[]
	 * @return
	 */
	final protected int get9938ModeNumber() {
		int mode = (vdpregs[1] & R1_M1) / R1_M1
		+ (vdpregs[1] & R1_M2) / R1_M2 * 2
		+ (vdpregs[0] & R0_M3) / R0_M3 * 4
		+ (vdpregs[0] & R0_M4) / R0_M4 * 8
		+ (vdpregs[0] & R0_M5) / R0_M5 * 16;
		if ((mode & 1) != 0 && mode != MODE_TEXT && mode != MODE_TEXT2)
			mode &= ~1;
		return mode;
	}
	
	@Override
	protected void establishVideoMode() {
		int mode = get9938ModeNumber();
		switch (mode) {
		case MODE_TEXT2:
			setText2Mode();
			break;
		case MODE_GRAPHICS3:
			setGraphics3Mode();
			break;
		case MODE_GRAPHICS4:
			setGraphics4Mode();
			break;
		case MODE_GRAPHICS5:
			setGraphics5Mode();
			break;
		case MODE_GRAPHICS6:
			setGraphics6Mode();
			break;
		default:
			super.establishVideoMode();
			break;
		}
	}
	
	@Override
	protected int getMaxRedrawblocks() {
		return 80 * 27;
	}
	
	protected void setText2Mode() {
		vdpCanvas.setSize(512, vdpCanvas.getHeight());
		vdpModeRedrawHandler = new Text2ModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas, createText2ModeInfo());
		spriteRedrawHandler = null;
		vdpMmio.setMemoryAccessCycles(1);
		initUpdateBlocks(6);
	}

	protected VdpModeInfo createText2ModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		int ramsize = getModeAddressMask();
		
		vdpModeInfo.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdpModeInfo.screen.size = 80 * 27;	// last 2.5 rows only visible in 212-line mode
		vdpModeInfo.patt.base = getPatternTableBase() & ramsize;
		vdpModeInfo.patt.size = 2048;
		return vdpModeInfo;
	}

	protected void setGraphics3Mode() {
		super.setGraphicsMode();
		spriteRedrawHandler = createSprite2RedrawHandler(false);
	}

	private Sprite2RedrawHandler createSprite2RedrawHandler(boolean wide) {
		return new Sprite2RedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas, createSpriteModeInfo(),
				wide);
	}
	
	protected void setGraphics4Mode() {
		vdpCanvas.setSize(256, vdpCanvas.getHeight());
		vdpModeRedrawHandler = new Graphics4ModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas, createGraphics45ModeInfo());
		spriteRedrawHandler = createSprite2RedrawHandler(false);
		vdpMmio.setMemoryAccessCycles(8);
		initUpdateBlocks(8);
	}

	
	protected VdpModeInfo createGraphics45ModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		int ramsize = getModeAddressMask();
		
		vdpModeInfo.screen.base = 0;
		vdpModeInfo.screen.size = 0;
		vdpModeInfo.color.base = 0;
		vdpModeInfo.color.size = 0;
		
		// paging!
		vdpModeInfo.patt.base = ((vdpregs[2] & 0x60) << 10) & ramsize;
		vdpModeInfo.patt.size = 27136;
		
		vdpModeInfo.sprite.base = getSpriteTableBase() & ramsize;
		vdpModeInfo.sprite.size = 128;
		vdpModeInfo.sprpat.base = (vdpregs[6] * 0x800) & ramsize;
		vdpModeInfo.sprpat.size = 2048;
		return vdpModeInfo;
	}
	
	protected void setGraphics5Mode() {
		vdpCanvas.setSize(512, vdpCanvas.getHeight());
		vdpModeRedrawHandler = new Graphics5ModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas, createGraphics45ModeInfo());
		spriteRedrawHandler = createSprite2RedrawHandler(true);
		vdpMmio.setMemoryAccessCycles(8);
		initUpdateBlocks(8);
	}

	protected void setGraphics6Mode() {
		vdpCanvas.setSize(512, vdpCanvas.getHeight());
		vdpModeRedrawHandler = new Graphics6ModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas, createGraphics6ModeInfo());
		spriteRedrawHandler = createSprite2RedrawHandler(true);
		vdpMmio.setMemoryAccessCycles(8);
		initUpdateBlocks(8);
	}

	protected VdpModeInfo createGraphics6ModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		int ramsize = getModeAddressMask();
		
		vdpModeInfo.screen.base = 0;
		vdpModeInfo.screen.size = 0;
		vdpModeInfo.color.base = 0;
		vdpModeInfo.color.size = 0;
		
		// paging!
		vdpModeInfo.patt.base = ((vdpregs[2] & 0x20) << 11) & ramsize;
		vdpModeInfo.patt.size = 54272;
		
		vdpModeInfo.sprite.base = getSpriteTableBase() & ramsize;
		vdpModeInfo.sprite.size = 128;
		vdpModeInfo.sprpat.base = (vdpregs[6] * 0x800) & ramsize;
		vdpModeInfo.sprpat.size = 2048;
		
		return vdpModeInfo;
	}
	@Override
	protected int getModeAddressMask() {
		return 0x1ffff;
	}
	@Override
	protected int getSpriteTableBase() {
		return (((vdpregs[11] & 0x3) << 8) | (vdpregs[5] & 0xff)) << 7; 
	}
	
	@Override
	protected int getColorTableBase() {
		return (((vdpregs[10] & 0x7) << 8) | (vdpregs[3] & 0xff)) << 6;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		// the "blink" controls either the r7/r12 selection for text mode
		// or the swapped page for graphics4-7 modes
		if (vdpregs[13] != 0) {
			if (--blinkPeriod <= 0) {
				blinkOn = !blinkOn;
				blinkPeriod = blinkOn ? blinkOnPeriod : blinkOffPeriod;
				dirtyAll();
			}
		}
	}
}
