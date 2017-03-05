/*
  IModeConverter.java

  (c) 2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;

/**
 * Convert an image to the restrictions of the video mode
 * (beyond color mapping, which has already been done) 
 * @author ejs
 *
 */
public interface IModeConverter {

	/**
	 * @param img
	 * @return
	 */
	BufferedImage prepareImage(BufferedImage img);

	/**
	 * /**
	 * Take the image, which has been palette-mapped and/or dithered, and
	 * create a version that follows the rules of the VdpFormat.
	 * @param img
	 * @param targWidth
	 * @param targHeight
	 */
	BufferedImage convert(BufferedImage img, int targWidth, int targHeight);


}
