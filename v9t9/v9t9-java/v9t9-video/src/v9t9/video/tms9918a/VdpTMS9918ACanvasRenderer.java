/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 20, 2006
 *
 */
package v9t9.video.tms9918a;

import static v9t9.common.hardware.VdpTMS9918AConsts.*;

import java.util.Arrays;

import ejs.base.properties.IProperty;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.hardware.IVdpTMS9918A;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryWriteListener;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.common.video.RedrawBlock;
import v9t9.common.video.VdpChanges;
import v9t9.common.video.VdpFormat;
import v9t9.common.video.VdpModeInfo;
import v9t9.video.BlankModeRedrawHandler;
import v9t9.video.IVdpModeRedrawHandler;
import v9t9.video.VdpRedrawInfo;

/**
 * This is a renderer for the TI-99/4A VDP chip which renders to an IVdpCanvas.
 * 
 * @author ejs
 */
public class VdpTMS9918ACanvasRenderer implements IVdpCanvasRenderer, IMemoryWriteListener, IRegisterWriteListener {
	private RedrawBlock[] blocks;

	protected byte vdpbg;
	protected byte vdpfg;
	protected boolean drawSprites = true;
	//private final static int REDRAW_NOW = 1		;	/* same-mode change */
	protected final static int REDRAW_SPRITES = 2	;	/* sprites change */
	protected final static int REDRAW_MODE = 4		;	/* mode change */
	protected final static int REDRAW_BLANK = 8		;	/* make blank */
	protected final static int REDRAW_PALETTE = 16;
	protected boolean vdpchanged;

	protected IVdpCanvas vdpCanvas;
	protected IVdpModeRedrawHandler vdpModeRedrawHandler;
	protected SpriteRedrawHandler spriteRedrawHandler;
	protected final VdpChanges vdpChanges = new VdpChanges(getMaxRedrawblocks());
	
	protected IVdpTMS9918A vdpChip;
	protected BlankModeRedrawHandler blankModeRedrawHandler;
	protected VdpModeInfo vdpModeInfo;
	protected VdpRedrawInfo vdpRedrawInfo;

	protected byte[] vdpregs;

	protected int modeNumber;

	protected final IVideoRenderer renderer;

	private IProperty pauseMachine;

	public VdpTMS9918ACanvasRenderer(ISettingsHandler settings, IVideoRenderer renderer) {
		this.renderer = renderer;
		this.vdpChip = (IVdpTMS9918A) renderer.getVdpHandler();

		this.vdpCanvas = renderer.getCanvas();
		vdpCanvas.setSize(256, 192);
		
		setupRegisters();
		
		vdpRedrawInfo = new VdpRedrawInfo(vdpregs, vdpChip, this, vdpChanges, vdpCanvas);
		blankModeRedrawHandler = new BlankModeRedrawHandler(vdpRedrawInfo, createBlankModeInfo());
		
		pauseMachine = settings.get(IMachine.settingPauseMachine);
		
		vdpChip.getVideoMemory().addWriteListener(this);
		vdpChip.addWriteListener(this);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.video.IVdpCanvasRenderer#dispose()
	 */
	@Override
	public void dispose() {
		
	}

	protected void setupRegisters() {
		// copy of registers in IVdpChip
		vdpregs = new byte[8];
	}

	protected final boolean CHANGED(byte old,byte val, int v) { return (old&(v))!=(val&(v)); }

    /* (non-Javadoc)
     * @see v9t9.handlers.VdpHandler#writeVdpReg(byte, byte, byte)
     */
    final synchronized protected void writeVdpReg(final int reg, byte val) {
    	int redraw = doWriteVdpReg(reg, vdpregs[reg], val);

    	/*  This flag must be checked first because
	 	   it affects the meaning of the following 
	 	   calls and checks. */
	 	if ((redraw & REDRAW_MODE) != 0) {
	 		setVideoMode();
	 		setupBackdrop();
	 		dirtyAll();
	 	}
	
	 	if ((redraw & REDRAW_SPRITES) != 0) {
			dirtySprites();
		}

	 	if ((redraw & REDRAW_PALETTE) != 0) {
	 		setupBackdrop();
	 		dirtyAll();
	 	}
	
	 	if ((redraw & REDRAW_BLANK) != 0) {
	 		if ((vdpregs[1] & R1_NOBLANK) == 0) {
	 			vdpCanvas.setBlank(true);
	 			dirtyAll();
	 			//update();
	 		} else {
	 			vdpCanvas.setBlank(false);
	 			dirtyAll();
	 			//update();
	 		}
	 	}
    }
    
    /** Set the backdrop based on the mode */
    protected void setupBackdrop() {
    	vdpCanvas.setClearColor(vdpbg & 0xf);
	}

	protected int doWriteVdpReg(int reg, byte old, byte val) {
    	int redraw = 0;
    	
    	vdpregs[reg] = val;
    	if (old == val)
    		return redraw;
    	
 
    	switch (reg) {
    	case 0:					/* bitmap/video-in */
    		if (CHANGED(old, val, R0_M3+R0_EXTERNAL)) {
    			redraw |= REDRAW_MODE;
    		}
    		break;

    	case 1:					/* various modes, sprite stuff */
    		if (CHANGED(old, val, R1_NOBLANK)) {
    			redraw |= REDRAW_BLANK | REDRAW_MODE;
    		}

    		if (CHANGED(old, val, R1_SPRMAG + R1_SPR4)) {
    			redraw |= REDRAW_SPRITES;
    		}

    		if (CHANGED(old, val, R1_M1 | R1_M2)) {
    			redraw |= REDRAW_MODE;
    		}


    		break;

    	case 2:					/* screen image table */
    	case 3:					/* color table */
    	case 4:					/* pattern table */
    	case 5:					/* sprite table */
    	case 6:					/* sprite pattern table */
    		redraw |= REDRAW_MODE;
    		break;

    	case 7:					/* foreground/background color */
			vdpfg = (byte) ((val >> 4) & 0xf);
			vdpbg = (byte) (val & 0xf);
			redraw |= REDRAW_PALETTE;
    		break;

    	default:

    	}

    	return redraw;
    }

    /** Tell if the registers indicate a blank screen. */
    protected boolean isBlank() {
    	return (vdpregs[1] & R1_NOBLANK) == 0;
    }
    
    /**
     * Set up the vdpModeRedrawHandler, spriteRedrawHandler, and memory access
     * times for the mode defined by the vdp registers.
     */
    protected final void setVideoMode() {
    	/* Is the screen really blank? */
		if (isBlank()) {
			// clear the canvas first
			if (vdpModeRedrawHandler != null)
				vdpModeRedrawHandler.clear();
			
			// now, ignore any changes or redraw requests
			setBlankMode();
			vdpModeRedrawHandler = blankModeRedrawHandler;
		}
		
		/* Set up actual mode stuff too */
		establishVideoMode();
    }
    
    protected void establishVideoMode() {
    	modeNumber = vdpChip.getModeNumber();
		switch (modeNumber) {
		case MODE_TEXT:
			setTextMode();
			dirtyAll();	// for border
			break;
		case MODE_MULTI:
			setMultiMode();
			break;
		case MODE_BITMAP:
			setBitmapMode();
			break;
		case MODE_GRAPHICS:
		default:
			setGraphicsMode();
			break;
		}
	}

    
    protected VdpModeInfo createSpriteModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 

		vdpModeInfo.sprite.base = vdpChip.getSpriteTableBase();
		vdpModeInfo.sprite.size = vdpChip.getSpriteTableSize();
		vdpModeInfo.sprpat.base = vdpChip.getSpritePatternTableBase();
		vdpModeInfo.sprpat.size = vdpChip.getSpritePatternTableSize();
		return vdpModeInfo;
	}


	protected void setGraphicsMode() {
		vdpCanvas.setFormat(VdpFormat.COLOR16_8x8);
		vdpCanvas.setSize(256, 192);
		vdpModeInfo = createGraphicsModeInfo();
		vdpModeRedrawHandler = new GraphicsModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = createSpriteRedrawHandler();
		
		initUpdateBlocks(8);
	}

	protected SpriteRedrawHandler createSpriteRedrawHandler() {
		return new SpriteRedrawHandler(vdpRedrawInfo, createSpriteModeInfo());
	}

	
	protected VdpModeInfo createGraphicsModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		vdpModeInfo.screen.base = vdpChip.getScreenTableBase();
		vdpModeInfo.screen.size = vdpChip.getScreenTableSize();
		vdpModeInfo.color.base = vdpChip.getColorTableBase();
		vdpModeInfo.color.size = 32;
		vdpModeInfo.patt.base = vdpChip.getPatternTableBase();
		vdpModeInfo.patt.size = 2048;
		vdpModeInfo.sprite.base = vdpChip.getSpriteTableBase();
		vdpModeInfo.sprite.size = 128;
		vdpModeInfo.sprpat.base = vdpChip.getSpritePatternTableBase();
		vdpModeInfo.sprpat.size = 2048;
		return vdpModeInfo;
	}

	protected void setMultiMode() {
		vdpCanvas.setFormat(VdpFormat.COLOR16_4x4);
		vdpCanvas.setSize(256, 192);
		vdpModeInfo = createMultiModeInfo();
		vdpModeRedrawHandler = new MulticolorModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = createSpriteRedrawHandler();
		
		initUpdateBlocks(8);
	}

	protected VdpModeInfo createMultiModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		
		vdpModeInfo.screen.base = vdpChip.getScreenTableBase();
		vdpModeInfo.screen.size = 768;
		vdpModeInfo.color.base = 0;
		vdpModeInfo.color.size = 0;
		vdpModeInfo.patt.base = vdpChip.getPatternTableBase();
		vdpModeInfo.patt.size = 1536;
		vdpModeInfo.sprite.base = vdpChip.getSpriteTableBase();
		vdpModeInfo.sprite.size = 128;
		
		return vdpModeInfo;
	}

	protected void setTextMode() {
		vdpCanvas.setFormat(VdpFormat.TEXT);
		vdpCanvas.setSize(256, 192);
		vdpModeInfo = createTextModeInfo();
		vdpModeRedrawHandler = new TextModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = null;
		
		initUpdateBlocks(6);
	}

	protected VdpModeInfo createTextModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		
		vdpModeInfo.screen.base = vdpChip.getScreenTableBase();
		vdpModeInfo.screen.size = 960;
		vdpModeInfo.color.base = vdpChip.getColorTableBase();
		vdpModeInfo.color.size = 0;
		vdpModeInfo.patt.base = vdpChip.getPatternTableBase();
		vdpModeInfo.patt.size = 2048;
		vdpModeInfo.sprite.base = vdpChip.getSpriteTableBase();
		vdpModeInfo.sprite.size = 0;
		vdpModeInfo.sprpat.base = vdpChip.getSpritePatternTableBase();
		vdpModeInfo.sprpat.size = 0;
		
		return vdpModeInfo;
	}

	protected void setBitmapMode() {
		vdpCanvas.setFormat(VdpFormat.COLOR16_8x1);
		vdpCanvas.setSize(256, 192);
		vdpModeInfo = createBitmapModeInfo();
		vdpModeRedrawHandler = new BitmapModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = createSpriteRedrawHandler();
		
		initUpdateBlocks(8);
	}

	protected VdpModeInfo createBitmapModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 

		vdpModeInfo.screen.base = vdpChip.getScreenTableBase();
		vdpModeInfo.screen.size = vdpChip.getScreenTableSize();
		vdpModeInfo.sprite.base = vdpChip.getSpriteTableBase();
		vdpModeInfo.sprite.size = vdpChip.getSpriteTableSize();
		vdpModeInfo.sprpat.base = vdpChip.getSpritePatternTableBase();
		vdpModeInfo.sprpat.size = vdpChip.getSpritePatternTableSize();

		vdpModeInfo.color.base = vdpChip.getColorTableBase();
		vdpModeInfo.color.size = vdpChip.getColorTableSize();
		
		vdpModeInfo.patt.base = vdpChip.getPatternTableBase();
		vdpModeInfo.patt.size = vdpChip.getPatternTableSize();
		
		return vdpModeInfo;
	}

	protected void setBlankMode() {
		vdpCanvas.setSize(256, vdpCanvas.getHeight());
		spriteRedrawHandler = null;
		initUpdateBlocks(8);
	}

    protected VdpModeInfo createBlankModeInfo() {
    	VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
    	vdpModeInfo.screen.base = 0;
		vdpModeInfo.screen.size = 0;
		vdpModeInfo.color.base = 0;
		vdpModeInfo.color.size = 0;
		vdpModeInfo.patt.base = 0;
		vdpModeInfo.patt.size = 0;
		vdpModeInfo.sprite.base = 0;
		vdpModeInfo.sprite.size = 0;
		vdpModeInfo.sprpat.base = 0;
		vdpModeInfo.sprpat.size = 0;	
		return vdpModeInfo;
	}

	/** preinitialize the update blocks with the sizes for this mode */
	protected void initUpdateBlocks(int blockWidth) {
		int w = blockWidth;
    	int h = 8;
		if (blocks == null) {
			blocks = new RedrawBlock[getMaxRedrawblocks()];
			for (int i = 0; i < blocks.length; i++) {
				blocks[i] = new RedrawBlock();
			}
		}
		if (blocks[0].w != blockWidth) {
			for (int i = 0; i < blocks.length; i++) {
				blocks[i].w = w;
				blocks[i].h = h;
			}
		}
	}

	protected int getMaxRedrawblocks() {
		return 1024;
	}

    public synchronized void touchAbsoluteVdpMemory(int vdpaddr) {
    	try {
			if (vdpModeRedrawHandler != null) {
				vdpChanges.changed |= vdpModeRedrawHandler.touch(vdpaddr);
		    	if (spriteRedrawHandler != null) {
		    		vdpChanges.changed |= spriteRedrawHandler.touch(vdpaddr);
		    	}
			}
    	} catch (NullPointerException e) {
    		// XXX: sprite.touch is null sometimes???
    	}
    }
    
	protected void dirtySprites() {
		vdpChanges.sprite = -1;
		Arrays.fill(vdpChanges.sprpat, 0, vdpChanges.sprpat.length, (byte)1);
		vdpChanges.changed = true;
	}


	protected void dirtyAll() {
		vdpChanges.changed = true;
		vdpChanges.fullRedraw = true;
	}
	
	public synchronized boolean update() {
		if (!vdpChanges.changed)
			return false;
		//System.out.println(System.currentTimeMillis());
		if (vdpModeRedrawHandler != null) {
			//long start = System.currentTimeMillis();
			
			int count = 0;
			
			// don't let video rendering happen in middle of updating
			synchronized (vdpCanvas) {
				vdpCanvas.syncColors();
				
				vdpModeRedrawHandler.prepareUpdate();
				
				if (vdpChanges.fullRedraw) {
					// clear for the actual mode (not blank mode)
					vdpModeRedrawHandler.clear();
					vdpCanvas.markDirty();
				}
				
				if (!isBlank()) {
					if (spriteRedrawHandler != null && drawSprites) {
						byte vdpStatus = (byte) vdpChip.getRegister(REG_ST);
						vdpStatus = spriteRedrawHandler.updateSpriteCoverage(
								vdpStatus, vdpChanges.fullRedraw);
						if (!pauseMachine.getBoolean())
							vdpChip.setRegister(REG_ST, vdpStatus);
					}
					
					if (vdpChanges.fullRedraw) {
						vdpChanges.screen.set(0, getMaxRedrawblocks());
					}
					
					count = vdpModeRedrawHandler.updateCanvas(blocks);
					if (spriteRedrawHandler != null && drawSprites) {
						spriteRedrawHandler.updateCanvas(vdpChanges.fullRedraw);
					}
				}
			}

			vdpCanvas.markDirty(blocks, count);
			
			vdpChanges.screen.clear();
			Arrays.fill(vdpChanges.patt, (byte) 0);
			Arrays.fill(vdpChanges.color, (byte) 0);
			
			if (drawSprites) {
				Arrays.fill(vdpChanges.sprpat, (byte) 0);
				vdpChanges.sprite = 0;
			}
			
			vdpChanges.fullRedraw = false;
			
			//System.out.println("elapsed: " + (System.currentTimeMillis() - start));
		}
		
		vdpchanged = false;
		return true;
	}

	@Override
	public VdpModeInfo getModeInfo() {
		return vdpModeInfo;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.canvas.video.IVdpCanvasHandler#getCanvas()
	 */
	@Override
	public IVdpCanvas getCanvas() {
		return vdpCanvas;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.IVdpChip.IVdpListener#vdpRegisterChanged(int, byte)
	 */
	@Override
	public void registerChanged(int reg, int value) {
		if (reg >= 0 && reg < vdpChip.getVdpRegisterCount())
			writeVdpReg(reg, (byte) value);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryWriteListener#changed(v9t9.common.memory.IMemoryEntry, int, boolean)
	 */
	@Override
	public void changed(IMemoryEntry entry, int addr, boolean isByte) {
		touchAbsoluteVdpMemory(addr);
		if (!isByte)
			touchAbsoluteVdpMemory(addr + 1);
	}
	
}
