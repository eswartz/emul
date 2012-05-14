/**
 * 
 */
package v9t9.video.v9938;


import static v9t9.common.hardware.VdpV9938Consts.*;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.hardware.IVdpV9938;
import v9t9.common.hardware.VdpV9938Consts;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.common.video.VdpColorManager;
import v9t9.common.video.VdpFormat;
import v9t9.video.common.VdpModeInfo;
import v9t9.video.tms9918a.VdpTMS9918ACanvasRenderer;

/**
 * This is a renderer for the V9938 video chip which renders to an IVdpCanvas.  
 * 
 * This functions as a superset of the TMS9918A renderer.
 * <p>
 * Mode bits:
 * <p>
 * R0:	M3 @ 1, M4 @ 2, M5 @ 3
 * R1:	M1 @ 4, M2 @ 3 
 * <p>
 * <pre>
 *                   M1  M2  M3  M4  M5     Mode #
 * Text 1 mode:      1   0   0   0   0		= 1		>81F0, >8000
 * Text 2 mode:      1   0   0   1   0		= 9		>81F0, >8004
 * Multicolor:       0   1   0   0   0		= 2		>81C0, >8000
 * Graphics 1 mode:  0   0   0   0   0		= 0		>81E0, >8000
 * Graphics 2 mode:  0   0   1   0   0		= 4		>81E0, >8002
 * Graphics 3 mode:  0   0   0   1   0		= 8		>81E0, >8004 (bitmap + new sprites)
 * Graphics 4 mode:  0   0   1   1   0		= 12	>81E0, >8006 (bitmap 256x192x16)
 * Graphics 5 mode:  0   0   0   0   1		= 16	>81E0, >8008 (bitmap 512x192x4)
 * Graphics 6 mode:  0   0   1   0   1		= 20	>81E0, >800A (bitmap 512x192x16)
 * Graphics 7 mode:  0   0   1   1   1		= 28	>81E0, >800E (bitmap 256x192x256)
 * </pre>
 * TODO: toying with row masking
 * @author ejs  
 *
 */
public class VdpV9938CanvasRenderer extends VdpTMS9918ACanvasRenderer implements IVdpCanvasRenderer {
	boolean blinkOn;
	private int pageOffset;

	private short[] palette = new short[16];
	private Runnable timerRunnable;

	public VdpV9938CanvasRenderer(ISettingsHandler settings, IVideoRenderer renderer) {
		super(settings, renderer);
		
		timerRunnable = new Runnable() {
			public void run() {
				doTick();
			}
		};
		renderer.getFastTimer().scheduleTask(
				timerRunnable, settings.get(IVdpChip.settingVdpInterruptRate).getInt());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.canvas.video.tms9918a.VdpTMS9918ACanvasRenderer#dispose()
	 */
	@Override
	public void dispose() {
		renderer.getFastTimer().cancelTask(timerRunnable);
	}

	protected void setupRegisters() {
		vdpregs = new byte[48];
		
		VdpColorManager cm = vdpCanvas.getColorMgr();
		
		for (int i = 0; i < 16; i++) {
			// RBxG format
			int value = vdpChip.getRegister(VdpV9938Consts.REG_PAL0 + i);
			cm.setGRB333(i, (value >> 0) & 0x7, (value >> 12) & 0x7, (value >> 8) & 0x7); 
		}
	}
	
	@Override
	protected int doWriteVdpReg(int reg, byte old, byte val) {
		//System.out.println(Utils.toHex2(reg) + " = " + Utils.toHex2(val));
		int redraw = super.doWriteVdpReg(reg, old, val);
		
		if (reg != 16 && reg != 17 && reg < 32 && old == val)
			return redraw;
		
		switch (reg) {
		case 0:
			if (CHANGED(old, val, R0_M4 + R0_M5)) {
				redraw |= REDRAW_MODE;
			}
			break;
		case 2:
			// page
			if (CHANGED(old, val, 0x60)) {
				redraw |= REDRAW_MODE;
			}
			break;
		case 8:
			// memory, input bus, display mode, line count
			if (CHANGED(old, val, R8_VR)) {
				redraw |= REDRAW_MODE;
			}
			if (CHANGED(old, val, R8_BW)) {
				vdpCanvas.getColorMgr().setGreyscale((val & R8_BW) != 0);
				redraw |= REDRAW_PALETTE;
			}
			if (CHANGED(old, val, R8_SPD)) {
				drawSprites = (val & R8_SPD) == 0;
				redraw |= REDRAW_SPRITES;
			}
			if (CHANGED(old, val, R8_TP)) {
				vdpCanvas.getColorMgr().setClearFromPalette((val & R8_TP) != 0);
				redraw |= REDRAW_PALETTE + REDRAW_SPRITES;
			}
			break;
		case 9:
			// lines and stuff
			if (CHANGED(old, val, R9_LN)) {
				//vdpCanvas.setSize(256, (val & R9_LN) != 0 ? 212 : 192);
				redraw |= REDRAW_MODE;
			}
			if (CHANGED(old, val, R9_EO + R9_IL)) {
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
			
			redraw |= REDRAW_PALETTE;
			break;
			
			
		case 18: {
			// display adjust register (pg 6) / pan
			updateOffset();
			break;
		}
		
		case 20:
		case 21:
		case 22:
			// color burst registers
			break;
		case 23: {
			updateOffset();
			break;
		}
		
		}
		return redraw;
	}

	/**
	 * 
	 */
	private void updateOffset() {
		int xoffs = (byte)(vdpregs[18] << 4) >> 4;
		int yoffs = ((vdpregs[18] & 0xf0) >> 4) + (vdpregs[23]);
		vdpCanvas.setOffset(xoffs, yoffs);
		// V9990 reference says:
		// (P1 and B1 by 1 pixel unit, P2, B2 and B3 by 2-pixel unit, B4, B5 and B6 by 4-pixel unit) 
		dirtyAll();
		
	}

	/**
	 * @param col
	 */
	protected void setPaletteColor(final int col) {
		int r = (palette[col] >> 12) & 0x7;
		int b = (palette[col] >> 8) & 0x7;
		int g = (palette[col]) & 0x7;
		//System.out.println("palette " + paletteidx + ": " + g +"|"+ r + "|"+ b);
		vdpCanvas.getColorMgr().setGRB333(col, g, r, b);
		dirtyAll();
	}

	@Override
	protected void establishVideoMode() {
		modeNumber = vdpChip.getModeNumber();
		switch (modeNumber) {
		case MODE_TEXT2:
			setText2Mode();
			dirtyAll();	// for border
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
		case MODE_GRAPHICS7:
			setGraphics7Mode();
			break;
		default:
			super.establishVideoMode();
			break;
		}
	}
	
	@Override
	protected void setupBackdrop() {
		if (modeNumber == MODE_GRAPHICS5) {
			// even-odd tiling function
			vdpCanvas.getColorMgr().setClearColor((vdpbg >> 2) & 0x3);
			vdpCanvas.getColorMgr().setClearColor1((vdpbg) & 0x3);
			vdpCanvas.clearToEvenOddClearColors();
		} else if (modeNumber == MODE_GRAPHICS7) {
			vdpCanvas.getColorMgr().setClearColor(vdpregs[7] & 0xff);
			vdpCanvas.clear();
		} else {
			super.setupBackdrop();
		}
	}
	@Override
	protected int getMaxRedrawblocks() {
		return 80 * 27;
	}
	
	
	protected void setText2Mode() {
		vdpCanvas.setFormat(VdpFormat.TEXT);
		vdpCanvas.setSize(512, getVideoHeight());
		vdpModeInfo = createText2ModeInfo();
		vdpModeRedrawHandler = new Text2ModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = null;
		
		initUpdateBlocks(6);
	}

	protected VdpModeInfo createText2ModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		
		vdpModeInfo.screen.base = vdpChip.getScreenTableBase();
		vdpModeInfo.screen.size = vdpChip.getScreenTableSize();
		vdpModeInfo.patt.base = vdpChip.getPatternTableBase();
		vdpModeInfo.patt.size = vdpChip.getPatternTableSize();
		vdpModeInfo.color.base = vdpChip.getColorTableBase();
		vdpModeInfo.color.size = vdpChip.getColorTableSize();
		return vdpModeInfo;
	}
	

	protected void setGraphics3Mode() {
		super.setBitmapMode();
		spriteRedrawHandler = createSprite2RedrawHandler(false);
	}

	private Sprite2RedrawHandler createSprite2RedrawHandler(boolean wide) {
		
		return new Sprite2RedrawHandler(vdpRedrawInfo, createSpriteModeInfo());
	}
	
	protected void setGraphics4Mode() {
		vdpCanvas.setFormat(VdpFormat.COLOR16_1x1);
		vdpCanvas.setSize(256, getVideoHeight(), isInterlacedEvenOdd());
		vdpModeInfo = createGraphics45ModeInfo();
		vdpModeRedrawHandler = new Graphics4ModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = createSprite2RedrawHandler(false);
		
		
		initUpdateBlocks(8);
	}

	
	protected VdpModeInfo createGraphics45ModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		

		vdpModeInfo.screen.base = vdpChip.getScreenTableBase();
		vdpModeInfo.screen.size = vdpChip.getScreenTableSize();
		vdpModeInfo.color.base = vdpChip.getColorTableBase();
		vdpModeInfo.color.size = vdpChip.getColorTableSize();
		
		vdpModeInfo.patt.base = vdpChip.getPatternTableBase();
		vdpModeInfo.patt.size = vdpChip.getPatternTableSize();
		
		vdpModeInfo.sprite.base = vdpChip.getSpriteTableBase();
		vdpModeInfo.sprite.size = vdpChip.getSpriteTableSize();
		vdpModeInfo.sprpat.base = vdpChip.getSpritePatternTableBase();
		vdpModeInfo.sprpat.size = vdpChip.getSpritePatternTableSize();
		
		return vdpModeInfo;
	}
	
	protected void setGraphics5Mode() {
		vdpCanvas.setFormat(VdpFormat.COLOR4_1x1);
		vdpCanvas.setSize(512, getVideoHeight(), isInterlacedEvenOdd());
		vdpModeInfo = createGraphics45ModeInfo();
		vdpModeRedrawHandler = new Graphics5ModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = createSprite2RedrawHandler(true);
		
		
		initUpdateBlocks(8);
	}

	protected void setGraphics6Mode() {
		vdpCanvas.setFormat(VdpFormat.COLOR16_1x1);
		vdpCanvas.setSize(512, getVideoHeight(), isInterlacedEvenOdd());
		vdpModeInfo = createGraphics67ModeInfo();
		vdpModeRedrawHandler = new Graphics6ModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = createSprite2RedrawHandler(true);
		
		
		initUpdateBlocks(8);
	}

	protected VdpModeInfo createGraphics67ModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		
		vdpModeInfo.screen.base = vdpChip.getScreenTableBase();
		vdpModeInfo.screen.size = vdpChip.getScreenTableSize();
		vdpModeInfo.color.base = vdpChip.getColorTableBase();
		vdpModeInfo.color.size = vdpChip.getColorTableSize();
		
		vdpModeInfo.patt.base = vdpChip.getPatternTableBase();
		vdpModeInfo.patt.size = vdpChip.getPatternTableSize();
		
		vdpModeInfo.sprite.base = vdpChip.getSpriteTableBase();
		vdpModeInfo.sprite.size = vdpChip.getSpriteTableSize();
		vdpModeInfo.sprpat.base = vdpChip.getSpritePatternTableBase();
		vdpModeInfo.sprpat.size = vdpChip.getSpritePatternTableSize();
		
		return vdpModeInfo;
	}
	
	protected void setGraphics7Mode() {
		vdpCanvas.setFormat(VdpFormat.COLOR256_1x1);
		vdpCanvas.setSize(256, getVideoHeight(), isInterlacedEvenOdd());
		vdpModeInfo = createGraphics67ModeInfo();
		vdpModeRedrawHandler = new Graphics7ModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = createSprite2RedrawHandler(false);
		
		initUpdateBlocks(8);
	}

	

	protected int getVideoHeight() {
		int baseHeight = ((vdpregs[9] & R9_LN) != 0 ? 212 : 192);
		//return ((vdpregs[9] & R9_IL) != 0) ? baseHeight * 2 : baseHeight;
		return baseHeight;
	}

	/**
	 * Tell whether interlacing is active.
	 * 
	 * For use in rendering, we need to know whether raw R9_IL (interlace) bit is set
	 * and also the R9_EO (even/odd) bit is set, which would provide the page flipping
	 * required to *see* two pages.  Finally, the "odd" graphics page must be visible
	 * for the flipping and interlacing to occur.
	 * @return
	 */
	
	public boolean isInterlacedEvenOdd() {
		return (vdpregs[9] & R9_EO + R9_IL) == R9_EO + R9_IL 
			&& (vdpregs[2] & 0x20) != 0;

	}

	/* (non-Javadoc)
	 * @see v9t9.canvas.video.tms9918a.VdpTMS9918ACanvasRenderer#registerChanged(int, int)
	 */
	@Override
	public void registerChanged(int reg, int value) {
		if (reg >= REG_PAL0 && reg < REG_PAL0 + 16) {
			int color = reg - REG_PAL0;
			palette[color] = (short) value;
			setPaletteColor(color);
		}
		else {
			if (reg == 12) {
				blinkOn = false;
			} else if (reg == 13) {
				blinkOn = value != 0 && (value & 0xf) == 0x0;
			}
			super.registerChanged(reg, value);
		}
	}


	public boolean isBlinkOn() {
		return blinkOn;
	}
	
	public int getGraphicsPageOffset() {
		return pageOffset;
	}
	/**
	 * 
	 */
	protected synchronized void doTick() {

		// The "blink" controls either the r7/r12 selection for text mode
		// or the page selection for graphics 4-7 modes.
		
		// We don't redraw for interlacing; we just detect changes on both
		// pages and draw them into an interleaved image.  There's not
		// enough speed in this implementation to allow redrawing at 60 fps.
		int prevPageOffset = pageOffset;
		pageOffset = 0;
		if ((modeNumber == MODE_TEXT2 || (modeNumber >= MODE_GRAPHICS4 && modeNumber <= MODE_GRAPHICS7))
				&& vdpregs[13] != 0) {
			boolean isBlinking = modeNumber == MODE_TEXT2;
			boolean isPageFlipping = (vdpregs[2] & 0x20) != 0 && !isBlinking;
			
			int blinkPeriod = ((IVdpV9938) vdpChip).getBlinkPeriod();
			int blinkOffPeriod = ((IVdpV9938) vdpChip).getBlinkOffPeriod();
			boolean isAltMode = System.currentTimeMillis() % blinkPeriod >= blinkOffPeriod;
			
			if (isPageFlipping) {
				pageOffset = isAltMode ? vdpChip.getGraphicsPageSize() : 0;
				if (prevPageOffset != pageOffset) {
					//System.out.println("dirtying " + pageOffset);
					vdpModeInfo.patt.base = vdpChip.getPatternTableBase() ^ pageOffset;
					dirtyAll();
				}
			} else if (isBlinking) {
				boolean wasOn = blinkOn;
				blinkOn = isAltMode;
				if (blinkOn != wasOn) {
					if (vdpModeRedrawHandler instanceof Text2ModeRedrawHandler) {
						((Text2ModeRedrawHandler) vdpModeRedrawHandler).updateForBlink();
					}
				}
			}
		}
	}

}
