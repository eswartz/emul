/*
  BaseRedrawHandler.java

  (c) 2008-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.video;

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
public abstract class BaseRedrawHandler implements IVdpModeRedrawHandler {
	
	protected final VdpRedrawInfo info;
	protected final VdpModeInfo modeInfo;

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
			if (info.changes.patt[currchar] != 0)	/* this pattern changed? */
				info.changes.screen.set(i);	/* then this char changed */
		}
		
	}
	
	public void clear() {
		info.canvas.clear();
	}
}
