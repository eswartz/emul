/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 20, 2006
 *
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;

import v9t9.emulator.handlers.VdpHandler;
import v9t9.engine.memory.MemoryDomain;

/**
 * A video module is expected to work by copying from an offscreen bitmap of the
 * 99/4A screen (maintained in vdp.c), the changed parts of which are announced
 * through callbacks to updatelist. This routine is sent lists of updateblocks,
 * which contain pointers to the upper-left corners of 8x8 blocks in the bitmap.
 * UPDATE_ROW_STRIDE is the width of a row. The extent of the bitmap is greater
 * than that of the 99/4A video screen for use in clipping sprites.
 * 
 * Never touch the bitmap! It is expected to maintain history between updates.
 * 
 * The bitmap is arranged using one byte per color, range 0 to 16. Bytes with
 * values 1 through 15 correspond to ordinary TI colors. Entry 0 is used for
 * "clear", the background. The video mode you use should allow palette flipping
 * for best performance, since entry 0 represents a color that changes often
 * (the setfgbg callback). Entry 16 is used for the foreground color in text
 * mode.
 * 
 * @author ejs
 */
public class InternalVdp implements VdpHandler {

	private VideoRenderer renderer;

	public InternalVdp(VideoRenderer renderer, MemoryDomain videoMemory, VdpCanvas vdpCanvas) {
		this.renderer = renderer;
		this.vdpMemory = videoMemory;
		this.vdpCanvas = vdpCanvas;
	}

	private RedrawBlock[] blocks;
	private MemoryDomain vdpMemory;

    int vdpmode;
	byte vdpregs[] = new byte[8];
	byte vdpbg;
	byte vdpfg;
	boolean draw_sprites;
	boolean five_sprites_on_a_line;
	int videoupdatespeed;
	final static int VDP_INTERRUPT = 0x80;
	final static int VDP_COINC = 0x40;
	final static int VDP_FIVE_SPRITES = 0x20;
	final static int VDP_FIFTH_SPRITE = 0x1f;
	final static int R0_BITMAP = 2;
	final static int R0_EXTERNAL = 1;
	final static int R1_RAMSIZE = 128;
	final static int R1_NOBLANK = 64;
	final static int R1_INT = 32;
	final static int R1_TEXT = 16;
	final static int R1_MULTI = 8;
	final static int R1_SPR4 = 2;
	final static int R1_SPRMAG = 1;
	final static int MODE_BITMAP = 1;
	final static int MODE_GRAPHICS = 0;
	final static int MODE_TEXT = 2;
	final static int MODE_MULTI = 3;
	boolean vdpchanged;
	final static int REDRAW_NOW = 1		;	/* same-mode change */
    final static int REDRAW_SPRITES = 2	;	/* sprites change */
    final static int REDRAW_MODE = 4		;	/* mode change */
    final static int REDRAW_BLANK = 8		;	/* make blank */
    final static int REDRAW_PALETTE = 16;

	private VdpCanvas vdpCanvas;
	private VdpModeRedrawHandler vdpModeRedrawHandler;
	private SpriteRedrawHandler spriteRedrawHandler;
	private VdpChanges vdpChanges = new VdpChanges();
	private byte vdpStatus;

	final boolean CHANGED(int r,byte val,int v) { return ((vdpregs[r]&(v))!=(val&(v))); }

    /* (non-Javadoc)
     * @see v9t9.handlers.VdpHandler#writeVdpReg(byte, byte, byte)
     */
    public void writeVdpReg(byte reg, byte val) {
    {
    	int         redraw = 0;

    	switch (reg) {
    	case 0:					/* bitmap/video-in */
    		if (CHANGED(0, val, R0_BITMAP+R0_EXTERNAL)) {
    			redraw |= REDRAW_MODE;
    		}
    		vdpregs[0] = val;
    		break;

    	case 1:					/* various modes, sprite stuff */
    		if (CHANGED(1, val, R1_NOBLANK)) {
    			redraw |= REDRAW_BLANK | REDRAW_MODE;
    		}

    		if (CHANGED(1, val, R1_SPRMAG + R1_SPR4)) {
    			redraw |= REDRAW_SPRITES;
    		}

    		if (CHANGED(1, val, R1_TEXT | R1_MULTI)) {
    			redraw |= REDRAW_MODE;
    		}

    		/* if interrupts enabled, and interrupt was pending, trigger it */
    		/*
    		if ((val & R1_INT) != 0 
    		&& 	(vdpregs[1] & R1_INT) == 0 
    		&&	(vdpstatus & VDP_INTERRUPT) != 0) 
    		{
    			trigger9901int(M_INT_VDP);
    		}
    		*/

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
    		vdp_update_mode();
    		vdp_update_params();
    		renderer.resize(vdpCanvas.getWidth(), vdpCanvas.getHeight());
    		vdp_dirty_all();
    	}

    	if ((redraw & REDRAW_SPRITES) != 0) 
    		vdp_dirty_sprites();

    	if ((redraw & REDRAW_PALETTE) != 0) {
    		renderer.setForegroundAndBackground(vdpbg, vdpfg);
    		vdpCanvas.setClearColor(vdpbg);
			// if screen is blank, force something to change
			if ((vdpregs[1] & R1_NOBLANK) == 0)
				redraw |= REDRAW_BLANK;
			vdpCanvas.clear();
			update();
    	}

    	if ((redraw & REDRAW_BLANK) != 0) {
    		if ((vdpregs[1] & R1_NOBLANK) == 0) {
    			renderer.setBlank(true);
    			update();
    		} else {
    			vdp_update_params();
    			renderer.setBlank(false);
    			update();
    		}
    	}
    }
    }


    private void vdp_update_mode() {
    	if ((vdpregs[0] & R0_BITMAP) != 0)
    		vdpmode = MODE_BITMAP;
    	else if ((vdpregs[1] & R1_TEXT) != 0)
    		vdpmode = MODE_TEXT;
    	else if ((vdpregs[1] & R1_MULTI) != 0)
    		vdpmode = MODE_MULTI;
    	else
    		vdpmode = MODE_GRAPHICS;
    	
    	// preinitialize the update blocks with the sizes for this mode
    	int w = 8;
    	int h = 8;
    	if (vdpmode == MODE_TEXT) {
    		w = 6;
    	}
		if (blocks == null) {
			blocks = new RedrawBlock[1024];
			for (int i = 0; i < blocks.length; i++) {
				blocks[i] = new RedrawBlock();
			}
		}
		for (int i = 0; i < blocks.length; i++) {
			blocks[i].w = w;
			blocks[i].h = h;
		}
	}

    /*	This routine updates all the updatefuncs when some
		register controlling the VDP context has changed. */
	private void vdp_update_params() {
		/* Is the screen really blank?  
		   If so, respond to nothing but calls to vdp_dirty_screen */
		if ((vdpregs[1] & R1_NOBLANK) == 0) {
			vdpModeRedrawHandler = new BlankModeRedrawHandler(
					vdpregs, vdpMemory, vdpChanges, vdpCanvas);
			spriteRedrawHandler = null;
			return;
		}

		switch (vdpmode) {
		case MODE_GRAPHICS:
			vdpModeRedrawHandler = new GraphicsModeRedrawHandler(
					vdpregs, vdpMemory, vdpChanges, vdpCanvas);
			spriteRedrawHandler = new SpriteRedrawHandler(
					vdpregs, vdpMemory, vdpChanges, vdpCanvas);

			break;

		case MODE_TEXT:
			vdpModeRedrawHandler = new TextModeRedrawHandler(
					vdpregs, vdpMemory, vdpChanges, vdpCanvas);
			spriteRedrawHandler = null;
			break;

		case MODE_BITMAP:
			vdpModeRedrawHandler = new BitmapModeRedrawHandler(
					vdpregs, vdpMemory, vdpChanges, vdpCanvas);
			spriteRedrawHandler = new SpriteRedrawHandler(
					vdpregs, vdpMemory, vdpChanges, vdpCanvas);

			break;

		case MODE_MULTI:
			vdpModeRedrawHandler = new MulticolorModeRedrawHandler(
					vdpregs, vdpMemory, vdpChanges, vdpCanvas);
			spriteRedrawHandler = new SpriteRedrawHandler(
					vdpregs, vdpMemory, vdpChanges, vdpCanvas);

			break;

		default:
			break;
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
    public void writeVdpMemory(short vdpaddr, byte val) {
    	vdpchanged |= vdpModeRedrawHandler.touch(vdpaddr);
    	if (spriteRedrawHandler != null) {
    		vdpchanged |= spriteRedrawHandler.touch(vdpaddr);
    	}
    	
    	//update();
    }

    final static int SPRBIT(int x) { return (1<<(x)); }
    final boolean VDP_IS_AL_VIDEO() { return ((vdpregs[0] & R0_EXTERNAL)!=0); }

    void
	vdp_redraw_screen(int x, int y, int sx, int sy)
	{
		int ptr;
		int width;

		//printf("vdp_dirty: %d,%d,%d,%d\n", x,y,sx,sy);
		if (sx < 0 || sy < 0) return;

		if ((vdpregs[1] & R1_TEXT) != 0) {
			// left blank column?
			if (x < 8) {
				vdpCanvas.clear();
				if (sx <= x)
					return;
				sx -= x;
				x = 0;
			// right blank column?
			} else if (x >= 240 + 8) {
				vdpCanvas.clear();
				return;
			} else {
				x -= 8;
			}
			width = 40;
		} else {
			width = 32;
		}

		if (x < 0) { sx += x; x = 0; }
		if (y < 0) { sy += y; y = 0; }
		if (x >= vdpCanvas.getWidth() || y >= vdpCanvas.getHeight()) return;

		//printf("2: %d,%d,%d,%d\n", x,y,sx,sy);

		if (width == 40) {
			sx = (sx + (x % 6) + 5) / 6; 
			x /= 6;
		}
		else {
			sx = (sx + (x & 7) + 7) >> 3;
			x >>= 3;
		}
		sy = (sy + (y & 7) + 7) >> 3;
		y >>= 3;

		if (x + sx > width) sx = width - x;
		if (y + sy > 24) sy = 24 - y;

		//printf("3: %d,%d,%d,%d\n", x,y,sx,sy);

		ptr = (y * width) + x;
		while (sy-- != 0) {
			Arrays.fill(vdpChanges.screen, ptr, ptr + sx, (byte)1);
			ptr += width;
		}
		vdpchanged = true;
	}


	void
	vdp_dirty_sprites()
	{
		if (!draw_sprites)
			return;
		vdpChanges.sprite = -1;
		Arrays.fill(vdpChanges.sprpat, 0, vdpChanges.sprpat.length, (byte)1);
		vdpchanged = true;
	}


	void
	vdp_dirty_all()
	{
		Arrays.fill(vdpChanges.screen, 0, vdpChanges.screen.length, (byte)VdpChanges.SC_BACKGROUND);
		Arrays.fill(vdpChanges.patt, 0, vdpChanges.patt.length, (byte) 1);
		vdp_dirty_sprites();
		vdpchanged = true;
		vdpCanvas.clear();
	}
	

	/*	Force a complete redraw of the display
		by making it look like the whole VDP context
		has changed. */
	void
	vdpcompleteredraw()
	{
		int          i;

		vdp_dirty_all();
		for (i = 0; i < 8; i++) {
			writeVdpReg((byte) i, vdpregs[i]);
		}
		update();
	}




	public void
	update()
	{
		if (!vdpchanged)
			return;
		
		if (vdpModeRedrawHandler != null) {
			vdpModeRedrawHandler.propagateTouches();
			
			if (spriteRedrawHandler != null) {
				vdpStatus = spriteRedrawHandler.updateSpriteCoverage(vdpStatus);
			}
			//updateSprites();
			
			int count = vdpModeRedrawHandler.updateCanvas(blocks);
			
			if (spriteRedrawHandler != null) {
				spriteRedrawHandler.updateCanvas();
			}

			//redrawSprites();
			
			renderer.updateList(blocks, count);
			
			Arrays.fill(vdpChanges.screen, 0, vdpChanges.screen.length, (byte) 0);
			Arrays.fill(vdpChanges.patt, 0, vdpChanges.patt.length, (byte) 0);
			Arrays.fill(vdpChanges.sprpat, 0, vdpChanges.sprpat.length, (byte) 0);
			Arrays.fill(vdpChanges.color, 0, vdpChanges.color.length, (byte) 0);
			vdpChanges.sprite = 0;
		}
		
		vdpchanged = false;
		
	}

}
