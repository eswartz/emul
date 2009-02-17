/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 20, 2006
 *
 */
package v9t9.emulator.clients.builtin.video.tms9918a;

import java.io.PrintWriter;
import java.util.Arrays;

import org.eclipse.jface.dialogs.IDialogSettings;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.video.BlankModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.MemoryCanvas;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpModeRedrawHandler;
import v9t9.emulator.hardware.InternalCru9901;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;
import v9t9.emulator.runtime.Cpu;
import v9t9.emulator.runtime.Logging;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;
import v9t9.utils.Utils;

/**
 * This is the 99/4A VDP chip.
 * <p>
 * Mode bits:
 * <p>
 * R0:	M3 @ 1
 * R1:	M1 @ 4, M2 @ 3 
 * <p>
 * <pre>
 *                   M1  M2  M3  
 * Text 1 mode:      1   0   0   = 1
 * Multicolor:       0   1   0   = 2
 * Graphics 1 mode:  0   0   0   = 0
 * Graphics 2 mode:  0   0   1   = 4
 * </pre>
 * @author ejs
 */
public class VdpTMS9918A implements VdpHandler {
	private RedrawBlock[] blocks;
	protected MemoryDomain vdpMemory;

	protected byte vdpregs[];
	protected byte vdpbg;
	protected byte vdpfg;
	protected boolean drawSprites = true;
	//private final static int REDRAW_NOW = 1		;	/* same-mode change */
	protected final static int REDRAW_SPRITES = 2	;	/* sprites change */
	protected final static int REDRAW_MODE = 4		;	/* mode change */
	protected final static int REDRAW_BLANK = 8		;	/* make blank */
	protected final static int REDRAW_PALETTE = 16;
	protected boolean vdpchanged;

	protected VdpCanvas vdpCanvas;
	protected VdpModeRedrawHandler vdpModeRedrawHandler;
	protected SpriteRedrawHandler spriteRedrawHandler;
	protected VdpChanges vdpChanges = new VdpChanges(getMaxRedrawblocks());
	protected byte vdpStatus;
	
	protected VdpMmio vdpMmio;
	protected PrintWriter vdplog;
	protected BlankModeRedrawHandler blankModeRedrawHandler;
	protected VdpModeInfo vdpModeInfo;
	protected int modeNumber;
	public final static int VDP_INTERRUPT = 0x80;
	public final static int VDP_COINC = 0x40;
	public final static int VDP_FIVE_SPRITES = 0x20;
	public final static int VDP_FIFTH_SPRITE = 0x1f;
	
	final public static int R0_M3 = 0x2; // bitmap
	public final static int R0_EXTERNAL = 1;
	public final static int R1_RAMSIZE = 128;
	public final static int R1_NOBLANK = 64;
	public final static int R1_INT = 32;
	final public static int R1_M1 = 0x10; // text
	final public static int R1_M2 = 0x8; // multi
	
	public final static int R1_SPR4 = 2;
	public final static int R1_SPRMAG = 1;

	public final static int MODE_TEXT = 1;
	public final static int MODE_GRAPHICS = 0;
	public final static int MODE_BITMAP = 4;
	public final static int MODE_MULTI = 2;
	
    static public final String sDumpVdpAccess = "DumpVdpAccess";
    static public final Setting settingDumpVdpAccess = new Setting(sDumpVdpAccess, new Boolean(false));
    static public final String sVdpInterruptRate = "VdpInterruptRate";
    static public final Setting settingVdpInterruptRate = new Setting(sVdpInterruptRate, new Integer(60));

    // this should pretty much stay on
    static public final Setting settingCpuSynchedVdpInterrupt = new Setting("CpuSynchedVdpInterrupt",
    		new Boolean(true));

	/** The circular counter for VDP interrupt timing. */
	private int vdpInterruptFrac;
	/** The number of CPU cycles corresponding to 1/60 second */
	private int vdpInterruptLimit;
	private int throttleCount;

	public VdpTMS9918A(MemoryDomain videoMemory) {
		settingVdpInterruptRate.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				recalcInterruptTiming();
			}
			
		});
		
		Cpu.settingCyclesPerSecond.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				recalcInterruptTiming();
			}
			
		});
		
		recalcInterruptTiming();
		
		// interleave with CPU log
		Logging.registerLog(settingDumpVdpAccess, "instrs_full.txt");
		
		settingDumpVdpAccess.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				if (setting.getBoolean())
					vdplog = Logging.getLog(setting);
				else
					vdplog = null;
			}
			
		});
		
		Cpu.settingRealTime.addListener(new ISettingListener() {
			public void changed(Setting setting, Object oldValue) {
				VdpTMS9918A.settingCpuSynchedVdpInterrupt.setBoolean(setting.getBoolean());				
			}
		});
		
		this.vdpMemory = videoMemory;
		this.vdpCanvas = new MemoryCanvas();
		this.vdpregs = allocVdpRegs();
		vdpCanvas.setSize(256, 192);
		
		blankModeRedrawHandler = new BlankModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas, createBlankModeInfo());
	}
	
	protected void recalcInterruptTiming() {
        vdpInterruptLimit = Cpu.settingCyclesPerSecond.getInt() / settingVdpInterruptRate.getInt();
        vdpInterruptFrac = 0;
        System.out.println("VDP interrupt target: " + vdpInterruptLimit);		
	}

	protected void log(String msg) {
		if (vdplog != null)
			vdplog.println("[VDP] " + msg);
	}
	
	public MemoryDomain getVideoMemory() {
		return vdpMemory;
	}
	 
	public void setVdpMmio(VdpMmio vdpMmio) {
		this.vdpMmio = vdpMmio;
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
	
	protected final boolean CHANGED(byte old,byte val, int v) { return (old&(v))!=(val&(v)); }

    /* (non-Javadoc)
     * @see v9t9.handlers.VdpHandler#writeVdpReg(byte, byte, byte)
     */
    final synchronized public void writeVdpReg(int reg, byte val) {
    	if (reg >= vdpregs.length)
    		return;
    	
    	byte old = vdpregs[reg];
    	vdpregs[reg] = val;
    	
   		log("register " + reg + " " + Utils.toHex2(old) + " -> " + Utils.toHex2(val));
    	
    	int         redraw = doWriteVdpReg(reg, old, val);

    	//synchronized (vdpCanvas) {
			
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
		 		if ((vdpregs[1] & VdpTMS9918A.R1_NOBLANK) == 0) {
		 			vdpCanvas.setBlank(true);
		 			dirtyAll();
		 			//update();
		 		} else {
		 			vdpCanvas.setBlank(false);
		 			dirtyAll();
		 			//update();
		 		}
		 	}
		//}

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
    		if (CHANGED(old, val, VdpTMS9918A.R0_M3+VdpTMS9918A.R0_EXTERNAL)) {
    			redraw |= REDRAW_MODE;
    		}
    		break;

    	case 1:					/* various modes, sprite stuff */
    		if (CHANGED(old, val, VdpTMS9918A.R1_NOBLANK)) {
    			redraw |= REDRAW_BLANK | REDRAW_MODE;
    		}

    		if (CHANGED(old, val, VdpTMS9918A.R1_SPRMAG + VdpTMS9918A.R1_SPR4)) {
    			redraw |= REDRAW_SPRITES;
    		}

    		if (CHANGED(old, val, VdpTMS9918A.R1_M1 | VdpTMS9918A.R1_M2)) {
    			redraw |= REDRAW_MODE;
    		}

    		/* if interrupts enabled, and interrupt was pending, trigger it */
    		if ((val & VdpTMS9918A.R1_INT) != 0 
    		&& 	(old & VdpTMS9918A.R1_INT) == 0 
    		&&	(vdpStatus & VdpTMS9918A.VDP_INTERRUPT) != 0) 
    		{
    			//trigger9901int( M_INT_VDP);	// TODO
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
    	return (vdpregs[1] & VdpTMS9918A.R1_NOBLANK) == 0;
    }
    
    public int calculateModeNumber() {
		int reg0 = vdpregs[0] & R0_M3;
		int reg1 = vdpregs[1] & R1_M1 + R1_M2;
    	
    	if (reg0 == R0_M3) {
    		// can support multi+bitmap or text+bitmap modes too... but not now
    		return MODE_BITMAP;
    	}
    	if (reg1 == R1_M2)
    		return MODE_MULTI;
    	if (reg1 == R1_M1)
    		return MODE_TEXT;
    	return MODE_GRAPHICS;
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
    	modeNumber = calculateModeNumber();
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

    /**
     * Get the address a table will take given the mode and memory size
     * @return
     */
    protected int getModeAddressMask() {
    	return vdpMmio.getMemorySize() - 1;
    }
    
    protected VdpModeInfo createSpriteModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		int ramsize = getModeAddressMask();

		vdpModeInfo.sprite.base = getSpriteTableBase() & ramsize;
		vdpModeInfo.sprite.size = 128;
		vdpModeInfo.sprpat.base = (vdpregs[6] * 0x800) & ramsize;
		vdpModeInfo.sprpat.size = 2048;
		return vdpModeInfo;
	}

	protected int getSpriteTableBase() {
		return (vdpregs[5] * 0x80) & getModeAddressMask();
	}

	protected void setGraphicsMode() {
		vdpCanvas.setSize(256, 192);
		vdpModeInfo = createGraphicsModeInfo();
		vdpModeRedrawHandler = new GraphicsModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas, vdpModeInfo);
		spriteRedrawHandler = createSpriteRedrawHandler();
		vdpMmio.setMemoryAccessCycles(8);
		initUpdateBlocks(8);
	}

	protected SpriteRedrawHandler createSpriteRedrawHandler() {
		return new SpriteRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas, createSpriteModeInfo());
	}

	
	protected VdpModeInfo createGraphicsModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		int ramsize = getModeAddressMask();
		vdpModeInfo.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdpModeInfo.screen.size = 768;
		vdpModeInfo.color.base = getColorTableBase();
		vdpModeInfo.color.size = 32;
		vdpModeInfo.patt.base = getPatternTableBase();
		vdpModeInfo.patt.size = 2048;
		vdpModeInfo.sprite.base = getSpriteTableBase();
		vdpModeInfo.sprite.size = 128;
		vdpModeInfo.sprpat.base = (vdpregs[6] * 0x800) & ramsize;
		vdpModeInfo.sprpat.size = 2048;
		return vdpModeInfo;
	}

	protected int getPatternTableBase() {
		return ((vdpregs[4] & 0xff) * 0x800) & getModeAddressMask();
	}

	protected int getColorTableBase() {
		return ((vdpregs[3] & 0xff) * 0x40) & getModeAddressMask();
	}

	protected void setMultiMode() {
		vdpCanvas.setSize(256, 192);
		vdpModeInfo = createMultiModeInfo();
		vdpModeRedrawHandler = new MulticolorModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas, vdpModeInfo);
		spriteRedrawHandler = createSpriteRedrawHandler();
		vdpMmio.setMemoryAccessCycles(2);
		initUpdateBlocks(8);
	}

	protected VdpModeInfo createMultiModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		int ramsize = getModeAddressMask();
		
		vdpModeInfo.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdpModeInfo.screen.size = 768;
		vdpModeInfo.color.base = 0;
		vdpModeInfo.color.size = 0;
		vdpModeInfo.patt.base = getPatternTableBase();
		vdpModeInfo.patt.size = 1536;
		vdpModeInfo.sprite.base = getSpriteTableBase();
		vdpModeInfo.sprite.size = 128;
		
		return vdpModeInfo;
	}

	protected void setTextMode() {
		vdpCanvas.setSize(256, 192);
		vdpModeInfo = createTextModeInfo();
		vdpModeRedrawHandler = new TextModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas, vdpModeInfo);
		spriteRedrawHandler = null;
		vdpMmio.setMemoryAccessCycles(1);
		initUpdateBlocks(6);
	}

	protected VdpModeInfo createTextModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		int ramsize = getModeAddressMask();
		
		vdpModeInfo.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdpModeInfo.screen.size = 960;
		vdpModeInfo.color.base = 0;
		vdpModeInfo.color.size = 0;
		vdpModeInfo.patt.base = getPatternTableBase();
		vdpModeInfo.patt.size = 2048;
		vdpModeInfo.sprite.base = 0;
		vdpModeInfo.sprite.size = 0;
		vdpModeInfo.sprpat.base = 0;
		vdpModeInfo.sprpat.size = 0;
		return vdpModeInfo;
	}

	protected void setBitmapMode() {
		vdpCanvas.setSize(256, 192);
		vdpModeInfo = createBitmapModeInfo();
		vdpModeRedrawHandler = new BitmapModeRedrawHandler(
				vdpregs, this, vdpChanges, vdpCanvas, vdpModeInfo);
		spriteRedrawHandler = createSpriteRedrawHandler();
		vdpMmio.setMemoryAccessCycles(8);
		initUpdateBlocks(8);
	}

	protected VdpModeInfo createBitmapModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		int ramsize = getModeAddressMask();

		vdpModeInfo.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdpModeInfo.screen.size = 768;
		vdpModeInfo.sprite.base = getSpriteTableBase();
		vdpModeInfo.sprite.size = 128;
		vdpModeInfo.sprpat.base = (vdpregs[6] * 0x800) & ramsize;
		vdpModeInfo.sprpat.size = 2048;

		vdpModeInfo.color.base = getColorTableBase() & ~0x1fff;
		vdpModeInfo.color.size = 6144;
		
		vdpModeInfo.patt.base = getPatternTableBase() & ~0x1fff;
		vdpModeInfo.patt.size = 6144;
		
		return vdpModeInfo;
	}

	protected void setBlankMode() {
		vdpCanvas.setSize(256, vdpCanvas.getHeight());
		spriteRedrawHandler = null;
		vdpMmio.setMemoryAccessCycles(0);
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

	/* (non-Javadoc)
     * @see v9t9.handlers.VdpHandler#readVdpStatus()
     */
    public byte readVdpStatus() {
		/* >8802, status read */
    	byte ret = vdpStatus;
		vdpStatus &= ~0x80;
		// TODO machine.getCpu().reset9901int(v9t9.cpu.Cpu.M_INT_VDP);

        return ret;
    }

    /* (non-Javadoc)
     * @see v9t9.handlers.VdpHandler#writeVdpMemory(short, byte)
     */
    public synchronized void touchAbsoluteVdpMemory(int vdpaddr, byte val) {
    	try {
    		vdpMemory.writeMemory(vdpaddr & 0x3fff);
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
    
    public byte readAbsoluteVdpMemory(int vdpaddr) {
    	return vdpMmio.readFlatMemory(vdpaddr);
    }
    
    public void writeAbsoluteVdpMemory(int vdpaddr, byte byt) {
    	vdpMmio.writeFlatMemory(vdpaddr, byt);
    }

	public ByteMemoryAccess getByteReadMemoryAccess(int addr) {
		return vdpMmio.getByteReadMemoryAccess(addr);
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
				vdpModeRedrawHandler.propagateTouches();
				
				if (vdpChanges.fullRedraw) {
					// clear for the actual mode (not blank mode)
					vdpModeRedrawHandler.clear();
					vdpCanvas.markDirty();
				}
				
				if (!isBlank()) {
					if (spriteRedrawHandler != null) {
						vdpStatus = spriteRedrawHandler.updateSpriteCoverage(vdpStatus, vdpChanges.fullRedraw);
					}
					count = vdpModeRedrawHandler.updateCanvas(blocks, vdpChanges.fullRedraw);
					if (spriteRedrawHandler != null && drawSprites) {
						spriteRedrawHandler.updateCanvas(vdpChanges.fullRedraw);
					}
				}
			}

			vdpCanvas.markDirty(blocks, count);
			
			Arrays.fill(vdpChanges.screen, 0, vdpChanges.screen.length, (byte) 0);
			Arrays.fill(vdpChanges.patt, 0, vdpChanges.patt.length, (byte) 0);
			Arrays.fill(vdpChanges.sprpat, 0, vdpChanges.sprpat.length, (byte) 0);
			Arrays.fill(vdpChanges.color, 0, vdpChanges.color.length, (byte) 0);
			vdpChanges.sprite = 0;
			
			vdpChanges.fullRedraw = false;
			
			//System.out.println("elapsed: " + (System.currentTimeMillis() - start));
		}
		
		vdpchanged = false;
		return true;
	}

	public synchronized VdpCanvas getCanvas() {
		return vdpCanvas;
	}

	public void tick() {
		vdpStatus |= 0x80;
	}
	
	public boolean isThrottled() {
		return true;
	}
	
	public void work() {
		
	}

	public void setCanvas(VdpCanvas canvas) {
		this.vdpCanvas = canvas;
		canvas.markDirty();
	}
	
	protected int getVideoHeight() {
		return 192;
	}
	
	public int getModeNumber() {
		return modeNumber;
	}

	public void saveState(IDialogSettings section) {
		String[] regState = new String[vdpregs.length];
		for (int i = 0; i < vdpregs.length; i++) {
			regState[i] = Utils.toHex2(vdpregs[i]);
		}
		section.put("Registers", regState);
		settingDumpVdpAccess.saveState(section);
		settingCpuSynchedVdpInterrupt.saveState(section);
		settingVdpInterruptRate.saveState(section);
	}
	
	public void loadState(IDialogSettings section) {
		if (section == null) return;
		
		String[] regState = section.getArray("Registers");
		if (regState != null) {
			for (int i = 0; i < regState.length; i++) {
				writeVdpReg(i, (byte) Integer.parseInt(regState[i], 16));
			}
		}
		
		settingDumpVdpAccess.loadState(section);
		settingCpuSynchedVdpInterrupt.loadState(section);
		settingVdpInterruptRate.loadState(section);
	}
	
	
	public void syncVdpInterrupt(Machine machine) {
		if (!settingCpuSynchedVdpInterrupt.getBoolean())
			return;
		
		// In Win32, the timer is not nearly as accurate as 1/100 second,
		// so we get a lot of interrupts at the same time.
		
		// Synchronize VDP interrupts along with the CPU in the same task
		// so we don't succumb to misscheduling between different timers
		// OR timer tasks.
		//System.out.print("[VDP delt:" + vdpInterruptFrac + "]");
		if (vdpInterruptFrac >= vdpInterruptLimit) {
	
			vdpInterruptFrac -= vdpInterruptLimit;
			
			if (machine.getExecutor().nVdpInterrupts < settingVdpInterruptRate.getInt()) {
	    		if (Machine.settingThrottleInterrupts.getBoolean()) {
	    			if (throttleCount-- < 0) {
	    				throttleCount = 60;
	    			} else {
	    				return;
	    			}
	    		}
	    		
	    		tick();
	    		
	    		machine.getCpu().getCruAccess().triggerInterrupt(InternalCru9901.INT_VDP);
	    		machine.getExecutor().nVdpInterrupts++;
	    		//System.out.print('!');
			}
		}
	}
	
	public void addCpuCycles(int cycles) {
		vdpInterruptFrac += cycles;
	}
}
