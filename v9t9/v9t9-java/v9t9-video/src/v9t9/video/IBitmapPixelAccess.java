/*
  IBitmapPixelAccess.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

/**
 * @author ejs
 *
 */
public interface IBitmapPixelAccess {
	/** Get image width */
	int getWidth();
	/** Get image height */
	int getHeight();
	/** Get native mode pixel value at the given col/row; 0-15 for most modes; 0-255 for gfx mode 7 */
	byte getPixel(int x, int y);
}
