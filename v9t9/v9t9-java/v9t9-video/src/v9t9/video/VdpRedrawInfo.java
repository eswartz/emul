/*
  VdpRedrawInfo.java

  (c) 2011-2013 Edward Swartz

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

import v9t9.common.hardware.IVdpChip;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.VdpChanges;

/**
 * @author ejs
 *
 */
public class VdpRedrawInfo {

	public final IVdpChip vdp;
	public final VdpChanges changes;
	public final IVdpCanvas canvas;
	public final VdpTouchHandlerBlock touch;
	public final byte[] vdpregs;
	
	public VdpRedrawInfo(byte[] vdpregs, IVdpChip vdp, 
			VdpChanges changed,
			IVdpCanvas vdpCanvas) {
		this.vdpregs = vdpregs;
		this.vdp = vdp;
		this.changes = changed;
		this.canvas = vdpCanvas;
		this.touch = new VdpTouchHandlerBlock();
	}		
}
