/*
  BaseRedrawHandler.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.video.common.VdpModeInfo;



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
public abstract class BaseRedrawHandler implements IVdpModeBlockRedrawHandler {
	
	protected final VdpRedrawInfo info;
	protected final VdpModeInfo modeInfo;

	protected static byte[] solidBlockMem = { 
		(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
		(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
	};
	public static ByteMemoryAccess solidBlockPattern = new ByteMemoryAccess(
			solidBlockMem, 0);
	
	public BaseRedrawHandler(VdpRedrawInfo info, VdpModeInfo vdpModeInfo) {
		this.info = info;
		this.modeInfo = vdpModeInfo;
		/*
		if (true && modeInfo.screen.size != 0) {
			System.out.println("VDP Tables:\n" +
					"Reg 1: " + Integer.toHexString(info.vdpregs[1] & 0xff) + "\n"+
					"Screen: " + Integer.toHexString(modeInfo.screen.base) + "\n"+
					"Pattern: " + Integer.toHexString(modeInfo.patt.base) + "\n" +
					"Color: " + Integer.toHexString(modeInfo.color.base) + "\n" +
					"Sprites: " + Integer.toHexString(modeInfo.sprite.base) + "\n"
					);
		}
		*/
	}
	
	protected VdpTouchHandler modify_screen_default = new VdpTouchHandler() {

		public void modify(int offs) {
			info.changes.screen.set(offs);
			info.changes.changed = true;
		}
		
	};
	protected VdpTouchHandler modify_patt_default = new VdpTouchHandler() {

		public void modify(int offs) {
			info.changes.patt[offs >> 3] = 1;
			info.changes.changed = true;
		}
		
	};
	
	public boolean touch(int addr) {
    	boolean visible = false;

    	if (modeInfo.screen.base <= addr && addr < modeInfo.screen.base + modeInfo.screen.size) {
    		info.touch.screen.modify(addr - modeInfo.screen.base);
    		visible = true;
    	}

    	if (modeInfo.patt.base <= addr && addr < modeInfo.patt.base + modeInfo.patt.size) {
    		info.touch.patt.modify(addr - modeInfo.patt.base);
    		visible = true;
    	}

    	if (modeInfo.color.base <= addr && addr < modeInfo.color.base + modeInfo.color.size) {
    		info.touch.color.modify(addr - modeInfo.color.base);
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
		
		int size = modeInfo.screen.size;
		for (int i = 0; i < size; i++) {
			int currchar = info.vdp.readAbsoluteVdpMemory(modeInfo.screen.base + i) & 0xff;	/* char # to update */
			if (info.changes.patt[currchar] != 0) {	/* this pattern changed? */
				info.changes.screen.set(i);	/* then this char changed */
			}
		}
	}
	
	public void clear() {
		info.canvas.clear();
	}
	
}
