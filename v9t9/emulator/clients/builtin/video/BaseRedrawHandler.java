/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 *
 */
public abstract class BaseRedrawHandler {

	protected final MemoryDomain vdpMemory;
	protected final VdpModeInfo vdpModeInfo;
	protected final VdpChanges vdpChanges;
	protected final VdpCanvas vdpCanvas;
	protected final VdpTouchHandlerBlock vdpTouchBlock;
	
	protected byte vdpchanged;
	protected final byte[] vdpregs;
	
	public BaseRedrawHandler(byte[] vdpregs, MemoryDomain vdpMemory, 
			VdpChanges changed, VdpCanvas vdpCanvas) {
		this.vdpregs = vdpregs;
		this.vdpMemory = vdpMemory;
		this.vdpChanges = changed;
		this.vdpCanvas = vdpCanvas;
		this.vdpModeInfo = new VdpModeInfo();
		this.vdpTouchBlock = new VdpTouchHandlerBlock();
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
	
	

	final static byte[] patternBuffer = new byte[8];
	protected ByteMemoryAccess readEightBytes(int pattOffs) {
		ByteMemoryAccess access = vdpMemory.getByteReadMemoryAccess(pattOffs);
		if (access == null) {
			for (int i = 0; i < 8; i++) {
				patternBuffer[i] = (byte) vdpMemory.flatReadByte(pattOffs + i);
			}
			access = new ByteMemoryAccess(patternBuffer, 0);
		} 
		return access;
	}

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
			int currchar = vdpMemory.flatReadByte(vdpModeInfo.screen.base + i) & 0xff;	/* char # to update */
			if (vdpChanges.patt[currchar] != 0)	/* this pattern changed? */
				vdpChanges.screen[i] = VdpChanges.SC_BACKGROUND;	/* then this char changed */
		}
		
	}
	
}
