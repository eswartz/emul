/*
  AwtImageImportSupport.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.awt;


import v9t9.common.client.IVideoRenderer;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.video.IVdpCanvas;
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
	protected IVdpCanvas getCanvas() {
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
