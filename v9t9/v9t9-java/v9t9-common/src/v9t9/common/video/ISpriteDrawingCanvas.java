/*
  ISpriteDrawingCanvas.java

  (c) 2011 Edward Swartz

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
public interface ISpriteDrawingCanvas {

	public void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	public void drawEightMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	public void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr);
	public VdpColorManager getColorMgr();
	public int getHeight();
	public int getWidth();
	public int getVisibleWidth();
	/** Get minimum Y drawn for this canvas */
	int getMinY();
	/** Get maximum (exclusive) Y for this canvas */
	int getMaxY();


}
