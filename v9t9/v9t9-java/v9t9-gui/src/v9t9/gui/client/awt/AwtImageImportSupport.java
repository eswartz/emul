package v9t9.gui.client.awt;


import v9t9.common.client.IVideoRenderer;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.gui.client.swt.imageimport.ImageImportHandler;
import v9t9.video.ImageDataCanvas;

public class AwtImageImportSupport extends ImageImportHandler {

	private final ImageDataCanvas canvas;
	private final IVdpChip vdp;
	private final IVideoRenderer videoRenderer;

	public AwtImageImportSupport(ImageDataCanvas canvas, IVdpChip vdp, IVideoRenderer videoRenderer) {
		this.canvas = canvas;
		this.vdp = vdp;
		this.videoRenderer = videoRenderer;
		getImageImportOptions();
	}

	@Override
	protected IVdpChip getVdpHandler() {
		return vdp;
	}

	@Override
	protected ImageDataCanvas getCanvas() {
		return canvas;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.imageimport.ImageImportHandler#getCanvasRenderer()
	 */
	@Override
	protected IVdpCanvasRenderer getCanvasRenderer() {
		return videoRenderer.getCanvasHandler();
	}


}
