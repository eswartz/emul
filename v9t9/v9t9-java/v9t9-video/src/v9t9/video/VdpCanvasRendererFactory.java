/*
  VdpCanvasRendererFactory.java

  (c) 2011-2013 Edward Swartz

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
package v9t9.video;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.IVideoRenderer;
import v9t9.common.hardware.IVdpTMS9918A;
import v9t9.common.hardware.IVdpV9938;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.video.tms9918a.VdpTMS9918ACanvasRenderer;
import v9t9.video.v9938.VdpV9938CanvasRenderer;

/**
 * @author ejs
 *
 */
public class VdpCanvasRendererFactory {
	private VdpCanvasRendererFactory() { }

	public static IVdpCanvasRenderer createCanvasRenderer(ISettingsHandler settings, IVideoRenderer video) {
		if (video.getVdpHandler() instanceof IVdpV9938)
			return new VdpV9938CanvasRenderer(settings, video);
		else if (video.getVdpHandler() instanceof IVdpTMS9918A)
			return new VdpTMS9918ACanvasRenderer(settings, video);
		return null;
	}
	
	
}
