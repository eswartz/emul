/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 20, 2006
 *
 */
package v9t9.emulator.clients.builtin.video.tms9918a;

import java.util.Arrays;

import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.settings.ISettingSection;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.clients.builtin.video.BlankModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.ImageDataCanvasPaletted;
import v9t9.emulator.clients.builtin.video.RedrawBlock;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpChanges;
import v9t9.emulator.clients.builtin.video.VdpModeInfo;
import v9t9.emulator.clients.builtin.video.VdpModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.VdpRedrawInfo;
import v9t9.emulator.common.IMachine;
import v9t9.emulator.common.WorkspaceSettings;
import v9t9.emulator.hardware.BaseCruAccess;
import v9t9.emulator.hardware.CruAccess;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;
import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.engine.memory.MemoryDomain;

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
	protected final VdpChanges vdpChanges = new VdpChanges(getMaxRedrawblocks());
	protected byte vdpStatus;
	
	protected VdpMmio vdpMmio;
	protected BlankModeRedrawHandler blankModeRedrawHandler;
	protected VdpModeInfo vdpModeInfo;
	protected VdpRedrawInfo vdpRedrawInfo;
	
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
	
    static public final SettingProperty settingDumpVdpAccess = new SettingProperty("DumpVdpAccess", new Boolean(false));
    static public final SettingProperty settingVdpInterruptRate = new SettingProperty("VdpInterruptRate", new Integer(60));

    // this should pretty much stay on
    static public final SettingProperty settingCpuSynchedVdpInterrupt = new SettingProperty("CpuSynchedVdpInterrupt",
    		new Boolean(true));
    
    
    public static final int REG_ST = 0;
	private static final int REG_COUNT = 8 + 1;

	/** The circular counter for VDP interrupt timing. */
	private int vdpInterruptFrac;
	/** The number of CPU cycles corresponding to 1/60 second */
	private int vdpInterruptLimit;
	private int vdpInterruptDelta;

	private int throttleCount;
	private final IMachine machine;
	private int fixedTimeVdpInterruptDelta;

	public VdpTMS9918A(IMachine machine) {
		this.machine = machine;
		
		vdpStatus = (byte) VDP_INTERRUPT;
		
		settingVdpInterruptRate.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				recalcInterruptTiming();
			}
			
		});
		
		WorkspaceSettings.CURRENT.register(settingVdpInterruptRate);
		
		Cpu.settingCyclesPerSecond.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				recalcInterruptTiming();
			}
			
		});
		
		recalcInterruptTiming();
		
		Cpu.settingRealTime.addListener(new IPropertyListener() {
			public void propertyChanged(IProperty setting) {
				VdpTMS9918A.settingCpuSynchedVdpInterrupt.setBoolean(setting.getBoolean());				
			}
		});
		
		this.vdpMemory = machine.getMemory().getDomain(MemoryDomain.NAME_VIDEO);
		this.vdpCanvas = new ImageDataCanvasPaletted();
		this.vdpregs = allocVdpRegs();
		vdpCanvas.setSize(256, 192);
		
		vdpRedrawInfo = new VdpRedrawInfo(vdpregs, this, vdpChanges, vdpCanvas);
		blankModeRedrawHandler = new BlankModeRedrawHandler(vdpRedrawInfo, createBlankModeInfo());
		
		resetPalette();
	}

	public void resetPalette() {
		// nothing
	}

	protected void recalcInterruptTiming() {
        vdpInterruptLimit = Cpu.settingCyclesPerSecond.getInt() / settingVdpInterruptRate.getInt();
        vdpInterruptFrac = 0;
        fixedTimeVdpInterruptDelta = (int) ((long) settingVdpInterruptRate.getInt() * 65536 / machine.getCpuTicksPerSec());
        //System.out.println("VDP interrupt target: " + Cpu.settingCyclesPerSecond.getInt() + " /  " + settingVdpInterruptRate.getInt() + " = " + vdpInterruptLimit);		
	}

	public static void log(String msg) {
		if (settingDumpVdpAccess.getBoolean() && Cpu.settingDumpFullInstructions.getBoolean())
			Executor.getDumpfull().println("[VDP] " + msg);
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
    	
    	if (Cpu.settingDumpFullInstructions.getBoolean() && settingDumpVdpAccess.getBoolean())
    		log("register " + reg + " " + HexUtils.toHex2(old) + " -> " + HexUtils.toHex2(val));
    	
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
		vdpModeInfo.sprpat.base = getSpritePatternTableBase(ramsize);
		vdpModeInfo.sprpat.size = 2048;
		return vdpModeInfo;
	}

	protected int getSpriteTableBase() {
		return (vdpregs[5] * 0x80) & getModeAddressMask();
	}

	protected void setGraphicsMode() {
		vdpCanvas.setFormat(VdpCanvas.Format.COLOR16_8x8);
		vdpCanvas.setSize(256, 192);
		vdpModeInfo = createGraphicsModeInfo();
		vdpModeRedrawHandler = new GraphicsModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = createSpriteRedrawHandler();
		vdpMmio.setMemoryAccessCycles(8);
		initUpdateBlocks(8);
	}

	protected SpriteRedrawHandler createSpriteRedrawHandler() {
		return new SpriteRedrawHandler(vdpRedrawInfo, createSpriteModeInfo());
	}

	
	protected VdpModeInfo createGraphicsModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		int ramsize = getModeAddressMask();
		vdpModeInfo.screen.base = getScreenTableBase(ramsize);
		vdpModeInfo.screen.size = 768;
		vdpModeInfo.color.base = getColorTableBase();
		vdpModeInfo.color.size = 32;
		vdpModeInfo.patt.base = getPatternTableBase();
		vdpModeInfo.patt.size = 2048;
		vdpModeInfo.sprite.base = getSpriteTableBase();
		vdpModeInfo.sprite.size = 128;
		vdpModeInfo.sprpat.base = getSpritePatternTableBase(ramsize);
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
		vdpCanvas.setFormat(VdpCanvas.Format.COLOR16_4x4);
		vdpCanvas.setSize(256, 192);
		vdpModeInfo = createMultiModeInfo();
		vdpModeRedrawHandler = new MulticolorModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = createSpriteRedrawHandler();
		vdpMmio.setMemoryAccessCycles(2);
		initUpdateBlocks(8);
	}

	protected VdpModeInfo createMultiModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		int ramsize = getModeAddressMask();
		
		vdpModeInfo.screen.base = getScreenTableBase(ramsize);
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
		vdpCanvas.setFormat(VdpCanvas.Format.TEXT);
		vdpCanvas.setSize(256, 192);
		vdpModeInfo = createTextModeInfo();
		vdpModeRedrawHandler = new TextModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = null;
		vdpMmio.setMemoryAccessCycles(1);
		initUpdateBlocks(6);
	}

	protected VdpModeInfo createTextModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		int ramsize = getModeAddressMask();
		
		vdpModeInfo.screen.base = getScreenTableBase(ramsize);
		vdpModeInfo.screen.size = 960;
		vdpModeInfo.color.base = getColorTableBase();
		vdpModeInfo.color.size = 0;
		vdpModeInfo.patt.base = getPatternTableBase();
		vdpModeInfo.patt.size = 2048;
		vdpModeInfo.sprite.base = getSpriteTableBase();
		vdpModeInfo.sprite.size = 0;
		vdpModeInfo.sprpat.base = getSpritePatternTableBase(ramsize);
		vdpModeInfo.sprpat.size = 0;
		return vdpModeInfo;
	}

	protected void setBitmapMode() {
		vdpCanvas.setFormat(VdpCanvas.Format.COLOR16_8x1);
		vdpCanvas.setSize(256, 192);
		vdpModeInfo = createBitmapModeInfo();
		vdpModeRedrawHandler = new BitmapModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
		spriteRedrawHandler = createSpriteRedrawHandler();
		vdpMmio.setMemoryAccessCycles(8);
		initUpdateBlocks(8);
	}

	protected VdpModeInfo createBitmapModeInfo() {
		VdpModeInfo vdpModeInfo = new VdpModeInfo(); 
		int ramsize = getModeAddressMask();

		vdpModeInfo.screen.base = getScreenTableBase(ramsize);
		vdpModeInfo.screen.size = 768;
		vdpModeInfo.sprite.base = getSpriteTableBase();
		vdpModeInfo.sprite.size = 128;
		vdpModeInfo.sprpat.base = getSpritePatternTableBase(ramsize);
		vdpModeInfo.sprpat.size = 2048;

		vdpModeInfo.color.base = (vdpregs[3] & 0x80) * 0x40;
		vdpModeInfo.color.size = 6144;
		
		vdpModeInfo.patt.base = (vdpregs[4] & 0x04) * 0x800;
		vdpModeInfo.patt.size = 6144;
		
		return vdpModeInfo;
	}

	private int getScreenTableBase(int ramsize) {
		return (vdpregs[2] * 0x400) & ramsize;
	}

	private int getSpritePatternTableBase(int ramsize) {
		return (vdpregs[6] * 0x800) & ramsize;
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
		/* >8802, status read and acknowledge interrupt */
    	byte ret = vdpStatus;
		vdpStatus &= ~VDP_INTERRUPT;
		// TODO machine.getCpu().reset9901int(v9t9.cpu.Cpu.M_INT_VDP);

        return ret;
    }

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
				vdpCanvas.syncColors();
				
				vdpModeRedrawHandler.propagateTouches();
				
				if (vdpChanges.fullRedraw) {
					// clear for the actual mode (not blank mode)
					vdpModeRedrawHandler.clear();
					vdpCanvas.markDirty();
				}
				
				if (!isBlank()) {
					if (spriteRedrawHandler != null && drawSprites) {
						vdpStatus = spriteRedrawHandler.updateSpriteCoverage(vdpStatus, vdpChanges.fullRedraw);
					}
					count = vdpModeRedrawHandler.updateCanvas(blocks, vdpChanges.fullRedraw);
					if (spriteRedrawHandler != null && drawSprites) {
						spriteRedrawHandler.updateCanvas(vdpChanges.fullRedraw);
					}
				}
			}

			vdpCanvas.markDirty(blocks, count);
			
			Arrays.fill(vdpChanges.screen, (byte) 0);
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

	public synchronized VdpCanvas getCanvas() {
		return vdpCanvas;
	}

	public void tick() {
		 if (VdpTMS9918A.settingCpuSynchedVdpInterrupt.getBoolean())
			 return;
		 
		 // in this model, we use the system clock to ensure reliable VDP
		 // interrupts, because the CPU speed is unbounded and unreliable.
		 
		if (machine.isExecuting()) {
			vdpInterruptDelta += fixedTimeVdpInterruptDelta;
			//System.out.print("[VDP delt:" + vdpInterruptDelta + "]");
			if (vdpInterruptDelta >= 65536) {
		
				vdpInterruptDelta -= 65536;
				
				doTick();
			}
		}
		
	}
	

	public void syncVdpInterrupt(IMachine machine) {
		if (!settingCpuSynchedVdpInterrupt.getBoolean())
			return;

		// in this model, the CPU is running at a fixed rate,
		// so we can trigger VDP interrupts in lockstep
		// with the CPU.
		
		if (vdpInterruptFrac >= vdpInterruptLimit) {
			vdpInterruptFrac -= vdpInterruptLimit;
			
			doTick();
		}
	}
	/**
	 * 
	 */
	protected void doTick() {
		if (true || machine.getExecutor().nVdpInterrupts < settingVdpInterruptRate.getInt()) {
    		if (IMachine.settingThrottleInterrupts.getBoolean()) {
    			if (throttleCount-- < 0) {
    				throttleCount = 6;
    			} else {
    				return;
    			}
    		}
    		
    		// a real interrupt only occurs if wanted
    		if ((readVdpReg(1) & VdpTMS9918A.R1_INT) != 0) {
    			if ((vdpStatus & VDP_INTERRUPT) == 0) {
    				vdpStatus |= VDP_INTERRUPT;
    				machine.getExecutor().nVdpInterrupts++;
    			}
    			
    			CruAccess cru = machine.getCpu().getCruAccess();
				if (cru instanceof BaseCruAccess)
    				cru.triggerInterrupt(((BaseCruAccess) cru).intVdp);
				
				//machine.getCpu().setIdle(false);
    		}
		}
		//System.out.print('!');
		
	}

	public boolean isThrottled() {
		return true;
	}
	
	public void work() {
		
	}

	public void setCanvas(VdpCanvas canvas) {
		this.vdpCanvas = canvas;
		canvas.markDirty();
		
		vdpRedrawInfo = new VdpRedrawInfo(vdpregs, this, vdpChanges, vdpCanvas);
		blankModeRedrawHandler = new BlankModeRedrawHandler(vdpRedrawInfo, createBlankModeInfo());

	}
	
	protected int getVideoHeight() {
		return 192;
	}
	
	public int getModeNumber() {
		return modeNumber;
	}

	public void saveState(ISettingSection section) {
		String[] regState = new String[vdpregs.length];
		for (int i = 0; i < vdpregs.length; i++) {
			regState[i] = HexUtils.toHex2(vdpregs[i]);
		}
		section.put("Registers", regState);
		//settingDumpVdpAccess.saveState(section);
		settingCpuSynchedVdpInterrupt.saveState(section);
		settingVdpInterruptRate.saveState(section);
	}
	
	public void loadState(ISettingSection section) {
		if (section == null) return;
		
		String[] regState = section.getArray("Registers");
		if (regState != null) {
			for (int i = 0; i < regState.length; i++) {
				byte val = (byte) Integer.parseInt(regState[i], 16);
				//vdpregs[i] = val;
				loadVdpReg(i, val);
			}
		}
		
		//settingDumpVdpAccess.loadState(section);
		settingCpuSynchedVdpInterrupt.loadState(section);
		settingVdpInterruptRate.loadState(section);
	}
	
	
	/**
	 * @param i
	 * @param val
	 */
	protected void loadVdpReg(int num, byte val) {
		writeVdpReg(num, val);
	}

	
	public void addCpuCycles(int cycles) {
		vdpInterruptFrac += cycles;
	}
	
	/**
	 * @return the vdpModeRedrawHandler
	 */
	public VdpModeRedrawHandler getVdpModeRedrawHandler() {
		return vdpModeRedrawHandler;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return REG_COUNT;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#getRegister(int)
	 */
	@Override
	public byte getRegister(int reg) {
		return reg == REG_ST ? vdpStatus : vdpregs[reg - 1] ;
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#getRegisterName(int)
	 */
	@Override
	public String getRegisterName(int reg) {
		return reg == REG_ST ? "ST" : "VR" + (reg - 1);
	}
	
	protected String yOrN(String label, int i) {
		return i != 0 ? label : "";
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#getRegisterTooltip(int)
	 */
	@Override
	public String getRegisterTooltip(int reg) {
		switch (reg) {
		case REG_ST:
			return "Status: " + getStatusString(vdpStatus);
		}
		reg--;
		byte val = vdpregs[reg];
		switch (reg) {
		case 0:
			return caten(yOrN("Bitmap", val & 0x2), yOrN("Ext Vid", val & 0x1))
				+ " (" + getModeName() + ")";
		case 1:
			return caten(yOrN("16K", val & 0x80), yOrN("Blank", val & 0x40),
					yOrN("Int on", val & 0x20), yOrN("Multi", val & 0x10),
					yOrN("Text", val & 0x08),
					yOrN("Size 4", val & 0x02), yOrN("Mag", val & 0x01))
				+ " (" + getModeName() + ")";
		case 2: 
			return "Screen: " + HexUtils.toHex4(vdpModeInfo.screen.base);
		case 3: 
			return "Colors: " + HexUtils.toHex4(vdpModeInfo.color.base)
			+ (vdpModeRedrawHandler instanceof BitmapModeRedrawHandler ?
					" | Mask: " + HexUtils.toHex4(((BitmapModeRedrawHandler) vdpModeRedrawHandler).bitcolormask) 
							: "");
		case 4: 
			return "Patterns: " + HexUtils.toHex4(vdpModeInfo.patt.base)
			+ (vdpModeRedrawHandler instanceof BitmapModeRedrawHandler ?
					" | Mask: " + HexUtils.toHex4(((BitmapModeRedrawHandler) vdpModeRedrawHandler).bitpattmask) 
					: "");
		case 5: 
			return "Sprites: " + HexUtils.toHex4(vdpModeInfo.sprite.base);
		case 6: 
			return "Sprite patterns: " + HexUtils.toHex4(vdpModeInfo.sprpat.base);
		case 7: 
			return "Color BG: " + HexUtils.toHex2(val & 0x7) 
			+ " | FG: " + HexUtils.toHex2((val & 0xf0) >> 4);
		}
		return null;
	}
	
	protected String getStatusString(byte s) {
		return caten(yOrN("Int", s & 0x80),
				yOrN("5 Sprites", s & 0x40),
				yOrN("Coinc", s & 0x20))
				+ " | 5th: " + (s & 0x1f);
	}

	/**
	 * @param yOrN
	 * @param yOrN2
	 * @param yOrN3
	 * @return
	 */
	protected String caten(String... vals) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String v : vals) {
			if (first)
				first = false;
			else
				sb.append(" | ");
			sb.append(v.length() == 0 ? "0" : v);
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.VdpHandler#setRegister(int, byte)
	 */
	@Override
	public void setRegister(int reg, byte value) {
		if (reg == REG_ST)
			vdpStatus = value;
		else
			writeVdpReg(reg - 1, value);
	}

	/**
	 * @return
	 */
	public String getModeName() {
		switch (modeNumber) {
		case MODE_BITMAP: return "Bitmap";
		case MODE_GRAPHICS: return "Graphics";
		case MODE_MULTI: return "MultiColor";
		case MODE_TEXT: return "Text";
		}
		return null;
	}
}
