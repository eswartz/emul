/*
  BlankModeRedrawHandler.java

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

import v9t9.common.video.RedrawBlock;
import v9t9.video.common.VdpModeInfo;



/**
 * @author ejs
 *
 */
public class BlankModeRedrawHandler extends BaseRedrawHandler implements
		IVdpModeRedrawHandler {

	public BlankModeRedrawHandler(VdpRedrawInfo info, VdpModeInfo modeInfo) {
		super(info, modeInfo);
		
		info.touch.patt = null;
		info.touch.sprite = info.touch.sprpat = null;
		info.touch.screen = null;
		info.touch.color = null;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#propagateTouches()
	 */
	public void prepareUpdate() {
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.InternalVdp.VdpModeRedrawHandler#updateCanvas(v9t9.emulator.clients.builtin.VdpCanvas, v9t9.emulator.clients.builtin.InternalVdp.RedrawBlock[])
	 */
	public int updateCanvas(RedrawBlock[] blocks) {
		/*if (force) {
			for (int i = 0; i < 768; i++) {
				blocks[i].r = (i / 32) * 8;
				blocks[i].c = (i % 32) * 8;
			}
			return 768;
		}*/
		return 0;
	}
}
