package v9t9.gui.client.awt;

import v9t9.engine.hardware.VdpChip;
import v9t9.gui.client.swt.ImageImportHandler;
import v9t9.gui.video.ImageDataCanvas;

public class AwtImageImportSupport extends ImageImportHandler {

	private final ImageDataCanvas canvas;
	private final VdpChip vdp;

	public AwtImageImportSupport(ImageDataCanvas canvas, VdpChip vdp) {
		this.canvas = canvas;
		this.vdp = vdp;
	}

	@Override
	protected VdpChip getVdpHandler() {
		return vdp;
	}

	@Override
	protected ImageDataCanvas getCanvas() {
		return canvas;
	}

}
