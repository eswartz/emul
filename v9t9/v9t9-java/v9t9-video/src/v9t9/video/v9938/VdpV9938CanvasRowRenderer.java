/*
  VdpV9938CanvasRowRenderer.java

  (c) 2014-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.v9938;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.video.IVdpModeRedrawHandler;
import v9t9.video.IVdpModeRowRedrawHandler;

/**
 * @author ejs
 *
 */
public class VdpV9938CanvasRowRenderer extends BaseVdpV9938CanvasRenderer {

	private IVdpModeRowRedrawHandler vdpModeRedrawHandler;
	
	public VdpV9938CanvasRowRenderer(ISettingsHandler settings,
			IVideoRenderer renderer) {
		super(settings, renderer);
	}

	/* (non-Javadoc)
	 * @see v9t9.video.v9938.BaseVdpV9938CanvasRenderer#getModeRedrawHandler()
	 */
	@Override
	protected
	IVdpModeRedrawHandler getModeRedrawHandler() {
		return vdpModeRedrawHandler;
	}
	/* (non-Javadoc)
	 * @see v9t9.video.tms9918a.BaseVdpTMS9918ACanvasRenderer#setVdpModeRedrawHandler(v9t9.video.IVdpModeRedrawHandler)
	 */
	@Override
	protected void setModeRedrawHandler(IVdpModeRedrawHandler redrawHandler) {
		this.vdpModeRedrawHandler = (IVdpModeRowRedrawHandler) redrawHandler;
	}

	/* (non-Javadoc)
	 * @see v9t9.video.v9938.BaseVdpV9938CanvasRenderer#setupBackdrop()
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
	
	
    public synchronized boolean update() {
    	return doUpdate(vdpModeRedrawHandler);
	}
}
