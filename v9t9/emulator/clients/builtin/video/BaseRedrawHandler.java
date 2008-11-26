/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import v9t9.engine.VdpHandler;


/**
 * The base class for redrawing the screen bitmap in accordance with
 * memory changes in a particular video mode.  
 * <p>
 * This translates modifications to VDP RAM into logical
 * "touches" to the memory regions designated by the setting of the VDP
 * registers, allowing for efficient updating of the screen.
 * <p>
 * (I.e., instead of reblitting the entire screen every 1/60 second,
 * we can detect that a particular screen location alone was modified,
 * or that a pattern was modified, reflecting 8 screen positions.)
 * @author ejs
 *
 */
public abstract class BaseRedrawHandler {

	protected final VdpHandler vdpMemory;
	protected final VdpModeInfo vdpModeInfo;
	protected final VdpChanges vdpChanges;
	protected final VdpCanvas vdpCanvas;
	protected final VdpTouchHandlerBlock vdpTouchBlock;
	
	protected byte vdpchanged;
	protected final byte[] vdpregs;
	
	public BaseRedrawHandler(byte[] vdpregs, VdpHandler vdpMemory, 
			VdpChanges changed, VdpCanvas vdpCanvas, VdpModeInfo modeInfo) {
		this.vdpregs = vdpregs;
		this.vdpMemory = vdpMemory;
		this.vdpChanges = changed;
		this.vdpCanvas = vdpCanvas;
		this.vdpModeInfo = modeInfo;
		this.vdpTouchBlock = new VdpTouchHandlerBlock();
		if (true && modeInfo.screen.size != 0) {
			System.out.println("VDP Tables:\nScreen: " + Integer.toHexString(modeInfo.screen.base) + "\n"+
					"Pattern: " + Integer.toHexString(modeInfo.patt.base) + "\n" +
					"Color: " + Integer.toHexString(modeInfo.color.base) + "\n");
					
		}
	}
	
	protected VdpTouchHandler modify_screen_default = new VdpTouchHandler() {

		public void modify(int offs) {
			vdpChanges.screen[offs] = vdpchanged = VdpChanges.SC_BACKGROUND;
		}
		
	};
	protected VdpTouchHandler modify_patt_default = new VdpTouchHandler() {

		public void modify(int offs) {
			vdpChanges.patt[offs >> 3] = vdpchanged = 1;			
		}
		
	};
	
	public boolean touch(int addr) {
    	boolean visible = false;

    	if (vdpModeInfo.screen.base <= addr && addr < vdpModeInfo.screen.base + vdpModeInfo.screen.size) {
    		vdpTouchBlock.screen.modify(addr - vdpModeInfo.screen.base);
    		visible = true;
    	}

    	if (vdpModeInfo.patt.base <= addr && addr < vdpModeInfo.patt.base + vdpModeInfo.patt.size) {
    		vdpTouchBlock.patt.modify(addr - vdpModeInfo.patt.base);
    		visible = true;
    	}

    	if (vdpModeInfo.color.base <= addr && addr < vdpModeInfo.color.base + vdpModeInfo.color.size) {
    		vdpTouchBlock.color.modify(addr - vdpModeInfo.color.base);
    		visible = true;
    	}

    	// sprites handled in sprite redraw handler
    	
    	return visible;
	}

	/** The default implementation propagates pattern table changes to the screen.
	 * (The touch handlers have already propagated color changes to the pattern table.)
	 */
	public void propagatePatternTouches() {
		/*  Set pattern changes in chars */
		
		int size = vdpModeInfo.screen.size;
		for (int i = 0; i < size; i++) {
			int currchar = vdpMemory.readAbsoluteVdpMemory(vdpModeInfo.screen.base + i) & 0xff;	/* char # to update */
			if (vdpChanges.patt[currchar] != 0)	/* this pattern changed? */
				vdpChanges.screen[i] = VdpChanges.SC_BACKGROUND;	/* then this char changed */
		}
		
	}
	
}
