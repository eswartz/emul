/**
 * May 22, 2011
 */
package v9t9.video;

import v9t9.common.hardware.IVdpChip;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.common.video.VdpChanges;

/**
 * @author ejs
 *
 */
public class VdpRedrawInfo {

	public final IVdpChip vdp;
	public final IVdpCanvasRenderer renderer;
	public final VdpChanges changes;
	public final IVdpCanvas canvas;
	public final VdpTouchHandlerBlock touch;
	public final byte[] vdpregs;
	
	public VdpRedrawInfo(byte[] vdpregs, IVdpChip vdp, 
			IVdpCanvasRenderer renderer,
			VdpChanges changed, IVdpCanvas vdpCanvas) {
		this.vdpregs = vdpregs;
		this.vdp = vdp;
		this.renderer = renderer;
		this.changes = changed;
		this.canvas = vdpCanvas;
		this.touch = new VdpTouchHandlerBlock();
	}		
}
