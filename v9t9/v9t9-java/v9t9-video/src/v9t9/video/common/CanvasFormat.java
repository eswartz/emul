/*
  CanvasFormat.java

  (c) 2013-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.common;

import v9t9.common.video.BitmapVdpCanvas;
import v9t9.video.BufferCanvasShortGreyscale;
import v9t9.video.BufferCanvasByteR3G3B2;
import v9t9.video.BufferCanvasInt;
import v9t9.video.BufferCanvasShort;
import v9t9.video.ImageDataCanvas24Bit;
import v9t9.video.ImageDataCanvasPaletted;

/**
 * @author ejs
 *
 */
public enum CanvasFormat {
	DEFAULT(null, 0.0f),
	RGB16_5_6_5(BufferCanvasShort.class, 1.2f),
	RGB16_GREY(BufferCanvasShortGreyscale.class, 1.2f), 
	RGB8_3_3_2(BufferCanvasByteR3G3B2.class, 1.2f),
	RGB24_8_8_8(BufferCanvasInt.class, 1.2f),
	RGB8(ImageDataCanvasPaletted.class, 1.0f),
	RGB24(ImageDataCanvas24Bit.class, 1.0f);

	
	private Class<? extends BitmapVdpCanvas> klass;
	private float minGLVersion;

	private CanvasFormat(Class<? extends BitmapVdpCanvas> klass, float minGLVersion) {
		this.klass = klass;
		this.minGLVersion = minGLVersion;
	}

	public float getMinGLVersion() {
		return minGLVersion;
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
