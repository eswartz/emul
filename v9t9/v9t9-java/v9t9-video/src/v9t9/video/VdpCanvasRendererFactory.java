/*
  VdpCanvasRendererFactory.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.hardware.IVdpTMS9918A;
import v9t9.common.hardware.IVdpV9938;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.video.tms9918a.VdpTMS9918ACanvasBlockRenderer;
import v9t9.video.v9938.VdpV9938CanvasBlockRenderer;

/**
 * @author ejs
 *
 */
public class VdpCanvasRendererFactory {
	private VdpCanvasRendererFactory() { }

	public static IVdpCanvasRenderer createCanvasRenderer(ISettingsHandler settings, IVideoRenderer video) {
		if (video.getVdpHandler() instanceof IVdpV9938)
			return new VdpV9938CanvasBlockRenderer(settings, video);
//			return new VdpV9938CanvasRowRenderer(settings, video);
		else if (video.getVdpHandler() instanceof IVdpTMS9918A)
			return new VdpTMS9918ACanvasBlockRenderer(settings, video);
//			return new VdpTMS9918ACanvasRowRenderer(settings, video);
		return null;
	}
	
	
}
