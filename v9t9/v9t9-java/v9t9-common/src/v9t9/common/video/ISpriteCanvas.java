/*
  ISpriteCanvas.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.video;

import java.util.BitSet;


/**
 * @author ejs
 *
 */
public interface ISpriteCanvas {
	/**
	 * @return
	 */
	VdpSprite[] getSprites();

	/**
	 * @param numchars
	 */
	void setNumSpriteChars(int numchars);

	/**
	 * @param isMag
	 */
	void setMagnified(boolean isMag);
	int updateSpriteCoverage(ICanvas canvas, BitSet screen,
			boolean forceRedraw);
	/**
	 * @param canvas
	 */
	void drawSprites(ISpriteDrawingCanvas canvas, boolean force);
}
