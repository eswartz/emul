/*
  ISVGLoader.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.svg;

import java.awt.image.BufferedImage;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;


/**
 * @author ejs
 *
 */
public interface ISVGLoader {
	boolean isSlow();

	/**
	 * Transcode and create an image from the SVG.
	 * @param size the size to scale to, or null
	 * @return new ImageData
	 */
	BufferedImage getImageData(Point size) throws SVGException;

	/**
	 * Transcode and create an image from the SVG.
	 * @param aoi area of interest
	 * @param size the size to scale to, or null
	 * @return new ImageData
	 */
	BufferedImage getImageData(Rectangle aoi, Point size) throws SVGException;

	Point getSize();

	/**
	 * @return
	 */
	String getURI();

	/**
	 * @return
	 */
	boolean isValid();

	/**
	 * @return
	 */
	boolean isLoaded();

}