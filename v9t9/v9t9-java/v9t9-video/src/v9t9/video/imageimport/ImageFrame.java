/*
  ImageFrame.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;

/**
 * @author ejs
 * 
 */
public class ImageFrame {
	public BufferedImage image;
	public boolean isLowColor;
	public int delayMs;

	public ImageFrame(BufferedImage image, boolean isLowColor, int delayMs) {
		this.image = image;
		this.isLowColor = isLowColor;
		this.delayMs = delayMs;
	}

	public ImageFrame(BufferedImage image, boolean isLowColor) {
		this.image = image;
		this.isLowColor = isLowColor;

		this.delayMs = 0;
	}

}
