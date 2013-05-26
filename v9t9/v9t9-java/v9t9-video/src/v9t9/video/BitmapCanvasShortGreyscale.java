/*
  BitmapCanvasShortGreyscale.java

  (c) 2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

import java.nio.Buffer;

import org.ejs.gui.images.ColorMapUtils;


/**
 * Render video content into a short array
 * @author ejs
 *
 */
public class BitmapCanvasShortGreyscale extends BitmapCanvasShort {
	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getImageType()
	 */
	@Override
	public int getImageType() {
		return GL_UNSIGNED_SHORT_5_6_5;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getImageFormat()
	 */
	@Override
	public int getImageFormat() {
		return GL_RGB;
	}

	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getInternalFormat()
	 */
	@Override
	public int getInternalFormat() {
		return GL_LUMINANCE12_ALPHA4;
	}


	/* (non-Javadoc)
	 * @see v9t9.common.video.BitmapVdpCanvas#getNextRGB(java.nio.Buffer, byte[])
	 */
	@Override
	public void getNextRGB(Buffer buffer, byte[] rgb) {
		super.getNextRGB(buffer, rgb);
		ColorMapUtils.rgbToGrey(rgb, rgb);
	}
}
