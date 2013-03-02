/*
  ISpriteVdpCanvas.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.video;


/**
 * This interface serves to access the sprite data written onto
 * a canvas, in the event the data is not directly blitted onto
 * an IVdpCanvas.
 * @author ejs
 *
 */
public interface ISpriteVdpCanvas extends ICanvas, ISpriteDrawingCanvas {

	byte getColorAtOffset(int offset);
	
	int getBitmapOffset(int x, int y);
}