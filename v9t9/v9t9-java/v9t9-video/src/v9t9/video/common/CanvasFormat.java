/*
  CanvasFormat.java

  (c) 2012-2013 Edward Swartz

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
