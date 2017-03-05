/*
  VdpTMS9918ACanvasRenderer.java

  (c) 2011-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.tms9918a;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.video.IVdpModeRedrawHandler;
import v9t9.video.IVdpModeRowRedrawHandler;

/**
 * This is a renderer for the TI-99/4A VDP chip which renders to an IVdpCanvas
 * in row-by-row fashion.
 * 
 * @author ejs
 */
public class VdpTMS9918ACanvasRowRenderer extends BaseVdpTMS9918ACanvasRenderer {
	protected IVdpModeRowRedrawHandler vdpModeRedrawHandler;
	
	public VdpTMS9918ACanvasRowRenderer(ISettingsHandler settings, IVideoRenderer renderer) {
		super(settings, renderer);

	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setModeRedrawHandler(v9t9.video.IVdpModeRedrawHandler)
	 */
	@Override
	protected void setModeRedrawHandler(IVdpModeRedrawHandler redrawHandler) {
		this.vdpModeRedrawHandler = (IVdpModeRowRedrawHandler) redrawHandler;
	}

	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setupBackdrop()
	 */
	@Override
	protected void setupBackdrop() {
		update();
		super.setupBackdrop();
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setVideoMode()
	 */
	@Override
	protected void setVideoMode() {
		// flush changes from the previous mode
		update();
		synchronized (vdpCanvas) {
			prevScanline = 0;
			scanlineWrapped = false;
			touchedRows.clear();
			touchedRows.set(0, vdpCanvas.getHeight());
		}
	}


	
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setBitmapMode()
	 */
	@Override
	protected void setBitmapMode() {
		super.setBitmapMode();
		vdpModeRedrawHandler = new BitmapModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setBlankMode()
	 */
	@Override
	protected void setBlankMode() {
		super.setBlankMode();
		vdpModeRedrawHandler = blankModeRedrawHandler;
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setGraphicsMode()
	 */
	@Override
	protected void setGraphicsMode() {
		super.setGraphicsMode();
		vdpModeRedrawHandler = new GraphicsModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setMultiMode()
	 */
	@Override
	protected void setMultiMode() {
		super.setMultiMode();
		vdpModeRedrawHandler = new MulticolorModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
	}

	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setTextMode()
	 */
	@Override
	protected void setTextMode() {
		super.setTextMode();
		vdpModeRedrawHandler = new TextModeRedrawHandler(vdpRedrawInfo, vdpModeInfo);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#onScanline(int)
	 */
	@Override
	protected void onScanline(int scanline) {
		synchronized (vdpCanvas) {
			super.onScanline(scanline);
			currentScanline = scanline;
			if (scanline == 0) {
				scanlineWrapped = true;
			}
		}
	}
	
    public synchronized boolean update() {
    	return doUpdate(vdpModeRedrawHandler);
	}
	
}
