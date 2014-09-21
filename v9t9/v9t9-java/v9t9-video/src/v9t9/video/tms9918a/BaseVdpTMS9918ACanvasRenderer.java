/**
 * 
 */
package v9t9.video.tms9918a;

import static v9t9.common.hardware.VdpTMS9918AConsts.*;

import java.util.Arrays;
import java.util.BitSet;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.hardware.IVdpTMS9918A;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryWriteListener;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.common.video.VdpChanges;
import v9t9.common.video.VdpColorManager;
import v9t9.common.video.VdpColorManager.IColorListener;
import v9t9.common.video.VdpFormat;
import v9t9.video.BlankModeRedrawHandler;
import v9t9.video.IVdpModeRedrawHandler;
import v9t9.video.VdpRedrawInfo;
import v9t9.video.common.VdpModeInfo;
import ejs.base.properties.IProperty;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

/**
 * @author ejs
 *
 */
public abstract class BaseVdpTMS9918ACanvasRenderer  implements IVdpCanvasRenderer, IMemoryWriteListener, IRegisterWriteListener {

	protected byte vdpbg;
	protected byte vdpfg;
	protected boolean drawSprites = true;
	protected static final int REDRAW_SPRITES = 2;
	protected static final int REDRAW_MODE = 4;
	protected static final int REDRAW_BLANK = 8;
	protected static final int REDRAW_PALETTE = 16;
	protected IVdpCanvas vdpCanvas;
	
	protected SpriteRedrawHandler spriteRedrawHandler;
	protected IVdpTMS9918A vdpChip;
	protected IProperty pauseMachine;
	protected VdpModeInfo vdpModeInfo;
	protected VdpRedrawInfo vdpRedrawInfo;
	protected byte[] vdpregs;
	protected int modeNumber;
	protected final IVideoRenderer renderer;
	protected BitSet vdpTouches;
	protected final VdpChanges vdpChanges = new VdpChanges(80 * 30);
	protected BlankModeRedrawHandler blankModeRedrawHandler;

	protected volatile boolean colorsChanged;
	protected IColorListener colorListener;
	protected ListenerList<CanvasListener> listeners = new ListenerList<CanvasListener>();
	protected IFire<CanvasListener> modeChangedFirer = new ListenerList.IFire<IVdpCanvasRenderer.CanvasListener>() {
	
			@Override
			public void fire(CanvasListener listener) {
				listener.modeChanged();
			}
		};

	/**
	 * @param renderer2 
	 * @param settings 
	 * 
	 */
	public BaseVdpTMS9918ACanvasRenderer(ISettingsHandler settings, IVideoRenderer renderer) {
		this.renderer = renderer;
		this.vdpChip = (IVdpTMS9918A) renderer.getVdpHandler();

		this.vdpCanvas = renderer.getCanvas();
		vdpCanvas.setSize(256, 192);
		
		setupRegisters();
		
		vdpRedrawInfo = new VdpRedrawInfo(vdpregs, vdpChip, vdpChanges, vdpCanvas);
		blankModeRedrawHandler = new BlankModeRedrawHandler(vdpRedrawInfo, createBlankModeInfo());
		
		pauseMachine = settings.get(IMachine.settingPauseMachine);
		
		vdpChip.getVideoMemory().addWriteListener(this);
		vdpChip.addWriteListener(this);
		
		vdpTouches = new BitSet(vdpChip.getMemorySize());
		
		this.colorsChanged = true;
		colorListener = new VdpColorManager.IColorListener() {
			
			@Override
			public void colorsChanged() {
				colorsChanged = true;
			}
		};
		vdpCanvas.getColorMgr().addListener(colorListener);
	}

	@Override
	public void dispose() {
		vdpChip.getVideoMemory().removeWriteListener(this);
		vdpChip.removeWriteListener(this);
	
		vdpCanvas.getColorMgr().removeListener(colorListener);
	}
	protected synchronized void flushVdpChanges(IVdpModeRedrawHandler vdpModeRedrawHandler) {
		if (vdpModeRedrawHandler != null) {
			synchronized (vdpCanvas) {
				for (int vdpaddr = vdpTouches.nextSetBit(0); vdpaddr >= 0; vdpaddr = vdpTouches.nextSetBit(vdpaddr + 1)) {
					vdpChanges.changed |= vdpModeRedrawHandler.touch(vdpaddr);
					if (spriteRedrawHandler != null) {
						vdpChanges.changed |= spriteRedrawHandler.touch(vdpaddr);
					}
				}
				vdpTouches.clear();
			}
		}
	}
	
	protected void setupRegisters() {
		// copy of registers in IVdpChip
		vdpregs = new byte[8];
	}

	protected final boolean CHANGED(byte old, byte val, int v) { return (old&(v))!=(val&(v)); }

	protected final synchronized void writeVdpReg(final int reg, byte val) {
		synchronized (vdpCanvas) {
	    	int redraw = doWriteVdpReg(reg, vdpregs[reg], val);
	
	    	/*  This flag must be checked first because
		 	   it affects the meaning of the following 
		 	   calls and checks. */
		 	if ((redraw & REDRAW_MODE) != 0) {
		 		setVideoMode();
		 		establishVideoMode();
		 		setupBackdrop();
		 		forceRedraw();
		 	}
		
		 	if ((redraw & REDRAW_SPRITES) != 0) {
				dirtySprites();
			}
	
		 	if ((redraw & REDRAW_PALETTE) != 0) {
		 		setupBackdrop();
		 		forceRedraw();
		 	}
		
		 	if ((redraw & REDRAW_BLANK) != 0) {
		 		if ((vdpregs[1] & R1_NOBLANK) == 0) {
		 			//vdpCanvas.setBlank(true);
			 		forceRedraw();
		 			//update();
		 		} else {
		 			//vdpCanvas.setBlank(false);
			 		forceRedraw();
		 			//update();
		 		}
		 	}
		}
	}

	abstract protected void setVideoMode();

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
			
			vdpCanvas.getColorMgr().setForegroundBackground(vdpfg, vdpbg);
			break;
	
		default:
	
		}
	
		return redraw;
	}

	/** Tell if the registers indicate a blank screen. */
	protected boolean isBlank() {
		return (vdpregs[1] & R1_NOBLANK) == 0;
	}

	protected void establishVideoMode() {
		modeNumber = vdpChip.getModeNumber();
		if (isBlank()) {
			setBlankMode();
			return;
		}
		
		switch (modeNumber) {
		case MODE_TEXT:
			setTextMode();
	 		forceRedraw();  // for border
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
		spriteRedrawHandler = createSpriteRedrawHandler();
		setModeRedrawHandler(new GraphicsModeRedrawHandler(vdpRedrawInfo, vdpModeInfo));
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
		spriteRedrawHandler = createSpriteRedrawHandler();
		setModeRedrawHandler(new MulticolorModeRedrawHandler(vdpRedrawInfo, vdpModeInfo));
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
		spriteRedrawHandler = null;
		setModeRedrawHandler(new TextModeRedrawHandler(vdpRedrawInfo, vdpModeInfo));
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
		spriteRedrawHandler = createSpriteRedrawHandler();
		setModeRedrawHandler(new BitmapModeRedrawHandler(vdpRedrawInfo, vdpModeInfo));
	}

	abstract protected void setModeRedrawHandler(IVdpModeRedrawHandler redrawHandler);

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
		setModeRedrawHandler(blankModeRedrawHandler);
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

	public synchronized void touchAbsoluteVdpMemory(int vdpaddr, Number val) {
		synchronized (vdpCanvas) {
	    	vdpTouches.set(vdpaddr);
	    	if (val instanceof Short)
	    		vdpTouches.set(vdpaddr + 1);
		}
	}

	protected void dirtySprites() {
		vdpChanges.sprite = -1;
		Arrays.fill(vdpChanges.sprpat, 0, vdpChanges.sprpat.length, (byte)1);
		vdpChanges.changed = true;
	}

	@Override
	public IVdpCanvas getCanvas() {
		return vdpCanvas;
	}

	@Override
	public void registerChanged(int reg, int value) {
		if (reg == REG_SCANLINE) {
			onScanline(value);
		}
		else {
			if (reg >= 0 && reg < vdpChip.getVdpRegisterCount())
				writeVdpReg(reg, (byte) value);
		}
	}
	protected void onScanline(int scanline) {
		synchronized (vdpCanvas) {
			vdpCanvas.setCurrentY(scanline);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.video.IVdpCanvasRenderer#refresh()
	 */
	@Override
	public void refresh() {
		for (int reg = 0; reg < vdpregs.length; reg++)
			vdpregs[reg] = (byte) vdpChip.getRegister(reg);
		setVideoMode();
		establishVideoMode();
		forceRedraw();
	}
	/* (non-Javadoc)
	 * @see v9t9.common.video.IVdpCanvasRenderer#forceRedraw()
	 */
	@Override
	public synchronized void forceRedraw() {
		synchronized (vdpCanvas) {
			vdpChanges.changed = true;
			vdpChanges.fullRedraw = true;
			listeners.fire(modeChangedFirer);
		}
	}

	@Override
	public void changed(IMemoryEntry entry, int addr, Number value) {
		touchAbsoluteVdpMemory(addr, value);
	}

	@Override
	public void addListener(CanvasListener listener) {
		listeners.add(listener);		
	}

	@Override
	public void removeListener(CanvasListener listener) {
		listeners.remove(listener);
	}

}