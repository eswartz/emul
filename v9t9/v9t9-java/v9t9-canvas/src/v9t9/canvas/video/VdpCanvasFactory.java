/**
 * 
 */
package v9t9.canvas.video;

import v9t9.canvas.video.tms9918a.VdpTMS9918ACanvasRenderer;
import v9t9.canvas.video.v9938.VdpV9938CanvasRenderer;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.hardware.IVdpTMS9918A;
import v9t9.common.hardware.IVdpV9938;
import v9t9.common.video.IVdpCanvas;

/**
 * @author ejs
 *
 */
public class VdpCanvasFactory {
	private VdpCanvasFactory() { }

	/**
	 * @param vdp
	 * @param canvas 
	 * @return
	 */
	public static IVdpCanvasHandler createCanvasHandler(IVdpChip vdp, IVdpCanvas canvas) {
		if (vdp instanceof IVdpV9938)
			return new VdpV9938CanvasRenderer((IVdpV9938) vdp, canvas);
		else if (vdp instanceof IVdpTMS9918A)
			return new VdpTMS9918ACanvasRenderer((IVdpTMS9918A) vdp, canvas);
		return null;
	}
	
	
}
