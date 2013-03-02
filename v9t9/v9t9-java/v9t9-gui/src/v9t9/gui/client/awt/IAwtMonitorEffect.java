/*
  IAwtMonitorEffect.java

  (c) 2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.awt;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import v9t9.common.client.IMonitorEffect;

/**
 * @author ejs
 *
 */
public interface IAwtMonitorEffect extends IMonitorEffect {

	/**
	 * @param destWidth
	 * @param destHeight
	 * @param surface
	 * @param logRect
	 * @param physRect
	 * @return
	 */
	BufferedImage applyEffect(int destWidth, int destHeight,
			BufferedImage surface, Rectangle logRect, Rectangle physRect);

}
