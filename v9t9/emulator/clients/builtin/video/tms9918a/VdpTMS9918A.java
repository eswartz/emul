/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 20, 2006
 *
 */
package v9t9.emulator.clients.builtin.video.tms9918a;

import java.util.Arrays;

import v9t9.emulator.clients.builtin.video.BlankModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpConstants;
import v9t9.emulator.clients.builtin.video.VdpModeRedrawHandler;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.engine.memory.ByteMemoryArea;
import v9t9.engine.memory.MemoryDomain;

/**
 * This is the 99/4A VDP chip.
 * @author ejs
 */
public class VdpTMS9918A implements VdpHandler {
	private RedrawBlock[] blocks;
	protected MemoryDomain vdpMemory;

	protected byte vdpregs[];
	protected byte vdpbg;
	protected byte vdpfg;
	protected boolean drawSprites;
	//private final static int REDRAW_NOW = 1		;	/* same-mode change */
	protected final static int REDRAW_SPRITES = 2	;	/* sprites change */
	protected final static int REDRAW_MODE = 4		;	/* mode change */
	protected final static int REDRAW_BLANK = 8		;	/* make blank */
	protected final static int REDRAW_PALETTE = 16;
	protected boolean vdpchanged;

	protected VdpCanvas vdpCanvas;
	protected VdpModeRedrawHandler vdpModeRedrawHandler;
	protected SpriteRedrawHandler spriteRedrawHandler;
	protected VdpChanges vdpChanges = new VdpChanges();
	protected byte vdpStatus;
	
	private final VdpMmio vdpMmio;

	public VdpTMS9918A(MemoryDomain videoMemory, VdpMmio vdpMmio, VdpCanvas vdpCanvas) {
		this.vdpMemory = videoMemory;
		
		this.vdpMmio = vdpMmio;
		this.vdpCanvas = vdpCanvas;
		this.vdpregs = allocVdpRegs();
		vdpMmio.setVdpHandler(this);
	}

	public VdpMmio getVdpMmio() {
		return vdpMmio;
	}
	protected byte[] allocVdpRegs() {
		return new byte[8];
	}

	public byte readVdpReg(int reg) {
		return vdpregs[reg];
	}
	
	protected final boolean CHANGED(int r,byte val,int v) { return ((vdpregs[r]&(v))!=(val&(v))); }

    /* (non-Javadoc)
     * @see v9t9.handlers.VdpHandler#writeVdpReg(byte, byte, byte)
     */
    public void writeVdpReg(int reg, byte val) {
    	int         redraw = 0;

    	switch (reg) {
    	case 0:					/* bitmap/video-in */
    		if (CHANGED(0, val, VdpConstants.R0_BITMAP+VdpConstants.R0_EXTERNAL)) {
    			redraw |= REDRAW_MODE;
    		}
    		vdpregs[0] = val;
    		break;

    	case 1:					/* various modes, sprite stuff */
    		if (CHANGED(1, val, VdpConstants.R1_NOBLANK)) {
    			redraw |= REDRAW_BLANK | REDRAW_MODE;
    		}

    		if (CHANGED(1, val, VdpConstants.R1_SPRMAG + VdpConstants.R1_SPR4)) {
    			redraw |= REDRAW_SPRITES;
    		}

    		if (CHANGED(1, val, VdpConstants.R1_TEXT | VdpConstants.R1_MULTI)) {
    			redraw |= REDRAW_MODE;
    		}

    		/* if interrupts enabled, and interrupt was pending, trigger it */
    		if ((val & VdpConstants.R1_INT) != 0 
    		&& 	(vdpregs[1] & VdpConstants.R1_INT) == 0 
    		&&	(vdpStatus & VdpConstants.VDP_INTERRUPT) != 0) 
    		{
    			//trigger9901int( M_INT_VDP);	// TODO
    		}

    		vdpregs[1] = val;
    		break;

    	case 2:					/* screen image table */
    		if (vdpregs[2] != val) {
    			redraw |= REDRAW_MODE;
    			vdpregs[2] = val;
    		}
    		break;

    	case 3:					/* color table */
    		if (vdpregs[3] != val) {
    			redraw |= REDRAW_MODE;
    			vdpregs[3] = val;
    		}
    		break;

    	case 4:					/* pattern table */
    		if (vdpregs[4] != val) {
    			redraw |= REDRAW_MODE;
    			vdpregs[4] = val;
    		}
    		break;

    	case 5:					/* sprite table */
    		if (vdpregs[5] != val) {
    			redraw |= REDRAW_MODE;
    			vdpregs[5] = val;
    		}
    		break;

    	case 6:					/* sprite pattern table */
    		if (vdpregs[6] != val) {
    			redraw |= REDRAW_MODE;
    			vdpregs[6] = val;
    		}
    		break;

    	case 7:					/* foreground/background color */
    		if (vdpregs[7] != val) {
    			vdpfg = (byte) ((val >> 4) & 0xf);
    			vdpbg = (byte) (val & 0xf);
    			redraw |= REDRAW_PALETTE;
    			vdpregs[7] = val;
    		}
    		break;

    	default:

    	}

    	/*  This flag must be checked first because
    	   it affects the meaning of the following 
    	   calls and checks. */
    	if ((redraw & REDRAW_MODE) != 0) {
    		establishVideoMode();
    		dirtyAll();
    	}

    	if ((redraw & REDRAW_SPRITES) != 0) {
			dirtySprites();
		}

    	if ((redraw & REDRAW_PALETTE) != 0) {
    		vdpCanvas.setClearColor(vdpbg);
    		dirtyAll();
    	}

    	if ((redraw & REDRAW_BLANK) != 0) {
    		if ((vdpregs[1] & VdpConstants.R1_NOBLANK) == 0) {
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

    /** Tell if the registers indicate a blank screen. */
    protected boolean isBlank() {
    	return (vdpregs[1] & VdpConstants.R1_NOBLANK) == 0;
    }
    
    /**
     * Set up the vdpModeRedrawHandler, spriteRedrawHandler, and memory access
     * times for the mode defined by the vdp registers.
     */
    protected void establishVideoMode() {
    	/* Is the screen really blank? */
		if (isBlank()) {
			setBlankMode();
			return;
		}
		
    	if ((vdpregs[0] & VdpConstants.R0_BITMAP) != 0) {
    		setBitmapMode();
    	} else if ((vdpregs[1] & VdpConstants.R1_TEXT) != 0) {
			setTextMode();
		} else if ((vdpregs[1] & VdpConstants.R1_MULTI) != 0) {
			setMultiMode();
		} else {
			setGraphicsMode();
		}
	}

	protected void setGraphicsMode() {
		vdpModeRedrawHandler = new GraphicsModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas);
		spriteRedrawHandler = new SpriteRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas);
		vdpMmio.setMemoryAccessCycles(8);
		initUpdateBlocks(8);
	}

	protected void setMultiMode() {
		vdpModeRedrawHandler = new MulticolorModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas);
		spriteRedrawHandler = new SpriteRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas);
		vdpMmio.setMemoryAccessCycles(2);
		initUpdateBlocks(8);
	}

	protected void setTextMode() {
		vdpModeRedrawHandler = new TextModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas);
		spriteRedrawHandler = null;
		vdpMmio.setMemoryAccessCycles(1);
		initUpdateBlocks(6);
	}

	protected void setBitmapMode() {
		vdpModeRedrawHandler = new BitmapModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas);
		spriteRedrawHandler = new SpriteRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas);
		vdpMmio.setMemoryAccessCycles(8);
		initUpdateBlocks(8);
	}

	protected void setBlankMode() {
		vdpModeRedrawHandler = new BlankModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas);
		spriteRedrawHandler = null;
		vdpMmio.setMemoryAccessCycles(0);
		initUpdateBlocks(8);
	}

    /** preinitialize the update blocks with the sizes for this mode */
	protected void initUpdateBlocks(int blockWidth) {
		int w = blockWidth;
    	int h = 8;
		if (blocks == null) {
			blocks = new RedrawBlock[1024];
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

	/* (non-Javadoc)
     * @see v9t9.handlers.VdpHandler#readVdpStatus()
     */
    public byte readVdpStatus() {
        return vdpStatus;
    }

    /* (non-Javadoc)
     * @see v9t9.handlers.VdpHandler#writeVdpMemory(short, byte)
     */
    public void touchAbsoluteVdpMemory(int vdpaddr, byte val) {
		if (vdpModeRedrawHandler != null) {
	    	vdpchanged |= vdpModeRedrawHandler.touch(vdpaddr);
	    	if (spriteRedrawHandler != null) {
	    		vdpchanged |= spriteRedrawHandler.touch(vdpaddr);
	    	}
		}
    }
    
    public byte readAbsoluteVdpMemory(int vdpaddr) {
    	return vdpMemory.readByte(vdpaddr & 0x3fff);
    }

	protected void dirtySprites() {
		if (!drawSprites)
			return;
		vdpChanges.sprite = -1;
		Arrays.fill(vdpChanges.sprpat, 0, vdpChanges.sprpat.length, (byte)1);
		vdpchanged = true;
	}


	protected void dirtyAll() {
		vdpchanged = true;
		vdpChanges.fullRedraw = true;
	}
	
	public void update() {
		if (!vdpchanged)
			return;
		
		if (vdpModeRedrawHandler != null) {
			vdpModeRedrawHandler.propagateTouches();
			
			if (spriteRedrawHandler != null) {
				vdpStatus = spriteRedrawHandler.updateSpriteCoverage(vdpStatus);
			}
			
			if (vdpChanges.fullRedraw) {
				vdpCanvas.clear();
				vdpCanvas.markDirty();
			}
			
			int count = vdpModeRedrawHandler.updateCanvas(blocks, vdpChanges.fullRedraw);
			
			if (spriteRedrawHandler != null) {
				spriteRedrawHandler.updateCanvas(vdpChanges.fullRedraw);
			}

			vdpCanvas.markDirty(blocks, count);
			
			Arrays.fill(vdpChanges.screen, 0, vdpChanges.screen.length, (byte) 0);
			Arrays.fill(vdpChanges.patt, 0, vdpChanges.patt.length, (byte) 0);
			Arrays.fill(vdpChanges.sprpat, 0, vdpChanges.sprpat.length, (byte) 0);
			Arrays.fill(vdpChanges.color, 0, vdpChanges.color.length, (byte) 0);
			vdpChanges.sprite = 0;
			
			vdpChanges.fullRedraw = false;
		}
		
		vdpchanged = false;
		
	}

	public VdpCanvas getCanvas() {
		return vdpCanvas;
	}
	
	public ByteMemoryAccess getByteReadMemoryAccess(int addr) {
		ByteMemoryArea area = (ByteMemoryArea) vdpMemory.getArea(addr);
		return ((ByteMemoryArea) area).getReadMemoryAccess(addr);
	}
}
