package v9t9.gui.client.awt;

import v9t9.canvas.video.ImageDataCanvas;
import v9t9.common.hardware.IVdpChip;
import v9t9.gui.client.swt.ImageImportHandler;

public class AwtImageImportSupport extends ImageImportHandler {

	private final ImageDataCanvas canvas;
	private final IVdpChip vdp;

	public AwtImageImportSupport(ImageDataCanvas canvas, IVdpChip vdp) {
		this.canvas = canvas;
		this.vdp = vdp;
	}

	@Override
	protected IVdpChip getVdpHandler() {
		return vdp;
	}

	@Override
	protected ImageDataCanvas getCanvas() {
		return canvas;
	}

}
