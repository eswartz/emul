/**
 * May 22, 2011
 */
package v9t9.engine.video;

import v9t9.engine.hardware.IVdpChip;

/**
 * @author ejs
 *
 */
public class VdpRedrawInfo {

	public final IVdpChip vdp;
	public final VdpChanges changes;
	public final VdpCanvas canvas;
	public final VdpTouchHandlerBlock touch;
	
	public final byte[] vdpregs;
	
	public VdpRedrawInfo(byte[] vdpregs, IVdpChip vdp, 
			VdpChanges changed, VdpCanvas vdpCanvas) {
		this.vdpregs = vdpregs;
		this.vdp = vdp;
		this.changes = changed;
		this.canvas = vdpCanvas;
		this.touch = new VdpTouchHandlerBlock();
	}		
}
