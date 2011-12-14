/**
 * 
 */
package v9t9.canvas.video;

import v9t9.canvas.video.tms9918a.VdpTMS9918ACanvasRenderer;
import v9t9.canvas.video.v9938.VdpV9938CanvasRenderer;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.hardware.IVdpTMS9918A;
import v9t9.common.hardware.IVdpV9938;
import v9t9.common.video.IVdpCanvasRenderer;

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
	public static IVdpCanvasRenderer createCanvasHandler(ISettingsHandler settings, IVideoRenderer video) {
		if (video.getVdpHandler() instanceof IVdpV9938)
			return new VdpV9938CanvasRenderer(settings, video);
		else if (video.getVdpHandler() instanceof IVdpTMS9918A)
			return new VdpTMS9918ACanvasRenderer(settings, video);
		return null;
	}
	
	
}
