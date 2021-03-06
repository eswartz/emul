/*
  VdpRedrawInfo.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
