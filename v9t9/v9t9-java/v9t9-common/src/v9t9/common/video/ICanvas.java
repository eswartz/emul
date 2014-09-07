/*
  ICanvas.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.video;

/**
 * @author ejs
 *
 */
public interface ICanvas {

	void setSize(int x, int y);

	/** Get the full screen width (this includes any overscan and possibly extra pixels
	 * not intended to be seen). */
	int getWidth();

	/** Get the full screen width (this includes any overscan and possibly extra pixels
	 * not intended to be seen). */
	int getVisibleWidth();

	/** Get the nominal screen height. This does not count interlacing. */
	int getHeight();

	int getVisibleHeight();

	/** Get minimum Y drawn for this canvas */
	int getMinY();
	/** Get maximum (exclusive) Y for this canvas */
	int getMaxY();
	/** Set minimum Y drawn for this canvas */
	void setMinY(int minY);
	/** Set maximum (exclusive) Y for this canvas */
	void setMaxY(int maxY);


	/** Get the delta for one pixel, in terms of the offset. 
	 * @see #getBitmapOffset(int, int) 
	 */ 
	int getPixelStride();
	/** Get the delta for one row, in terms of the offset. 
	 * @see #getBitmapOffset(int, int) 
	 */ 
	int getLineStride();


	int getBlockCount();
	
	void markDirty();

	void markDirty(RedrawBlock[] blocks, int count);

}
