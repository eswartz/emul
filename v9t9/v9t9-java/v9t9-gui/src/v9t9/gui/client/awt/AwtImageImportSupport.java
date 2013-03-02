/*
  AwtImageImportSupport.java

  (c) 2011-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
