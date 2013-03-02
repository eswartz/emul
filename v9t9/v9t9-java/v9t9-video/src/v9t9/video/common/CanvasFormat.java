/*
  CanvasFormat.java

  (c) 2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.common;

import v9t9.common.video.BitmapVdpCanvas;
import v9t9.video.BitmapCanvasInt;
import v9t9.video.BitmapCanvasShort;
import v9t9.video.BitmapCanvasShortGreyscale;
import v9t9.video.ImageDataCanvas24Bit;
import v9t9.video.ImageDataCanvasPaletted;
import v9t9.video.ImageDataCanvasR3G3B2;

/**
 * @author ejs
 *
 */
public enum CanvasFormat {
	DEFAULT(null),
	RGB16_5_6_5(BitmapCanvasShort.class), 
	RGB16_GREY(BitmapCanvasShortGreyscale.class), 
	RGB8_3_3_2(ImageDataCanvasR3G3B2.class),
	RGB24_8_8_8(BitmapCanvasInt.class),
	RGB8(ImageDataCanvasPaletted.class),
	RGB24(ImageDataCanvas24Bit.class);

	
	private Class<? extends BitmapVdpCanvas> klass;

	/**
	 * 
	 */
	private CanvasFormat(Class<? extends BitmapVdpCanvas> klass) {
		this.klass = klass;
	}
	
	public BitmapVdpCanvas create() {
		if (klass == null)
			return null;
		try {
			return klass.newInstance();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
