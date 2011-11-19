package v9t9.emulator.clients.builtin.awt;

import v9t9.emulator.clients.builtin.swt.ImageImportHandler;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.engine.VdpHandler;

public class AwtImageImportSupport extends ImageImportHandler {

	private final ImageDataCanvas canvas;
	private final VdpHandler vdp;

	public AwtImageImportSupport(ImageDataCanvas canvas, VdpHandler vdp) {
		this.canvas = canvas;
		this.vdp = vdp;
	}

	@Override
	protected VdpHandler getVdpHandler() {
		return vdp;
	}

	@Override
	protected ImageDataCanvas getCanvas() {
		return canvas;
	}

}
